/*
 * Copyright 2020 Daniel Spiewak
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

package cats.effect.testing.scalatest

import cats.effect._
import cats.effect.syntax.effect._
import org.scalatest.{BeforeAndAfterAll, FixtureAsyncTestSuite, FutureOutcome}

import scala.concurrent.duration._

trait CatsResource[F[_], A] extends BeforeAndAfterAll {
  asyncTestSuite: FixtureAsyncTestSuite =>

  def resource: Resource[F, A]

  implicit def ResourceEffect: Effect[F]
  protected val ResourceTimeout: Duration = 10.seconds

  private var value: Option[A] = None
  private var shutdown: F[Unit] = ResourceEffect.unit

  override def beforeAll(): Unit = {
    ResourceEffect
      .map(resource.allocated) {
        case (a, shutdownAction) =>
          value = Some(a)
          shutdown = shutdownAction
      }
      .toIO
      .unsafeRunTimed(ResourceTimeout)
  }

  override def afterAll(): Unit = {
    shutdown.toIO.unsafeRunTimed(ResourceTimeout)
    value = None
    shutdown = ResourceEffect.unit
  }

  override type FixtureParam = A

  override def withFixture(test: OneArgAsyncTest): FutureOutcome =
    withFixture(test.toNoArgAsyncTest(value.getOrElse {
      fail("Resource Not Initialized When Trying to Use")
    }))

}
