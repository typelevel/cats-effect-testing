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

import cats.effect._
import org.specs2.specification.BeforeAfterAll
import cats.effect.syntax.effect._
import scala.concurrent.duration._
import org.specs2.execute.{AsResult, Failure, Result}

trait CatsResource[F[_], A] extends BeforeAfterAll {
  
  def resource: Resource[F, A]

  implicit def ResourceEffect: Effect[F]
  protected val ResourceTimeout: Duration = 10.seconds

  private var value : Option[A] = None
  private var shutdown : F[Unit] = ResourceEffect.unit

  override def beforeAll(): Unit = {
    ResourceEffect.map(resource.allocated){ case (a, shutdownAction) => 
      value = Some(a)
      shutdown = shutdownAction
    }.toIO.unsafeRunTimed(ResourceTimeout)
  }
  override def afterAll(): Unit = {
    shutdown.toIO.unsafeRunTimed(ResourceTimeout)
    value = None
    shutdown = ResourceEffect.unit
  }

  def withResource[R](r: A => R)(implicit R: AsResult[R]): Result = {
    value.fold[Result](
      Failure("Resource Not Initialized When Trying to Use")
    )(a => 
      R.asResult(r(a))
    )

  }
}