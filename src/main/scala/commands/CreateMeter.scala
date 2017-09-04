package commands

import infrastructure.AggregateCommand
import io.centular.common.lib.{EmptyID, ID}

/**
  * Created by rudolf on 2017/08/05.
  */

case class CreateMeter(name: String, number: String) extends AggregateCommand[ID] {
  override val aggregateId: ID = EmptyID
}
