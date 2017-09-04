import spray.json.DefaultJsonProtocol._
import spray.json.{JsValue, _}

trait Event {
  val id: String
  val name: String
}

trait EventJsonFormat[TEvent <: Event] extends JsonFormat[TEvent] {
  private val standardFields = Seq("id", "name")

  final def read(jsValue: JsValue): TEvent = {
    val jsObject = jsValue.asJsObject
    jsObject.getFields("id", "name") match {
      case Seq(id, name) =>
        read(
          id.convertTo[String],
          JsObject(jsObject.fields.filterKeys(key => !standardFields.contains(key))))
    }
  }

  protected class JsEvent(override val fields: Map[String, JsValue]) extends JsObject(fields)

  protected object JsEvent {
    def apply(members: JsField*)(implicit event: TEvent): JsEvent = new JsEvent(
      Map(
        "id" -> event.id.toJson,
        "name" -> event.name.toJson
      ) ++ members
    )
  }

  final def write(obj: TEvent): JsValue = {
    write(obj)
  }

  protected def write()(implicit obj: TEvent): JsEvent
  protected def read(id: String, event: JsObject): TEvent
}

case class MyEvent private(id: String, something: String) extends Event {
  override val name: String = "MyEvent"
}

object MyEvent {
  implicit object MyEventFormat extends EventJsonFormat[MyEvent] {
    override protected def read(id: String, event: JsObject): MyEvent =
      event.getFields("something") match {
        case Seq(something, asdf) => MyEvent(id, something.convertTo[String])
        case other => deserializationError("Cannot deserialize MyEvent: invalid input. Raw input: " + other)
      }

    override protected def write()(implicit obj: MyEvent): JsEvent = JsEvent(
      "something" -> obj.something.toJson
    )
  }
}

val myFirstEvent = MyEvent("event1", "a thing")
myFirstEvent.toJson




















