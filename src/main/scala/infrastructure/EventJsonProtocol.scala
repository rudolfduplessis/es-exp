package infrastructure

/**
  * Created by rudolf on 2017/09/02.
  */
/*object EventJsonProtocol extends DefaultJsonProtocol {
  implicit def eventJsonFormat[TIDFormat :JsonFormat, TPayloadFormat :RootJsonFormat] = new RootJsonFormat[Event[TIDFormat, TPayloadFormat]] {
    override def read(json: JsValue): Event[TIDFormat, TPayloadFormat] = {
      val jsObject = json.asJsObject
      jsObject.getFields("id", "name", "raised", "description", "senderId", "aggregatedId", "payload") match {
        case Seq(id, name, raised, description, senderId, aggregateId, payload) =>
          new Event(
            id.convertTo[TIDFormat],
            name.convertTo[String],
            raised.convertTo[String],
            description.convertTo[String],
            senderId.convertTo[TIDFormat],
            aggregateId.convertTo[TIDFormat],
            payload.convertTo[TPayloadFormat])
      }
    }

    override def write(obj: Event[TIDFormat, TPayloadFormat]): JsValue = JsObject(
      "id" -> obj.id.toJson,
      "name" -> obj.name.toJson,
      "raised" -> obj.raised.toJson,
      "description" -> obj.description.toJson,
      "senderId" -> obj.senderId.toJson,
      "aggregatedId" -> obj.aggregateId.toJson,
      "payload" -> obj.payload.toJson
    )
  }
}*/
