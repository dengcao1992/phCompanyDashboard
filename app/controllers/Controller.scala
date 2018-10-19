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
import services.{FindDataByCompanyAndYM, FindKeyWordByCompanyIdAndTime}

@Singleton
class Controller @Inject()(implicit val cc: ControllerComponents,
                           implicit val actorSystem: ActorSystem,
                           implicit val dbt: dbInstanceManager)
        extends AbstractController(cc) with Circe with CirceJsonapiSupport {

    def routes(pkg: String, pkg2: String): Action[RootObject] = Action(circe.json[RootObject]) {
        implicit request =>
        Ok(
            (pkg, pkg2) match {
                case ("dashboard", "saleData") => FindDataByCompanyAndYM().selectData().asJson
                case ("dashboard", "keyWord") => {
                    val a = FindKeyWordByCompanyIdAndTime().selectKeyWord()
                    a.asJson
                }
            }
        )
    }

//    def routes2(pkg1: String  , pkg2: String, step: Int): Action[RootObject] = routes(pkg1 + "/" + pkg2, step)
}
