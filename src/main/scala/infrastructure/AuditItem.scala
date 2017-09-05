package infrastructure

import anorm.SqlParser._
import anorm.{RowParser, ~}

/**
  * Created by rudolf on 2017/09/05.
  */
case class AuditItem(eventName: String, eventRaised: String, eventApplies: String, eventDescription: Option[String], eventSenderId: String)

object AuditItem {
  val parser: RowParser[AuditItem] = {
    get[String]("event_name") ~
    get[String]("event_raised") ~
    get[String]("event_applies") ~
    get[Option[String]]("event_description") ~
    get[String]("event_sender_id") map {
      case event_name ~ event_raised ~ event_applies ~ event_description ~ event_sender_id =>
        AuditItem(event_name, event_raised, event_applies, event_description, event_sender_id)
    }
  }
}
