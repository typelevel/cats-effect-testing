package cats.effect.scalatest.scalacheck

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
