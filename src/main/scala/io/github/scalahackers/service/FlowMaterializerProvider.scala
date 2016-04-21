package io.github.scalahackers.service

import akka.stream._

trait FlowMaterializerProvider {
  implicit val materializer: Materializer
}
