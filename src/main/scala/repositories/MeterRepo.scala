package repositories

import infrastructure._
import io.centular.common.lib.ID
import io.centular.common.model.Context
import model.{Meter, MeterCommandProcessor, MeterFactory}

/**
  * Created by rudolf on 2017/08/27.
  */
object MeterRepo extends TemporalAggregateRepository[ID, Meter] with PostgresSqlEventStore[ID] with MeterFactory {

  override val commandProcessor: CommandProcessor[ID, Meter] = new MeterCommandProcessor()

  def getUntil(id: ID, untilConditions: Map[String, AnyVal])(implicit context: Context) = {
    foldEvents(selectEventsQuery(id, untilConditions).map(_.convertTo[Event[ID]]), selectSnapshotQuery(id).map(_.convertTo[Meter]))
  }
}
