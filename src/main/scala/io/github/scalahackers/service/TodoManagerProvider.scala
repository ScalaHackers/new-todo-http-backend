package io.github.scalahackers.service

import akka.actor._

trait TodoManagerProvider{
  val todoManager: ActorRef
}
