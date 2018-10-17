# IHMC CI

Tools for running large amounts of tests with special requirements.

#### Tutorial

This plugin defines a concept of `categories`. Categories are communicated via the `category` Gradle
property and are used to set up a test process to run sets of tests based on tags, with specific parallel
execution settings and Java properties.

Built in categories:

|Category|Configuration|Summary|
|---|---|---|
|`fast`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 4`<br>`excludeTags = all`|Run untagged tests as fast as possible.<br>Assume no special runtime requirements.|
|`allocation`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 1`<br>`includeTags = allocation`<br>`jvmArgs += allocationAgentJVMArg`|Run only 1 test per JVM process so allocations don't overlap.<br>Uses provided special accessor, `allocationAgentJVMArg`,<br>to get `-javaagent:[..]java-allocation-instrumenter[..].jar`|
|`scs`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 1`<br>`includeTags = scs`<br>`jvmArgs += -Dcreate.scs.gui=false`<br>`jvmArgs += -Dshow.scs.windows=false`<br>`jvmArgs += -Dshow.scs.yographics=false`<br>`jvmArgs += -Djava.awt.headless=true`<br>`jvmArgs += -Dcreate.videos=false`<br>`jvmArgs += -Dopenh264.license=accept`<br>`jvmArgs += -Ddisable.joint.subsystem.publisher=true`<br>`jvmArgs += -Dscs.dataBuffer.size=8142`|Run SCS tests.<br>(Will eventually move SCS Gradle plugin)|
|`video`|`classesPerJVM = 1`<br>`maxJVMs = 2`<br>`maxParallelTests = 1`<br>`includeTags = video`<br>`jvmArgs += -Dcreate.scs.gui=true`<br>`jvmArgs += -Dshow.scs.windows=true`<br>`jvmArgs += -Dcreate.videos.dir=/home/shadylady/bamboo-videos/`<br>`jvmArgs += -Dshow.scs.yographics=true`<br>`jvmArgs += -Djava.awt.headless=false`<br>`jvmArgs += -Dcreate.videos=true`<br>`jvmArgs += -Dopenh264.license=accept`<br>`jvmArgs += -Ddisable.joint.subsystem.publisher=true`<br>`jvmArgs += -Dscs.dataBuffer.size=8142`|Run SCS video recordings.<br>(Will eventually move SCS Gradle plugin)|
Note: The above numbers are assuming 8 CPU threads per job.
 
It is possible to set up custom categories in your project's build.gradle.

The plugin will do a few other things too:

- If `-PrunningOnCIServer=true`, set `-Drunning.on.ci.server=true`.
- Pass `-Dresources.dir` that points to your resources folder on disk.

#### Examples

```bash
$ gradle test -Pcategory=fast   // run fast tests
```

```
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Test
public void fastTest() { ... } // runs in fast category

@Tag("allocation")
@Test
public void allocationTest() { ... } // runs in allocation category
```

#### Implementation notes

- This plugin assumes use of ihmc-build?

#### Publish to Bintray

`gradle publish -PpublishUrl=ihmcRelease`

#### Publish to Gradle plugins site

`gradle publishPlugins -PpascalCasedName=IHMCCIPlugin -PkebabCasedName=ihmc-ci-plugin -PextraSourceSets=[]`
