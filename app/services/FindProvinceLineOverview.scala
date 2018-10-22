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

case class FindProvinceLineOverview()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceLineOverview(): model.RootObject = {
        val provinceLineOverview = new ProvinceLineOverview()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market)
        provinceLineOverview.MixedGraphData = Some(findMixedGraphDataList(dashboard))
        toJsonapi(provinceLineOverview)
    }

    private def findMixedGraphDataList(dashboard: phMaxProvinceDashboard): List[MixedGraphData] = {
        dashboard.getProvinceSalesLstMap(ym).map(m => {
            val mixedGraphData = new MixedGraphData()
            mixedGraphData.province = m.getOrElse("province", "无")
            mixedGraphData.scale = getFormatSales(m.getOrElse("provinceSales", "0.0").toDouble)
            mixedGraphData.market_growth = getFormatShare(m.getOrElse("provMomGrowth", "0.0").toDouble)
            mixedGraphData.sales = getFormatSales(m.getOrElse("companySales", "0.0").toDouble)
            mixedGraphData.prod_growth = getFormatShare(m.getOrElse("companySalesMomGrowth", "0.0").toDouble)
            mixedGraphData
        })
    }
}
