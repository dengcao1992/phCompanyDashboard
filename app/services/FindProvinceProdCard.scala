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

case class FindProvinceProdCard()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceProdCard(): model.RootObject = {
        val provinceProductCard = new ProvinceProductCard()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market, province)
        provinceProductCard.ProProductCard = Some(findProvinceProductCardList(dashboard))
        toJsonapi(provinceProductCard)
    }

    private def findProvinceProductCardList(dashboard: phMaxProvinceDashboard): List[ProProductCard] = {
        val provinceProdSalesLst = dashboard.getCurrProvinceAllProdLstMap

        val competingProdCount = provinceProdSalesLst match {
            case Nil => 0
            case _ => dashboard.getCurrProvinceCompetingProdCount
        }
        val currProvMaxShareMap: Map[String, String] = provinceProdSalesLst match {
            case Nil => Map.empty
            case _ => dashboard.getCurrProvinceMaxShareProdMap
        }
        val currProvFastGrowingSaleMap: Map[String, String] = provinceProdSalesLst match {
            case Nil => Map.empty
            case _ => dashboard.getCurrProvSalesFastGrowingProdMap
        }
        val currProvFastDeclineShareMap: Map[String, String] = provinceProdSalesLst match {
            case Nil => Map.empty
            case _ => dashboard.getCurrProvShareFastDeclineProdMap
        }

        List(
            new ProProductCard("竞品数量", time, "", province, "count", competingProdCount.toString, "", 0.0, 0.0),
            new ProProductCard("份额最大", time, "", province, "%", currProvMaxShareMap.getOrElse("PRODUCT_NAME", "无"),
                currProvMaxShareMap.getOrElse("CORP_NAME", "无"), getFormatShare(currProvMaxShareMap.getOrElse("share", "0.0").toDouble),
                getFormatShare(currProvMaxShareMap.getOrElse("shareGrowth", "0.0").toDouble)
            ),
            new ProProductCard("销售额增长最快", time, "", province, "mil", currProvFastGrowingSaleMap.getOrElse("PRODUCT_NAME", "无"),
                currProvFastGrowingSaleMap.getOrElse("CORP_NAME", "无"),
                getFormatSales(currProvFastGrowingSaleMap.getOrElse("sales", "0.0").toDouble),
                getFormatShare(currProvFastGrowingSaleMap.getOrElse("salesGrowth", "0.0").toDouble)
            ),
            new ProProductCard("份额下降最多", time, "", province, "%", currProvFastDeclineShareMap.getOrElse("PRODUCT_NAME", "无"),
                currProvFastDeclineShareMap.getOrElse("CORP_NAME", "无"),
                getFormatShare(currProvFastDeclineShareMap.getOrElse("share", "0.0").toDouble),
                getFormatShare(currProvFastDeclineShareMap.getOrElse("shareGrowth", "0.0").toDouble)
            )
        )
    }
}
