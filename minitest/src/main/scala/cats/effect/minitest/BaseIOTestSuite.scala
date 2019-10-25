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

import cats.Eval
import cats.effect.{ContextShift, IO, Timer}
import minitest.api._


private[effect] abstract class BaseIOTestSuite[A, Ec <: ExecutionContext] extends AbstractTestSuite with Asserts {

  protected def makeExecutionContext(): Ec

  private[effect] lazy val executionContext: Ec = makeExecutionContext()
  protected[effect] implicit def suiteEc: ExecutionContext = executionContext

  implicit def ioContextShift: ContextShift[IO] = IO.contextShift(executionContext)
  implicit def ioTimer: Timer[IO] = IO.timer(executionContext)

  protected[effect] def mkSpec(name: String, ec: Ec, io: A => IO[Unit]): TestSpec[A, Unit]

  private[this] def alreadyInitializedError(str: String): Error =
    new AssertionError(s"Cannot define $str after TestSuite was initialized")

  private[this] var fixture: Option[Eval[A]] = None
  private[this] var tearDownF: A => IO[Unit] = _ => IO.pure(())

  def setup(f: => IO[A]): Unit =
    synchronized {
      if (isInitialized) throw alreadyInitializedError("setup")
      fixture = Some(Eval.later(f.unsafeRunSync))
    }

  def tearDown(a: A => IO[Unit]): Unit =
    synchronized {
      if (isInitialized) throw alreadyInitializedError("tear down")
      tearDownF = a
    }

  def test(name: String)(f: A => IO[Unit]): Unit =
    synchronized {
      if (isInitialized) throw alreadyInitializedError("new tests")
      propertiesSeq :+= mkSpec(name, executionContext, f)
    }

   lazy val properties: Properties[_] =
    synchronized {
      if (!isInitialized) isInitialized = true
      Properties[A](
        () => fixture
          .map(_.value)
          .getOrElse(throw new AssertionError("could not create fixture")),
        a => tearDownF(a).unsafeRunSync,
        () => (),
        () => (),
        propertiesSeq
      )
    }


  private[this] var propertiesSeq = Vector.empty[TestSpec[A, Unit]]
  private[this] var isInitialized = false
}
