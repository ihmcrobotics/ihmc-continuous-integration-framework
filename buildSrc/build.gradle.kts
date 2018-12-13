import com.gradle.publish.MavenCoordinates
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
   kotlin("jvm") version "1.2.61"
   `java-gradle-plugin`
   id("com.gradle.plugin-publish") version "0.10.0"
}

group = "us.ihmc"
version = "4.2"

repositories {
   mavenCentral()
   jcenter()
   maven{
      url = uri("https://dl.bintray.com/ihmcrobotics/maven-release")
   }
}

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

tasks.withType<KotlinJvmCompile> {
   kotlinOptions.jvmTarget = "1.8"
}

dependencies {
   compile(gradleKotlinDsl())
   compile(kotlin("stdlib-jdk8", "1.2.61"))
   compile("org.junit.platform:junit-platform-console:1.3.1")
   compile("org.junit.jupiter:junit-jupiter-engine:5.3.1")
   compile("com.github.kittinunf.fuel:fuel:1.15.1")
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