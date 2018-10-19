package services

import com.pharbers.builder.dashboard.phCompanyDashboard
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.request.request
import com.pharbers.search.phMaxCompanyDashboard
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.models.entity.max._


case class FindSalesOverviewByCompanyAndYM()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phCompanyDashboard with CirceJsonapiSupport{

    def selectOverview(): model.RootObject ={
        val requestData = formJsonapi[request](rq.body)
        var overview = new Overview()

        requestData.eqcond.getOrElse(Nil) match {
            case Nil => ???
            case eqconds if eqconds.length > 1 => {
                var companyId = ""
                var time = ""
                eqconds.foreach(x =>{
                    if (x.key == "company_id" && x.`val` != null) companyId = x.`val`.toString
                    if (x.key == "time" && x.`val` != null)  time = x.`val`.toString
                })
                overview = findProdSalesOverview(companyId, time)
                toJsonapi(overview)
            }
        }
    }

    private def findProdSalesOverview(companyId: String, time: String): Overview ={
        val ym = time.replaceAll("-", "")
        val dashboard = phMaxCompanyDashboard(companyId, ym)
        val companyProdLstMap = dashboard.getCompanyProdCurrSalesGrowth
        val overview = new Overview()
        val prodSalesOverview = new ProdSalesOverview()
        prodSalesOverview.subtitle = time
        overview.ProdSalesOverview = Some(prodSalesOverview)
        overview.ProdSalesValue = Some(findProdSalesValueList(companyProdLstMap))
        overview
    }

    private def findProdSalesValueList(companyProdLstMap: List[Map[String, String]]): List[ProdSalesValue] ={
        var prodSalesValueList: List[ProdSalesValue] = Nil
        companyProdLstMap.foreach(m =>{
            val prodSalesValue = new ProdSalesValue()
            prodSalesValue.prod = m.getOrElse("product", "无")
            prodSalesValue.market = m.getOrElse("market", "无")
            prodSalesValue.market_scale = getFormatSales(m.getOrElse("marketSales", "0.0").toDouble)
            prodSalesValue.market_growth = getFormatShare(m.getOrElse("marketGrowth", "0.0").toDouble)
            prodSalesValue.sales = getFormatSales(m.getOrElse("sales", "0.0").toDouble)
            prodSalesValue.sales_growth = getFormatShare(m.getOrElse("productGrowth", "0.0").toDouble)
            prodSalesValue.ev_value = getFormatShare(m.getOrElse("EV", "0.0").toDouble)
            prodSalesValue.share = getFormatShare(m.getOrElse("companyProdShare", "0.0").toDouble)
            prodSalesValue.share_growth = getFormatShare(m.getOrElse("companyProdShareGrowth", "0.0").toDouble)
            prodSalesValueList = prodSalesValueList :+ prodSalesValue
        })
        prodSalesValueList
    }
}
