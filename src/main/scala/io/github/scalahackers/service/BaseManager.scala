package io.github.scalahackers.service

import akka.actor._
import io.github.scalahackers.database.TodoTxsTable

// worker state table
sealed trait WorkerBaseState

abstract class BaseManager extends Actor with TodoTxsTable with ActorLogging {
}
