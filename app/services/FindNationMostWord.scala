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

case class FindNationMostWord()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phNationDashboard with CirceJsonapiSupport with RequestHand {

    def selectNationMostWord(): model.RootObject ={
        val nationMostWord = new NationMostWord()
        requestData = formJsonapi[request](rq.body)
        init()
        val ym = time.replaceAll("-", "")
        val dashboard = phMaxNativeDashboard(companyId, ym, market)
        val prodSalesGrowthLst = dashboard.getProdSalesGrowthByYM(ym)

        val prodShareMax: Map[String, String] = prodSalesGrowthLst match {
            case Nil => Map.empty
            case lst => lst.maxBy(x => x("prodShare").toDouble)
        }

        val prodShareGrowing: Map[String, String] = prodSalesGrowthLst match {
            case Nil => Map.empty
            case _ => dashboard.getFastestShareGrowingProd
        }

        val prodShareDecline: Map[String, String] = prodSalesGrowthLst match {
            case Nil => Map.empty
            case _ => dashboard.getFastestShareDeclineProd
        }

        nationMostWord.mostCard = Some(
            List(
                new MostCard(title = "竟品数量", subtitle = time, area = "全国", name = dashboard.getCompetingProductCount.toString, tag = "count"),
                new MostCard("份额最大", time, "全国", prodShareMax.getOrElse("product", "无"), prodShareMax.getOrElse("corp", "无"),
                    "%", getFormatShare(prodShareMax.getOrElse("prodShare", "0.0").toDouble),
                    getFormatShare(prodShareMax.getOrElse("prodShareGrowth", "0.0").toDouble)
                ),
                new MostCard("份额增长最快", time, "全国", prodShareGrowing.getOrElse("product", "无"),
                    prodShareGrowing.getOrElse("corp", "无"), "mil", getFormatSales(prodShareGrowing.getOrElse("sales", "0.0").toDouble),
                    getFormatShare(prodShareGrowing.getOrElse("salesGrowth", "0.0").toDouble)
                ),
                new MostCard("份额下降最多", time, "全国", prodShareDecline.getOrElse("product", "无"),
                    prodShareDecline.getOrElse("corp", "无"), "%", getFormatShare(prodShareDecline.getOrElse("prodShare", "0.0").toDouble),
                    getFormatShare(prodShareDecline.getOrElse("prodShareGrowth", "0.0").toDouble)
                )
            )
        )
        toJsonapi(nationMostWord)
    }

}
