# IHMC CI

Gradle plugin for running groups of tests with complex runtime requirements.

**IMPORTANT: This plugin assumes the use of the ihmc-build plugin!!!**

### Features

- Easily define and run categories of tests
- Uses the standard `gradle test` task
- IDE support for running tests in parallel
- Built-in allocation testing category to test realtime safety
- Built on JUnit 5
- Load resources from file or classpath, using `resource.dir` JVM property
- Know if you're running on CI server with `runningOnCIServer` JVM property

### Download

```kotlin
plugins {
   id("us.ihmc.ihmc-build") version "0.15.1"
   id("us.ihmc.ihmc-ci") version "1.0.8"
}
```

### User Guide

This plugin defines a concept of `categories`. Categories are communicated via the `category` Gradle
property (i.e. `gradle test -Pcategory=fast`)and are used to set up a test process to run tests based on tags, parallel
execution settings, and JVM arguments.

#### Built in categories

The default settings can be scaled via the `cpuThreads` property (i.e. `-PcpuThreads=8`). The default value is `8`.

|Category|Configuration|Summary|
|---|---|---|
|`fast`|`classesPerJVM = 0 // no limit`<br>`maxJVMs = 2`<br>`maxParallelTests = 4`|Run untagged tests as fast as possible.<br>Assume no special runtime requirements.|
|`allocation`|`classesPerJVM = 0`<br>`maxJVMs = 2`<br>`maxParallelTests = 1`<br>`includeTags += "allocation"`<br>`jvmArgs += getAllocationAgentJVMArg()`|Run only 1 test per JVM process so allocations don't overlap.<br>Uses provided special accessor, `allocationAgentJVMArg`,<br>to get `-javaagent:[..]java-allocation-instrumenter[..].jar`|
|`scs`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 1`<br>`includeTags += "scs"`<br>`jvmProperties.putAll(getScsDefaultJVMProps())`<br>`minHeapSizeGB = 6`<br>`maxHeapSizeGB = 8`|Run SCS tests.<br>(Will eventually move SCS Gradle plugin)<br>These are the default settings for SCS. Accessible via `getSCSDefaultJVMArgs()`.|
|`video`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 1`<br>`includeTags += "video"`<br>`jvmProperties["create.scs.gui"] = "true"`<br>`jvmProperties["show.scs.windows"] = "true"`<br>`jvmProperties["create.videos.dir"] = "/home/shadylady/bamboo-videos/"`<br>`jvmProperties["show.scs.yographics"] = "true"`<br>`jvmProperties["java.awt.headless"] = "false"`<br>`jvmProperties["create.videos"] = "true"`<br>`jvmProperties["openh264.license"] = "accept"`<br>`jvmProperties["disable.joint.subsystem.publisher"] = "true"`<br>`jvmProperties["scs.dataBuffer.size"] = "8142"`<br>`minHeapSizeGB = 6`<br>`maxHeapSizeGB = 8`|Run SCS video recordings.<br>(Will eventually move SCS Gradle plugin)|
 
Default SCS properties:
```kotlin
mapOf("create.scs.gui" to "false",
      "show.scs.windows" to "false",
      "show.scs.yographics" to "false",
      "java.awt.headless" to "true",
      "create.videos" to "false",
      "openh264.license" to "accept",
      "disable.joint.subsystem.publisher" to "true",
      "scs.dataBuffer.size" to "8142")
```
 
#### Custom categories

In your project's `build.gradle`:
```groovy
categories.create("slow-scs")
{
   classesPerJVM = 1   // default: 1
   maxJVMs = 2   // default: 2
   maxParallelTests = 1   // default: 4
   excludeTags += "none"   // default: all
   includeTags += ["slow", "scs"]   // default: empty
   jvmProperties += "some.arg" to "value"   // default: empty List
   jvmArguments += "-Dsome.arg=value"   // default: empty List
   minHeapSizeGB = 1   // default: 1
   maxHeapSizeGB = 8   // default: 4
}
```

Special JVM argument accessors:

- `getAllocationAgentJVMArg()` - Find location of `-javaagent:[..]java-allocation-instrumenter[..].jar`
- `getSCSDefaultJVMArgs()` - Default settings for SCS

The plugin will do a few other things too:

- If `-PrunningOnCIServer=true`, set `-DrunningOnCIServer=true`.
- Pass `-Dresources.dir` that points to your resources folder on disk.
- Pass `-ea` JVM argument to enable JVM assertions

#### Examples

```bash
$ gradle test -Pcategory=fast   // run fast tests
```

```java
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Test
public void fastTest() { ... }   // runs in fast category

@Tag("allocation")
@Test
public void allocationTest() { ... }   // runs in allocation category
```

#### Running tests locally in your IDE

It is possible to run tests in parallel in your IDE, just pass these VM arguments:

```
-Djunit.jupiter.execution.parallel.enabled=true
-Djunit.jupiter.execution.parallel.config.strategy=dynamic
```
