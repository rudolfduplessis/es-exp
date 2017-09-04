List.empty.foldLeft(Option.empty[String])((a: Option[String], e: String) =>
 Some("asdf" + e)
)