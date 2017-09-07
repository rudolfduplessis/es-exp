package repositories

import infrastructure.data.PostgreSqlEventStore
import infrastructure.repo.TemporalAggregateRepository
import io.centular.common.lib.{EmptyID, ID}
import model.{Meter, MeterJsonProtocol}

/**
  * Created by rudolf on 2017/08/27.
  */
object MeterRepo extends PostgreSqlEventStore[ID]("event_schema", "event", "snapshot")
  with TemporalAggregateRepository[ID, Meter] with MeterJsonProtocol {
  override val emptyAggregate: Meter = Meter(EmptyID, -1, "", "")
}
