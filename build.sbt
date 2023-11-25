import java.nio.file.{Files, StandardCopyOption}

ThisBuild / organization := "mx.m-k"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "3.2.0"

ThisBuild / idePackagePrefix := Some("mx.mk.explicits")
Global / excludeLintKeys += idePackagePrefix

lazy val root = (project in file("."))
  .dependsOn(generic, impl3_2, impl3_2_1, impl3_3_0, impl3_3_1)
  .settings(
    name := "explicits",

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },

    publishMavenStyle := true,

    versionScheme := Some("semver-spec"),
    licenses := Seq("Apache 2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://github.com/makkarpov/explicits")),
    organizationName := "Maxim Karpov",
    organizationHomepage := Some(url("https://github.com/makkarpov")),
    scmInfo := Some(ScmInfo(
      browseUrl = url("https://github.com/makkarpov/explicits"),
      connection = "scm:git://github.com/makkarpov/explicits.git"
    )),

    pomExtra := {
      <developers>
        <developer>
          <id>makkarpov</id>
          <name>Maxim Karpov</name>
          <url>https://github.com/makkarpov</url>
        </developer>
      </developers>
    },

    // i'm too lazy to merge them all + impls don't contain any useful docs or sources
    Compile / packageSrc := (generic / Compile / packageSrc).value,
    Compile / packageDoc := (generic / Compile / packageDoc).value,

    Compile / packageBin := Packager.combineJars(
      // output:
      target.value / ("explicits_3-" + version.value + ".jar"),

      // inputs:
      (generic / Compile / packageBin).value,
      (impl3_2 / Compile / packageBin).value,
      (impl3_2_1 / Compile / packageBin).value,
      (impl3_3_0 / Compile / packageBin).value,
      (impl3_3_1 / Compile / packageBin).value
    )
  )

val subprojectSettings = Seq(
  publishArtifact := false,
  publishTo := None,
  publish / skip := true
)

val compilerImplSettings = subprojectSettings ++ Seq(
  name := "impl-sc" + scalaVersion.value,
  Compile / scalaSource := baseDirectory.value,
  idePackagePrefix := Some((ThisBuild / idePackagePrefix).value.get + ".sc" +
    scalaVersion.value.stripPrefix("3.").stripSuffix(".0").replace('.', '_')),

  libraryDependencies ++= Seq(
    "org.scala-lang" %% "scala3-compiler" % scalaVersion.value % Provided
  )
)

lazy val generic = (project in file("generic"))
  .settings(subprojectSettings)
  .settings(
    name := "explicits-generic",
    scalaVersion := "3.2.0" // must be lowest of supported versions
  )

lazy val impl3_2 = (project in file("impl/scala-3.2.0"))
  .dependsOn(generic)
  .settings(compilerImplSettings)
  .settings(scalaVersion := "3.2.0")

lazy val impl3_2_1 = (project in file("impl/scala-3.2.1"))
  .dependsOn(generic, impl3_2)
  .settings(compilerImplSettings)
  .settings(scalaVersion := "3.2.1")

lazy val impl3_3_0 = (project in file("impl/scala-3.3.0"))
  .dependsOn(generic, impl3_2)
  .settings(compilerImplSettings)
  .settings(scalaVersion := "3.3.0")

lazy val impl3_3_1 = (project in file("impl/scala-3.3.1"))
  .dependsOn(generic, impl3_2, impl3_3_0)
  .settings(compilerImplSettings)
  .settings(scalaVersion := "3.3.1")

lazy val playground = (project in file("playground"))
  .dependsOn(root)
  .settings(subprojectSettings)
  .settings(
    name := "playground",
    Compile / scalaSource := baseDirectory.value / "src",
    scalaVersion := "3.3.1"
  )

commands += Command.command("prepareTest") { st =>
  val extracted = Project.extract(st)
  val (nst, ret) = extracted.runTask(root / Compile / packageBin, st)
  val baseDir = st.getSetting(baseDirectory).get

  val libDir = baseDir / "test" / "lib"
  val copyDst = libDir / "explicits-build.jar"

  val log = st.getSetting(sLog).get
  log.info("Copying built JAR file:")
  log.info(" from: " + ret.getAbsolutePath)
  log.info(" to:   " + copyDst.getAbsolutePath)

  if (!libDir.exists()) {
    Files.createDirectories(libDir.toPath)
  }

  Files.copy(ret.toPath, copyDst.toPath, StandardCopyOption.REPLACE_EXISTING)

  nst
}
