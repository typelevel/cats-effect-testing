/*
 * Copyright 2020 Typelevel
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
package scalatest

import cats.effect.{Async, IO}
import cats.effect.unsafe.IORuntime

import org.scalatest.FixtureAsyncTestSuite

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

trait CatsResourceIO[A] extends CatsResource[IO, A] with RuntimePlatform { this: FixtureAsyncTestSuite =>

  override implicit def executionContext: ExecutionContext = ExecutionContext.global

  final def ResourceAsync = Async[IO]

  final def ResourceUnsafeRun = _ResourceUnsafeRun

  private lazy val _ResourceUnsafeRun =
    new UnsafeRun[IO] {
      private implicit val runtime: IORuntime = createIORuntime(executionContext)

      override def unsafeToFuture[B](ioa: IO[B]): Future[B] =
        unsafeToFuture(ioa, None)

      override def unsafeToFuture[B](ioa: IO[B], timeout: Option[FiniteDuration]): Future[B] =
        timeout.fold(ioa)(ioa.timeout).unsafeToFuture()
    }
}
