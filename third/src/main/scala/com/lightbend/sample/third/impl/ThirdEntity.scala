package com.lightbend.sample.third.impl

import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{ AggregateEvent, AggregateEventTag, PersistentEntity }
import com.lightbend.lagom.scaladsl.playjson.{ JsonSerializer, JsonSerializerRegistry }
import org.slf4j.LoggerFactory
import play.api.libs.json.{ Format, Json }
import scala.collection.immutable

class ThirdEntity extends PersistentEntity {

  override type Command = ThirdCommand[_]
  override type Event = ThirdEvent
  override type State = ThirdState

  val log = LoggerFactory.getLogger(getClass)

  override def initialState: ThirdState = ThirdState(0)

  override def behavior: Behavior = {
    case _ => Actions().onReadOnlyCommand[GetThird, String] {
      case (GetThird(id), ctx, state) =>
        log.info(s"$id - get")
        ctx.reply(s"third: $id = ${state.count}")
    }.onCommand[UpdateThird, String] {
      case (UpdateThird(id), ctx, state) =>
        log.info(s"$id - update")
        ctx.thenPersist(ThirdUpdated(id)) { _ =>
          log.info(s"$id - updated")
          ctx.reply(s"third: $id = ${state.count} + 1")
        }
    }.onEvent {
      case (ThirdUpdated(id), state) =>
        ThirdState(state.count + 1)
    }
  }
}

case class ThirdState(count: Int)

object ThirdState {
  implicit val format: Format[ThirdState] = Json.format
}

sealed trait ThirdCommand[R] extends ReplyType[R]

case class GetThird(id: String) extends ThirdCommand[String]

object GetThird {
  implicit val format: Format[GetThird] = Json.format
}

case class UpdateThird(id: String) extends ThirdCommand[String]

object UpdateThird {
  implicit val format: Format[UpdateThird] = Json.format
}

sealed trait ThirdEvent extends AggregateEvent[ThirdEvent] {
  def aggregateTag = ThirdEvent.Tag
}

object ThirdEvent {
  val Tag = AggregateEventTag[ThirdEvent]
}

case class ThirdUpdated(id: String) extends ThirdEvent

object ThirdUpdated {
  implicit val format: Format[ThirdUpdated] = Json.format
}

object ThirdSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: immutable.Seq[JsonSerializer[_]] = immutable.Seq(
    JsonSerializer[GetThird],
    JsonSerializer[UpdateThird],
    JsonSerializer[ThirdUpdated],
    JsonSerializer[ThirdState]
  )
}
