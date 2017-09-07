package events

import infrastructure.Event
import spray.json.DefaultJsonProtocol._
import spray.json.{JsonFormat, _}

/**
  * Created by rudolf on 2017/08/27.
  */

case class MeterCreated(name: String,
                        number: String) extends Event


object MeterCreated {
  val jsonFormat: JsonFormat[MeterCreated] = jsonFormat2(MeterCreated.apply)
}


