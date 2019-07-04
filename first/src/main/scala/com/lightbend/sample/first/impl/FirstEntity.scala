package com.lightbend.sample.first.impl

import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{ AggregateEvent, AggregateEventTag, PersistentEntity }
import com.lightbend.lagom.scaladsl.playjson.{ JsonSerializer, JsonSerializerRegistry }
import org.slf4j.LoggerFactory
import play.api.libs.json.{ Format, Json }
import scala.collection.immutable

class FirstEntity extends PersistentEntity {

  override type Command = FirstCommand[_]
  override type Event = FirstEvent
  override type State = FirstState

  val log = LoggerFactory.getLogger(getClass)

  override def initialState: FirstState = FirstState(0)

  override def behavior: Behavior = {
    case _ => Actions().onReadOnlyCommand[GetFirst, String] {
      case (GetFirst(id), ctx, state) =>
        log.info(s"$id - get")
        ctx.reply(s"first: $id = ${state.count}")
    }.onCommand[UpdateFirst, String] {
      case (UpdateFirst(id), ctx, state) =>
        log.info(s"$id - update")
        ctx.thenPersist(FirstUpdated(id)) { _ =>
          log.info(s"$id - updated")
          ctx.reply(s"first: $id = ${state.count} + 1")
        }
    }.onEvent {
      case (FirstUpdated(id), state) =>
        FirstState(state.count + 1)
    }
  }
}

case class FirstState(count: Int)

object FirstState {
  implicit val format: Format[FirstState] = Json.format
}

sealed trait FirstCommand[R] extends ReplyType[R]

case class GetFirst(id: String) extends FirstCommand[String]

object GetFirst {
  implicit val format: Format[GetFirst] = Json.format
}

case class UpdateFirst(id: String) extends FirstCommand[String]

object UpdateFirst {
  implicit val format: Format[UpdateFirst] = Json.format
}

sealed trait FirstEvent extends AggregateEvent[FirstEvent] {
  def aggregateTag = FirstEvent.Tag
}

object FirstEvent {
  val Tag = AggregateEventTag[FirstEvent]
}

case class FirstUpdated(id: String) extends FirstEvent

object FirstUpdated {
  implicit val format: Format[FirstUpdated] = Json.format
}

object FirstSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    JsonSerializer[GetFirst],
    JsonSerializer[UpdateFirst],
    JsonSerializer[FirstUpdated],
    JsonSerializer[FirstState]
  )
}
