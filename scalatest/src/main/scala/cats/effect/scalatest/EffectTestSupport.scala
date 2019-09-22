/*
 * Copyright 2019 Daniel Spiewak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cats.effect.scalatest

import cats.effect._
import cats.implicits._
import scala.concurrent.Future
import org.scalatest.{Assertion, Succeeded}

/**
 * Copied from FS2
 * https://github.com/functional-streams-for-scala/fs2/blob/188a37883d7bbdf22bc4235a3a1223b14dc10b6c/core/shared/src/test/scala/fs2/EffectTestSupport.scala
 */
trait EffectTestSupport {

  implicit def syncIoToFutureAssertion(io: SyncIO[Assertion]): Future[Assertion] =
    io.toIO.unsafeToFuture
  implicit def ioToFutureAssertion(io: IO[Assertion]): Future[Assertion] =
    io.unsafeToFuture
  implicit def syncIoUnitToFutureAssertion(io: SyncIO[Unit]): Future[Assertion] =
    io.toIO.as(Succeeded).unsafeToFuture
  implicit def ioUnitToFutureAssertion(io: IO[Unit]): Future[Assertion] =
    io.as(Succeeded).unsafeToFuture
}