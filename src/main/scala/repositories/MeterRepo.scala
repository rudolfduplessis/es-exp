package repositories

import commands.CreateMeter
import events.{MeterCreated, MeterCreatedJsonProtocol}
import infrastructure.{AggregateCommand, Event, TemporalAggregateRepository}
import io.centular.common.lib.{EmptyID, ID}
import model.Meter
import org.joda.time.DateTime
import spray.json._

/**
  * Created by rudolf on 2017/08/27.
  */
object MeterRepo extends TemporalAggregateRepository[ID, Meter] with DefaultJsonProtocol {
  override val commandProcessor: (AggregateCommand[ID], Option[Meter]) => Seq[Event[ID]] = (command, meter) => {
    command match {
      case c: CreateMeter => Seq(MeterCreated(ID(), DateTime.now().toString, ID("3bec333a-8ff6-11e7-abc4-cec278b6b50a"), None, ID(), c.name, c.number))
      case other => throw new Exception(s"No command processor found for ${command.getClass.getName}")
    }
  }

  override implicit val aggregateJsonFormat: JsonFormat[Meter] = jsonFormat3(Meter.apply)

  override val emptyInstance: () => Meter = () => Meter(EmptyID, "", "")

  override val eventProcessor: (Meter, Event[ID]) => Meter = (meter, event) => {
    event match {
      case e: MeterCreated => Meter(e.aggregateId, e.name, e.number)
    }
  }

  override val eventJsonWriter: (Event[ID]) => JsValue = {
    case e: MeterCreated => MeterCreatedJsonProtocol.MeterCreatedFormat.write(e)
  }
  override val eventJsonReader: (JsValue) => Event[ID] = { json =>
    json.asJsObject.getFields("eventName") match {
      case Seq(eventName) => eventName.convertTo[String] match {
        case n if n == "Meter Created" => MeterCreatedJsonProtocol.MeterCreatedFormat.read(json)
        case other => deserializationError(s"Cannot deserialize $eventName: invalid input. Raw input: " + other)
      }
    }
  }
}
