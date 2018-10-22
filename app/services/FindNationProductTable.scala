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
case class FindNationProductTable()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phNationDashboard with CirceJsonapiSupport with RequestHand {
    def selectNationProductTable(): model.RootObject = {
        val nationProductTable = new NationProductTable()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxNativeDashboard(companyId, ym, market)
        nationProductTable.ProdSalesOverview = Some({
            val prodSalesOverview = new ProdSalesOverview()
            prodSalesOverview.title = "各产品销售概况"
            prodSalesOverview.subtitle = time
            prodSalesOverview.area = "全国"
            prodSalesOverview
        })
        nationProductTable.ProdSalesValue = Some(findProdSalesValueList(dashboard))
        toJsonapi(nationProductTable)
    }

    private def findProdSalesValueList(dashboard: phMaxNativeDashboard): List[ProdSalesValue] = {
        var prodSalesValueList: List[ProdSalesValue] = Nil
        dashboard.getProdSalesGrowthByYM(ym).foreach(m => {
            val prodSalesValue = new ProdSalesValue()
            prodSalesValue.prod = m.getOrElse("product", "无")
            prodSalesValue.manufacturer = m.getOrElse("corp", "无")
            prodSalesValue.market_scale = getFormatSales(m.getOrElse("sales", "0.0").toDouble)
            prodSalesValue.sales_growth = getFormatShare(m.getOrElse("salesGrowth", "0.0").toDouble)
            prodSalesValue.ev_value = getFormatShare(m.getOrElse("EV", "0.0").toDouble)
            prodSalesValue.share = getFormatShare(m.getOrElse("prodShare", "0.0").toDouble)
            prodSalesValue.share_growth = getFormatShare(m.getOrElse("prodShareGrowth", "0.0").toDouble)
            prodSalesValueList = prodSalesValueList :+ prodSalesValue
        })
        prodSalesValueList
    }
}
