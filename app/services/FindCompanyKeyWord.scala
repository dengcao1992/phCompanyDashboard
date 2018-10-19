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

case class FindCompanyKeyWord()(implicit val rq: Request[model.RootObject], dbt: dbInstanceManager)
        extends phCompanyDashboard with CirceJsonapiSupport with RequestHand {
    def selectKeyWord(): model.RootObject ={
        var cards: Cards = new Cards()
        requestData = formJsonapi[request](rq.body)
        init()
        cards = findCards(companyId, time)
        toJsonapi(cards)
    }

    private def findCards(CompanyId: String, time: String): Cards ={
        val ym = time.replaceAll("-", "")
        val dashboard = phMaxCompanyDashboard(CompanyId, ym)

        val mktGrowthLst = dashboard.getMktCurrSalesGrowth
        val fastestGrowingMkt: Map[String, String] = mktGrowthLst match {
            case Nil => Map.empty
            case _ => dashboard.getFastestGrowingMkt
        }

        val companyProdSalesGrowthLst = dashboard.getCompanyProdCurrSalesGrowth
        val fastestSaleGrowingProd: Map[String, String] = companyProdSalesGrowthLst match {
            case Nil => Map.empty
            case _ => dashboard.getCompanyFastestSaleGrowingProd
        }

        val fastestSaleDeclineProd: Map[String, String] = companyProdSalesGrowthLst match {
            case Nil => Map.empty
            case _ => dashboard.getCompanyFastestSaleDeclineProd
        }

        val maxShareProd: Map[String, String] = companyProdSalesGrowthLst match {
            case Nil => Map.empty
            case _ => dashboard.getCompanyMaxShareProd
        }

        val fastestShareGrowingProd: Map[String, String] = companyProdSalesGrowthLst match {
            case Nil => Map.empty
            case _ => dashboard.getCompanyFastestShareGrowingProd
        }

        val fastestShareDeclineProd: Map[String, String] = companyProdSalesGrowthLst match {
            case Nil => Map.empty
            case _ => dashboard.getCompanyFastestShareDeclineProd
        }

        val cards: Cards = new Cards()
        cards.Card = Some(List(findScaleFastestCard(fastestGrowingMkt, time),
            findSalesFastestCard(fastestSaleGrowingProd, time),
            findSalesSlowestCard(fastestSaleDeclineProd, time),
            findShareMostCard(maxShareProd, time),
            findShareFastestCard(fastestShareGrowingProd, time),
            findShareSlowestCard(fastestShareDeclineProd, time)))
        //        cards.ScaleFastest = Some(findScaleFastestCard(fastestGrowingMkt, time))
        //        cards.SalesFastest = Some(findSalesFastestCard(fastestSaleGrowingProd, time))
        //        cards.SalesSlowest = Some(findSalesSlowestCard(fastestSaleDeclineProd, time))
        //        cards.ShareMost = Some(findShareMostCard(maxShareProd, time))
        //        cards.ShareFastest = Some(findShareFastestCard(fastestShareGrowingProd, time))
        //        cards.ShareSlowest = Some(findShareSlowestCard(fastestShareDeclineProd, time))
        cards
    }

    private def findScaleFastestCard(fastestGrowingMkt:Map[String,String], time: String): Card ={
        val scaleFastest = new Card()
        scaleFastest.subtitle = time
        scaleFastest.title = "市场规模增长最快"
        scaleFastest.name = fastestGrowingMkt.getOrElse("market", "无")
        scaleFastest.subname = ""
        scaleFastest.unit = "mil"
        scaleFastest.value = getFormatSales(fastestGrowingMkt.getOrElse("sales", "0.0").toDouble)
        scaleFastest.percent = getFormatShare(fastestGrowingMkt.getOrElse("growth", "0.0").toDouble)
        scaleFastest
    }

    private def findSalesFastestCard(fastestSaleGrowingProd: Map[String, String], time: String): Card = {
        val salesFastest = new Card()
        salesFastest.subtitle = time
        salesFastest.title = "产品销售增长最快"
        salesFastest.name = fastestSaleGrowingProd.getOrElse("product", "无")
        salesFastest.subname = fastestSaleGrowingProd.getOrElse("market", "无")
        salesFastest.unit = "mil"
        salesFastest.value = getFormatSales(fastestSaleGrowingProd.getOrElse("sales", "0.0").toDouble)
        salesFastest.percent = getFormatShare(fastestSaleGrowingProd.getOrElse("productGrowth", "0.0").toDouble)
        salesFastest
    }

    private def findSalesSlowestCard(fastestSaleDeclineProd: Map[String, String], time: String): Card = {
        val salesSlowest = new Card()
        salesSlowest.title = "产品销售下滑最多"
        salesSlowest.subtitle = time
        salesSlowest.name = fastestSaleDeclineProd.getOrElse("product", "无")
        salesSlowest.subname = fastestSaleDeclineProd.getOrElse("market", "无")
        salesSlowest.unit = "mil"
        salesSlowest.value = getFormatSales(fastestSaleDeclineProd.getOrElse("sales", "0.0").toDouble)
        salesSlowest.percent = getFormatShare(fastestSaleDeclineProd.getOrElse("productGrowth", "0.0").toDouble)
        salesSlowest
    }

    private def findShareMostCard(maxShareProd: Map[String, String], time: String): Card = {
        val shareMost = new Card()
        shareMost.title = "份额最多"
        shareMost.subtitle = time
        shareMost.name = maxShareProd.getOrElse("product", "无")
        shareMost.subname = maxShareProd.getOrElse("market", "无")
        shareMost.unit = "%"
        shareMost.value = getFormatShare(maxShareProd.getOrElse("companyProdShare", "0.0").toDouble)
        shareMost.percent = getFormatShare(maxShareProd.getOrElse("companyProdShareGrowth", "0.0").toDouble)
        shareMost
    }

    private def findShareFastestCard(fastestShareGrowingProd: Map[String, String], time: String): Card = {
        val salesFastest = new Card()
        salesFastest.title = "产品份额增长最快"
        salesFastest.subtitle = time
        salesFastest.name = fastestShareGrowingProd.getOrElse("product", "无")
        salesFastest.subname = fastestShareGrowingProd.getOrElse("market", "无")
        salesFastest.unit = "%"
        salesFastest.value = getFormatShare(fastestShareGrowingProd.getOrElse("companyProdShare", "0.0").toDouble)
        salesFastest.percent = getFormatShare(fastestShareGrowingProd.getOrElse("companyProdShareGrowth", "0.0").toDouble)
        salesFastest
    }

    private def findShareSlowestCard(fastestShareDeclineProd: Map[String, String], time: String): Card = {
        val shareSlowest = new Card()
        shareSlowest.title = "份额下滑最多"
        shareSlowest.subtitle = time
        shareSlowest.name = fastestShareDeclineProd.getOrElse("product", "无")
        shareSlowest.subname = fastestShareDeclineProd.getOrElse("market", "无")
        shareSlowest.unit = "%"
        shareSlowest.value = getFormatShare(fastestShareDeclineProd.getOrElse("companyProdShare", "0.0").toDouble)
        shareSlowest.percent = getFormatShare(fastestShareDeclineProd.getOrElse("companyProdShareGrowth", "0.0").toDouble)
        shareSlowest
    }
}
