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
import com.pharbers.models.entity.max.{Cards, SalesFastest}

class FindKeyWordByCompanyIdAndTime(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phCompanyDashboard with CirceJsonapiSupport {
    def selectKeyWord(): model.RootObject ={
        var requestData: request = new request()
        var cards: Cards = new Cards()
        requestData = formJsonapi[request](rq.body)
        requestData.eqcond.getOrElse(Nil) match {
            case Nil => ???
            case eqconds if eqconds.length > 1 => {
                cards = findCards(eqconds(0).`val`.toString, eqconds(1).`val`.toString)
                toJsonapi(cards)
            }
        }
    }

    private def findCards(CompanyId: String, time: String): Cards ={
        val ym = time.replaceAll("-", "")
        val dashboard = phMaxCompanyDashboard(CompanyId, ym)
        val cards: Cards = new Cards()


        cards
    }

    private def findSalesFastestCard(dashboard: phMaxCompanyDashboard, time: String): SalesFastest ={
        val salesFastest = new SalesFastest()
        salesFastest.subtitle = time
        ???
    }
}
