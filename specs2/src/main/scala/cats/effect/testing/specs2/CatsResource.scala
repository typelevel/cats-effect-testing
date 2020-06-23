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

import cats._
import cats.implicits._
import cats.effect._
import org.specs2.specification.core.Execution
import org.specs2.specification.BeforeAfterAll
import cats.effect.syntax.effect._
import scala.concurrent.duration._
import org.specs2.execute.{AsResult, Failure, Result}
import org.specs2.specification.core.AsExecution
import cats.effect.concurrent.Deferred

trait CatsResource[F[_], A] extends BeforeAfterAll with CatsEffect {
  
  def resource: Resource[F, A]

  implicit def ResourceEffect: ConcurrentEffect[F]
  protected val ResourceTimeout: Duration = 10.seconds

  private var value : Option[A] = None
  private var shutdown : F[Unit] = ResourceEffect.unit
  private var started : Deferred[F, Unit] = Deferred.unsafe[F, Unit]

  override def beforeAll(): Unit = {
    ResourceEffect.map(resource.allocated){ case (a, shutdownAction) => 
      value = Some(a)
      shutdown = shutdownAction
    }.flatTap(_ => started.complete(())).toIO.unsafeRunTimed(ResourceTimeout)
  }
  override def afterAll(): Unit = {
    shutdown.toIO.unsafeRunTimed(ResourceTimeout)
    value = None
    shutdown = ResourceEffect.unit
  }


  def withResource[R](r: A => R)(implicit R: AsResult[R]): Execution = {
    useResource[R]({a: A => r(a).pure[F]})
  }

  def useResource[R](f: A => F[R])(implicit R: AsResult[R]): Execution = {
    effectAsExecution[F, Result].execute(
      started.get >> value.fold[F[Result]](
        Applicative[F].pure(
            Failure("Resource Not Initialized When Trying to Use")
          )
        )(a => 
        f(a).map(R.asResult(_))
      )
    )
  }

  def resource[R](f: A => Resource[F,R])(implicit R: AsResult[R]): Execution = {
    resourceAsExecution[F, Result].execute(
      Resource.liftF(started.get) >> value.fold[Resource[F, Result]](
        Applicative[Resource[F, *]].pure(
            Failure("Resource Not Initialized When Trying to Use")
          )
        )(a => 
        f(a).map(R.asResult(_))
      )
    )
  }

}