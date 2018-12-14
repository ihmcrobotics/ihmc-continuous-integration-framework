import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

plugins {
   id("us.ihmc.ihmc-build") version "0.15.5"
   id("us.ihmc.ihmc-ci")
   id("us.ihmc.log-tools") version "0.3.0"
   kotlin("jvm") version "1.2.61"
   id("us.ihmc.scs") version "0.3"
}

subprojects {
   this.apply<KotlinPlatformJvmPlugin>()
}

ihmc {
   group = "us.ihmc"
   version = "4.2"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-ci"
   openSource = true
   maintainer = "Duncan Calvert"

   configureDependencyResolution()
   configurePublications()
}

categories.configure("fast").doFirst = { scs.showGui()
   println("HELOOOOOOOOOO THERE") }

categories.configure("allocation")

ihmc.sourceSetProject("junitfive-test").dependencies {
   compile("org.apache.commons:commons-lang3:3.8.1")
   compile("commons-io:commons-io:2.6")
   compile("us.ihmc:ihmc-commons:0.25.0")
   compile("us.ihmc:ihmc-commons-testing:0.25.0")
//   compile("us.ihmc:categories-test:source")  // for testing discovery of external classpath tests
}
