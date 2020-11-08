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

package cats.effect.testing.specs2

import cats.effect.{Effect, Resource, Sync}
import cats.effect.syntax.effect._
import org.specs2.execute.{AsResult, Failure, Result}

import scala.concurrent.duration._

trait CatsEffect {

  protected val Timeout: Duration = 10.seconds

  implicit def effectAsResult[F[_]: Effect, R](implicit R: AsResult[R]): AsResult[F[R]] = new AsResult[F[R]] {
    def asResult(t: => F[R]): Result =
      t.toIO.unsafeRunTimed(Timeout)
        .map(R.asResult(_))
        .getOrElse(Failure(s"expectation timed out after $Timeout"))
  }

  implicit def resourceAsResult[F[_]: Effect, R](implicit R: AsResult[R]): AsResult[Resource[F,R]] = new AsResult[Resource[F,R]]{
    def asResult(t: => Resource[F, R]): Result =
      t.use(r => Sync[F].delay(R.asResult(r)))
        .toIO
        .unsafeRunTimed(Timeout)
        .getOrElse(Failure(s"expectation timed out after $Timeout"))
  }
}
