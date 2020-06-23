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

import cats.implicits._
import cats.effect.{Async, Effect, Resource, Sync, Timer}
import cats.effect.syntax.concurrent._
import cats.effect.syntax.effect._
import org.specs2.execute.{AsResult, Failure, Result}
import specs2._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.specification.core._

import scala.concurrent.duration._
import scala.language.higherKinds

trait CatsEffect {

  protected val Timeout: FiniteDuration = 10.seconds

  implicit def effectAsExecution[F[_]: Effect, R: AsResult] = new AsExecution[F[R]] {
    def execute(r: => F[R]): Execution = 
      Execution.withEnvAsync(env => 
        (Async.shift(env.executionContext) >> r).toIO.unsafeToFuture())
  }

  implicit def resourceAsExecution[F[_]: Effect, R: AsResult] = new AsExecution[Resource[F, R]]{
    def execute(t: => Resource[F,R]): Execution = 
      Execution.withEnvAsync(env =>
        (Async.shift(env.executionContext) >> t.use(_.pure[F])).toIO.unsafeToFuture()
      )
  }
}
