package infrastructure

/**
  * Created by rudolf on 2017/08/27.
  */
trait AggregateCommand[ID] {
  val aggregateId: ID
}
