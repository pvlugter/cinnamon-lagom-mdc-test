package com.lightbend.sample.second.impl

import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{ AggregateEvent, AggregateEventTag, PersistentEntity }
import com.lightbend.lagom.scaladsl.playjson.{ JsonSerializer, JsonSerializerRegistry }
import org.slf4j.LoggerFactory
import play.api.libs.json.{ Format, Json }
import scala.collection.immutable

class SecondEntity extends PersistentEntity {

  override type Command = SecondCommand[_]
  override type Event = SecondEvent
  override type State = SecondState

  val log = LoggerFactory.getLogger(getClass)

  override def initialState: SecondState = SecondState(0)

  override def behavior: Behavior = {
    case _ => Actions().onReadOnlyCommand[GetSecond, String] {
      case (GetSecond(id), ctx, state) =>
        log.info(s"$id - get")
        ctx.reply(s"second: $id = ${state.count}")
    }.onCommand[UpdateSecond, String] {
      case (UpdateSecond(id), ctx, state) =>
        log.info(s"$id - update")
        ctx.thenPersist(SecondUpdated(id)) { _ =>
          log.info(s"$id - updated")
          ctx.reply(s"second: $id = ${state.count} + 1")
        }
    }.onEvent {
      case (SecondUpdated(id), state) =>
        SecondState(state.count + 1)
    }
  }
}

case class SecondState(count: Int)

object SecondState {
  implicit val format: Format[SecondState] = Json.format
}

sealed trait SecondCommand[R] extends ReplyType[R]

case class GetSecond(id: String) extends SecondCommand[String]

object GetSecond {
  implicit val format: Format[GetSecond] = Json.format
}

case class UpdateSecond(id: String) extends SecondCommand[String]

object UpdateSecond {
  implicit val format: Format[UpdateSecond] = Json.format
}

sealed trait SecondEvent extends AggregateEvent[SecondEvent] {
  def aggregateTag = SecondEvent.Tag
}

object SecondEvent {
  val Tag = AggregateEventTag[SecondEvent]
}

case class SecondUpdated(id: String) extends SecondEvent

object SecondUpdated {
  implicit val format: Format[SecondUpdated] = Json.format
}

object SecondSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    JsonSerializer[GetSecond],
    JsonSerializer[UpdateSecond],
    JsonSerializer[SecondUpdated],
    JsonSerializer[SecondState]
  )
}
