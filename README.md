# cats-effect-testing

A quickie little utility which makes it easier to write tests using [specs2](https://specs2.org) (mutable or functional), [scalatest](http://scalatest.org), [µTest](https://github.com/lihaoyi/utest) or [minitest](https://github.com/monix/minitest) where the examples are effectful within `cats.effect.IO`.

## Specs2

```scala
import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import org.specs2.mutable.Specification

// for some reason, only class works here; object will not be detected by sbt
class ExampleSpec extends Specification with CatsEffect {
  "examples" should {
    "do the things" in IO {
      true must beTrue
    }
  }
}
```

The above compiles and runs exactly as you would expect.

By default, tests run with a 10 second timeout. If you wish to override this, simply override the inherited `Timeout` val:

```scala
override val Timeout = 5.seconds
```

If you need an `ExecutionContext`, one is available in the `executionContext` val.

### Usage

```sbt
libraryDependencies += "org.typelevel" %% "cats-effect-testing-specs2" % "<version>" % Test
```

Published for Scala 3.0.0-RC3, 2.13, 2.12, as well as ScalaJS 1.5.1. Depends on Cats Effect 3.1.0 and specs2 4.11.0.

## ScalaTest

```scala
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.freespec.AsyncFreeSpec

class MySpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  "My Code " - {
    "works" in {
      IO(1).asserting(_ shouldBe 1)
    }
}

```
### Usage

```sbt
libraryDependencies += "org.typelevel" %% "cats-effect-testing-scalatest" % "<version>" % Test
```

Published for Scala 3.0.0-RC3, 2.13, 2.12, as well as ScalaJS 1.5.1. Depends on Cats Effect 3.1.0 and scalatest 3.2.6.

## ScalaTest ScalaCheck

The module provides an instance of the `org.scalatestplus.scalacheck.CheckerAsserting`, which can be used with any type of effect that has an instance of `cats.effect.Effect`: `IO`, `EitherT[IO, Throwable, *]`, and so on.

```scala
import cats.data.EitherT
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Sync}
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.{CheckerAsserting, ScalaCheckPropertyChecks}

class MySpec extends AsyncIOSpec with Matchers with ScalaCheckPropertyChecks {

  "My IO Code" - {

    "works with effect-full property-based testing" in {
      forAll { (l1: List[Int], l2: List[Int]) =>
        IO.delay(l1.size + l2.size shouldBe (l1 ::: l2).size)
      }
    }
  
    implicit def ioCheckingAsserting[A]: CheckerAsserting[IO[A]] { type Result = IO[Unit] } =
      new EffectCheckerAsserting
  }
  
  "My EitherT[IO, Throwable, A] code" - {
  
    type Eff[A] = EitherT[IO, Throwable, A]

    "works with effect-full property-based testing" in {
      val check = forAll { (l1: List[Int], l2: List[Int]) =>
        Sync[Eff].delay(l1.size + l2.size shouldBe (l1 ::: l2).size)
      }

      check.leftSemiflatMap[Unit](IO.raiseError).merge.assertNoException
    }

    implicit def checkingAsserting[A]: CheckerAsserting[Eff[A]] { type Result = Eff[Unit] } =
      new EffectCheckerAsserting
  }

}

```
### Usage

```sbt
libraryDependencies += "org.typelevel" %% "cats-effect-testing-scalatest-scalacheck" % "<version>" % Test
```

Published for Scala 3.0.0-RC3, 2.13, 2.12, as well as ScalaJS 1.5.1. Depends on Cats Effect 3.1.0, scalatest-scalacheck-1-15 3.2.6.0, and scalacheck 1.15.3.

## µTest

```scala
import scala.concurrent.duration._
import utest._
import cats.implicits._
import cats.effect.IO
import cats.effect.testing.utest.{IOTestSuite, DeterministicIOTestSuite}

// IOTestSuite uses real ExecutionContext for async operations
object SimpleSuite extends IOTestSuite {
  override val timeout = 1.second // Default timeout is 10 seconds

  val tests = Tests {
    test("do the thing") {
      IO(assert(true))
    }
  }
}

// DeterministicIOTestSuite simulates time with TestContext from cats-effect-laws
// package. That allows to simulate long timeouts and have async operations
// without actually slowing down your test suite, but it cannot use operations
// that are hard-wired to do real async calls
object DetSuite extends DeterministicIOTestSuite {
  // By default, both types of suite prevents using non-IO return values.
  // I recommend separating effectful and pure suites altogether, but
  // this can be overriden like so:
  override val allowNonIOTests = true
  val tests = Tests {
    test("Simulated time!") {
      IO.sleep(8.hours) >> IO(assert(!"life".isEmpty))
    }
    
    test("Non-IO tests") {
     assert(true)
    }
  }
}

```

### Usage

```sbt
libraryDependencies += "org.typelevel" %% "cats-effect-testing-utest" % "<version>" % Test
```

Published for Scala 3.0.0-RC3, 2.13, 2.12, as well as ScalaJS 1.5.1. Depends on Cats Effect 3.1.0 and µTest 0.7.9.

## Minitest

Minitest is very similar to uTest, but being strongly typed, there's no need to support
non-IO tests

```scala
import scala.concurrent.duration._
import cats.implicits._
import cats.effect.IO
import cats.effect.testing.minitest.{IOTestSuite, DeterministicIOTestSuite}

// IOTestSuite uses real ExecutionContext for async operations
// (can be overriden by reimplementing makeExecutionContext)
object SimpleSuite extends IOTestSuite {
  override val timeout = 1.second // Default timeout is 10 seconds

  test("do the thing") {
    IO(assert(true))
  }
}

// DeterministicIOTestSuite simulates time with TestContext from cats-effect-laws
// package. That allows to simulate long timeouts and have async operations
// without actually slowing down your test suite, but it cannot use operations
// that are hard-wired to do real async calls
object DetSuite extends DeterministicIOTestSuite {
  test("Simulated time!") {
    IO.sleep(8.hours) >> IO(assert(!"life".isEmpty))
  }
}

```

### Usage

```sbt
libraryDependencies += "org.typelevel" %% "cats-effect-testing-minitest" % "<version>" % Test
```

Published for Scala 3.0.0-RC3, 2.13, 2.12, as well as ScalaJS 1.5.1. Depends on Cats Effect 3.1.0 and minitest 2.9.5.
