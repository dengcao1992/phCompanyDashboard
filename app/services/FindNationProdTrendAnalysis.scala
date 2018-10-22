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

case class FindNationProdTrendAnalysis()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phNationDashboard with CirceJsonapiSupport with RequestHand  {

    def selectNationProdTrendAnalysis(): model.RootObject ={
        val nationProdTrendAnalysis = new ProdTrendAnalysis()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxNativeDashboard(companyId, ym, market)
        nationProdTrendAnalysis.ProdTrendLine = Some(findProdTrendLineList(dashboard))
        nationProdTrendAnalysis.ProdSalesOverview = Some({
            val prodSalesOverview = new ProdSalesOverview
            prodSalesOverview.title = "产品销售趋势分析"
            prodSalesOverview.timeStart = getFormatYM(dashboard.dashboardStartYM)
            prodSalesOverview.timeOver = getFormatYM(dashboard.dashboardEndYM)
            prodSalesOverview.area = "全国"
            prodSalesOverview
        })
        toJsonapi(nationProdTrendAnalysis)
    }

    private def findProdTrendLineList(dashboard: phMaxNativeDashboard): List[ProdTrendLine] = {
        var prodTrendLineList: List[ProdTrendLine] = Nil
        val unit = tag match {
            case t if t.toLowerCase().contains("share") => "%"
            case t if t.toLowerCase().contains("grow") => "%"
            case t if t.toLowerCase().contains("sale") => "mil"
            case _ => "undefined"
        }
        dashboard.getSeveralMonthProdMap.groupBy(x => x("PRODUCT_NAME")).foreach(one => {
            val prodTrendLine = new ProdTrendLine()
            prodTrendLine.name = one._1
            prodTrendLine.Value = Some(dashboard.getDashboardMonthLst.map(temp_ym => {
                val value = new ProdValue()
                value.unit = unit
                value.ym = temp_ym
                value.value = formatValue(one._2.find(m => m.getOrElse("ym", "无") == temp_ym).getOrElse(Map.empty).getOrElse(tag, "0.0"))(tag)
                value
            }))
            prodTrendLineList = prodTrendLineList :+ prodTrendLine
        })
        prodTrendLineList
    }
}
