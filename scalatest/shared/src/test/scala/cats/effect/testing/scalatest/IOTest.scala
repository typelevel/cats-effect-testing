/*
 * Copyright 2020 Typelevel
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

package cats.effect.testing.scalatest

import cats.effect.{IO, SyncIO}
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

class IOSpecTests extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "Asserting Syntax " - {
    "IO Asserting" in {
      IO(1).asserting(_ shouldBe 1)
    }

    "SyncIO Asserting" in {
      SyncIO(1).asserting(_ shouldBe 1)
    }

    "IO assert no exception" in {
      IO(()).assertNoException
    }

    "IO assert Exception" in {
      IO.raiseError(AError).assertThrows[AError.type]
    }

    "IO assert Exception with message" in {
      val expectedMessage = "foo"
      IO.raiseError(AErrorWithMessage(expectedMessage))
        .assertThrowsWithMessage[AErrorWithMessage](expectedMessage)
    }

  }

  "Effect assertions" - {
    "IO Assertion" in {
      IO(1 shouldBe 1)
    }

    "SyncIO Assertion" in {
      SyncIO(1 shouldBe 1)
    }

    "Successful IO[Unit] treated as success" in {
      IO(())
    }

    "Successful SyncIO[Unit] treated as success" in {
      SyncIO(())
    }
  }
}

case object AError extends Throwable
case class AErrorWithMessage(message: String) extends Throwable {
  override def getMessage: String = message
}
