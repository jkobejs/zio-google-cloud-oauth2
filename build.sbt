import microsites._

val mainScala = "2.12.10"
val allScala  = Seq("2.13.1", mainScala)

name := "zio-google-cloud-oauth2"

inThisBuild(
  List(
    organization := "io.github.jkobejs",
    homepage := Some(url("https://github.com/jkobejs/zio-google-cloud-oauth2")),
    organizationName := "Josip Grgurica and Contributors",
    startYear := Some(2019),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "jkobejs",
        "Josip Grgurica",
        "josip.grgurica@gmail.com",
        url("https://github.com/jkobejs")
      )
    ),
    scalaVersion := mainScala,
    crossScalaVersions := allScala
  )
)

lazy val root = (project in file("."))
  .aggregate(core, http4s, sttp, docs)

lazy val core = (project in file("core"))
  .settings(commonSettingsForModule("zio-google-cloud-oauth2-core"))
  .settings(scalacOptions ++= scalacOptionsForVersion(scalaVersion.value))
  .settings(libraryDependencies ++= libraryDependenciesForVersion(scalaVersion.value))
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](
      name,
      version,
      "serviceAccountKeyPath" -> sys.env.getOrElse("SERVICE_ACCOUNT_KEY_PATH", ""),
      "oauthClientKeyPath"    -> sys.env.getOrElse("OAUTH_CLIENT_KEY_PATH", "")
    ),
    buildInfoPackage := "io.github.jkobejs.zio.google.cloud.oauth2"
  )
  .enablePlugins(BuildInfoPlugin)

lazy val http4s = (project in file("http4s"))
  .settings(commonSettingsForModule("zio-google-cloud-oauth2-http4s"))
  .settings(scalacOptions ++= scalacOptionsForVersion(scalaVersion.value))
  .settings(
    libraryDependencies ++= Seq(
      library.http4sCirce,
      library.http4sBlazeClient,
      library.http4sDsl         % Test,
      library.http4sBlazeServer % Test
    )
  )
  .dependsOn(core % "test->test;compile->compile")

lazy val sttp = (project in file("sttp"))
  .settings(moduleName := "zio-google-cloud-oauth2-sttp")
  .settings(noPublishSettings)
  .dependsOn(core)

lazy val docs = (project in file("docs"))
  .settings(moduleName := "zio-google-cloud-oauth-docs")
  .settings(noPublishSettings)
  .settings(docsSettings)
  .enablePlugins(MicrositesPlugin)
  .dependsOn(core, http4s, sttp)

lazy val docsSettings = Seq(
  micrositeName := "zio-google-cloud-oauth2",
  micrositeDescription := "zio-google-cloud-oauth2",
  micrositeTwitterCreator := "@jkobejs",
  micrositeConfigYaml := ConfigYml(
    yamlCustomProperties = Map(
      "tsecVersion"             -> libraryVersion.tsec,
      "http4sVersion"           -> libraryVersion.http4s,
      "circeVersion"            -> libraryVersion.circe,
      "zioVersion"              -> libraryVersion.zio,
      "zioMacrosVersion"        -> libraryVersion.zioMacros,
      "betterMonadicForVersion" -> libraryVersion.betterMonadicFor,
      "zioGoogleCloudOauth2Version" -> dynverGitDescribeOutput.value
        .map(_.ref.value.tail)
        .getOrElse(throw new Exception("There's no output from dynver!"))
    )
  ),
  micrositeAuthor := "Josip Grgurica",
  micrositeCompilingDocsTool := WithMdoc,
  micrositeGithubOwner := "jkobejs",
  micrositeGithubRepo := "zio-google-cloud-oauth2",
  micrositeBaseUrl := "zio-google-cloud-oauth2",
  micrositePushSiteWith := GitHub4s,
  micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
  micrositeDocumentationUrl := "server-2-server",
  micrositeTwitterCreator := "@jkobejs",
  includeFilter in Jekyll := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.md"
)

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  skip in publish := true
)

def commonSettingsForModule(name: String) = Seq(
  moduleName := name,
  testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  libraryDependencies ++= Seq(
    library.tsecCommon,
    library.tsecJWTSig,
    library.zio,
    library.zioInteropCats,
    library.zioMacros,
    library.zioMacrosTest,
    library.simulacrum,
    library.magnolia,
    library.fs2IO,
    compilerPlugin(library.betterMonadicFor),
    library.zioTest    % Test,
    library.zioTestSbt % Test
  ),
  // Skip scaladocs
  sources in (Compile, doc) := Seq()
)

def libraryDependenciesForVersion(scalaVersion: String) =
  (CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, x)) if x <= 12 =>
      Seq(
        compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.full))
      )
    case _ => Nil
  })

def scalacOptionsForVersion(scalaVersion: String): Seq[String] = {
  val defaultOpts = Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-explaintypes",
    "-Yrangepos",
    "-feature",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:existentials",
    "-unchecked",
    "-Xlint:_,-type-parameter-shadow",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:patvars,-implicits",
    "-Ywarn-value-discard"
  )

  val versionOpts: Seq[String] = CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 12)) =>
      Seq(
        "-Xsource:2.13",
        "-Yno-adapted-args",
        "-Ypartial-unification",
        "-Ywarn-extra-implicit",
        "-Ywarn-inaccessible",
        "-Ywarn-infer-any",
        "-Ywarn-nullary-override",
        "-Ywarn-nullary-unit",
        "-opt-inline-from:<source>",
        "-opt-warnings",
        "-opt:l:inline"
      )
    case Some((2, 13)) =>
      Seq("-Ymacro-annotations")
    case _ => Nil
  }
  defaultOpts ++ versionOpts
}

lazy val libraryVersion = new {
  val tsec             = "0.2.0-M1"
  val http4s           = "0.21.32"
  val circe            = "0.12.3"
  val zio              = "1.0.0-RC17"
  val zioInteropCats   = "2.0.0.0-RC10"
  val zioMacros        = "0.6.0"
  val betterMonadicFor = "0.3.1"
  val simulacrum       = "1.0.0"
  val magnolia         = "0.12.0"
  val cats             = "2.0.0-RC1"
  val fs2              = "2.0.1"
}

lazy val library =
  new {
    val tsecCommon        = "io.github.jmcardon" %% "tsec-common"         % libraryVersion.tsec
    val tsecJWTSig        = "io.github.jmcardon" %% "tsec-jwt-sig"        % libraryVersion.tsec
    val http4sBlazeClient = "org.http4s"         %% "http4s-blaze-client" % libraryVersion.http4s
    val http4sCirce       = "org.http4s"         %% "http4s-circe"        % libraryVersion.http4s
    val http4sDsl         = "org.http4s"         %% "http4s-dsl"          % libraryVersion.http4s
    val http4sBlazeServer = "org.http4s"         %% "http4s-blaze-server" % libraryVersion.http4s
    val circeCore         = "io.circe"           %% "circe-core"          % libraryVersion.circe
    val circeGeneric      = "io.circe"           %% "circe-generic"       % libraryVersion.circe
    val zio               = "dev.zio"            %% "zio"                 % libraryVersion.zio
    val zioInteropCats    = "dev.zio"            %% "zio-interop-cats"    % libraryVersion.zioInteropCats
    val zioTest           = "dev.zio"            %% "zio-test"            % libraryVersion.zio
    val zioTestSbt        = "dev.zio"            %% "zio-test-sbt"        % libraryVersion.zio
    val zioMacros         = "dev.zio"            %% "zio-macros-core"     % libraryVersion.zioMacros
    val zioMacrosTest     = "dev.zio"            %% "zio-macros-test"     % libraryVersion.zioMacros
    val betterMonadicFor  = "com.olegpy"         %% "better-monadic-for"  % libraryVersion.betterMonadicFor
    val simulacrum        = "org.typelevel"      %% "simulacrum"          % libraryVersion.simulacrum
    val magnolia          = "com.propensive"     %% "magnolia"            % libraryVersion.magnolia
    val fs2IO             = "co.fs2"             %% "fs2-io"              % libraryVersion.fs2

  }

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
