package events

import infrastructure.Event
import spray.json.DefaultJsonProtocol._
import spray.json._

/**
  * Created by rudolf on 2017/09/07.
  */
case class MeterNumberChanged(number: String) extends Event

object MeterNumberChanged {
  val jsonFormat: JsonFormat[MeterNumberChanged] = jsonFormat1(MeterNumberChanged.apply)
}
