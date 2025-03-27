import org.scalajs.linker.interface.ModuleSplitStyle

lazy val scrpgHelper = project.in(file("."))
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
    .settings(
      scalaVersion := "3.3.5",
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= { _.withSourceMap(true) },
      scalaJSLinkerConfig ~= {
        _.withModuleKind(ModuleKind.ESModule)
            .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("scprgHelper")))
      },
      useCoursier := true,
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
      libraryDependencies += "com.raquo" %%% "airstream" % "17.0.0-M6",
      libraryDependencies += "com.raquo" %%% "laminar" % "17.0.0-M6",
      libraryDependencies += "com.raquo" %%% "waypoint" % "8.0.0-M2",
      libraryDependencies += "be.doeraene" %%% "url-dsl" % "0.6.0",
      libraryDependencies += "com.lihaoyi" %%% "upickle" % "2.0.0",
      libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test,
      externalNpm := baseDirectory.value,
    )
