package events

import spray.json.DefaultJsonProtocol._
import spray.json.JsonFormat
import temporal.Event

/**
  * Created by rudolf on 2017/08/27.
  */

case class MeterCreated(name: String,
                        number: String) extends Event


object MeterCreated {
  val jsonFormat: JsonFormat[MeterCreated] = jsonFormat2(MeterCreated.apply)
}


