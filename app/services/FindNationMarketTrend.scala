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

case class FindNationMarketTrend()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phNationDashboard with CirceJsonapiSupport with RequestHand {

    def selectNationMarketTrend(): model.RootObject ={
        val nationMarketTrend = new NationMarketTrend()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxNativeDashboard(companyId, ym, market)
        nationMarketTrend.ProdSalesOverview = Some(findNationProdSalesOverview(dashboard))
        nationMarketTrend.MultipleLine = Some(findMultiDataList(dashboard))
        toJsonapi(nationMarketTrend)
    }

    private def findNationProdSalesOverview(dashboard: phMaxNativeDashboard): ProdSalesOverview ={
        val nationProdSalesOverview = new ProdSalesOverview()
        nationProdSalesOverview.title = "市场规模&产品销售趋势"
        nationProdSalesOverview.timeStart = getFormatYM(dashboard.dashboardStartYM)
        nationProdSalesOverview.timeOver = getFormatYM(dashboard.dashboardEndYM)
        nationProdSalesOverview.area = "全国"
        nationProdSalesOverview
    }

    private def findMultiDataList(dashboard: phMaxNativeDashboard): List[MultipleLine] ={
        var multiDataList: List[MultipleLine] = Nil
        dashboard.getListMonthTrend.foreach(x => {
            val multiData = new MultipleLine()
            multiData.ym = getFormatYM(x("ym"))
            multiData.marketSales = getFormatSales(x("NationSales").toDouble)
            multiData.prodSales = getFormatSales(x("CompanySales").toDouble)
            multiData.share = getFormatShare(x("CompanyShare").toDouble)
            multiDataList = multiDataList :+ multiData
        })
        multiDataList
    }
}
