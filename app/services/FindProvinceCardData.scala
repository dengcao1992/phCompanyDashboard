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

case class FindProvinceCardData()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phProvinceDashboard with CirceJsonapiSupport with RequestHand {

    def selectProvinceCardData(): model.RootObject = {
        val provinceName = new ProvinceName()
        requestData = formJsonapi[request](rq.body)
        init()
        val dashboard = phMaxProvinceDashboard(companyId, ym, market)
        provinceName.ProvinceWord = Some(findProvinceWordList(dashboard))
        toJsonapi(provinceName)
    }

    private def findProvinceWordList(dashboard: phMaxProvinceDashboard): List[ProvinceWord] = {
        val provinceSalesLst = dashboard.getProvinceSalesLstMap(ym)
        val provSalesMax: Map[String, String] = provinceSalesLst match {
            case Nil => Map.empty
            case _ => dashboard.getMaxProvinceSalesMap
        }
        val provSalesGrowingFastest: Map[String, String] = provinceSalesLst match {
            case Nil => Map.empty
            case _ => dashboard.getFastProvinceGrowingMap
        }
        val provCompanySalesMax: Map[String, String] = provinceSalesLst match {
            case Nil => Map.empty
            case _ => dashboard.getMaxCompanySalesMap
        }
        val provCompanySalesGrowingFastest: Map[String, String] = provinceSalesLst match {
            case Nil => Map.empty
            case _ => dashboard.getFastCompanySalesGrowingMap
        }
        val provCompanyShareMax: Map[String, String] = provinceSalesLst match {
            case Nil => Map.empty
            case _ => dashboard.getMaxCompanyShareMap
        }
        val provCompanyShareGrowingFastest: Map[String, String] = provinceSalesLst match {
            case Nil => Map.empty
            case _ => dashboard.getFastCompanyShareGrowingMap
        }
        List(
            new ProvinceWord("市场规模最大", time, provSalesMax.getOrElse("province", "无"), "mil",
                getFormatSales(provSalesMax.getOrElse("provinceSales", "0.0").toDouble),
                getFormatShare(provSalesMax.getOrElse("provMomGrowth", "0.0").toDouble)
            ),
            new ProvinceWord("市场规模增长最快", time, provSalesGrowingFastest.getOrElse("province", "无"),
                "mil", getFormatSales(provSalesGrowingFastest.getOrElse("provinceSales", "0.0").toDouble),
                getFormatShare(provSalesGrowingFastest.getOrElse("provMomGrowth", "0.0").toDouble)
            ),
            new ProvinceWord("产品销售额最高", time, provCompanySalesMax.getOrElse("province", "无"), "mil",
                getFormatSales(provCompanySalesMax.getOrElse("companySales", "0.0").toDouble),
                getFormatShare(provCompanySalesMax.getOrElse("companySalesMomGrowth", "0.0").toDouble)
            ),
            new ProvinceWord("产品销售额增长最快", time, provCompanySalesGrowingFastest.getOrElse("province", "无"),
                "mil", getFormatSales(provCompanySalesGrowingFastest.getOrElse("companySales", "0.0").toDouble),
                getFormatShare(provCompanySalesGrowingFastest.getOrElse("companySalesMomGrowth", "0.0").toDouble)
            ),
            new ProvinceWord("产品份额最高", time, provCompanyShareMax.getOrElse("province", "无"), "%",
                getFormatShare(provCompanyShareMax.getOrElse("companyShare", "0.0").toDouble),
                getFormatShare(provCompanyShareMax.getOrElse("companyShareMomGrowth", "0.0").toDouble)
            ),
            new ProvinceWord("产品份额增长最快", time, provCompanyShareGrowingFastest.getOrElse("province", "无"), "%",
                getFormatShare(provCompanyShareGrowingFastest.getOrElse("companyShare", "0.0").toDouble),
                getFormatShare(provCompanyShareGrowingFastest.getOrElse("companyShareMomGrowth", "0.0").toDouble)
            )
        )
    }
}
