package app.layer.activator

import app.layer.activator.transformer.KeyCodeMapper
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import zio.*
import zio.stream.ZStream

import java.awt.event.KeyEvent
import java.util.concurrent.TimeUnit
import javax.swing.JButton

object Activator:
  import Status.*
  import ZIO.*

  val actionTimeout: Int = 300
  val actionKey: Int = NativeKeyEvent.VC_CONTROL
  
  private val stubComponent = JButton()

  enum Status:
    case Activated, Deactivated

  type Activations = ZStream[Any, Throwable, Activator.Status]
  type GlobalKeyEvents = ZStream[Any, Throwable, NativeKeyEvent]

  object GlobalKeyEvent:
    def apply(e: NativeKeyEvent): KeyEvent =
      KeyEvent(
        stubComponent,
        KeyCodeMapper.fromNativeKey(e.getID),
        e.getWhen,
        KeyCodeMapper.modifiers(e),
        KeyCodeMapper.fromNativeKey(e.getKeyCode),
        e.getKeyChar
      )

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
      override val stream: Activations = events
        .changes
        .zipWithPrevious
        .collect:
          case (Some(_), s) => s
