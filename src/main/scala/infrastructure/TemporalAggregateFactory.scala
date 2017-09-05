package infrastructure

import io.centular.common.lib.Identifier
import spray.json.JsonFormat

/**
  * Created by rudolf on 2017/09/05.
  */
trait TemporalAggregateFactory[ID <: Identifier[_], TAggregate <: AnyRef] {
  val emptyInstance: TAggregate
  implicit val eventJsonFormat: JsonFormat[Event[ID]]
  implicit val aggregateJsonFormat: JsonFormat[TAggregate]
}
