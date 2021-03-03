val currentScalaVersion = "2.13.5"
val monixVersion        = "3.3.0"

name := "monix-mdc"

description := "Monix support for MDC using TaskLocal"

scalaVersion := currentScalaVersion

crossScalaVersions := Seq("2.12.11", currentScalaVersion)

scalacOptions in Test in ThisBuild ++= Seq("-Yrangepos")

organization := "org.mdedetrich"

homepage := Some(url("https://github.com/mdedetrich/monix-mdc"))
scmInfo := Some(ScmInfo(url("https://github.com/mdedetrich/monix-mdc"), "git@github.com:mdedetrich/monix-mdc.git"))

developers := List(
  Developer("mdedetrich", "Oleg Pyzhcov", "http://olegpy.com", url("https://github.com/oleg-py")),
  Developer("mdedetrich", "Matthew de Detrich", "mdedetrich@gmail.com", url("https://github.com/mdedetrich"))
)

licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

publishMavenStyle := true
publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
publishArtifact in Test := false
pomIncludeRepository := (_ => false)

libraryDependencies := Seq(
  "io.monix"       %% "monix-execution" % monixVersion,
  "ch.qos.logback" % "logback-classic"  % "1.2.3",
  "io.monix"       %% "monix"           % monixVersion % Test,
  "org.scalatest"  %% "scalatest"       % "3.1.0" % Test,
  "org.scalacheck" %% "scalacheck"      % "1.14.0" % Test
)

val flagsFor12 = Seq(
  "-Xlint:_",
  "-Ywarn-infer-any",
  "-opt-inline-from:<sources>"
)

val flagsFor13 = Seq(
  "-Xlint:_",
  "-opt-inline-from:<sources>"
)

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 13 =>
      flagsFor13
    case Some((2, n)) if n == 12 =>
      flagsFor12
  }
}

// Since our tests are using a global MDC context and the tests clear the context inbetween runs,
// we need to run them synchronously
parallelExecution in Test in ThisBuild := false
