package commands

import temporal.Command

/**
  * Created by rudolf on 2017/08/05.
  */

case class CreateMeter(name: String, number: String) extends Command
