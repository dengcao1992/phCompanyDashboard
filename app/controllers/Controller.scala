package controllers

import play.api.mvc._
import io.circe.syntax._
import akka.actor.ActorSystem
import play.api.libs.circe.Circe
import javax.inject.{Inject, Singleton}
import com.pharbers.jsonapi.model.RootObject
import com.pharbers.jsonapi.json.circe.CirceJsonapiSupport
import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import services._

@Singleton
class Controller @Inject()(implicit val cc: ControllerComponents,
                           implicit val actorSystem: ActorSystem,
                           implicit val dbt: dbInstanceManager)
        extends AbstractController(cc) with Circe with CirceJsonapiSupport {

    def routes(pkg: String, pkg2: String): Action[RootObject] = Action(circe.json[RootObject]) {
        implicit request =>
        Ok(
            (pkg, pkg2) match {
                case ("dashboard", "saleData") => FindCompanyData().selectData().asJson
                case ("dashboard", "keyWord") => FindCompanyKeyWord().selectKeyWord().asJson
                case ("dashboard", "overView") => FindCompanySalesOverview().selectOverview().asJson
                case ("dashboard", "contribution") => FindCompanyProdSalesContribution().selectContribution().asJson
            }
        )
    }

    def routes2(pkg1: String  , pkg2: String):Action[RootObject] = Action(circe.json[RootObject]) {
        implicit request =>
            Ok(
                (pkg1, pkg2) match{
                    case ("market", "all") => FindAllMarket().selectAllMarket().asJson
                    case ("nation", "saleShare") => FindNationSaleShare().selectNationSaleShare().asJson
                    case ("nation", "marketTrend") => FindNationMarketTrend().selectNationMarketTrend().asJson
                    case ("nation", "mostWord") => FindNationMostWord().selectNationMostWord().asJson
                    case ("nation", "productShare") => FindNationProductShare().selectNationProductShare().asJson
                    case ("nation", "productRank") => FindNationProductRank().selectNationProductRank().asJson
                    case ("nation", "productTable") => FindNationProductTable().selectNationProductTable().asJson
                    case ("nation", "prodTrendAnalysis") => FindNationProdTrendAnalysis().selectNationProdTrendAnalysis().asJson
                    case ("province", "all") => FindProvinceLst().selectProvinceList().asJson
                    case ("province", "provinceName") => FindProvinceCardData().selectProvinceCardData().asJson
                    case ("province", "lineOverview") => FindProvinceLineOverview().selectProvinceLineOverview().asJson
                    case ("province", "tableOverview") => FindProvinceTableOverview().selectProvinceTableOverview().asJson
                    case ("province", "marketPart") => FindProvinceMarketPart().selectProvinceMarketPart().asJson
                    case ("province", "provLevelRank") => FindProvinceLevelRank().selectProvinceLevelRank().asJson
                }
            )
    }
}
