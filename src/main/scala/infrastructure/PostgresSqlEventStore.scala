package infrastructure

import anorm.{SqlParser, _}
import io.centular.common.CentularPostgres._
import io.centular.common.lib.Identifier
import io.centular.common.model.Context
import spray.json.JsValue

/**
  * Created by rudolf on 2017/09/05.
  */
class PostgresSqlEventStore[ID <: Identifier[_]] extends SqlEventStore[ID] {

  override protected def insertSnapshotStatement(snapshot: JsValue)(implicit context: Context): SimpleSql[Row] = ???

  override protected def insertEventStatement(event: JsValue)(implicit context: Context): SimpleSql[Row] = {
    SQL(
      """
        |INSERT INTO event_schema.event (event, instance)
        |VALUES ({event}::jsonb, {instance}::uuid)
      """.stripMargin)
      .on(
        'event -> event.toString,
        'instance -> context.instanceId)
  }

  override protected def selectEventsAsAtQuery(aggregateId: ID, asAtDateTime: String)(implicit context: Context): Seq[JsValue] = {
    implicit val con = dataSource.getConnection
    try {
      val results = SQL(
        """
          |WITH aggregate_snapshot AS (
          |  SELECT taken FROM event_schema.snapshot
          |  WHERE (aggregate ->> 'id')::UUID = {aggregateId}::UUID
          |    AND instance = {instance}::UUID
          |    AND taken <= {asAtDateTime}::TIMESTAMP
          |  LIMIT 1
          |)
          |SELECT event
          |FROM event_schema.event
          |WHERE (event ->> 'aggregateId')::UUID = {aggregateId}::UUID
          |  AND (event ->> 'eventRaised')::TIMESTAMP > COALESCE((SELECT taken FROM aggregate_snapshot), '1970-01-01 00:00:00.000000'::TIMESTAMP)
          |  AND (event ->> 'eventRaised')::TIMESTAMP <= {asAtDateTime}::TIMESTAMP
          |  AND instance = {instance}::UUID
          |ORDER BY (event ->> 'eventApplies');
        """.stripMargin)
        .on(
          'aggregateId -> aggregateId.value.toString,
          'asAtDateTime -> asAtDateTime,
          'instance -> context.instanceId)
        .executeQuery().as(SqlParser.scalar[JsValue].*)
      results
    } finally {
      con.close()
    }
  }

  override protected def selectEventsAsOfQuery(aggregateId: ID, asOfDateTime: String)(implicit context: Context): Seq[JsValue] = {
    implicit val con = dataSource.getConnection
    try {
      val results = SQL(
        """
          |WITH aggregate_snapshot AS (
          |  SELECT taken FROM event_schema.snapshot
          |  WHERE (aggregate ->> 'id')::UUID = {aggregateId}::UUID
          |    AND instance = {instance}::UUID
          |    AND taken <= {asOfDateTime}::TIMESTAMP
          |  LIMIT 1
          |)
          |SELECT event
          |FROM event_schema.event
          |WHERE (event ->> 'aggregateId')::UUID = {aggregateId}::UUID
          |  AND (event ->> 'eventRaised')::TIMESTAMP > COALESCE((SELECT taken FROM aggregate_snapshot), '1970-01-01 00:00:00.000000'::TIMESTAMP)
          |  AND (event ->> 'eventApplies')::TIMESTAMP <= {asOfDateTime}::TIMESTAMP
          |  AND instance = {instance}::UUID
          |ORDER BY (event ->> 'eventApplies');
        """.stripMargin)
        .on(
          'aggregateId -> aggregateId.value.toString,
          'asOfDateTime -> asOfDateTime,
          'instance -> context.instanceId)
        .executeQuery().as(SqlParser.scalar[JsValue].*)
      results
    } finally {
      con.close()
    }
  }

  override protected def selectSnapshotQuery(aggregateId: ID)(implicit context: Context): Option[JsValue] = {
    implicit val con = dataSource.getConnection
    try {
      SQL(
        """
          |SELECT aggregate FROM event_schema.snapshot
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

  override protected def auditQuery(aggregateId: ID)(implicit context: Context): Seq[AuditItem] = {
    implicit val con = dataSource.getConnection
    try {
      SQL(
        """
          |SELECT
          |    (event ->> 'eventName') AS event_name,
          |    (event ->> 'eventRaised') AS event_raised,
          |    (event ->> 'eventApplies') AS event_applies,
          |    (event ->> 'eventDescription') AS event_description,
          |    (event ->> 'eventSenderId') AS event_sender_id
          |FROM event_schema.event
          |WHERE (event ->> 'aggregateId')::UUID = {aggregateId}::UUID
          |  AND instance = {instance}::UUID
          |ORDER BY (event ->> 'eventRaised') DESC;
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
