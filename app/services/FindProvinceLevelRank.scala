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

case class FindProvinceLevelRank()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceLevelRank(): model.RootObject = {
        val provinceLevelRank = new ProvinceLevelRank()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market)
        provinceLevelRank.Ranking = Some(findRankingList(dashboard))
        provinceLevelRank.unit = tag match {
            case t if t.toLowerCase().contains("share") => "%"
            case t if t.toLowerCase().contains("grow") => "%"
            case t if t.toLowerCase().contains("sale") => "mil"
            case _ => "undefined"
        }
        toJsonapi(provinceLevelRank)
    }

    private def findRankingList(dashboard: phMaxProvinceDashboard): List[Ranking] ={
        dashboard.getProvinceSalesLstMap(ym) match {
            case Nil => List.empty
            case _ => {
                dashboard.getCurrMonthProvSortByKey(tag).map(m => {
                    val value = m.getOrElse(tag, "0.0").toDouble
                    val formatValue: Double = tag match {
                        case t if t.toLowerCase().contains("share") => getFormatShare(value)
                        case t if t.toLowerCase().contains("grow") => getFormatShare(value)
                        case t if t.toLowerCase().contains("sale") => getFormatSales(value)
                        case _ => 0.0
                    }
                    val ranking = new Ranking()
                    ranking.no = m.getOrElse(s"${tag}Rank", "0").toInt
                    ranking.province = m.getOrElse("province", "无")
                    ranking.growth = m.getOrElse(s"${tag}RankChanges", "0").toInt
                    ranking.value = formatValue
                    ranking
                })
            }
        }
    }
}
