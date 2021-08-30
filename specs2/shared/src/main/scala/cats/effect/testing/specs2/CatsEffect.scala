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
package specs2

import cats.effect.{IO, MonadCancel, Resource}
import cats.effect.testkit.TestControl
import cats.syntax.all._

import org.specs2.execute.{AsResult, Result}
import org.specs2.matcher.{Expectable, Matcher, MatchFailureException, MatchResult}
import org.specs2.specification.core.{AsExecution, Execution}

import scala.concurrent.duration._

trait CatsEffect {

  protected val Timeout: Duration = 10.seconds
  protected def finiteTimeout: Option[FiniteDuration] =
    Some(Timeout) collect {
      case fd: FiniteDuration => fd
    }

  implicit def effectAsExecution[F[_]: UnsafeRun, R](implicit R: AsResult[R]): AsExecution[F[R]] = new AsExecution[F[R]] {
    def execute(t: => F[R]): Execution =
      Execution
        .withEnvAsync(_ => UnsafeRun[F].unsafeToFuture(t, finiteTimeout))
        .copy(timeout = finiteTimeout)
  }

  implicit def resourceAsExecution[F[_]: UnsafeRun, R](implicit F: MonadCancel[F, Throwable], R: AsResult[R]): AsExecution[Resource[F, R]] = new AsExecution[Resource[F, R]] {
    def execute(t: => Resource[F, R]): Execution =
      effectAsExecution[F, R].execute(t.use(_.pure[F]))
  }

  def execute[A](body: (TestControl, () => Option[Either[Throwable, A]]) => Result): Matcher[IO[A]] =
    executeWithOptionSeed(None)(body)

  def executeWithSeed[A](
      seed: String)(
      body: (TestControl, () => Option[Either[Throwable, A]]) => Result)
      : Matcher[IO[A]] =
    executeWithOptionSeed(Some(seed))(body)

  private[this] def executeWithOptionSeed[A](
      seed: Option[String])(
      body: (TestControl, () => Option[Either[Throwable, A]]) => Result)
      : Matcher[IO[A]] =
    new Matcher[IO[A]] {
      def apply[B <: IO[A]](exp: Expectable[B]): MatchResult[B] = {
        val control = TestControl(seed = seed)
        val future = exp.value.unsafeToFuture()(control.runtime)

        val r = try {
          body(control, () => future.value.map(_.toEither))
        } catch {
          case mfe: MatchFailureException[_] =>
            val f2 = mfe.failure.updateMessage(m => s"$m (seed = ${control.seed})")
            throw new MatchFailureException(f2)
        }

        result(
          r.isSuccess,
          s"${exp.description} fully executed: ${r.message}",
          s"${exp.description} failed to execute: ${r.message} (seed = ${control.seed})",
          exp)
      }
    }
}
