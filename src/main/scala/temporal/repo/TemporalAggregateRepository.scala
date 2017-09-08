package temporal.repo

import io.centular.common.CentularPostgres.dataSource
import io.centular.common.lib.Identifier
import io.centular.common.model.Context
import spray.json._
import temporal.data.SqlEventStore
import temporal.{Envelope, _}

/**
  * Created by rudolf on 2017/08/26.
  */
trait TemporalAggregateRepository[ID <: Identifier[_], TAggregate <: TemporalAggregate[ID, TAggregate]]
  extends SqlEventStore[ID] { this: AggregateJsonProtocol[ID, TAggregate] =>

  val emptyAggregate: TAggregate

  final def exists(id: ID)(implicit context: Context): Boolean = queryExists(id)

  def save(aggregate: TAggregate, envelopes: Seq[Envelope])(implicit context: Context): TAggregate = {
    val upToDateAggregate = foldEvents(envelopes.map(toJson), Some(aggregate)).get
    val snapshot = if (upToDateAggregate.takeSnapshot) Some(upToDateAggregate) else None
    persist(envelopes, snapshot)
    upToDateAggregate
  }

  private def persist(envelopes: Seq[Envelope], aggregateSnapshot: Option[TAggregate])(implicit context: Context): Unit = {
    implicit val con = dataSource.getConnection
    con.setAutoCommit(false)
    try {
      envelopes.foreach(envelope => persistEventStatement(toJson(envelope)).execute())
      aggregateSnapshot.map(aggregate => persistSnapshotStatement(aggregate.toJson).execute())
      con.commit()
    } catch {
      case ex: Throwable =>
      con.rollback()
      throw ex
    } finally {
      con.close()
    }
  }

  protected final def foldEvents(events: Seq[data.Envelope], overAggregate: Option[TAggregate]): Option[TAggregate] =
    events.foldLeft(overAggregate)((a: Option[TAggregate], e: data.Envelope) => {
      Some(a.getOrElse(emptyAggregate).applyEvent(fromJson(e)))
    })

  // Get a version of the aggregate at a point in time, WITHOUT retroactive events applied
  final def getAggregateAsAt(aggregateId: ID, asAtTime: String)(implicit context: Context): Option[TAggregate] = {
    foldEvents(queryEventsAsAt(aggregateId, asAtTime), querySnapshot(aggregateId).map(_.convertTo[TAggregate]))
  }

  // Get a version of the aggregate at a point in time, WITH retroactive events applied
  final def getAggregateAsOf(aggregateId: ID, asOfTime: String)(implicit context: Context): Option[TAggregate] = {
    foldEvents(queryEventsAsOf(aggregateId, asOfTime), querySnapshot(aggregateId).map(_.convertTo[TAggregate]))
  }

  final def audit(aggregateId: ID)(implicit context: Context): Seq[AuditItem] = {
    queryAuditLog(aggregateId)
  }

  private def toJson(e: Envelope): data.Envelope =
    data.Envelope(
      e.eventId,
      e.eventName,
      e.eventRaised,
      e.eventApplies,
      e.eventDescription,
      e.eventSenderId,
      e.aggregateId,
      e.event.toJson)

  private def fromJson(e: data.Envelope): Envelope =
    Envelope(
      e.eventId,
      e.eventName,
      e.eventRaised,
      e.eventApplies,
      e.eventDescription,
      e.eventSenderId,
      e.aggregateId,
      readJsonFor(e.eventName, e.event))
}








