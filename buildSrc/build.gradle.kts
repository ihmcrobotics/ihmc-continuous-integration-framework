import com.gradle.publish.MavenCoordinates
import com.gradle.publish.PluginConfig
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

plugins {
   `java-gradle-plugin`
   kotlin("jvm") version "1.2.61"
   id("com.gradle.plugin-publish") version "0.9.9"
}

group = "us.ihmc"
version = "2.4"

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
   compile(gradleApi())
   compile(kotlin("stdlib-jdk8"))
   compile("org.junit.platform:junit-platform-console:1.3.1")
   compile("org.junit.jupiter:junit-jupiter-engine:5.3.1")
   compile("com.github.kittinunf.fuel:fuel:1.15.1")
   compile("org.json:json:20180813")
}

gradlePlugin {
   plugins {
      register("ihmcCIPlugin") {
         id = project.group as String + "." + project.name
         displayName = "IHMC CI"
         implementationClass = "us.ihmc.ci.IHMCCIPlugin"
         description = "Continuous integration tools for IHMC Robotics."
      }
   }
}

pluginBundle {
   website = "https://github.com/ihmcrobotics/ihmc-ci"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-ci"
   description = "Continuous integration tools for IHMC Robotics."
   tags = listOf("ci", "continuous", "integration", "ihmc", "robotics")

   plugins.register("ihmcCIPlugin") {
      id = project.group as String + "." + project.name
      version = project.version as String
      displayName = "IHMC CI"
   }

   mavenCoordinates(closureOf<MavenCoordinates> {
      groupId = project.group as String
      artifactId = project.name
      version = project.version as String
   })
}