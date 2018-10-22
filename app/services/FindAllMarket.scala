package services

import com.pharbers.builder.phMarketTable.phMarketManager
import com.pharbers.jsonapi.model
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.models.entity.max._
import com.pharbers.models.request.request

case class FindAllMarket ()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phMarketManager  with RequestHand{

    def selectAllMarket(): model.RootObject = {
        val allMarket = new AllMarket()
        requestData = formJsonapi[request](rq.body)
        init()
        allMarket.Market = Some(getAllMarkets(companyId).map(x => {
            val market = new Market()
            market.name = x
            market
        }))
        toJsonapi(allMarket)
    }
}
