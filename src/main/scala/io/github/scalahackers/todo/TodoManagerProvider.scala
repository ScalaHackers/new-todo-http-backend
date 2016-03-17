package io.github.scalahackers.todo

import akka.actor._

trait TodoManagerProvider{
  val todoManager: ActorRef
}
