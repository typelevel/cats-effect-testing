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

import cats.effect.{Effect, Resource, Sync, Timer}
import cats.effect.syntax.concurrent._
import cats.effect.syntax.effect._
import org.specs2.execute.{AsResult, Failure, Result}
import specs2._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers.futureAsResult

import scala.concurrent.duration._
import scala.language.higherKinds

trait CatsEffect {

  protected val Timeout: FiniteDuration = 10.seconds

  implicit def effectAsResult[F[_]: Effect, R](implicit ee: ExecutionEnv, R: AsResult[R]): AsResult[F[R]] = new AsResult[F[R]] {
    def asResult(t: => F[R]): Result =
      Effect[F].map(t)(R.asResult(_))
        .toIO
        .unsafeToFuture
        .awaitFor(Timeout)
  }

  implicit def resourceAsResult[F[_]: Effect, R](implicit ee: ExecutionEnv, R: AsResult[R]): AsResult[Resource[F,R]] = new AsResult[Resource[F,R]]{
    def asResult(t: => Resource[F, R]): Result = 
      t.use(r => Sync[F].delay(R.asResult(r)))
        .toIO
        .unsafeToFuture
        .awaitFor(Timeout)
  }
}
