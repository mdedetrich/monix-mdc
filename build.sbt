val currentScalaVersion = "2.11.12"

name := "monix-mdc"

description := "Monix support for MDC using TaskLocal"

updateOptions := updateOptions.value.withLatestSnapshots(false)

resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

scalaVersion := "2.11.12"

crossScalaVersions := Seq(currentScalaVersion, "2.12.8")

scalacOptions in Test in ThisBuild ++= Seq("-Yrangepos")

organization := "org.mdedetrich"

homepage := Some(url("https://github.com/mdedetrich/webmodels"))
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
  "io.monix"       %% "monix"          % "3.0.0-RC2-SNAPSHOT-9e79718-SNAPSHOT",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest"  %% "scalatest"      % "3.0.5" % Test
)

val flagsFor11 = Seq(
  "-Xlint:_",
  "-Yconst-opt",
  "-Ywarn-infer-any",
  "-Yclosure-elim",
  "-Ydead-code",
  "-Xsource:2.12" // required to build case class construction
)

val flagsFor12 = Seq(
  "-Xlint:_",
  "-Ywarn-infer-any",
  "-opt-inline-from:<sources>"
)

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, n)) if n >= 12 =>
      flagsFor12
    case Some((2, n)) if n == 11 =>
      flagsFor11
  }
}
