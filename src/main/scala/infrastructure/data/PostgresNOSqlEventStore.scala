package infrastructure.data

/**
  * Created by rudolf on 2017/09/05.
  */
/*class PostgresNOSqlEventStore[ID <: Identifier[_]](schema: String, eventTable: String, snapshotTable: String) extends SqlEventStore[ID] {

  override protected def queryExists(aggregateId: ID)(implicit context: Context): Boolean = {
    implicit val con = dataSource.getConnection
    try {
      SQL(
        s"""
           |SELECT (event ->> 'aggregateId')::UUID AS id FROM $schema.$eventTable
           |WHERE (event ->> 'aggregateId')::UUID = {aggregateId}::UUID
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

  override protected def persistEventStatement(event: JsValue)(implicit context: Context): SimpleSql[Row] = {
    SQL(
      s"""
        |INSERT INTO $schema.$eventTable (event, instance)
        |VALUES ({event}::jsonb, {instance}::uuid)
      """.stripMargin)
      .on(
        'event -> event.toString,
        'instance -> context.instanceId)
  }

  override protected def queryEventsAsAt(aggregateId: ID, asAtDateTime: String)(implicit context: Context): Seq[JsValue] = {
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
          |SELECT event
          |FROM $schema.$eventTable
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

  override protected def queryEventsAsOf(aggregateId: ID, asOfDateTime: String)(implicit context: Context): Seq[JsValue] = {
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
          |SELECT event
          |FROM $schema.$eventTable
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
          |SELECT
          |    (event ->> 'eventName') AS event_name,
          |    (event ->> 'eventRaised') AS event_raised,
          |    (event ->> 'eventApplies') AS event_applies,
          |    (event ->> 'eventDescription') AS event_description,
          |    (event ->> 'eventSenderId') AS event_sender_id
          |FROM $schema.$eventTable
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


}*/
