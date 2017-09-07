package infrastructure

import io.centular.common.lib.Identifier
import spray.json.{JsValue, JsonFormat, JsonWriter}

/**
  * Created by rudolf on 2017/09/05.
  */
trait AggregateJsonProtocol[ID <: Identifier[_], TAggregate <: AnyRef] {
  implicit val aggregateJsonFormat: JsonFormat[TAggregate]
  implicit def readJsonFor(eventName: String, json: JsValue): Event
  implicit val eventJsonWriter: JsonWriter[Event]
}
