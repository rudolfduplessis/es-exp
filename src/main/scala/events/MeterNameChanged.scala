package events

import spray.json.DefaultJsonProtocol._
import spray.json._
import temporal.Event

/**
  * Created by rudolf on 2017/09/07.
  */
case class MeterNameChanged(name: String) extends Event

object MeterNameChanged {
  def jsonFormat: JsonFormat[MeterNameChanged] = jsonFormat1(MeterNameChanged.apply)
}
