package infrastructure


import anorm.{SqlParser, _}
import io.centular.common.CentularPostgres.dataSource
import io.centular.common.lib.Identifier
import io.centular.common.model.Context
import spray.json.{JsValue, _}

/**
  * Created by rudolf on 2017/08/26.
  */
trait TemporalAggregateRepository[ID <: Identifier[_], TAggregate <: TemporalAggregate[ID, TAggregate]] {

  val commandProcessor: (AggregateCommand[ID], Option[TAggregate]) => Seq[Event[ID]]
  val eventProcessor: (TAggregate, Event[ID]) => TAggregate
  val emptyInstance: () => TAggregate
  val eventJsonWriter: Event[ID] => JsValue
  val eventJsonReader: JsValue => Event[ID]
  implicit val aggregateJsonFormat: JsonFormat[TAggregate]

  final def exists(id: ID): Boolean = false

  def perform(command: AggregateCommand[ID])(implicit context: Context): Unit = {
    persist(commandProcessor(command, getById(command.aggregateId)))
  }

  protected def persist(events: Seq[Event[ID]])(implicit context: Context): Unit = {
    implicit val con = dataSource.getConnection
    con.setAutoCommit(false)
    try {
      events foreach { event =>
        SQL(
          """
          |INSERT INTO event_schema.event (event, instance)
          |VALUES ({event}::jsonb, {instance}::uuid)
        """.stripMargin)
        .on(
          'event -> eventJsonWriter(event).toString,
          'instance -> context.instanceId)
        .execute()
      }
      con.commit()
    } catch {
      case ex: Throwable =>
        con.rollback()
        throw ex
    } finally {
      con.close()
    }
  }

  def getById(id: ID)(implicit context: Context): Option[TAggregate] = {
    selectEvents(id, Map.empty).foldLeft(selectSnapshot(id).map(_.convertTo[TAggregate]))((a: Option[TAggregate], e: JsValue) => {
      Some(eventProcessor(a.getOrElse(emptyInstance()), eventJsonReader(e)))
    })
  }

  def getUntil(id: ID, untilConditions: Map[String, AnyVal])(implicit context: Context) = {
    selectEvents(id, untilConditions).foldLeft(selectSnapshot(id).map(_.convertTo[TAggregate]))((a: Option[TAggregate], e: JsValue) => {
      Some(eventProcessor(a.getOrElse(emptyInstance()), eventJsonReader(e)))
    })
  }

  implicit def columnToJsValue: Column[JsValue] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case pgo: org.postgresql.util.PGobject =>
        Right(pgo.getValue.parseJson)
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" +
        value.asInstanceOf[AnyRef].getClass + " to JsValue for column " + qualified))
    }
  }

  protected def selectEvents(aggregateId: ID, untilConditions: Map[String, AnyVal])(implicit context: Context): Seq[JsValue] = {
    implicit val con = dataSource.getConnection
    try {
      val results = SQL(
        """
          |WITH aggregate_snapshot AS (
          |  SELECT taken FROM event_schema.snapshot
          |  WHERE (aggregate ->> 'id')::UUID = {aggregateId}::UUID
          |  LIMIT 1
          |)
          |SELECT event
          |FROM event_schema.event
          |WHERE (event ->> 'aggregateId')::UUID = {aggregateId}::UUID
          |  AND (event ->> 'eventRaised')::TIMESTAMP >= COALESCE((SELECT taken FROM aggregate_snapshot), '1970-01-01 00:00:00.000000'::TIMESTAMP)
          |ORDER BY (event ->> 'raised');
        """.stripMargin)
        .on(
          'aggregateId -> aggregateId.value.toString,
          'instance -> context.instanceId)
        .executeQuery().as(SqlParser.scalar[JsValue].*)
      results
    } finally {
      con.close()
    }
  }

  private def selectSnapshot(aggregateId: ID)(implicit context: Context): Option[JsValue] = {
    implicit val con = dataSource.getConnection
    try {
      SQL(
        """
          |SELECT aggregate FROM event_schema.snapshot
          |WHERE (aggregate ->> 'id')::UUID = {aggregateId}::UUID
          |LIMIT 1
        """.stripMargin)
        .on(
          'aggregateId -> aggregateId.value.toString,
          'instance -> context.instanceId)
        .executeQuery().as(SqlParser.scalar[JsValue].singleOpt)
    } finally {
      con.close()
    }
  }
}








