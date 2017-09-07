import com.fasterxml.uuid.Generators
import io.centular.common.CentularPostgres.dataSource
import io.centular.common.FlywayMigration
import io.centular.common.lib.ID
import io.centular.common.model.Context
import repositories.MeterRepo

/**
  * Created by rudolf on 2017/08/04.
  */
object Main extends App {

  FlywayMigration.runMigration(dataSource)

  val userId = Generators.timeBasedGenerator().generate().toString
  implicit val context =
    Context("66a56910-8ff7-11e7-abc4-cec278b6b50a", "a6ee411e-91ae-11e7-abc4-cec278b6b50a", "3bec333a-8ff6-11e7-abc4-cec278b6b50a")

/*  val meter = MeterRepo.emptyAggregate
  val createdEvents = meter.executeCommand(CreateMeter("A Meter", "11111"))
  val createdMeter = MeterRepo.save(meter, createdEvents)

  val nameChangedEvents = createdMeter.executeCommand(ChangeMeterName("Meter name updated"))
  val nameChangedMeter = MeterRepo.save(createdMeter, nameChangedEvents)

  val numberChangedEvents = nameChangedMeter.executeCommand(ChangeMeterNumber("2222222"))
  val numberChangedMeter = MeterRepo.save(nameChangedMeter, numberChangedEvents)

  println(createdMeter)
  println(nameChangedMeter)
  println(numberChangedMeter)

  println(MeterRepo.audit(numberChangedMeter.id))*/

  println(MeterRepo.getAggregateAsOf(ID("8e9ef662-93dc-11e7-b113-0d4f4dd840a6"), "2017-09-07 16:55:16.000000"))
  println(MeterRepo.audit(ID("8e9ef662-93dc-11e7-b113-0d4f4dd840a6")))
}


