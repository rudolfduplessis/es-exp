package temporal.data

import java.util.UUID

import anorm.{Row, SimpleSql, SqlParser, _}
import io.centular.common.CentularPostgres._
import io.centular.common.lib.Identifier
import io.centular.common.model.Context
import org.joda.time.DateTime
import spray.json.JsValue
import temporal.AuditItem
import temporal.data.JsColumn._

/**
  * Created by rudolf on 2017/09/07.
  */
class PostgreSqlEventStore[ID <: Identifier[_]](schema: String, eventTable: String, snapshotTable: String) extends SqlEventStore[ID] {
  override protected def queryExists(aggregateId: ID)(implicit context: Context): Boolean = {
    implicit val con = dataSource.getConnection
    try {
      SQL(
        s"""
           |SELECT aggregate_id FROM $schema.$eventTable
           |WHERE aggregate_id = {aggregateId}::UUID
           |  AND instance = {instance}::UUID
           |LIMIT 1
        """.stripMargin)
        .on(
          'aggregateId -> aggregateId.toString,
          'instance -> context.instanceId)
        .executeQuery().as(SqlParser.scalar[UUID].singleOpt)
        .isDefined
    } finally {
      con.close()
    }
  }

  override protected def persistEventStatement(event: Envelope)(implicit context: Context): SimpleSql[Row] = {
    SQL(
      s"""
         |INSERT INTO $schema.$eventTable (event_id, event_name, event_raised, event_applies, event_description, event_sender_id, aggregate_id, event, instance)
         |VALUES ({eventId}::UUID, {eventName}, {eventRaised}::TIMESTAMP, {eventApplies}::TIMESTAMP, {eventDescription}, {eventSenderId}::UUID, {aggregateId}::UUID, {event}::JSONB, {instance}::UUID)
      """.stripMargin)
      .on(
        'eventId -> event.eventId.value,
        'eventName -> event.eventName,
        'eventRaised -> event.eventRaised,
        'eventApplies -> event.eventApplies,
        'eventDescription -> event.eventDescription,
        'eventSenderId -> event.eventSenderId.value,
        'aggregateId -> event.aggregateId.value,
        'event -> event.event.toString,
        'instance -> context.instanceId)
  }

  override protected def persistSnapshotStatement(aggregate: JsValue)(implicit context: Context): SimpleSql[Row] = {
    SQL(
      s"""
         |INSERT INTO $schema.$snapshotTable (aggregate, taken, instance)
         |VALUES ({aggregate}::jsonb, {timeStamp}::TIMESTAMP, {instance}::uuid)
      """.stripMargin)
      .on(
        'aggregate -> aggregate.toString,
        'timeStamp -> DateTime.now().toString(),
        'instance -> context.instanceId)
  }

  override protected def queryEventsAsAt(aggregateId: ID, asAtDateTime: String)(implicit context: Context): Seq[Envelope] = {
    implicit val con = dataSource.getConnection
    try {
      val results = SQL(
        s"""
           |WITH aggregate_snapshot AS (
           |  SELECT taken FROM $schema.$snapshotTable
           |  WHERE (aggregate ->> 'id')::UUID = {aggregateId}::UUID
           |    AND instance = {instance}::UUID
           |    AND taken <= {asAtDateTime}::TIMESTAMP
           |  LIMIT 1
           |)
           |SELECT event_id, event_name, event_raised, event_applies, event_description, event_sender_id, aggregate_id, event
           |FROM $schema.$eventTable
           |WHERE aggregate_id = {aggregateId}::UUID
           |  AND event_raised > COALESCE((SELECT taken FROM aggregate_snapshot), '1970-01-01 00:00:00.000000'::TIMESTAMP)
           |  AND event_raised <= {asAtDateTime}::TIMESTAMP
           |  AND instance = {instance}::UUID
           |ORDER BY event_applies
        """.stripMargin)
        .on(
          'aggregateId -> aggregateId.value.toString,
          'asAtDateTime -> asAtDateTime,
          'instance -> context.instanceId)
        .executeQuery().as(Envelope.parser.*)
      results
    } finally {
      con.close()
    }
  }

  override protected def queryEventsAsOf(aggregateId: ID, asOfDateTime: String)(implicit context: Context): Seq[Envelope] = {
    implicit val con = dataSource.getConnection
    try {
      val results = SQL(
        s"""
           |WITH aggregate_snapshot AS (
           |  SELECT taken FROM $schema.$snapshotTable
           |  WHERE (aggregate ->> 'id')::UUID = {aggregateId}::UUID
           |    AND instance = {instance}::UUID
           |    AND taken <= {asOfDateTime}::TIMESTAMP
           |  LIMIT 1
           |)
           |SELECT event_id, event_name, event_raised, event_applies, event_description, event_sender_id, aggregate_id, event
           |FROM $schema.$eventTable
           |WHERE aggregate_id = {aggregateId}::UUID
           |  AND event_raised > COALESCE((SELECT taken FROM aggregate_snapshot), '1970-01-01 00:00:00.000000'::TIMESTAMP)
           |  AND event_applies <= {asOfDateTime}::TIMESTAMP
           |  AND instance = {instance}::UUID
           |ORDER BY event_applies
        """.stripMargin)
        .on(
          'aggregateId -> aggregateId.value.toString,
          'asOfDateTime -> asOfDateTime,
          'instance -> context.instanceId)
        .executeQuery().as(Envelope.parser.*)
      results
    } finally {
      con.close()
    }
  }

  override protected def querySnapshot(aggregateId: ID)(implicit context: Context): Option[JsValue] = {
    implicit val con = dataSource.getConnection
    try {
      SQL(
        s"""
           |SELECT aggregate FROM $schema.$snapshotTable
           |WHERE (aggregate ->> 'id')::UUID = {aggregateId}::UUID
           |  AND instance = {instance}::UUID
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

  override protected def queryAuditLog(aggregateId: ID)(implicit context: Context): Seq[AuditItem] = {
    implicit val con = dataSource.getConnection
    try {
      SQL(
        s"""
           |SELECT event_name, event_raised, event_applies, event_description, event_sender_id
           |FROM $schema.$eventTable
           |WHERE aggregate_id = {aggregateId}::UUID
           |  AND instance = {instance}::UUID
           |ORDER BY event_raised DESC
        """.stripMargin)
        .on(
          'aggregateId -> aggregateId.value.toString,
          'instance -> context.instanceId)
        .executeQuery().as(AuditItem.parser.*)
    } finally {
      con.close()
    }
  }
}
