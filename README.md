# cats-effect-testing

A quickie little utility which makes it easier to write tests using [specs2](https://specs2.org) (mutable or functional) where the examples are effectful within `cats.effect.IO`. Our goal is to shortly expand this functionality to µTest and ScalaTest.

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

## Usage

```sbt
resolvers += Resolver.bintrayRepo("djspiewak", "maven")

libraryDependencies += "com.codecommit" %% "cats-effect-testing" % "<version>"
```

Published for Scala 2.13.0.
