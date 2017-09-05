package infrastructure

import anorm.{Column, MetaDataItem, Row, SimpleSql, TypeDoesNotMatch}
import io.centular.common.lib.Identifier
import io.centular.common.model.Context
import spray.json.{JsValue, _}

/**
  * Created by rudolf on 2017/09/05.
  */
trait SqlEventStore[ID <: Identifier[_]] {

  final protected implicit def columnToJsValue: Column[JsValue] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case pgo: org.postgresql.util.PGobject =>
        Right(pgo.getValue.parseJson)
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" +
        value.asInstanceOf[AnyRef].getClass + " to JsValue for column " + qualified))
    }
  }

  protected def insertEventStatement(event: JsValue)(implicit context: Context): SimpleSql[Row]

  protected def insertSnapshotStatement(snapshot: JsValue)(implicit context: Context): SimpleSql[Row]

  protected def selectEventsQuery(aggregateId: ID, untilConditions: Map[String, AnyVal])(implicit context: Context): Seq[JsValue]

  protected def selectSnapshotQuery(aggregateId: ID)(implicit context: Context): Option[JsValue]
}
