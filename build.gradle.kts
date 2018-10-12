plugins {
   id("us.ihmc.ihmc-build") version "0.15.1"
   id("us.ihmc.ihmc-ci")
}

ihmc {
   group = "us.ihmc"
   version = "0.20.6"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-ci"
   openSource = true
   maintainer = "Duncan Calvert"

   configureDependencyResolution()
   resourceDirectory("generator-test", "builds")
   resourceDirectory("junitfive-test", "builds")
   configurePublications()
}

val junit = "junit:junit:4.12"
val unirestJava = "com.mashape.unirest:unirest-java:1.4.9"
val commonsLang = "org.apache.commons:commons-lang3:3.8.1"
val commonsIo = "commons-io:commons-io:2.6"
val javaparser = "com.github.javaparser:javaparser-core:3.6.20"
val guava = "com.google.guava:guava:26.0-jre"
val snakeYaml = "org.yaml:snakeyaml:1.23"
val jung2 = "net.sf.jung:jung2:2.0.1"
val ihmcCommons = "us.ihmc:ihmc-commons:0.24.0"
val ihmcCommonsTesting = "us.ihmc:ihmc-commons-testing:0.24.0"
val euclidCore = "us.ihmc:euclid-core:0.6.1"
val jacksonCore = "com.fasterxml.jackson.core:jackson-core:2.9.6"
val jacksonDatabind = "com.fasterxml.jackson.core:jackson-databind:2.9.6"
val jacksonAnnotations = "com.fasterxml.jackson.core:jackson-annotations:2.9.6"
val jgit = "org.eclipse.jgit:org.eclipse.jgit:5.0.3.201809091024-r"
val groovy = "org.codehaus.groovy:groovy:2.3.11"

ihmc.sourceSetProject("core-api").dependencies {
   compile(junit)
}

ihmc.sourceSetProject("generator").dependencies {
   compile(junit)
   compile(ihmc.sourceSetProject("core-api"))
   compile(unirestJava)
   compile(commonsLang)
   compile(javaparser)
   compile(guava)
   compile(snakeYaml)
   compile(jung2)
   compile(commonsIo)
   compile(ihmcCommons)
   compile(euclidCore)
   compile(jacksonDatabind)
   compile(jacksonAnnotations)
   compile(jacksonCore)
   compile(groovy)
}

ihmc.sourceSetProject("generator-test").dependencies {
   compile(ihmc.sourceSetProject("core-api"))
   compile(ihmc.sourceSetProject("generator"))
   compile(javaparser)
   compile(jgit)
   compile(ihmcCommonsTesting)
}

ihmc.sourceSetProject("junitfive-test").dependencies {
   implementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
   runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
   compile(commonsLang)
   compile(commonsIo)
   compile(ihmcCommons)
}

ihmc.sourceSetProject("junitfive-test").tasks.withType<Test> {
    useJUnitPlatform()
}