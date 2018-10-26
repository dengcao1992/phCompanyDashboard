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

case class FindProvinceMarketSale()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceMarketSale(): model.RootObject = {
        val provinceMarketSale = new ProvinceMarketSale()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market, province)
        provinceMarketSale.SaleShareCard = Some(findSaleShareCardList(dashboard))
        toJsonapi(provinceMarketSale)
    }

    private def findSaleShareCardList(dashboard: phMaxProvinceDashboard): List[SaleShareCard] = {
        val currProvSalesMap: Map[String, String] = dashboard.getCurrProvinceSalesMap
        List(
            new SaleShareCard("市场总销售额", time, province,  province,getFormatSales(currProvSalesMap.getOrElse("provinceSales", "0.0").toDouble),
                "mil", getFormatShare(currProvSalesMap.getOrElse("provYoyGrowth", "0.0").toDouble),
                getFormatShare(currProvSalesMap.getOrElse("provMomGrowth", "0.0").toDouble)
            ),
            new SaleShareCard("产品销售额", time, province,  province,getFormatSales(currProvSalesMap.getOrElse("companySales", "0.0").toDouble),
                "mil", getFormatShare(currProvSalesMap.getOrElse("companySalesYoyGrowth", "0.0").toDouble),
                getFormatShare(currProvSalesMap.getOrElse("companySalesMomGrowth", "0.0").toDouble)
            ),
            new SaleShareCard("产品份额", time, province,  province,getFormatShare(currProvSalesMap.getOrElse("companyShare", "0.0").toDouble),
                "%", getFormatShare(currProvSalesMap.getOrElse("companyShareYoyGrowth", "0.0").toDouble),
                getFormatShare(currProvSalesMap.getOrElse("companyShareMomGrowth", "0.0").toDouble)
            )
        )
    }
}
