package repositories

import io.centular.common.lib.{EmptyID, ID}
import model.{Meter, MeterJsonProtocol}
import temporal.Repository
import temporal.data.PostgreSqlEventStore

/**
  * Created by rudolf on 2017/08/27.
  */
object MeterRepo extends PostgreSqlEventStore[ID]("event_schema", "event", "snapshot")
  with Repository[ID, Meter] with MeterJsonProtocol {
  override val emptyAggregate: Meter = Meter(EmptyID, -1, "", "")
}
