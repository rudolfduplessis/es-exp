import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.text.SimpleDateFormat
import java.util.{Date, UUID}

import anorm.SqlParser._
import anorm.{SqlParser, _}
import com.fasterxml.uuid.Generators
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import commands.CreateMeterCommand
import io.centular.common.CentularPostgres.dataSource
import io.centular.common.FlywayMigration
import io.centular.meter.model._
import io.centular.user.model.User
import model.{MeterCommandProcessor, MeterEventCodec}
import org.apache.thrift.protocol.TCompactProtocol
import org.apache.thrift.transport.TIOStreamTransport

import scala.reflect.runtime.{universe => ru}
import scala.util.{Failure, Success, Try}

/**
  * Created by rudolf on 2017/08/04.
  */
object Main extends App {

  FlywayMigration.runMigration(dataSource)
/*
  val eventId = Generators.timeBasedGenerator().generate()
  val meterId = UUID.fromString("3c45c9e8-795f-11e7-853c-5b198d66d7fc")
  val userId = Generators.timeBasedGenerator().generate()

  val meterCreatedEvent = MeterCreated("Some Meter", "ZZ-123", MeterType.Water, MeterStatus.Inactive, Option(MeterModel("asdf", "ZXCV-1234")))

  val binaryEvent = {
    val out = new ByteArrayOutputStream()
    meterCreatedEvent.write(new TCompactProtocol(new TIOStreamTransport(out)))
    out.toByteArray
  }

  val writeCon = dataSource.getConnection
  try {
    SQL("""
          |INSERT INTO eventstore.meter_events (event_id, event_type, event_name, event_raised, sender_id, sender_type, aggregate_id, event)
          |VALUES ({eventId}::uuid, {eventType}, {eventName}, now(), {senderId}::uuid, {senderType}, {meterId}::uuid, {event})
        """.stripMargin)
      .on(
        'eventId -> eventId,
        'eventType -> MeterCreated.getClass.getCanonicalName,
        'eventName -> "New Meter Added",
        'senderId -> userId,
        'senderType -> User.getClass.getCanonicalName,
        'meterId -> meterId,
        'event -> encode(MeterCreated, meterCreatedEvent)
      ).executeInsert(SqlParser.scalar[UUID].singleOpt)(writeCon)
  } catch {
    case ex: Throwable =>
      println(ex.getMessage)
  }
  finally {
    writeCon.close()
  }

  /*  sealed abstract class BaseEvent(eventId: String, eventType: String, eventName: String, eventRaised: String, userId: String, aggregateId: String)
    case class Event[T](eventId: String, eventType: String, eventName: String, eventRaised: String, userId: String, aggregateId: String, event: T)
      extends BaseEvent(eventId, eventType, eventName, eventRaised, userId, aggregateId)

    def typeName2(b: Box) = b match {
      case TypedBox(v: Double) => "Double"
      case TypedBox(v: String) => "String"
      case TypedBox(v: Boolean) => "Boolean"
      case _ => "Unknown"
    }*/
  case class Event(eventId: String, eventType: String, eventName: String, eventRaised: String, userId: String, aggregateId: String, event: Array[Byte])

  val eventParser: RowParser[Event] = {
    get[UUID]("event_id") ~
      get[String]("event_type") ~
      get[String]("event_name") ~
      get[Date]("event_raised") ~
      get[UUID]("sender_id") ~
      get[UUID]("aggregate_id") ~
      get[Array[Byte]]("event") map {
      case eventId ~ eventType ~ eventName ~ eventRaised ~ userId ~ aggregateId ~ event =>
        Event(eventId.toString, eventType, eventName, eventRaised.toString, userId.toString, aggregateId.toString, event)
    }
  }

  def getEvents(aggregateId: UUID): Seq[Event] = {
    implicit val readCon = dataSource.getConnection
    try {
      SQL("""
            |SELECT * FROM eventstore.meter_events WHERE aggregate_id = {aggregateId}::uuid
          """.stripMargin)
        .on(
          'aggregateId -> aggregateId
        ).executeQuery().as(eventParser.*)
    } catch {
      case ex: Throwable =>
        println(ex.getMessage)
        throw ex
    }
    finally {
      readCon.close()
    }
  }

  val events = getEvents(meterId)

  val meterCreatedEventRead = {
    val eventType = events.head.eventType
    val meterCreatedType = MeterCreated.getClass.getCanonicalName
    eventType match {
      case e if e == meterCreatedType =>
        decode(MeterCreated, events.head.event)
      case _ => throw new Exception("Not a MeterCreated event")
    }
  }

  def encode[T <: ThriftStruct](structCodec: ThriftStructCodec[T], struct: T): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    structCodec.encode(struct, new TCompactProtocol(new TIOStreamTransport(out)))
    out.toByteArray
  }

  def decode[T <: ThriftStruct](structCodec: ThriftStructCodec[T], data: Array[Byte]): T = {
    val stream = new ByteArrayInputStream(data)
    structCodec.decode(new TCompactProtocol(new TIOStreamTransport(stream)))
  }*/

  val userId = Generators.timeBasedGenerator().generate().toString

  val meterRepo = new MeterRepository(new MeterCommandProcessor, new MeterEventCodec)
  val createdMeter = meterRepo.create(userId, User.getClass.getCanonicalName,
    new CreateMeterCommand(
      "Some Meter",
      "ZZ-123",
      MeterType.Water,
      MeterStatus.Inactive,
      Option(MeterModel("asdf", "ZXCV-1234")))) match {
    case Success(newMeter) =>
      val meter = meterRepo.getById(newMeter.id)
      println(meter)
    case Failure(ex) => println(ex.getMessage)
  }
/*  val m = MeterCreated("Some Meter",
    "ZZ-123",
    MeterType.Water,
    MeterStatus.Inactive,
    Option(MeterModel("asdf", "ZXCV-1234")))

  def getTypeTag[T: ru.TypeTag](obj: T) = ru.typeTag[T]

  println(m.getClass.getCanonicalName)
  println(m.getClass.getName)
  println(m.getClass.getSimpleName)
  println(m.getClass.getTypeName)

  println(getTypeTag(m).tpe)*/



}


/*
object MeterAggregateModule {






  implicit object MeterAggregate extends CommandProcessingAggregate[model.Meter] {

    type AggregateCommand = MeterCommand

    def newInstance() = model.Meter("", "", "", null, null, None)

    type AggregateEvent = MeterEvent

    def processCommand(aggregate: model.Meter, command: MeterCommand) = Try(command match {
      case CreateMeterCommand(name, number, meterType, meterStatus, meterModel) =>
        Seq(MeterCreated(name, number, meterType, meterStatus, meterModel))
    })

    def applyEvent(aggregate: model.Meter, event: MeterEvent) = event match {
      case MeterCreatedEvent(newName, _) => aggregate.copy(name=newName)
    }
  }
}*/


