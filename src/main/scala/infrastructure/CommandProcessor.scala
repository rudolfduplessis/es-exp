package infrastructure

import io.centular.common.lib.Identifier

/**
  * Created by rudolf on 2017/09/05.
  */
trait CommandProcessor[ID <: Identifier[_], TAggregate <: TemporalAggregate[ID, TAggregate]] {
  def process(command: AggregateCommand[ID], aggregate: Option[TAggregate]): Seq[Event[ID]]
}
