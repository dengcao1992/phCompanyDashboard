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
        if (companyProdLstMapWithColor.isEmpty) pieList = testPie
        pieList
    }

    def findProdContValue(companyProdLstMapWithColor: List[Map[String, String]], time: String): List[ProdContValue] ={
        var prodContValueList: List[ProdContValue] = Nil
        companyProdLstMapWithColor.foreach(m => {
            val prodContValue = new ProdContValue()
            prodContValue.show_value = getFormatShare(m.getOrElse("contribution", "0.0").toDouble)
            prodContValue.show_unit = "%"
            prodContValue.title = m.getOrElse("product", "无")
            prodContValue.color = m.getOrElse("color", "#FFFFFF")
            prodContValue.TipDetail = Some(List(new TipDetail("销售额",getFormatSales(m.getOrElse("sales", "0.0").toDouble).toString, "mil"),
                new TipDetail("贡献度", getFormatShare(m.getOrElse("contribution", "0.0").toDouble).toString, "%")))
            prodContValueList = prodContValueList :+ prodContValue
        })
        if (companyProdLstMapWithColor.isEmpty) prodContValueList = testprodContValue
        prodContValueList
    }

    private lazy val testPie = List({
        val pie = new Pie()
        pie.prod = "产品一"
        pie.market = "麻醉市场"
        pie.sales = 13422
        pie.cont = 422
        pie.contMonth = 0
        pie.contSeason = 0
        pie.contYear = 0
        pie.color = "#3399FF"
        pie
    })

    private lazy val testprodContValue = List({
        val prodContValue = new ProdContValue()
        prodContValue.show_value = 10
        prodContValue.show_unit = "%"
        prodContValue.title = "产品一"
        prodContValue.color = "#3399FF"
        prodContValue.TipDetail = Some(List(new TipDetail("销售额","848", "mil"),
            new TipDetail("贡献度", "10", "%")
        ))
        prodContValue
    })
}
