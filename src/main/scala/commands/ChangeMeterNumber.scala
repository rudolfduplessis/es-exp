package commands

import infrastructure.AggregateCommand

/**
  * Created by rudolf on 2017/09/07.
  */
case class ChangeMeterNumber(number: String) extends AggregateCommand
