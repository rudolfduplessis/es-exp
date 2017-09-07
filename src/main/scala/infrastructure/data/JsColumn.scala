package infrastructure.data

import anorm.{Column, MetaDataItem, TypeDoesNotMatch}
import spray.json.{JsValue, _}

/**
  * Created by rudolf on 2017/09/07.
  */
object JsColumn {
  final implicit def columnToJsValue: Column[JsValue] = Column.nonNull { (value, meta) =>
    val MetaDataItem(qualified, nullable, clazz) = meta
    value match {
      case pgo: org.postgresql.util.PGobject =>
        Right(pgo.getValue.parseJson)
      case _ => Left(TypeDoesNotMatch("Cannot convert " + value + ":" +
        value.asInstanceOf[AnyRef].getClass + " to JsValue for column " + qualified))
    }
  }
}
