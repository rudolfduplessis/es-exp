package repositories

import io.centular.common.lib.{EmptyID, ID}
import model.{Meter, MeterJsonProtocol}
import temporal.data.PostgreSqlEventStore
import temporal.repo.TemporalAggregateRepository

/**
  * Created by rudolf on 2017/08/27.
  */
object MeterRepo extends PostgreSqlEventStore[ID]("event_schema", "event", "snapshot")
  with TemporalAggregateRepository[ID, Meter] with MeterJsonProtocol {
  override val emptyAggregate: Meter = Meter(EmptyID, -1, "", "")
}
