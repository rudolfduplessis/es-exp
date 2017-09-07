package infrastructure.repo

import infrastructure._
import infrastructure.data.{Event, SqlEventStore}
import io.centular.common.CentularPostgres.dataSource
import io.centular.common.lib.Identifier
import io.centular.common.model.Context
import spray.json._

/**
  * Created by rudolf on 2017/08/26.
  */
trait TemporalAggregateRepository[ID <: Identifier[_], TAggregate <: TemporalAggregate[ID, TAggregate]]
  extends SqlEventStore[ID] { this: AggregateJsonProtocol[ID, TAggregate] =>

  val emptyAggregate: TAggregate

  final def exists(id: ID)(implicit context: Context): Boolean = queryExists(id)

  def save(aggregate: TAggregate, envelopes: Seq[Envelope])(implicit context: Context): TAggregate = {
    val upToDateAggregate = foldEvents(envelopes.map(toEvent), Some(aggregate)).get
    val snapshot = if (upToDateAggregate.takeSnapshot) Some(upToDateAggregate) else None
    persist(envelopes, snapshot)
    upToDateAggregate
  }

  private def persist(envelopes: Seq[Envelope], aggregateSnapshot: Option[TAggregate])(implicit context: Context): Unit = {
    implicit val con = dataSource.getConnection
    con.setAutoCommit(false)
    try {
      envelopes.foreach(envelope => persistEventStatement(toEvent(envelope)).execute())
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

  protected final def foldEvents(events: Seq[Event], overAggregate: Option[TAggregate]): Option[TAggregate] =
    events.foldLeft(overAggregate)((a: Option[TAggregate], e: Event) => {
      Some(a.getOrElse(emptyAggregate).applyEvent(fromEvent(e)))
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

  private def toEvent(e: Envelope): Event =
    Event(
      e.eventId,
      e.eventName,
      e.eventRaised,
      e.eventApplies,
      e.eventDescription,
      e.eventSenderId,
      e.aggregateId,
      e.event.toJson)

  private def fromEvent(e: Event): Envelope =
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








