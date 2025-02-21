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

package cats.effect.testing.scalatest

import cats.effect.{Outcome as _, *}
import org.scalatest.*
import org.scalatest.wordspec.FixtureAsyncWordSpec

import java.util.concurrent.TimeoutException
import scala.concurrent.duration.*

case class BlowUpResourceException() extends RuntimeException("boom")

class CatsResourceErrorSpecs
  extends FixtureAsyncWordSpec
    with CatsResourceIO[Int]  {

  private val expectedException = BlowUpResourceException()

  override protected val ResourceTimeout: Duration = 10.millis
  override val resource: Resource[IO, Int] =
    IO.raiseError(expectedException).toResource

  override def withFixture(test: OneArgAsyncTest): FutureOutcome =
    new FutureOutcome(super.withFixture(test).toFuture.recover {
      case TestFailedException(`expectedException`) =>
        Succeeded
      case ex: TimeoutException =>
        fail("Timeout received, probably because of unreported resource acquisition failure", ex)
    })

  "cats resource specifications" should {
    "report errors in resource acquisition" in { i =>
      fail(s"should not get here, but received $i")
    }
  }
}

object TestFailedException {
  def unapply(tfEx: org.scalatest.exceptions.TestFailedException): Option[Throwable] =
    Option(tfEx.getCause)
}
