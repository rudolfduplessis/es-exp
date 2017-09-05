package events

import infrastructure.Event
import io.centular.common.lib.ID
import spray.json.{DefaultJsonProtocol, JsObject, JsValue, RootJsonFormat, _}

/**
  * Created by rudolf on 2017/08/27.
  */

case class MeterCreated(eventId: ID,
                        eventRaised: String,
                        eventApplies: String,
                        eventSenderId: ID,
                        eventDescription: Option[String],
                        aggregateId: ID,
                        name: String,
                        number: String) extends Event[ID] {
  override val eventName: String = "Meter Created"
}

object MeterCreatedJsonProtocol extends DefaultJsonProtocol {

  implicit object MeterCreatedFormat extends RootJsonFormat[MeterCreated] {
    override def read(json: JsValue): MeterCreated = {
      val jsObject = json.asJsObject
      jsObject.getFields("eventId", "eventRaised", "eventApplies", "eventSenderId", "aggregateId", "name", "number") match {
        case Seq(eventId, eventRaised, eventApplies, eventSenderId, aggregateId, name, number) => MeterCreated(
          eventId.convertTo[ID],
          eventRaised.convertTo[String],
          eventApplies.convertTo[String],
          eventSenderId.convertTo[ID],
          jsObject.fields.get("eventDescription").map(_.convertTo[String]),
          aggregateId.convertTo[ID],
          name.convertTo[String],
          number.convertTo[String])
      }
    }

    override def write(obj: MeterCreated): JsValue = JsObject(
      List(
        Some("eventId" -> obj.eventId.toJson),
        Some("eventName" -> obj.eventName.toJson),
        Some("eventRaised" -> obj.eventRaised.toJson),
        Some("eventApplies" -> obj.eventApplies.toJson),
        obj.eventDescription.map(d => "eventDescription" -> d.toJson),
        Some("eventSenderId" -> obj.eventSenderId.toJson),
        Some("aggregateId" -> obj.aggregateId.toJson),
        Some("name" -> obj.name.toJson),
        Some("number" -> obj.number.toJson)
      ).flatten: _*
    )
  }
}


