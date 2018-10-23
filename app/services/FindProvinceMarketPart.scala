package services

import com.pharbers.builder.dashboard.phProvinceDashboard
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.search.phMaxProvinceDashboard
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.models.entity.max._
import com.pharbers.models.request.request

case class FindProvinceMarketPart()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceMarketPart(): model.RootObject = {
        val provinceMarketPart = new ProvinceMarketPart()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market)
        provinceMarketPart.ProdSalesOverview = Some({
            val prodSalesOverview = new ProdSalesOverview()
            prodSalesOverview.title = "各产品销售概况"
            prodSalesOverview.subtitle = time
            prodSalesOverview
        })
        provinceMarketPart.Pie = Some(findPieList(dashboard))
        toJsonapi(provinceMarketPart)
    }

    private def findPieList(dashboard: phMaxProvinceDashboard): List[ProdContValue] = {
        val provLstMapWithColor = dashboard.getProvinceSalesLstMap(ym) match {
            case Nil => List.empty
            case lst => lst.sortBy(x => x("provinceShare").toDouble).reverse.zipWithIndex.map(m => {
                val color = getIndexColor(m._2, lst.length).toUpperCase()
                m._1 ++ Map("color" -> color)
            })
        }
        provLstMapWithColor.map(m => {
            val prodContValue = new ProdContValue()
            prodContValue.showValue = getFormatShare(m.getOrElse("provinceShare", "0.0").toDouble)
            prodContValue.showUnit = "%"
            prodContValue.title = m.getOrElse("province", "无")
            prodContValue.color = m.getOrElse("color", "#FFFFFF")
            prodContValue.Tips = Some(
                List(
                    new Tips("省份销售额", getFormatSales(m.getOrElse("provinceSales", "0.0").toDouble).toString, "mil"),
                    new Tips("占全国份额", getFormatShare(m.getOrElse("provinceShare", "0.0").toDouble).toString, "%")
                )
            )
            prodContValue
        })
    }
}
