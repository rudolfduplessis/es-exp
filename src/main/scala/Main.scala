import com.fasterxml.uuid.Generators
import io.centular.common.CentularPostgres.dataSource
import io.centular.common.FlywayMigration
import io.centular.common.lib.ID
import io.centular.common.model.Context
import repositories.MeterRepo

import scala.reflect.runtime.{universe => ru}

/**
  * Created by rudolf on 2017/08/04.
  */
object Main extends App {

  FlywayMigration.runMigration(dataSource)

  val userId = Generators.timeBasedGenerator().generate().toString
  implicit val context =
    Context("66a56910-8ff7-11e7-abc4-cec278b6b50a", "a6ee411e-91ae-11e7-abc4-cec278b6b50a", "3bec333a-8ff6-11e7-abc4-cec278b6b50a")

  //MeterRepo.execute(CreateMeter("Some other meter", "11111"))
  //println(MeterRepo.getAggregateAsOf(ID("ca754f3c-926f-11e7-8ace-7f9368c9de62"), "2017-09-06T21:10:15.576+02:00"))
  println(MeterRepo.audit(ID("ca754f3c-926f-11e7-8ace-7f9368c9de62")))
}


