import com.gradle.publish.MavenCoordinates
import com.gradle.publish.PluginConfig

plugins {
   `java-gradle-plugin`
   kotlin("jvm") version "1.2.61"
   id("com.gradle.plugin-publish") version "0.9.9"
}

group = "us.ihmc"
version = "1.0.7"

repositories {
   mavenCentral()
   jcenter()
}

dependencies {
   compile(gradleApi())
   compile(kotlin("stdlib"))
//   runtimeOnly(kotlin("runtime"))
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