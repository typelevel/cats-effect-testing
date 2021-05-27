/*
 * Copyright 2020-2021 Typelevel
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

package cats.effect.testing
package utest

import cats.effect.Temporal
import cats.effect.syntax.all._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

abstract class EffectTestSuite[F[_]: Temporal: UnsafeRun](implicit Tag: ClassTag[F[Any]])
    extends _root_.utest.TestSuite {

  protected def timeout: FiniteDuration = 10.seconds
  protected def allowNonIOTests: Boolean = false

  override def utestWrap(path: Seq[String], runBody: => Future[Any])(implicit ec: ExecutionContext): Future[Any] = {
    runBody flatMap {
      case Tag(io) => UnsafeRun[F].unsafeToFuture(io, Some(timeout))
      case other if allowNonIOTests => Future.successful(other)
      case other => throw new RuntimeException(s"Test body must return an IO value. Got $other")
    }
  }
}

