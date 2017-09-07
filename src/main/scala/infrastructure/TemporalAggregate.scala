package infrastructure

import io.centular.common.lib.Identifier
import io.centular.common.model.Context

/**
  * Created by rudolf on 2017/08/27.
  */
trait TemporalAggregate[ID <: Identifier[_], TAggregate] {
  val id: ID
  val version: Int
  def executeCommand(command: AggregateCommand)(implicit context: Context): Seq[Envelope]
  def applyEvent(envelope: Envelope): TAggregate
  def takeSnapshot: Boolean
}


