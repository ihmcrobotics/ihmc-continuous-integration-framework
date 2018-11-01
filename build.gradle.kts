plugins {
   id("us.ihmc.ihmc-build") version "0.15.1"
   id("us.ihmc.ihmc-ci")
}

ihmc {
   group = "us.ihmc"
   version = "1.1.6"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-ci"
   openSource = true
   maintainer = "Duncan Calvert"

   configureDependencyResolution()
   resourceDirectory("junitfive-test", "builds")
   configurePublications()
}

categories.create("slow")
{
   includeTags += "slow"
}

val junit = "junit:junit:4.12"
val javaparser = "com.github.javaparser:javaparser-core:3.6.20"
val ihmcCommonsTesting = "us.ihmc:ihmc-commons-testing:0.24.0"
val jacksonCore = "com.fasterxml.jackson.core:jackson-core:2.9.6"
val jacksonDatabind = "com.fasterxml.jackson.core:jackson-databind:2.9.6"



ihmc.sourceSetProject("junitfive-test").dependencies {
   compile("org.apache.commons:commons-lang3:3.8.1")
   compile("commons-io:commons-io:2.6")
   compile("us.ihmc:ihmc-commons:0.24.0")
}
