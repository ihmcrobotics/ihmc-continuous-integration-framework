# IHMC CI

Tools for running large amounts of tests with special requirements.

### User Guide

This plugin defines a concept of `categories`. Categories are communicated via the `category` Gradle
property (i.e. `gradle test -Pcategory=fast`)and are used to set up a test process to run tests based on tags, parallel
execution settings, and JVM arguments.

#### Built in categories

The default settings can be scaled via the `cpuThreads` property (i.e. `-PcpuThreads=8`). The default value is `8`.

|Category|Configuration|Summary|
|---|---|---|
|`fast`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 4`<br>`excludeTags = ["all"]`|Run untagged tests as fast as possible.<br>Assume no special runtime requirements.|
|`allocation`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 1`<br>`includeTags = ["allocation"]`<br>`jvmArgs += getAllocationAgentJVMArg()`|Run only 1 test per JVM process so allocations don't overlap.<br>Uses provided special accessor, `allocationAgentJVMArg`,<br>to get `-javaagent:[..]java-allocation-instrumenter[..].jar`|
|`scs`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 1`<br>`includeTags = ["scs"]`<br>`jvmArgs += "-Dcreate.scs.gui=false"`<br>`jvmArgs += "-Dshow.scs.windows=false"`<br>`jvmArgs += "-Dshow.scs.yographics=false"`<br>`jvmArgs += "-Djava.awt.headless=true"`<br>`jvmArgs += "-Dcreate.videos=false"`<br>`jvmArgs += "-Dopenh264.license=accept"`<br>`jvmArgs += "-Ddisable.joint.subsystem.publisher=true"`<br>`jvmArgs += "-Dscs.dataBuffer.size=8142"`|Run SCS tests.<br>(Will eventually move SCS Gradle plugin)<br>These are the default settings for SCS. Accessible via `getSCSDefaultJVMArgs()`.|
|`video`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 1`<br>`includeTags = ["video"]`<br>`jvmArgs += "-Dcreate.scs.gui=true"`<br>`jvmArgs += "-Dshow.scs.windows=true"`<br>`jvmArgs += "-Dcreate.videos.dir=/home/shadylady/bamboo-videos/"`<br>`jvmArgs += "-Dshow.scs.yographics=true"`<br>`jvmArgs += "-Djava.awt.headless=false"`<br>`jvmArgs += "-Dcreate.videos=true"`<br>`jvmArgs += "-Dopenh264.license=accept"`<br>`jvmArgs += "-Ddisable.joint.subsystem.publisher=true"`<br>`jvmArgs += "-Dscs.dataBuffer.size=8142"`|Run SCS video recordings.<br>(Will eventually move SCS Gradle plugin)|
 
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
   jvmArgs += "-Dsome.arg=value"   // default: empty List
   minHeapSizeGB = 1   // default: 1
   maxHeapSizeGB = 8   // default: 4
}
```

Special JVM argument accessors:

- `getAllocationAgentJVMArg()` - Find location of `-javaagent:[..]java-allocation-instrumenter[..].jar`
- `getSCSDefaultJVMArgs()` - Default settings for SCS

The plugin will do a few other things too:

- If `-PrunningOnCIServer=true`, set `-Drunning.on.ci.server=true`.
- Pass `-Dresources.dir` that points to your resources folder on disk.

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

#### Implementation notes

- This plugin assumes use of ihmc-build?

#### Publish to Bintray

`gradle publish -PpublishUrl=ihmcRelease`

#### Publish to Gradle plugins site

`gradle publishPlugins -PpascalCasedName=IHMCCIPlugin -PkebabCasedName=ihmc-ci-plugin -PextraSourceSets=[]`
