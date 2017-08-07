import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import io.centular.meter.model.{MeterCreated, MeterModel, MeterStatus, MeterType}

import scala.reflect.classTag
import scala.reflect.runtime.universe._
import scala.reflect.runtime.{universe => ru}

val m = MeterCreated("Some Meter",
  "ZZ-123",
  MeterType.Water,
  MeterStatus.Inactive,
  Option(MeterModel("asdf", "ZXCV-1234")))

def getTypeTag[T: ru.TypeTag](obj: T) = ru.typeTag[T]
val typeName = getTypeTag(m).tpe.toString
// to db then back
val clazz = Class.forName(typeName)
getDecoder(typeName).

def getDecoder(clazz: String):ThriftStructCodec[_] = {
  clazz match {
    case c if c == typeTag[MeterCreated].tpe.toString => MeterCreated
  }
}


/*X(clazz)

def X[T: TypeTag](ob: Class[T]) = ob match {
  case x if typeOf[T] <:< typeOf[MeterCreated]   => println("MeterCreated thing")
  case x if typeOf[T] <:< typeOf[Boolean]  => println("Boolean obs")
  case x if typeOf[T] <:< typeOf[Int]      => println("Int obs")
}*/




