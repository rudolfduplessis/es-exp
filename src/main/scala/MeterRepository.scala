import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.{Date, UUID}

import anorm._
import anorm.SqlParser._
import com.fasterxml.uuid.Generators
import io.centular.common.CentularPostgres.dataSource
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import commands.{CreateMeterCommand, MeterCommand}
import model.{AggregateCommandProcessor, CompactEncodedEvent, EventCodec, Meter}
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TIOStreamTransport

import scala.reflect.runtime.{universe => ru}
import scala.util.{Failure, Success, Try}


/**
  * Created by rudolf on 2017/08/05.
  */
class MeterRepository(processor: AggregateCommandProcessor[MeterCommand, Meter, ThriftStruct], eventCodec: EventCodec[ThriftStruct, CompactEncodedEvent]) {

  case class NewEvent(eventName: String,
                      senderId: String,
                      senderType: String,
                      aggregateId: String,
                      payloadType: String,
                      payload: Array[Byte])

  case class Event(eventId: String,
                   eventName: String,
                   eventRaised: String,
                   senderId: String,
                   senderType: String,
                   aggregateId: String,
                   payloadType: String,
                   payload: Array[Byte])

  implicit val eventParser: RowParser[Event] = {
    get[UUID]("event_id") ~
      get[String]("event_name") ~
      get[Date]("event_raised") ~
      get[UUID]("sender_id") ~
      get[String]("sender_type") ~
      get[UUID]("aggregate_id") ~
        get[String]("payload_type") ~
      get[Array[Byte]]("payload") map {
      case eventId ~ eventName ~ eventRaised ~ senderId ~ senderType ~ aggregateId ~ payloadType ~ payload =>
        Event(eventId.toString, eventName, eventRaised.toString, senderId.toString, senderType, aggregateId.toString, payloadType, payload)
    }
  }

  def create(senderId: String, senderType: String, createMeterCommand: CreateMeterCommand): Try[Meter] = {
    processor.processCommand(createMeterCommand) match {
      case Success(payloads) =>
        val newAggregateId = Generators.timeBasedGenerator().generate().toString
        val events = payloads.map(p => {
          val encoded = eventCodec.encode(p)
          NewEvent(
            "UC",
            senderId,
            senderType,
            newAggregateId,
            encoded.eventType,
            encoded.encodedObject)
        })
        insert(events.head)
        Success(payloads.foldLeft(processor.newInstance())((a: Meter, event: ThriftStruct) =>
          processor.applyEvent(a.copy(id=newAggregateId), event)))

      case Failure(ex) => Failure(ex)
    }
  }

  private def insert(event: NewEvent) = {
    implicit val con = dataSource.getConnection
    try {
      SQL("""
            |INSERT INTO eventstore.meter_events (event_id, event_name, event_raised, sender_id, sender_type, aggregate_id, payload_type, payload)
            |VALUES ({eventId}::uuid, {eventName}, now(), {senderId}::uuid, {senderType}, {aggregateId}::uuid, {payloadType}, {payload})
          """.stripMargin)
        .on(
          'eventId -> Generators.timeBasedGenerator().generate().toString,
          'eventName -> event.eventName,
          'senderId -> event.senderId,
          'senderType -> event.senderType,
          'aggregateId -> event.aggregateId,
          'payloadType -> event.payloadType,
          'payload -> event.payload
        ).executeInsert(SqlParser.scalar[UUID].singleOpt)
    } catch {
      case ex: Throwable =>
        println(ex.getMessage)
    }
    finally {
      con.close()
    }
  }

  /*def issueCommand(meterCommand: MeterCommand): Meter = {
    val events = processor.processCommand(meterCommand)

  }*/

  def getById(aggregateId: String): Meter = {
    val events = selectEvents(aggregateId)
    events.foldLeft(processor.newInstance())((a: Meter, e: Event) => {
      processor.applyEvent(a.copy(id=aggregateId), eventCodec.decode(CompactEncodedEvent(e.payloadType, e.payload)))
    })
  }

  private def selectEvents(aggregateId: String): Seq[Event] = {
    implicit val con = dataSource.getConnection
    try {
      SQL("""
            |SELECT * FROM eventstore.meter_events
            |WHERE aggregate_id = {aggregateId}::uuid
            |ORDER BY event_raised
          """.stripMargin)
        .on('aggregateId -> aggregateId).executeQuery().as(eventParser.*)
    } finally {
      con.close()
    }
  }


}

