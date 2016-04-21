package io.github.scalahackers.service

import akka.actor.{ Actor, FSM }
import scala.concurrent.duration._

sealed trait TxsStates
case object InitState extends TxsStates
case object PrepState extends TxsStates
case object ValidateState extends TxsStates
case object SearchState extends TxsStates
case object FinalState extends TxsStates

sealed trait TxsData
case object Uninitialized extends TxsData
case class ComparisonState(id: String, oldSystem: Option[Int] = None, newSystem: Option[Int] = None)
          extends TxsData

/**
  * Created by love on 3/16/2016.
  */
class StateMachineManager extends Actor with FSM[TxsStates, TxsData]{

  startWith(InitState, Uninitialized)

  initialize()

}
