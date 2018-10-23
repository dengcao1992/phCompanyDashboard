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

case class FindProvinceProductShare()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceProductShare():  model.RootObject = {
        val provinceProductShare = new ProvinceProductShare()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market, province)
        provinceProductShare.ProdSalesOverview = Some({
            val prodSalesOverview = new ProdSalesOverview()
            prodSalesOverview.title = "各产品销售份额"
            prodSalesOverview.subtitle = time
            prodSalesOverview.area = province
            prodSalesOverview
        })
        provinceProductShare.Pie = Some(findPieList(dashboard))
        toJsonapi(provinceProductShare)
    }

    private def findPieList(dashboard: phMaxProvinceDashboard): List[ProdContValue] = {
        val provProdSalesLst = dashboard.getCurrProvinceAllProdLstMap
        val provLstMapWithColor = provProdSalesLst match {
            case Nil => List.empty
            case lst => lst.sortBy(x => x("share").toDouble).reverse.zipWithIndex.map(m => {
                val color = getIndexColor(m._2, lst.length).toUpperCase()
                m._1 ++ Map("color" -> color)
            })
        }
        provLstMapWithColor.map(m => {
            val pie = new ProdContValue()
            pie.showValue = getFormatShare(m.getOrElse("share", "0.0").toDouble)
            pie.showUnit = "%"
            pie.title = m.getOrElse("PRODUCT_NAME", "无")
            pie.color = m.getOrElse("color", "#FFFFFF")
            pie.Tips = Some(
                List(
                    new Tips("生产商", m.getOrElse("CORP_NAME", "无"), "str"),
                    new Tips("销售额", getFormatSales(m.getOrElse("sales", "0.0").toDouble).toString, "mil"),
                    new Tips("份额", getFormatShare(m.getOrElse("share", "0.0").toDouble).toString, "%")
                )
            )
            pie
        })
    }
}
