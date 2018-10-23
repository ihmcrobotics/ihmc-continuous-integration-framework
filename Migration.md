# Migration Guide


## In place migration

Add empty generateTestSuites task

## Code Migration

Add ihmc-ci plugin

remove testSuites block

### Upgrade to ihmc-commons 0.25.0


### Switch all imports
```
Remove these:
\Rimport\s+us\.ihmc\.continuousIntegration\.IntegrationCategory;
\Rimport\s+us\.ihmc\.continuousIntegration\.ContinuousIntegrationAnnotations\.ContinuousIntegrationPlan;
\Rimport\s+us\.ihmc\.continuousIntegration\.ContinuousIntegrationAnnotations\.ContinuousIntegrationTest;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;

Add these:
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;

```



### Switch all annotations:
```
Remove these:
@ContinuousIntegrationPlan(categories = {IntegrationCategory.FAST})
@ContinuousIntegrationTest(estimatedDuration = 46.9)
@Test(timeout = 230000)
@Ignore


Add these:
@Tag("slow")
@Test
@Disabled
```

@Test(timeout = 30000, expected = RuntimeException.class)
preserve timeouts somehow? maybe in a comment
preserve expected exception in comment


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

