package io.github.scalahackers.todo

import scala.sys.process._

object WorkExecutor {

    def callExtProgram(request: String): String = {
      val cmd = "ls -al"
      val result = cmd.!!
      return result
    }
}