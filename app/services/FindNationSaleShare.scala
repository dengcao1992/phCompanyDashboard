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

case class FindNationSaleShare()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phNationDashboard with CirceJsonapiSupport with RequestHand {

    def selectNationSaleShare():  model.RootObject ={
        val nationSaleShare = new NationSaleShare()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxNativeDashboard(companyId, ym, market)
        nationSaleShare.SaleShareCard = Some(findSaleShareCardList(dashboard))
        toJsonapi(nationSaleShare)
    }

    private def findSaleShareCardList(dashboard: phMaxNativeDashboard): List[SaleShareCard] ={
        List(
            new SaleShareCard("市场总销售额", time, "全国", getFormatSales(dashboard.getCurrMonthNationSales),
                "mil", getFormatShare(dashboard.getNationSalesYearOnYear), getFormatShare(dashboard.getNationSalesMonthOnMonth)
            ),
            new SaleShareCard("产品销售额", time, "全国", getFormatSales(dashboard.getCurrMonthCompanySales), "mil",
                getFormatShare(dashboard.getCompanySalesYearOnYear), getFormatShare(dashboard.getCompanySalesMonthOnMonth)
            ),
            new SaleShareCard("产品份额", time, "全国", getFormatShare(dashboard.getCurrMonthCompanyShare), "%",
                getFormatShare(dashboard.getCompanyShareYearOnYear), getFormatShare(dashboard.getCompanyShareMonthOnMonth)
            )
        )
    }

}
