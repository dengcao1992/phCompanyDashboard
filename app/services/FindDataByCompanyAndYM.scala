package services

import com.pharbers.builder.dashboard.phCompanyDashboard
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.models.entity.{ProdSalesOverview, ProdSalesTable, TableSale}
import com.pharbers.models.request.request
import com.pharbers.search.phMaxCompanyDashboard
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.dbManagerTrait.dbInstanceManager


case class FindDataByCompanyAndYM()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phCompanyDashboard with CirceJsonapiSupport{
    def selectData(): model.RootObject ={
        var tableSale: TableSale = new TableSale()
        var requestData: request = new request()
        requestData = formJsonapi[request](rq.body)
        requestData.eqcond.getOrElse(Nil) match {
            case Nil => ???
            case eqconds if eqconds.length > 1 => {
                if (eqconds(0).`val` != null && eqconds(1).`val` != null) {
                    tableSale = findTableSale(eqconds(0).`val`.toString, eqconds(1).`val`.toString)
                }
                toJsonapi(tableSale)
            }
            case _ => ???
        }
    }

    private def findTableSale(CompanyId: String, time: String): TableSale = {
        val ym = time.replaceAll("-", "")
        val dashboard = phMaxCompanyDashboard(CompanyId, ym)
        val tableSale = new TableSale()
        val prodSalesOverview = findProdSalesOverview(dashboard)
        val prodSalesTables = findProdSalesTableList(dashboard)
        tableSale.ProdSalesOverview = Some(prodSalesOverview)
        tableSale.ProdSalesTable = Some(prodSalesTables)
        tableSale
    }

    private def findProdSalesOverview(dashboard: phMaxCompanyDashboard): ProdSalesOverview = {
        val prodSalesOverview = new ProdSalesOverview()
        prodSalesOverview.timeStart = getFormatYM(dashboard.dashboardStartYM)
        prodSalesOverview.timeOver = getFormatYM(dashboard.dashboardEndYM)
        prodSalesOverview.curMoSales = getFormatSales(dashboard.getCurrMonthCompanySales)
        prodSalesOverview.yearYear = getFormatShare(dashboard.getCompanyYearOnYear)
        prodSalesOverview.ring = getFormatShare(dashboard.getCompanyMonthOnMonth)
        prodSalesOverview.totle = getFormatSales(dashboard.getCurrFullYearCompanySales)
        prodSalesOverview.ave = getFormatSales(dashboard.getCurrYearCompanySalesAvg)
        prodSalesOverview
    }

    private def findProdSalesTableList(dashboard: phMaxCompanyDashboard): List[ProdSalesTable] = {
        var prodSalesTableList: List[ProdSalesTable] = Nil
        dashboard.getListMonthCompanySales.foreach(x => {
            val prodSalesTable = new ProdSalesTable()
            prodSalesTable.ym = getFormatYM(x("ym"))
            prodSalesTable.sales = getFormatSales(x("sales").toDouble)
            prodSalesTableList = prodSalesTableList :+ prodSalesTable
        })
        prodSalesTableList
    }
}
