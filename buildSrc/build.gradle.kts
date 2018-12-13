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

gradlePlugin {
   plugins.register(project.name) {
      id = project.group as String + "." + project.name
      implementationClass = "us.ihmc.ci.IHMCCIPlugin"
      displayName = "IHMC CI"
      description = "Continuous integration tools for IHMC Robotics."
   }
}

pluginBundle {
   website = "https://github.com/ihmcrobotics/ihmc-ci"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-ci"
   description = "Continuous integration tools for IHMC Robotics."
   tags = listOf("ci", "continuous", "integration", "ihmc", "robotics")

   plugins.getByName(project.name) {
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