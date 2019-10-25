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

package cats.effect.minitest
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

import cats.effect.IO
import minitest.api._

abstract class IOTestSuite extends BaseIOTestSuite[Unit, ExecutionContext] {
  protected def makeExecutionContext(): ExecutionContext = DefaultExecutionContext

  protected def timeout: FiniteDuration = 10.seconds

  setup(IO.pure(()))

  def test(name: String)(f: => IO[Unit]): Unit = super.test(name)(_ => f)

  protected[effect] def mkSpec(name: String, ec: ExecutionContext, io: Unit => IO[Unit]): TestSpec[Unit, Unit] =
    TestSpec.async[Unit](name, a => io(a).timeout(timeout).unsafeToFuture())

}
