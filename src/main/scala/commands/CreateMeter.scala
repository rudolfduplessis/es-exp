package commands

import temporal.AggregateCommand

/**
  * Created by rudolf on 2017/08/05.
  */

case class CreateMeter(name: String, number: String) extends AggregateCommand
