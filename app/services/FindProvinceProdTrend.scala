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

case class FindProvinceProdTrend()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceProdTrend(): model.RootObject = {
        val provinceProdTrend = new ProvinceProdTrend()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market)
        provinceProdTrend.ProdSalesOverview = Some({
            val prodSalesOverview = new ProdSalesOverview()
            prodSalesOverview.title = "市场规模&产品销售趋势"
            prodSalesOverview.timeStart = getFormatYM(dashboard.dashboardStartYM)
            prodSalesOverview.timeOver = getFormatYM(dashboard.dashboardEndYM)
            prodSalesOverview.area = province
            prodSalesOverview
        })
        provinceProdTrend.MultipleLine = Some(findMultipleLineList(dashboard))
        toJsonapi(provinceProdTrend)
    }

    private def findMultipleLineList(dashboard: phMaxProvinceDashboard): List[MultipleLine] = {
        dashboard.getSeveralMonthProvinceSalesMap.map(x => {
            val multipleLine = new MultipleLine()
            multipleLine.ym = getFormatYM(x("ym"))
            multipleLine.marketSales = getFormatSales(x("provinceSale").toDouble)
            multipleLine.prodSales = getFormatSales(x("currProvinceCompanySale").toDouble)
            multipleLine.share = getFormatShare(x("currProvinceCompanyShare").toDouble)
            multipleLine
        })
    }
}
