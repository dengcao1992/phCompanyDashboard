package services

import com.pharbers.builder.dashboard.phNationDashboard
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.search.phMaxNativeDashboard
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.models.entity.max._
import com.pharbers.models.request.request

case class FindNationProductShare()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phNationDashboard with CirceJsonapiSupport with RequestHand  {

    def selectNationProductShare(): model.RootObject ={
        val nationProductShare = new NationProductShare
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxNativeDashboard(companyId, ym, market)
        nationProductShare.ProdSalesOverview = Some({
            val overview = new ProdSalesOverview()
            overview.title = "各产品销售份额"
            overview.subtitle = time
            overview.area = "全国"
            overview
        })

        val prodSalesGrowthLst = dashboard.getProdSalesGrowthByYM(ym)

        val prodLstMapWithColor = prodSalesGrowthLst match {
            case Nil => List.empty
            case lst => lst.sortBy(x => x("prodShare").toDouble).reverse.zipWithIndex.map(m => {
                val color = getIndexColor(m._2, lst.length).toUpperCase()
                m._1 ++ Map("color" -> color)
            })
        }
        nationProductShare.Pie = Some(findPieList(prodLstMapWithColor))
        toJsonapi(nationProductShare)
    }

    private def findPieList(prodLstMapWithColor: List[Map[String, String]]): List[ProdContValue] ={
        var pieList: List[ProdContValue] = Nil
        prodLstMapWithColor.foreach(m => {
            val pie = new ProdContValue()
            pie.show_value = getFormatShare(m.getOrElse("prodShare", "0.0").toDouble)
            pie.show_unit = "%"
            pie.title = m.getOrElse("product", "无")
            pie.color = m.getOrElse("color", "#FFFFFF")
            pie.TipDetail = Some(
                List(
                    new TipDetail("生产商", m.getOrElse("corp", "无"), "str"),
                    new TipDetail("销售额", getFormatSales(m.getOrElse("sales", "0.0").toDouble).toString, "mil"),
                    new TipDetail("份额", getFormatShare(m.getOrElse("prodShare", "0.0").toDouble).toString, "%")
                )
            )
            pieList = pieList :+ pie
        })
        pieList
    }
}
