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

case class FindProvinceProductRankChange()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceProductRankChange(): model.RootObject = {
        val provinceProductRankChange = new ProvinceProductRankChange()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market, province)
        provinceProductRankChange.ProdSalesOverview = Some({
            val prodSalesOverview = new ProdSalesOverview()
            prodSalesOverview.unit = tag match {
                case t if t.toLowerCase().contains("share") => "%"
                case t if t.toLowerCase().contains("grow") => "%"
                case t if t.toLowerCase().contains("sale") => "mil"
                case _ => "undefined"
            }
            prodSalesOverview
        })
        provinceProductRankChange.Ranking = Some(FindRankChangeList(dashboard))
        toJsonapi(provinceProductRankChange)
    }

    private def FindRankChangeList(dashboard: phMaxProvinceDashboard): List[Ranking] = {
        dashboard.getCurrProvinceAllProdLstMap match {
            case Nil => List.empty
            case _ => {
                dashboard.getCurrMonthProvProdSortByKey(tag).map(m => {
                    val ranking = new Ranking()
                    ranking.no = m.getOrElse(s"${tag}Rank", "0").toInt
                    ranking.prod = m.getOrElse("PRODUCT_NAME", "无")
                    ranking.manu = m.getOrElse("CORP_NAME", "无")
                    ranking.growth = m.getOrElse(s"${tag}RankChanges", "0").toInt
                    ranking.value = formatValue(m.getOrElse(tag, "0.0"))(tag)
                    ranking
                })
            }
        }
    }
}
