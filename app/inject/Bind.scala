package inject

import com.pharbers.driver.util.PhRedisTrait
import com.pharbers.dbManagerTrait.dbInstanceManager
import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}

class Bind extends Module{
    override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
        Seq(
            bind[dbInstanceManager].to[DbInstanceManager]
//            ,bind[PhRedisTrait].to[PhRedisTrait]
        )
    }
}
