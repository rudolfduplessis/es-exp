  import java.util.UUID

  import infrastructure.Event
  import org.joda.time.DateTime
  import spray.json.DefaultJsonProtocol._
  import spray.json._



  case class PersonCreated(name: String, surname: String)
  case class NameChanged(name: String)
  case class SurnameChanged(surname: String)

  val createdEvent =
    new Event(
      UUID.randomUUID(),
      "Created",
      DateTime.now().toString,
      "",
      UUID.randomUUID(),
      UUID.randomUUID(), PersonCreated("Rudolf", "Du Plessis"))

  val nameChangedEvent =
    new Event(
      UUID.randomUUID(),
      "Name Changed",
      DateTime.now().toString,
      "",
      UUID.randomUUID(),
      UUID.randomUUID(), NameChanged("Dolf"))

  val surnameChangedEvent =
    new Event(
      UUID.randomUUID(),
      "Surname Changed",
      DateTime.now().toString,
      "",
      UUID.randomUUID(),
      UUID.randomUUID(), SurnameChanged("du Plessis"))

  val json = createdEvent.toJson(EventJsonProtocol.eventJsonFormat(jsonFormat2(PersonCreated.apply))).prettyPrint
  val obj = json.parseJson.convertTo[Event[PersonCreated]](EventJsonProtocol.eventJsonFormat(jsonFormat2(PersonCreated.apply)))
  val payload = createdEvent.payload.toJson(jsonFormat2(PersonCreated.apply))


  case class Person(name: String, surname: String)

  trait EventProcessor[T] {
    def process(aggregate: T, event: Event[_]): T
  }

  object PersonEventProcessor extends EventProcessor[Person] {
    override def process(aggregate: Person, event: Event[_]): Person = event.payload match {
      case e: PersonCreated => Person(e.name, e.surname)
      case e: NameChanged => aggregate.copy(name = e.name)
      case e: SurnameChanged => aggregate.copy(surname = e.surname)
    }
  }

  object EventSource {
    def getById(id: UUID): Person = {
      val person = Person("", "")
      Seq(createdEvent, nameChangedEvent, surnameChangedEvent)
        .foldLeft(person)((a: Person, e:Event[_]) => PersonEventProcessor.process(a, e))

    }
  }

  EventSource.getById(UUID.randomUUID())


























