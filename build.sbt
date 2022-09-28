/*
 * Copyright 2020-2021 Typelevel
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

name := "cats-effect-testing"

ThisBuild / tlBaseVersion := "1.4"
ThisBuild / startYear := Some(2020)
ThisBuild / developers += tlGitHubDev("djspiewak", "Daniel Spiewak")

ThisBuild / crossScalaVersions := Seq("3.1.3", "2.12.17", "2.13.8")
ThisBuild / tlVersionIntroduced := Map("3" -> "1.1.1")

ThisBuild / tlCiReleaseBranches := Seq("series/1.x")

val CatsEffectVersion = "3.3.14"

lazy val root = tlCrossRootProject
  .aggregate(core, specs2, utest, minitest, scalatest)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .in(file("core"))
  .settings(
    name := "cats-effect-testing-core",
    tlVersionIntroduced := List("2.12", "2.13", "3").map(_ -> "1.3.0").toMap,
    libraryDependencies += "org.typelevel" %%% "cats-effect" % CatsEffectVersion)

lazy val specs2 = crossProject(JSPlatform, JVMPlatform)
  .in(file("specs2"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-specs2",
    libraryDependencies += ("org.specs2" %%% "specs2-core" % "4.17.0")
      .cross(CrossVersion.for3Use2_13)
      .exclude("org.scala-js", "scala-js-macrotask-executor_sjs1_2.13"))

lazy val scalatest = crossProject(JSPlatform, JVMPlatform)
  .in(file("scalatest"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-scalatest",

    libraryDependencies ++= Seq(
      "org.scalatest"    %%% "scalatest" % "3.2.14"))

lazy val utest = crossProject(JSPlatform, JVMPlatform)
  .in(file("utest"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-utest",

    testFrameworks += new TestFramework("utest.runner.Framework"),

    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion,
      "com.lihaoyi" %%% "utest" % "0.8.1"),

    Test / scalacOptions -= "-Xfatal-warnings")

lazy val minitest = crossProject(JSPlatform, JVMPlatform)
  .in(file("minitest"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-minitest",
    testFrameworks += new TestFramework("minitest.runner.Framework"),

    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion,
      "io.monix" %%% "minitest" % "2.9.6"),

    Test / scalacOptions -= "-Xfatal-warnings")
