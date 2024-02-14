import org.scalajs.linker.interface.ModuleSplitStyle

lazy val scrpgHelper = project.in(file("."))
    .enablePlugins(ScalaJSPlugin)
    .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
    .settings(
      scalaVersion := "3.3.1",
      scalaJSUseMainModuleInitializer := true,
      scalaJSLinkerConfig ~= {
        _.withModuleKind(ModuleKind.ESModule)
            .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("scprgHelper")))
      },
      libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.4.0",
      libraryDependencies += "com.raquo" %%% "laminar" % "15.0.1",
      libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test,
      externalNpm := baseDirectory.value,
    )
