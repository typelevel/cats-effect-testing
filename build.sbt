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

ThisBuild / baseVersion := "1.4"
ThisBuild / strictSemVer := true

ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"

ThisBuild / startYear := Some(2020)
ThisBuild / endYear := Some(2021)

ThisBuild / developers := List(
  Developer("djspiewak", "Daniel Spiewak", "@djspiewak", url("https://github.com/djspiewak")))

ThisBuild / crossScalaVersions := Seq("3.1.0", "2.12.15", "2.13.7")

ThisBuild / githubWorkflowTargetBranches := Seq("series/1.x")

ThisBuild / homepage := Some(url("https://github.com/djspiewak/cats-effect-testing"))

ThisBuild / startYear := Some(2020)
ThisBuild / endYear := Some(2021)

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/djspiewak/cats-effect-testing"),
    "git@github.com:djspiewak/cats-effect-testing.git"))

val CatsEffectVersion = "3.3.1"

lazy val root = project
  .in(file("."))
  .aggregate(
    core.jvm,
    core.js,
    specs2.jvm,
    specs2.js,
    utest.jvm,
    utest.js,
    minitest.jvm,
    minitest.js,
    scalatest.jvm,
    scalatest.js)
  .enablePlugins(NoPublishPlugin)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .in(file("core"))
  .settings(
    name := "cats-effect-testing-core",
    mimaPreviousArtifacts ~= {
      _.filterNot { moduleId =>
        val v = moduleId.revision
        v.startsWith("1.0") || v.startsWith("1.1") || v.startsWith("1.2")
      }
    },
    libraryDependencies += "org.typelevel" %%% "cats-effect" % CatsEffectVersion)

lazy val specs2 = crossProject(JSPlatform, JVMPlatform)
  .in(file("specs2"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-specs2",
    libraryDependencies += ("org.specs2" %%% "specs2-core" % "4.13.1")
      .cross(CrossVersion.for3Use2_13)
      .exclude("org.scala-js", "scala-js-macrotask-executor_sjs1_2.13"))

lazy val scalatest = crossProject(JSPlatform, JVMPlatform)
  .in(file("scalatest"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-scalatest",

    libraryDependencies ++= Seq(
      "org.scalatest"    %%% "scalatest" % "3.2.10"))

lazy val utest = crossProject(JSPlatform, JVMPlatform)
  .in(file("utest"))
  .dependsOn(core)
  .settings(
    name := "cats-effect-testing-utest",

    testFrameworks += new TestFramework("utest.runner.Framework"),

    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion,
      "com.lihaoyi" %%% "utest" % "0.7.10"),

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
