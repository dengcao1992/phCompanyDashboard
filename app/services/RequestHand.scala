package services

import com.pharbers.jsonapi.model
import com.pharbers.models.request.request
import play.api.mvc.Request

trait RequestHand {
    implicit val rq: Request[model.RootObject]
    var requestData: request = null
    var companyId = ""
    var time = ""
    var market = ""
    var tag = ""
    var ym = ""
    var province = ""
    def init(): Unit ={
        requestData.eqcond.getOrElse(Nil) match {
            case Nil => ???
            case eqconds => {
                //                if (eqconds(0).`val` != null && eqconds(1).`val` != null) {
                //                    tableSale = findTableSale(eqconds(0).`val`.toString, eqconds(1).`val`.toString)
                //                }
                //                toJsonapi(tableSale)
                eqconds.foreach(x => {
                    if (x.key == "company_id" && x.`val` != null) companyId = x.`val`.toString
                    if (x.key == "time" && x.`val` != null) time = x.`val`.toString
                    if (x.key == "market" && x.`val` != null) market = x.`val`.toString
                    if (x.key == "tag" && x.`val` != null) tag = x.`val`.toString
                    if (x.key == "province" && x.`val` != null) province = x.`val`.toString
                })
            }
            case _ => ???
        }
        ym = time.replaceAll("-", "")
    }

}
