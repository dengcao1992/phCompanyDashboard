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

case class FindNationProductRank()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phNationDashboard with CirceJsonapiSupport with RequestHand {

    def selectNationProductRank(): model.RootObject ={
        val nationProductRank = new NationProductRank()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxNativeDashboard(companyId, ym, market)
        nationProductRank.Ranking = Some(findRankingList(dashboard))
        val unit = tag match {
            case t if t.toLowerCase().contains("share") => "%"
            case t if t.toLowerCase().contains("grow") => "%"
            case t if t.toLowerCase().contains("sale") => "mil"
            case _ => "undefined"
        }
        nationProductRank.subtitle = time
        nationProductRank.unit = unit
        toJsonapi(nationProductRank)
    }

    private def findRankingList(dashboard: phMaxNativeDashboard): List[Ranking] ={
        var rankingList: List[Ranking] = Nil
        if(dashboard.getProdSalesGrowthByYM(ym) != Nil) {
            dashboard.getCurrMonthProdSortByKey(tag).foreach(m =>{
                val ranking = new Ranking()
                val value = m.getOrElse(tag, "0.0").toDouble
                ranking.value =tag match {
                    case t if t.toLowerCase().contains("share") => getFormatShare(value)
                    case t if t.toLowerCase().contains("grow") => getFormatShare(value)
                    case t if t.toLowerCase().contains("sale") => getFormatSales(value)
                    case _ => 0.0
                }
                ranking.no = m.getOrElse(s"${tag}Rank", "0").toInt
                ranking.prod = m.getOrElse("product", "无")
                ranking.manu = m.getOrElse("corp", "无")
                ranking.growth = m.getOrElse(s"${tag}RankChanges", "0").toInt
                rankingList = rankingList :+ ranking
            })
        }
        rankingList
    }
}
