package commands

import io.centular.meter.model.{MeterModel, MeterStatus, MeterType}

/**
  * Created by rudolf on 2017/08/05.
  */
trait MeterCommand

case class CreateMeterCommand(name: String,
                              number: String,
                              meterType: MeterType,
                              meterStatus: MeterStatus,
                              meterModel: Option[MeterModel] = None) extends MeterCommand
