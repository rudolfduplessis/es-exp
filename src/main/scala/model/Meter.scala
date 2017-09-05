package model

import commands.CreateMeter
import events.{MeterCreated, MeterCreatedJsonProtocol}
import infrastructure._
import io.centular.common.lib.{EmptyID, ID}
import org.joda.time.DateTime
import spray.json._

/**
  * Created by rudolf on 2017/08/05.
  */
case class Meter(id: ID,
                 name: String,
                 number: String) extends TemporalAggregate[ID, Meter] {
  def apply(event: Event[ID]): Meter = event match {
    case e: MeterCreated => Meter(e.aggregateId, e.name, e.number)
  }
}

class MeterCommandProcessor extends CommandProcessor[ID, Meter] {
  override def process(command: AggregateCommand[ID], aggregate: Option[Meter]): Seq[Event[ID]] = command match {
    case c: CreateMeter => {
      val dt = DateTime.now().toString
      Seq(MeterCreated(ID(), dt, dt, ID("3bec333a-8ff6-11e7-abc4-cec278b6b50a"), None, ID(), c.name, c.number))
    }

    case other => throw new Exception(s"No command processor found for ${command.getClass.getName}")
  }
}

trait MeterFactory extends TemporalAggregateFactory[ID, Meter] with DefaultJsonProtocol {
  override val emptyInstance = Meter(EmptyID, "", "")

  implicit val eventJsonFormat: JsonFormat[Event[ID]] = new JsonFormat[Event[ID]] {
    override def read(json: JsValue): Event[ID] = {
      json.asJsObject.getFields("eventName") match {
        case Seq(eventName) => eventName.convertTo[String] match {
          case n if n == "Meter Created" => MeterCreatedJsonProtocol.MeterCreatedFormat.read(json)
          case other => deserializationError(s"Cannot deserialize $eventName: invalid input. Raw input: " + other)
        }
      }
    }

    override def write(obj: Event[ID]): JsValue = obj match {
      case e: MeterCreated => MeterCreatedJsonProtocol.MeterCreatedFormat.write(e)
    }
  }

  override implicit val aggregateJsonFormat: JsonFormat[Meter] = jsonFormat3(Meter.apply)
}