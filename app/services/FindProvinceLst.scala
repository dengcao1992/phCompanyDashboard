package services

import java.text.Collator
import com.pharbers.builder.dashboard.phProvinceDashboard
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.jsonapi.model
import play.api.mvc.Request
import com.pharbers.macros._
import com.pharbers.macros.convert.jsonapi.JsonapiMacro._
import com.pharbers.search.phMaxProvinceDashboard
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import com.pharbers.models.entity.max._
import com.pharbers.models.request.request

case class FindProvinceLst()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceList(): model.RootObject ={
        val allProvince = new AllProvince()
        requestData = formJsonapi[request](rq.body)
        init()
        val collator = Collator.getInstance(java.util.Locale.CHINA)
        val dashboard = phMaxProvinceDashboard(companyId, ym, market)
        allProvince.Province = Some(dashboard.getCurrMonthAllProvLst.sortWith((s1, s2) => collator.compare(s1, s2) < 0).map(x => {
            val province = new Province()
            province.name = x
            province
        }))
        toJsonapi(allProvince)
    }

}
