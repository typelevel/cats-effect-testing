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

name := "cats-effect-testing"

ThisBuild / baseVersion := "1.0"
ThisBuild / strictSemVer := false

ThisBuild / organization := "com.codecommit"
ThisBuild / publishGithubUser := "djspiewak"
ThisBuild / publishFullName := "Daniel Spiewak"

ThisBuild / crossScalaVersions := Seq("3.0.0-M3", "2.12.12", "2.13.3")

ThisBuild / githubWorkflowTargetBranches := Seq("series/1.x")

ThisBuild / homepage := Some(url("https://github.com/djspiewak/cats-effect-testing"))

ThisBuild / startYear := Some(2020)
ThisBuild / endYear := Some(2021)

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/djspiewak/cats-effect-testing"),
    "git@github.com:djspiewak/cats-effect-testing.git"))

val CatsEffectVersion = "3.0.0-M5"

lazy val root = project
  .in(file("."))
  .aggregate(core, specs2, /*utest,*/ minitest, scalatest)
  .enablePlugins(NoPublishPlugin)

lazy val core = project
  .in(file("core"))
  .settings(libraryDependencies += "org.typelevel" %% "cats-effect" % CatsEffectVersion)

lazy val specs2 = project
  .in(file("specs2"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-specs2",

    libraryDependencies += "org.specs2"    %% "specs2-core" % "4.10.5")
  .settings(dottyLibrarySettings)

lazy val scalatest = project
  .in(file("scalatest"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-scalatest",

    libraryDependencies ++= Seq(
      "org.scalatest"    %% "scalatest" % "3.2.3"))

/*lazy val utest = project
  .in(file("utest"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-utest",

    testFrameworks += new TestFramework("utest.runner.Framework"),

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect-testkit" % CatsEffectVersion,
      "com.lihaoyi" %% "utest" % "0.7.5"))*/

lazy val minitest = project
  .in(file("minitest"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-minitest",
    testFrameworks += new TestFramework("minitest.runner.Framework"),

    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect-testkit" % CatsEffectVersion,
      "io.monix" %% "minitest" % "2.9.2"))
