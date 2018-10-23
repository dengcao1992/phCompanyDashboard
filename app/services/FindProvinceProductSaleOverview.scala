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

case class FindProvinceProductSaleOverview()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceProductSaleOverview(): model.RootObject = {
        val provinceProductSaleOverview = new ProvinceProductSaleOverview()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market, province)
        provinceProductSaleOverview.ProdSalesOverview = Some({
            val prodSalesOverview = new ProdSalesOverview()
            prodSalesOverview.title = s"${market}各产品销售份额"
            prodSalesOverview.subtitle = time
            prodSalesOverview.area = province
            prodSalesOverview
        })
        provinceProductSaleOverview.ProdSalesValue = Some(findProdSalesValueList(dashboard))
        toJsonapi(provinceProductSaleOverview)
    }

    private def findProdSalesValueList(dashboard: phMaxProvinceDashboard): List[ProdSalesValue] = {
        dashboard.getCurrProvinceAllProdLstMap.map(m => {
            val prodSalesValue = new ProdSalesValue()
            prodSalesValue.prod = m.getOrElse("PRODUCT_NAME", "无")
            prodSalesValue.manufacturer = m.getOrElse("CORP_NAME", "无")
            prodSalesValue.market_sale = getFormatSales(m.getOrElse("sales", "0.0").toDouble)
            prodSalesValue.sales_growth = getFormatShare(m.getOrElse("salesGrowth", "0.0").toDouble)
            prodSalesValue.ev_value = getFormatShare(m.getOrElse("EV", "0.0").toDouble)
            prodSalesValue.share = getFormatShare(m.getOrElse("share", "0.0").toDouble)
            prodSalesValue.share_growth = getFormatShare(m.getOrElse("shareGrowth", "0.0").toDouble)
            prodSalesValue
        })
    }
}
