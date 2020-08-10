import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

plugins {
   kotlin("jvm") version "1.3.41"
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci")
   id("us.ihmc.log-tools")
   id("us.ihmc.scs") version "0.4"
}

subprojects {
   this.apply<KotlinPlatformJvmPlugin>()
}

ihmc {
   group = "us.ihmc"
   version = "4.25"
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
   api("org.apache.commons:commons-lang3:3.9")
   api("commons-io:commons-io:2.6")
   api("us.ihmc:ihmc-commons:0.30.2")
   api("us.ihmc:ihmc-commons-testing:0.30.2")
//   api("us.ihmc:categories-test:source")  // for testing discovery of external classpath tests
}
