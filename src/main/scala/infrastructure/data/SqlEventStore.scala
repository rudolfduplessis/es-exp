package infrastructure.data

import anorm.{Row, SimpleSql}
import infrastructure.AuditItem
import io.centular.common.lib.Identifier
import io.centular.common.model.Context
import spray.json.JsValue

/**
  * Created by rudolf on 2017/09/05.
  */
trait SqlEventStore[ID <: Identifier[_]] {

  protected def queryExists(id: ID)(implicit context: Context): Boolean

  protected def persistEventStatement(event: Event)(implicit context: Context): SimpleSql[Row]

  protected def persistSnapshotStatement(snapshot: JsValue)(implicit context: Context): SimpleSql[Row]

  protected def queryEventsAsAt(aggregateId: ID, asAtDateTime: String)(implicit context: Context): Seq[Event]

  protected def queryEventsAsOf(aggregateId: ID, asOfDateTime: String)(implicit context: Context): Seq[Event]

  protected def querySnapshot(aggregateId: ID)(implicit context: Context): Option[JsValue]

  protected def queryAuditLog(aggregateId: ID)(implicit context: Context): Seq[AuditItem]
}
