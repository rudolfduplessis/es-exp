package infrastructure
/**
  * Created by rudolf on 2017/09/07.
  */

import io.centular.common.lib.ID
import io.centular.common.model.Context
import org.joda.time.DateTime

/**
  * Created by rudolf on 2017/09/07.
  */
class Envelope private(val eventId: ID,
                       val eventName: String,
                       val eventRaised: String,
                       val eventApplies: String,
                       val eventDescription: Option[String],
                       val eventSenderId: ID,
                       val aggregateId: ID,
                       val event: Event)

object Envelope {
  def apply(eventName: String,
            aggregateId: ID,
            event: Event,
            eventApplies: Option[String] = None,
            eventDescription: Option[String] = None)
           (implicit context: Context): Envelope = {
    val now = DateTime.now.toString
    new Envelope(ID(), eventName, now, eventApplies.getOrElse(now), eventDescription, ID(context.userId), aggregateId, event)
  }

  private [infrastructure] def apply(eventId: ID,
                                     eventName: String,
                                     eventRaised: String,
                                     eventApplies: String,
                                     eventDescription: Option[String],
                                     eventSenderId: ID,
                                     aggregateId: ID,
                                     event: Event): Envelope =
    new Envelope(eventId, eventName, eventRaised, eventApplies, eventDescription, eventSenderId, aggregateId, event)


  /*private[infrastructure] implicit object EventMetadataJsonFormat extends DefaultJsonProtocol with RootJsonFormat[EventMetadata] {
    override def write(obj: EventMetadata): JsValue = JsObject(
      List(
        Some("eventId" -> obj.eventId.toJson),
        Some("eventName" -> obj.eventName.toJson),
        Some("eventRaised" -> obj.eventRaised.toJson),
        Some("eventApplies" -> obj.eventApplies.toJson),
        obj.eventDescription.map(d => "eventDescription" -> d.toJson),
        Some("eventSenderId" -> obj.eventSenderId.toJson),
        Some("aggregateId" -> obj.aggregateId.toJson)
      ).flatten: _*
    )

    override def read(json: JsValue): EventMetadata = {
      val jsObject = json.asJsObject
      jsObject.getFields("eventId", "eventName", "eventRaised", "eventApplies", "eventSenderId", "aggregateId") match {
        case Seq(eventId, eventName, eventRaised, eventApplies, eventSenderId, aggregateId) =>
          new EventMetadata(
            eventId.convertTo[ID],
            eventName.convertTo[String],
            eventRaised.convertTo[String],
            eventApplies.convertTo[String],
            jsObject.fields.get("eventDescription").map(_.convertTo[String]),
            eventSenderId.convertTo[ID],
            aggregateId.convertTo[ID])
      }
    }
  }*/
}


