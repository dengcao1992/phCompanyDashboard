package services

import com.pharbers.builder.dashboard.phCompanyDashboard
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.request.request
import com.pharbers.search.phMaxCompanyDashboard
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.models.entity.max._

case class FindCompanyProdSalesContribution()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phCompanyDashboard with CirceJsonapiSupport with RequestHand {

    def selectContribution(): model.RootObject ={
        var contribution = new Contribution()
        requestData = formJsonapi[request](rq.body)
        init()
        contribution = findTableSale(companyId, time)
        toJsonapi(contribution)
    }

    def findTableSale(companyId: String, time: String): Contribution ={
        val ym = time.replaceAll("-", "")
        val dashboard = phMaxCompanyDashboard(companyId, ym)
        val contribution = new Contribution()

        val prodSalesOverview = new ProdSalesOverview()
        prodSalesOverview.title = "产品销售贡献度"
        prodSalesOverview.subtitle = time
        contribution.ProdSalesOverview = Some(prodSalesOverview)

        val companyProdLstMap = dashboard.getCompanyProdCurrSalesGrowth
        val colorStep = companyProdLstMap.length
        val companyProdLstMapWithColor = companyProdLstMap.sortBy(x => x("contribution").toDouble).reverse.zipWithIndex.map(m => {
            val color = getIndexColor(m._2, colorStep).toUpperCase()
            m._1 ++ Map("color" -> color)
        })

        contribution.Pie = Some(findPie(companyProdLstMapWithColor,time))
        contribution.ProdContValue = Some(findProdContValue(companyProdLstMapWithColor,time))
        contribution
    }

    def findPie(companyProdLstMapWithColor: List[Map[String, String]], time: String): List[Pie] ={
        var pieList: List[Pie] = Nil
        companyProdLstMapWithColor.foreach(m =>{
            val pie = new Pie()
            pie.prod = m.getOrElse("product", "无")
            pie.market = m.getOrElse("market", "无")
            pie.sales = getFormatSales(m.getOrElse("sales", "0.0").toDouble)
            pie.cont = getFormatShare(m.getOrElse("contribution", "0.0").toDouble)
            pie.contMonth = getFormatShare(m.getOrElse("lastMonthContribution", "0.0").toDouble)
            pie.contSeason = getFormatShare(m.getOrElse("lastSeasonContribution", "0.0").toDouble)
            pie.contYear = getFormatShare(m.getOrElse("lastYearContribution", "0.0").toDouble)
            pie.color = m.getOrElse("color", "#FFFFFF")
            pieList = pieList :+ pie
        })
        pieList
    }

    def findProdContValue(companyProdLstMapWithColor: List[Map[String, String]], time: String): List[ProdContValue] ={
        var prodContValueList: List[ProdContValue] = Nil
        companyProdLstMapWithColor.foreach(m => {
            val prodContValue = new ProdContValue()
            prodContValue.showValue = getFormatShare(m.getOrElse("contribution", "0.0").toDouble)
            prodContValue.showUnit = "%"
            prodContValue.title = m.getOrElse("product", "无")
            prodContValue.color = m.getOrElse("color", "#FFFFFF")
            prodContValue.Tips = Some(List(new Tips("销售额",getFormatSales(m.getOrElse("sales", "0.0").toDouble), "mil"),
                new Tips("贡献度", getFormatShare(m.getOrElse("contribution", "0.0").toDouble), "%")))
//            prodContValue.Tips = Some({
//                val tips = List(new Tips,new Tips)
//                tips(0).key = "销售额"
//                tips(0).value = getFormatSales(m.getOrElse("sales", "0.0").toDouble)
//                tips(0).unit = "mil"
//                tips(1).key = "贡献度"
//                tips(1).value = getFormatShare(m.getOrElse("contribution", "0.0").toDouble)
//                tips(1).unit = "%"
//                tips
//            })
            prodContValueList = prodContValueList :+ prodContValue
        })
        prodContValueList
    }
}
