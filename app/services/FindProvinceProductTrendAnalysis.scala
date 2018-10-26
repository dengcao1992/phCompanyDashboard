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

case class FindProvinceProductTrendAnalysis()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceProductTrendAnalysis(): model.RootObject = {
        val provinceProductTrendAnalysis = new ProvinceProductTrendAnalysis()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market, province)
        provinceProductTrendAnalysis.ProdSalesOverview = Some({
            val prodSalesOverview = new ProdSalesOverview()
            prodSalesOverview.title = s"${market}各产品销售份额"
            prodSalesOverview.subtitle = time
            prodSalesOverview.timeStart = getFormatYM(dashboard.dashboardStartYM)
            prodSalesOverview.timeOver = getFormatYM(dashboard.dashboardEndYM)
            prodSalesOverview.area = province
            prodSalesOverview
        })
        provinceProductTrendAnalysis.ProdTrendLine = Some(findProdTrendLineList(dashboard))
        toJsonapi(provinceProductTrendAnalysis)
    }

    private def findProdTrendLineList(dashboard: phMaxProvinceDashboard): List[ProdTrendLine] = {
        var currProvinceSeveralMonthProdMap = dashboard.getCurrProvinceSeveralMonthProdMap
        var prodTrendLineList: List[ProdTrendLine] = Nil
        val unit = tag match {
            case t if t.toLowerCase().contains("share") => "%"
            case t if t.toLowerCase().contains("grow") => "%"
            case t if t.toLowerCase().contains("sale") => "mil"
            case _ => "undefined"
        }
        currProvinceSeveralMonthProdMap.groupBy(x => x("PRODUCT_NAME")).foreach(one => {
            val prodTrendLine = new ProdTrendLine()
            prodTrendLine.name = one._1
            val DashboardMonthLst = dashboard.getDashboardMonthLst
            prodTrendLine.ProdValue = Some(DashboardMonthLst.map(temp_ym => {
                val value = new ProdValue
                value.ym = temp_ym
                value.unit = unit
                value.value = formatValue(one._2.find(m => m.getOrElse("ym", "无") == temp_ym).getOrElse(Map.empty).getOrElse(tag, "0.0"))(tag)
                value
            }))
            prodTrendLineList = prodTrendLineList :+ prodTrendLine
        })
        prodTrendLineList
    }
}
