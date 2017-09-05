package repositories

import infrastructure._
import io.centular.common.lib.ID
import model.{Meter, MeterCommandProcessor, MeterFactory}

/**
  * Created by rudolf on 2017/08/27.
  */
object MeterRepo extends PostgresSqlEventStore[ID] with TemporalAggregateRepository[ID, Meter] with MeterFactory {
  override val commandProcessor: CommandProcessor[ID, Meter] = new MeterCommandProcessor()
}
