# cats-effect-testing

A quickie little utility which makes it easier to write tests using [specs2](https://specs2.org) (mutable or functional)
or [µTest](https://github.com/lihaoyi/utest) where the examples are effectful within `cats.effect.IO`.
Our goal is to shortly expand this functionality to minitest and ScalaTest.


## Specs2
```scala
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
libraryDependencies += "com.codecommit" %% "cats-effect-testing-specs2" % "<version>"
```

Published for Scala 2.13 and 2.12. Depends on cats-effect 2.0.0-M4 and specs2 4.6.0.

## µTest

```scala
import scala.concurrent.duration._
import utest._
import cats.implicits._
import cats.effect.IO
import cats.effect.utest.{IOTestSuite, DeterministicIOTestSuite}

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
libraryDependencies += "com.codecommit" %% "cats-effect-testing-utest" % "<version>" % Test
```

Published for Scala 2.13 and 2.12. Depends on cats-effect 2.0.0-M4 and µTest 0.7.1.
