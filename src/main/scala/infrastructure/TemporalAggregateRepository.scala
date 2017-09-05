package infrastructure


import io.centular.common.CentularPostgres.dataSource
import io.centular.common.lib.Identifier
import io.centular.common.model.Context
import org.joda.time.DateTime
import spray.json._

/**
  * Created by rudolf on 2017/08/26.
  */
trait TemporalAggregateRepository[ID <: Identifier[_], TAggregate <: TemporalAggregate[ID, TAggregate]]
  extends SqlEventStore[ID] { this: TemporalAggregateFactory[ID, TAggregate] =>

  val commandProcessor: CommandProcessor[ID, TAggregate]

  final def exists(id: ID): Boolean = false

  def execute(command: AggregateCommand[ID])(implicit context: Context): Unit = {
    val aggregate = if (command.aggregateId.isDefined) getAggregateAsAt(command.aggregateId, DateTime.now.toString) else None
    val events = commandProcessor.process(command, aggregate)
    persist(events, None)
  }

  def executeWithSnapshot(command: AggregateCommand[ID])(implicit context: Context): Unit = {
    val aggregate = if (command.aggregateId.isDefined) getAggregateAsAt(command.aggregateId, DateTime.now.toString) else None
    val events = commandProcessor.process(command, aggregate)
    persist(events, foldEvents(events, aggregate))
  }

  private def persist(events: Seq[Event[ID]], aggregateSnapshot: Option[TAggregate])(implicit context: Context): Unit = {
    implicit val con = dataSource.getConnection
    con.setAutoCommit(false)
    try {
      events.foreach(event => insertEventStatement(event.toJson).execute())
      aggregateSnapshot.map(aggregate => insertSnapshotStatement(aggregate.toJson).execute())
      con.commit()
    } catch {
      case ex: Throwable =>
      con.rollback()
      throw ex
    } finally {
      con.close()
    }
  }

  protected final def foldEvents(events: Seq[Event[ID]], overAggregate: Option[TAggregate]): Option[TAggregate] =
    events.foldLeft(overAggregate)((a: Option[TAggregate], e: Event[ID]) => {
      Some(a.getOrElse(emptyInstance).apply(e))
    })

  // Get a version of the aggregate at a point in time, WITHOUT retroactive events applied
  final def getAggregateAsAt(aggregateId: ID, asAtTime: String)(implicit context: Context): Option[TAggregate] = {
    foldEvents(selectEventsAsAtQuery(aggregateId, asAtTime).map(_.convertTo[Event[ID]]), selectSnapshotQuery(aggregateId).map(_.convertTo[TAggregate]))
  }

  // Get a version of the aggregate at a point in time, WITH retroactive events applied
  final def getAggregateAsOf(aggregateId: ID, asOfTime: String)(implicit context: Context): Option[TAggregate] = {
    foldEvents(selectEventsAsOfQuery(aggregateId, asOfTime).map(_.convertTo[Event[ID]]), selectSnapshotQuery(aggregateId).map(_.convertTo[TAggregate]))
  }

  final def audit(aggregateId: ID)(implicit context: Context): Seq[AuditItem] = {
    auditQuery(aggregateId)
  }
}








