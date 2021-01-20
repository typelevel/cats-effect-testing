/*
 * Copyright 2020-2021 Daniel Spiewak
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

package cats.effect
package testing

import scala.concurrent.Future

trait UnsafeRun[F[_]] {
  def unsafeToFuture[A](fa: F[A]): Future[A]
}

object UnsafeRun {

  def apply[F[_]](implicit F: UnsafeRun[F]): UnsafeRun[F] = F

  implicit object unsafeRunForCatsIO extends UnsafeRun[IO] {
    import unsafe.implicits.global

    // TODO is it worth isolating runtimes between test runs?
    def unsafeToFuture[A](ioa: IO[A]): Future[A] =
      ioa.unsafeToFuture()
  }
}
