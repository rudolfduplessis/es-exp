package infrastructure.data


import java.util.UUID

import anorm.SqlParser._
import anorm.{RowParser, ~}
import infrastructure.data.JsColumn._
import io.centular.common.lib.ID
import org.joda.time.DateTime
import spray.json.{DefaultJsonProtocol, JsObject, JsValue, RootJsonFormat, _}

/**
  * Created by rudolf on 2017/09/07.
  */
private [infrastructure] case class Envelope(eventId: ID,
                                             eventName: String,
                                             eventRaised: String,
                                             eventApplies: String,
                                             eventDescription: Option[String],
                                             eventSenderId: ID,
                                             aggregateId: ID,
                                             event: JsValue)

private [infrastructure] object Envelope {
  val parser: RowParser[Envelope] = {
    get[UUID]("event_id") ~
    get[String]("event_name") ~
    get[DateTime]("event_raised") ~
    get[DateTime]("event_applies") ~
    get[Option[String]]("event_description") ~
    get[UUID]("event_sender_id") ~
    get[UUID]("aggregate_id") ~
    get[JsValue]("event") map {
      case eventId ~ eventName ~ eventRaised ~ eventApplies ~ eventDescription ~ eventSenderId ~ aggregateId ~ event =>
        Envelope(ID(eventId), eventName, eventRaised.toString, eventApplies.toString, eventDescription, ID(eventSenderId), ID(aggregateId), event)
    }
  }

  private[infrastructure] implicit object EventJsonFormat extends DefaultJsonProtocol with RootJsonFormat[Envelope] {
    override def write(obj: Envelope): JsValue = JsObject(
      List(
        Some("eventId" -> obj.eventId.toJson),
        Some("eventName" -> obj.eventName.toJson),
        Some("eventRaised" -> obj.eventRaised.toJson),
        Some("eventApplies" -> obj.eventApplies.toJson),
        obj.eventDescription.map(d => "eventDescription" -> d.toJson),
        Some("eventSenderId" -> obj.eventSenderId.toJson),
        Some("aggregateId" -> obj.aggregateId.toJson),
        Some("event" -> obj.event)
      ).flatten: _*
    )

    override def read(json: JsValue): Envelope = {
      val jsObject = json.asJsObject
      jsObject.getFields("eventId", "eventName", "eventRaised", "eventApplies", "eventSenderId", "aggregateId", "event") match {
        case Seq(eventId, eventName, eventRaised, eventApplies, eventSenderId, aggregateId, event) =>
          Envelope(
            eventId.convertTo[ID],
            eventName.convertTo[String],
            eventRaised.convertTo[String],
            eventApplies.convertTo[String],
            jsObject.fields.get("eventDescription").map(_.convertTo[String]),
            eventSenderId.convertTo[ID],
            aggregateId.convertTo[ID],
            event)
      }
    }
  }
}
