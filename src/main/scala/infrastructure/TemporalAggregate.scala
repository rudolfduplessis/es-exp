package infrastructure

import io.centular.common.lib.Identifier

/**
  * Created by rudolf on 2017/08/27.
  */
trait TemporalAggregate[ID <: Identifier[_], TAggregate] {
  def apply(event: Event[ID]): TAggregate
}
