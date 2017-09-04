package model

import events.MeterCreated
import infrastructure.{Event, TemporalAggregate}
import io.centular.common.lib.ID

/**
  * Created by rudolf on 2017/08/05.
  */
case class Meter(id: ID,
                 name: String,
                 number: String) extends TemporalAggregate[ID, Meter] {
  def apply(event: Event[ID]): Meter = event match {
    case e: MeterCreated => Meter(id, e.name, e.number)
  }
}