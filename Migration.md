# Migration Guide


## In place migration

Add empty generateTestSuites task

## Code Migration

Add ihmc-ci plugin

remove testSuites block

### Upgrade build scripts w/o new ci plugin

id("us.ihmc.ihmc-ci-plugin") version "0.18.0"

### Junit 5 dependencies

plugin
id\("us\.ihmc\.ihmc-ci"\)\s+version\s+"1\.1\.6"
id("us.ihmc.ihmc-ci") version "1.2.1"

ihmc-commons
0\.24\.0
0.25.0

### Switch all annotations:
```
add JUnit 5 imports

replace test import
import org\.junit\.Test[ \t\x0B]*;
import org.junit.jupiter.api.Test;

test
([ \t\x0B]*)(@Test)[ \t\x0B]*\((.*)\)
$1$2// $3

replace beforeeach import
import org\.junit\.Before[ \t\x0B]*;
import org.junit.jupiter.api.BeforeEach;

beforeeach
([ \t\x0B]*)@Before[ \t\x0B]*\R
$1@BeforeEach\R

replace beforeall import
import org\.junit\.BeforeClass[ \t\x0B]*;
import org.junit.jupiter.api.BeforeAll;

beforeall
([ \t\x0B]*)@BeforeClass[ \t\x0B]*\R
$1@BeforeAll\R

replace aftereach import
import org\.junit\.After[ \t\x0B]*;
import org.junit.jupiter.api.AfterEach;

aftereach
([ \t\x0B]*)@After[ \t\x0B]*\R
$1@AfterEach\R

replace afterall import
import org\.junit\.AfterClass[ \t\x0B]*;
import org.junit.jupiter.api.AfterAll;

afterall
([ \t\x0B]*)@AfterClass[ \t\x0B]*\R
$1@AfterAll\R

((import us\.ihmc\.continuousIntegration\.ContinuousIntegrationAnnotations[ \t\x0B\S]*;\s*)+)
$1import org.junit.jupiter.api.Tag;\Rimport org.junit.jupiter.api.Disabled;\R

add Disabled import
(import org\.junit\.jupiter\.api\.Tag[ \t\x0B\S]*;\s*)
$1import org.junit.jupiter.api.Disabled;\R

fast
([ \t\x0B]*)(@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*categories\w*[ \t\x0B\S]+FAST.*\R)
$1@Tag\("fast"\)\R$1$2

slow
([ \t\x0B]*)(@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*categories\w*[ \t\x0B\S]+SLOW.*\R)
$1@Tag\("slow"\)\R$1$2

video
([ \t\x0B]*)(@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*categories\w*[ \t\x0B\S]+VIDEO.*\R)
$1@Tag\("video"\)\R$1$2

ui
([ \t\x0B]*)(@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*categories\w*[ \t\x0B\S]+UI.*\R)
$1@Tag\("ui"\)\R$1$2

flaky
([ \t\x0B]*)(@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*categories\w*[ \t\x0B\S]+FLAKY.*\R)
$1@Tag\("flaky"\)\R$1$2

indev
([ \t\x0B]*)(@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*categories\w*[ \t\x0B\S]+IN_DEVELOPMENT.*\R)
$1@Tag\("in-development"\)\R$1$2

manual
([ \t\x0B]*)(@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*categories\w*[ \t\x0B\S]+MANUAL.*\R)
$1@Tag\("manual"\)\R$1$2

replace ignore keeping message
([ \t\x0B]*)(@Ignore)(.*)\R
$1@Disabled$3\R

exclude
([ \t\x0B]*)(@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*categories\w*[ \t\x0B\S]+EXCLUDE.*\R)
$1@Disabled\R$1$2

remove custom annotations
[ \t\x0B]*@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*\).*\R

delete existing test suites

remove custom imports
import us\.ihmc\.continuousIntegration\.ContinuousIntegrationAnnotations[ \t\x0B\S]*;\s*\R
import us\.ihmc\.continuousIntegration\.IntegrationCategory[ \t\x0B\S]*;\s*\R

remove junit4 annotations
[ \t\x0B]*import[ \t\x0B]+org\.junit\.Ignore[ \t\x0B]*;[ \t\x0B]*\R

```

### Switch all assertions
```
create Assert.java in ihmc-robotics-toolkit-test from included file

switch junit to Assert.java
((import\s*static\s*org\.junit\.Assert[ \t\x0B\S]*;\s*)+)
import static us.ihmc.robotics.Assert.*;\R\R

switch junit framework to Assert.java
((import\s*static\s*junit\.framework\.[ \t\x0B\S\.]*;\s*)+)
import static us.ihmc.robotics.Assert.*;\R\R

switch non-static junit to Assert
((import\s*org\.junit\.Assert[ \t\x0B\S]*;\s*)+)
import us.ihmc.robotics.Assert;\R

add to a few projects
compile group: "us.ihmc", name: "ihmc-robotics-toolkit-test", version: "source"

switch ContinuousIntegrationTools source
import us.ihmc.continuousIntegration.ContinuousIntegrationTools;
import us.ihmc.commons.ContinuousIntegrationTools;

disable on debug thing
a few asserts in footstep planning
a few misc items (<20)

```

### Remove JUnit 4

remove junit4
\R[ \t\x0B]+compile[ \t\x0B\S]*junit[ \t\x0B\S]*junit[ \t\x0B\S]*[0-9\.]+"[ \t\x0B\S]*

remove ihmc-ci-core-api
(\R[ \t\x0B]+)compile[ \t\x0B\S]*us\.ihmc[ \t\x0B\S]*ihmc-ci-core-api[ \t\x0B\S]*[0-9\.]+"[ \t\x0B\S]*\R
$1compile "us.ihmc:ihmc-java-toolkit-test:source"\R
