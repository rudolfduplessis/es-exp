package model

import commands.{ChangeMeterName, ChangeMeterNumber, CreateMeter}
import events._
import infrastructure._
import io.centular.common.lib.ID
import io.centular.common.model.Context
import spray.json._

/**
  * Created by rudolf on 2017/08/05.
  */
case class Meter(id: ID,
                 version: Int,
                 name: String,
                 number: String) extends TemporalAggregate[ID, Meter] {

  override def executeCommand(command: AggregateCommand)(implicit context: Context): Seq[Envelope] = command match {
    case c: CreateMeter =>
      Seq(Envelope(ID(), MeterCreated(c.name, c.number)))
    case c: ChangeMeterName =>
      Seq(Envelope(id, MeterNameChanged(c.name)))
    case c: ChangeMeterNumber =>
      Seq(Envelope(id, MeterNumberChanged(c.number)))
    case other => throw new Exception(s"No command processor found for ${command.getClass.getName}")
  }

  def applyEvent(envelope: Envelope): Meter = envelope.event match {
    case e: MeterCreated => Meter(envelope.aggregateId, version + 1,  e.name, e.number)
    case e: MeterNameChanged => copy(name = e.name, version = version + 1)
    case e: MeterNumberChanged => copy(number = e.number, version = version + 1)
  }

  override def takeSnapshot: Boolean = version % 2 == 0 && version != 0
}

trait MeterJsonProtocol extends AggregateJsonProtocol[ID, Meter] with DefaultJsonProtocol {

  override implicit val aggregateJsonFormat: JsonFormat[Meter] = jsonFormat4(Meter.apply)

  override implicit def readJsonFor(eventName: String, json: JsValue): Event = {
    eventName match {
      case n if n == classOf[MeterCreated].getSimpleName => MeterCreated.jsonFormat.read(json)
      case n if n == classOf[MeterNameChanged].getSimpleName => MeterNameChanged.jsonFormat.read(json)
      case n if n == classOf[MeterNumberChanged].getSimpleName => MeterNumberChanged.jsonFormat.read(json)
      case other => deserializationError(s"Cannot deserialize $eventName: invalid input. Raw input: " + other)
    }
  }

  override implicit val eventJsonWriter: JsonWriter[Event] = new JsonWriter[Event] {
    override def write(obj: Event): JsValue = obj match {
      case e: MeterCreated => MeterCreated.jsonFormat.write(e)
      case e: MeterNameChanged => MeterNameChanged.jsonFormat.write(e)
      case e: MeterNumberChanged => MeterNumberChanged.jsonFormat.write(e)
    }
  }
}