import com.gradle.publish.MavenCoordinates

plugins {
   `kotlin-dsl`
   `java-gradle-plugin`
   `maven-publish`
   id("com.gradle.plugin-publish") version "0.10.0"
}

group = "us.ihmc"
version = "5.8"

repositories {
   mavenCentral()
   jcenter()
   maven{
      url = uri("https://dl.bintray.com/ihmcrobotics/maven-release")
   }
}

dependencies {
   compile("org.junit.platform:junit-platform-console:1.5.1")
   compile("org.junit.jupiter:junit-jupiter-engine:5.5.1")
   compile("com.github.kittinunf.fuel:fuel:2.0.1")
   compile("org.json:json:20180813")
}

val pluginDisplayName = "IHMC CI"
val pluginDescription = "Gradle plugin for running groups of tests with varied runtime requirements."
val pluginVcsUrl = "https://github.com/ihmcrobotics/ihmc-ci"
val pluginTags = listOf("ci", "continuous", "integration", "ihmc", "robotics")

gradlePlugin {
   plugins.register(project.name) {
      id = project.group as String + "." + project.name
      implementationClass = "us.ihmc.ci.IHMCCIPlugin"
      displayName = pluginDisplayName
      description = pluginDescription
   }
}

pluginBundle {
   website = pluginVcsUrl
   vcsUrl = pluginVcsUrl
   description = pluginDescription
   tags = pluginTags

   plugins.getByName(project.name) {
      id = project.group as String + "." + project.name
      version = project.version as String
      displayName = pluginDisplayName
      description = pluginDescription
      tags = pluginTags
   }

   mavenCoordinates(closureOf<MavenCoordinates> {
      groupId = project.group as String
      artifactId = project.name
      version = project.version as String
   })
}