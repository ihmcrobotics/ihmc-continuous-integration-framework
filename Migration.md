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
id("us.ihmc.ihmc-ci") version "1.2.0"

ihmc-commons
0\.24\.0
0.25.0

junit4
\R[ \t\x0B]+compile[ \t\x0B\S]*junit[ \t\x0B\S]*junit[ \t\x0B\S]*[0-9\.]+"[ \t\x0B\S]*

ihmc-ci-core-api
\R[ \t\x0B]+compile[ \t\x0B\S]*us\.ihmc[ \t\x0B\S]*ihmc-ci-core-api[ \t\x0B\S]*[0-9\.]+"[ \t\x0B\S]*

### Switch all annotations:
```
add Tag import
((import us\.ihmc\.continuousIntegration\.ContinuousIntegrationAnnotations[ \t\x0B\S]*;\s*)+)
$1import org.junit.jupiter.api.Tag;\R

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
[ \t\x0B]*@[a-zA-Z\.\s]*ContinuousIntegration\w{4}\s*\([ \t\x0B\S]*categories[ \t\x0B\S]*\).*\R



\Rimport\s+us\.ihmc\.continuousIntegration\.IntegrationCategory;
\Rimport\s+us\.ihmc\.continuousIntegration\.ContinuousIntegrationAnnotations\.ContinuousIntegrationPlan;
\Rimport\s+us\.ihmc\.continuousIntegration\.ContinuousIntegrationAnnotations\.ContinuousIntegrationTest;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;

@ContinuousIntegrationPlan(categories = {IntegrationCategory.FAST})
@ContinuousIntegrationTest(estimatedDuration = 46.9)
@Test(timeout = 230000)
@Ignore

@Tag("slow")
@Test
@Disabled

@Test(timeout = 30000, expected = RuntimeException.class)
preserve timeouts somehow? maybe in a comment
preserve expected exception in comment
```

### Switch all assertions
```
Remove imports:
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

Add these:
import static org.junit.jupiter.api.Assertions.*;

```

Messages are now the last parameter in assertion methods.


### Useful regexes

@ContinuousIntegrationTest[ \t\x0B\S]+[ \t\x0B]*\)
\R[ \t\x0B]+@ContinuousIntegrationTest\s*\(\s*estimatedDuration[ \t\x0B\S]*=[ \t\x0B0-9\.^A-Z]+\)
\Rimport us\.ihmc\.continuousIntegration\.IntegrationCategory;
\Rimport us\.ihmc\.continuousIntegration\.ContinuousIntegrationAnnotations\.ContinuousIntegrationPlan;
\R\s+@Tag\("fast"\)
@Continuous[ \t\x0B\S]+VIDEO[ \t\x0B\S]*\)

