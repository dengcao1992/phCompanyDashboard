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
        var prodSalesOverview = new ProdSalesOverview()

        requestData.eqcond.getOrElse(Nil) match {
            case Nil => ???
            case eqconds if eqconds.length > 1 => {
                if (eqconds(0).`val` != null && eqconds(1).`val` != null) {
                    prodSalesOverview = findProdSalesOverview(eqconds(0).`val`.toString, eqconds(1).`val`.toString)
                }
                toJsonapi(prodSalesOverview)
            }
        }
    }

    private def findProdSalesOverview(companyId: String, time: String): ProdSalesOverview ={
        val ym = time.replaceAll("-", "")
        val dashboard = phMaxCompanyDashboard(companyId, ym)
        val companyProdLstMap = dashboard.getCompanyProdCurrSalesGrowth
        val prodSalesOverview = new ProdSalesOverview()
        prodSalesOverview.subtitle = time
        prodSalesOverview.ProdSalesValue = Some(findProdSalesValueList(companyProdLstMap))
        prodSalesOverview
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
