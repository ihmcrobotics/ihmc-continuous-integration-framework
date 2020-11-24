plugins {
   id("us.ihmc.ihmc-build") version "0.22.0"
   id("us.ihmc.ihmc-ci")
   id("us.ihmc.log-tools-plugin") version "0.5.0"
   id("us.ihmc.scs") version "0.5"
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

println(junit.jupiterVersion)
println(junit.jupiterApi())
println(allocation.instrumenter())

junitfiveTestDependencies {
   api("org.apache.commons:commons-lang3:3.9")
   api("commons-io:commons-io:2.6")
   api("us.ihmc:ihmc-commons:0.30.2")
   api("us.ihmc:ihmc-commons-testing:0.30.2")
//   api("us.ihmc:categories-test:source")  // for testing discovery of external classpath tests
}
