package model

import io.centular.meter.model.{MeterModel, MeterStatus, MeterType}

/**
  * Created by rudolf on 2017/08/05.
  */
case class Meter(id: String,
                 name: String,
                 number: String,
                 meterType: MeterType,
                 meterStatus: MeterStatus,
                 meterModel: Option[MeterModel] = None)