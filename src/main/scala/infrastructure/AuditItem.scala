package infrastructure

import java.util.UUID

import anorm.SqlParser._
import anorm.{RowParser, ~}
import io.centular.common.lib.ID
import org.joda.time.DateTime

/**
  * Created by rudolf on 2017/09/05.
  */
case class AuditItem(eventName: String, eventRaised: String, eventApplies: String, eventDescription: Option[String], eventSenderId: ID)

object AuditItem {
  val parser: RowParser[AuditItem] = {
    get[String]("event_name") ~
    get[DateTime]("event_raised") ~
    get[DateTime]("event_applies") ~
    get[Option[String]]("event_description") ~
    get[UUID]("event_sender_id") map {
      case event_name ~ event_raised ~ event_applies ~ event_description ~ event_sender_id =>
        AuditItem(event_name, event_raised.toString, event_applies.toString, event_description, ID(event_sender_id))
    }
  }
}
