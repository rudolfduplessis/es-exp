package infrastructure


import io.centular.common.CentularPostgres.dataSource
import io.centular.common.lib.Identifier
import io.centular.common.model.Context
import spray.json._

/**
  * Created by rudolf on 2017/08/26.
  */
trait TemporalAggregateRepository[ID <: Identifier[_], TAggregate <: TemporalAggregate[ID, TAggregate]]
  extends SqlEventStore[ID] { this: TemporalAggregateFactory[ID, TAggregate] =>

  val commandProcessor: CommandProcessor[ID, TAggregate]

  final def exists(id: ID): Boolean = false

  def execute(command: AggregateCommand[ID])(implicit context: Context): Unit = {
    val aggregate = if (command.aggregateId.isDefined) getById(command.aggregateId) else None
    val events = commandProcessor.process(command, aggregate)
    persist(events, None)
  }

  def executeWithSnapshot(command: AggregateCommand[ID])(implicit context: Context): Unit = {
    val aggregate = if (command.aggregateId.isDefined) getById(command.aggregateId) else None
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

  final def getById(id: ID)(implicit context: Context): Option[TAggregate] = {
    foldEvents(selectEventsQuery(id, Map.empty).map(_.convertTo[Event[ID]]), selectSnapshotQuery(id).map(_.convertTo[TAggregate]))
  }
}








