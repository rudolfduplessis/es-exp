package model

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import commands.{CreateMeterCommand, MeterCommand}
import io.centular.meter.model.MeterCreated
import org.apache.thrift.protocol.{TCompactProtocol, TProtocol}
import org.apache.thrift.transport.TIOStreamTransport

import scala.reflect.runtime.{universe => ru}
import scala.util.Try

/**
  * Created by rudolf on 2017/08/05.
  */
trait ThriftCompactCodec {
  final def encode[T <: ThriftStruct](structCodec: ThriftStructCodec[T], struct: T): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    structCodec.encode(struct, new TCompactProtocol(new TIOStreamTransport(out)))
    out.toByteArray
  }
  final def decode[T <: ThriftStruct](structCodec: ThriftStructCodec[T], data: Array[Byte]): T = {
    val stream = new ByteArrayInputStream(data)
    structCodec.decode(new TCompactProtocol(new TIOStreamTransport(stream)))
  }
}

trait EventCodec[TDecoded, TEncoded] {
  def encode(decoded: TDecoded): TEncoded
  def decode(encoded: TEncoded): TDecoded
}

case class CompactEncodedEvent(eventType: String, encodedObject: Array[Byte])

class MeterEventCodec extends EventCodec[ThriftStruct, CompactEncodedEvent] with ThriftCompactCodec {
  override def encode(decoded: ThriftStruct): CompactEncodedEvent = decoded match {
    case s: MeterCreated =>
      CompactEncodedEvent(classOf[MeterCreated].getTypeName, encode(MeterCreated, decoded.asInstanceOf[MeterCreated]))
  }
  override def decode(encoded: CompactEncodedEvent): ThriftStruct = encoded.eventType match {
    case et if et == classOf[MeterCreated].getTypeName => decode(MeterCreated, encoded.encodedObject)
  }
}

trait AggregateCommandProcessor[TBaseCommand, TAggregate, TEventBase] {
  def newInstance(): TAggregate
  def processCommand(meterCommand: TBaseCommand): Try[Seq[TEventBase]]
  def applyEvent(aggregate: TAggregate, eventPayload: ThriftStruct): TAggregate
}


class MeterCommandProcessor extends AggregateCommandProcessor[MeterCommand, Meter, ThriftStruct] {

  override def newInstance(): Meter = Meter("", "", "", null, null, None)

  //private def getTypeTag[T: ru.TypeTag](obj: T) = ru.typeTag[T]
  override def processCommand(meterCommand: MeterCommand): Try[Seq[ThriftStruct]] = Try(meterCommand match {
    case CreateMeterCommand(name, number, meterType, meterStatus, meterModel) =>
      Seq(MeterCreated(name, number, meterType, meterStatus, meterModel))
  })

  override def applyEvent(aggregate: Meter, eventPayload: ThriftStruct): Meter = eventPayload match {
    case MeterCreated(name, number, meterType, meterStatus, meterModel) =>
      aggregate.copy(name=name, number=number, meterType=meterType, meterStatus=meterStatus, meterModel=meterModel)
  }

}


