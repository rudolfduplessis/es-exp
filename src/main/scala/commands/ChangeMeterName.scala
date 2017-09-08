package commands

import temporal.AggregateCommand

/**
  * Created by rudolf on 2017/09/07.
  */
case class ChangeMeterName(name: String) extends AggregateCommand
