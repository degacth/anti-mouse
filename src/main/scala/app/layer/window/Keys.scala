package app.layer.window

import zio.*
import ZIO.*
import zio.stream.{UStream, ZStream}

import java.awt.event.{KeyAdapter, KeyEvent}

object Keys:
  type Stream = UStream[KeyEvent]

  def live: ZLayer[Frame.Service, Nothing, Stream] = ZLayer.scoped:
    serviceWith[Frame.Service]: frame =>
      ZStream.asyncScoped[Any, Nothing, KeyEvent] { cb =>
        debug("create frame key listener") *>
          succeed:
            frame.addKeyListener(new KeyAdapter:
              override def keyPressed(e: KeyEvent): Unit = cb(succeed(Chunk.single(e)))
              override def keyReleased(e: KeyEvent): Unit = cb(succeed(Chunk.single(e)))
            )
      }.changesWith((e1, e2) => e1.getKeyCode == e2.getKeyCode && e1.getID == e2.getID)
