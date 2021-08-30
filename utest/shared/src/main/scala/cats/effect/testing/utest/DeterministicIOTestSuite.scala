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

package cats.effect.testing.utest

import cats.effect.{unsafe, IO}
import cats.effect.testkit.TestContext

import utest.TestSuite

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

abstract class DeterministicIOTestSuite extends TestSuite {
  protected val testContext: TestContext = TestContext()
  protected def allowNonIOTests: Boolean = false

  override def utestWrap(path: Seq[String], runBody: => Future[Any])(implicit ec: ExecutionContext): Future[Any] = {
    val scheduler = new unsafe.Scheduler {

      def sleep(delay: FiniteDuration, action: Runnable): Runnable = {
        val cancel = testContext.schedule(delay, action)
        new Runnable { def run() = cancel() }
      }

      def nowMillis() = testContext.now().toMillis
      def monotonicNanos() = testContext.now().toNanos
    }

    implicit val runtime: unsafe.IORuntime =
      unsafe.IORuntime(testContext, testContext, scheduler, () => (), unsafe.IORuntimeConfig())

    runBody.flatMap {
      case io: IO[Any] =>
        val f = io.unsafeToFuture()
        testContext.tickAll()
        assert(testContext.state.tasks.isEmpty)
        f.value match {
          case Some(_) => f
          case None => throw new RuntimeException(s"The IO in ${path.mkString(".")} did not terminate.")
        }
      case other if allowNonIOTests => Future.successful(other)
      case other =>
        throw new RuntimeException(s"Test body must return an IO value. Got $other")
    }(new ExecutionContext {
      def execute(runnable: Runnable): Unit = runnable.run()
      def reportFailure(cause: Throwable): Unit = throw cause
    })
  }
}
