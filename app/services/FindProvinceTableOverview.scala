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

case class FindProvinceTableOverview()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceTableOverview(): model.RootObject = {
        val provinceTableOverview = new ProvinceTableOverview()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market)
        provinceTableOverview.ProdSalesOverview = Some({
            val prodSalesOverview = new ProdSalesOverview()
            prodSalesOverview.title = "市场各省销售概况"
            prodSalesOverview
        })
        provinceTableOverview.ProdSalesValue = Some(findProdSalesValueList(dashboard))
        toJsonapi(provinceTableOverview)
    }

    private def findProdSalesValueList(dashboard: phMaxProvinceDashboard): List[ProdSalesValue] = {
        dashboard.getProvinceSalesLstMap(ym).map(m => {
            val prodSalesValue = new ProdSalesValue
            prodSalesValue.province = m.getOrElse("province", "无")
            prodSalesValue.market_size = getFormatSales(m.getOrElse("provinceSales", "0.0").toDouble)
            prodSalesValue.market_growth =getFormatShare(m.getOrElse("provMomGrowth", "0.0").toDouble)
            prodSalesValue.sales_amount = getFormatSales(m.getOrElse("companySales", "0.0").toDouble)
            prodSalesValue.sales_growth = getFormatShare(m.getOrElse("companySalesMomGrowth", "0.0").toDouble)
            val a = getFormatShare(m.getOrElse("EV", "0.0").toDouble)
            prodSalesValue.ev_value = if (m.getOrElse("EV", "0.0") == "NaN") 0.0 else a
            prodSalesValue.share = getFormatShare(m.getOrElse("companyShare", "0.0").toDouble)
            prodSalesValue.share_growth = getFormatShare(m.getOrElse("companyShareMomGrowth", "0.0").toDouble)
            prodSalesValue
        })
    }
}
