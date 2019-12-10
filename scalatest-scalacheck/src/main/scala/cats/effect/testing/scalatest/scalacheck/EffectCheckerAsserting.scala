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

package cats.effect.testing.scalatest.scalacheck

import cats.effect.Effect
import org.scalactic.source
import org.scalatest.exceptions._
import org.scalatestplus.scalacheck.CheckerAsserting

class EffectCheckerAsserting[F[_], A](implicit F: Effect[F])
    extends CheckerAsserting.CheckerAssertingImpl[F[A]] {

  override type Result = F[Unit]

  override def succeed(result: F[A]): (Boolean, Option[Throwable]) =
    F.toIO(result)
      .attempt
      .unsafeRunSync()
      .fold(e => (false, Some(e)), _ => (true, None))

  override def indicateSuccess(message: => String): Result = F.unit

  override def indicateFailure(
      messageFun: StackDepthException => String,
      undecoratedMessage: => String,
      scalaCheckArgs: List[Any],
      scalaCheckLabels: List[String],
      optionalCause: Option[Throwable],
      pos: source.Position
  ): Result = {
    val error = new GeneratorDrivenPropertyCheckFailedException(
      messageFun,
      optionalCause,
      pos,
      None,
      undecoratedMessage,
      scalaCheckArgs,
      None,
      scalaCheckLabels
    )

    F.raiseError(error)
  }

}
