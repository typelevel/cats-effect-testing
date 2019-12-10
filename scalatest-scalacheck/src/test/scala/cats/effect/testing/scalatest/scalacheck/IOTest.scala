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

import cats.data.EitherT
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Sync}
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.{CheckerAsserting, ScalaCheckPropertyChecks}

class IOTest extends AsyncIOSpec with Matchers with ScalaCheckPropertyChecks {

  "Scalacheck IO assertions" - {

    "Assert success" in {
      forAll { (l1: List[Int], l2: List[Int]) =>
        IO.delay(l1.size + l2.size shouldBe (l1 ::: l2).size)
      }
    }

    "Assert exception" in {
      val check: IO[Unit] = forAll { (l1: List[Int], l2: List[Int]) =>
        IO.delay(l1.size + l2.size shouldBe -1)
      }

      check.assertThrows[Exception]
    }

    implicit def ioCheckingAsserting[A]: CheckerAsserting[IO[A]] { type Result = IO[Unit] } =
      new EffectCheckerAsserting

  }

  "Scalacheck EitherT[IO, Throwable, A] assertions" - {

    type Eff[A] = EitherT[IO, Throwable, A]

    "Assert success" in {
      val check = forAll { (l1: List[Int], l2: List[Int]) =>
        Sync[Eff].delay(l1.size + l2.size shouldBe (l1 ::: l2).size)
      }

      check.leftSemiflatMap[Unit](IO.raiseError).merge.assertNoException
    }

    "Assert exception" in {
      val check = forAll { (l1: List[Int], l2: List[Int]) =>
        Sync[Eff].delay(l1.size + l2.size shouldBe -1)
      }

      check.leftSemiflatMap[Unit](IO.raiseError[Unit]).merge.assertThrows[Exception]
    }

    implicit def checkingAsserting[A]: CheckerAsserting[Eff[A]] { type Result = Eff[Unit] } =
      new EffectCheckerAsserting
  }

}
