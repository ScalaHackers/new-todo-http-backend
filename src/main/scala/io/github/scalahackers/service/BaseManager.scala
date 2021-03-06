package io.github.scalahackers.service

import akka.actor._

// worker state table
sealed trait WorkerBaseState

abstract class BaseManager extends Actor with TodoTxsTable with ActorLogging {
}
