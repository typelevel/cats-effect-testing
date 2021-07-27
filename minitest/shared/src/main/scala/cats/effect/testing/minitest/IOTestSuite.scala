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

package cats.effect.testing.minitest

import cats.effect.{unsafe, IO}
import cats.effect.testing.RuntimePlatform

import minitest.api._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

abstract class IOTestSuite extends BaseIOTestSuite[ExecutionContext] with RuntimePlatform {
  protected def makeExecutionContext(): ExecutionContext = DefaultExecutionContext

  protected def timeout: FiniteDuration = 10.seconds

  protected[effect] def mkSpec(name: String, ec: ExecutionContext, io: => IO[Unit]): TestSpec[Unit, Unit] = {
    TestSpec.async[Unit](name, { _ =>
      // TODO cleanup
      implicit val runtime: unsafe.IORuntime = createIORuntime(ec)
      io.timeout(timeout).unsafeToFuture()
    })
  }

}
