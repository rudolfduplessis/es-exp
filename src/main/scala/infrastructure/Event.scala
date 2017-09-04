package infrastructure

import io.centular.common.lib.Identifier

/**
  * Created by rudolf on 2017/08/26.
  */
trait Event[ID <: Identifier[_]] {
  val eventId: ID
  val eventName: String
  val eventRaised: String
  val eventDescription: Option[String]
  val eventSenderId: ID
  val aggregateId: ID
}

