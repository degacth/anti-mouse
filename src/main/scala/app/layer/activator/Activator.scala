package app.layer.activator

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import zio.*
import zio.stream.ZStream

import java.util.concurrent.TimeUnit

object Activator:
  import Status.*
  import ZIO.*

  val actionTimeout: Int = 300
  val actionKey: Int = NativeKeyEvent.VC_CONTROL

  enum Status:
    case Activated, Deactivated

  type Activations = ZStream[Any, Throwable, Activator.Status]

  trait Service:
    def stream: Activations

  def live: RLayer[Activations, Activator.Service] = ZLayer.scoped:
    for
      keyEvents <- service[Activations]
      events <- keyEvents
        .partition(_ == Deactivated)
        .map((released, pressed) => released.merge(
          pressed
            .mapZIO(_ => Clock.currentTime(TimeUnit.MILLISECONDS))
            .zipWithPrevious
            .collect:
              case (Some(prev), curr) if curr - prev < actionTimeout => Activated
        ))
    yield new Service:
      override val stream: Activations = events.changes
