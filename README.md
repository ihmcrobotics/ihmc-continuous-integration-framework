# IHMC CI

Parallelization tools for long-running robot simulation tests.

## Publishing

With gradle.properties:
```groovy
publishUrl = local
pascalCasedName = IHMCCIPlugin
kebabCasedName = ihmc-ci-plugin
extraSourceSets = []
#pascalCasedName = IHMCCI
#kebabCasedName = ihmc-ci
#extraSourceSets = ["core-api", "generator", "generator-test"]
groupDependencyVersion = source
compositeSearchHeight = 0
excludeFromCompositeBuild = true
```

Run:

`gradle publishPlugins`

With gradle.properties:
```groovy
publishUrl = local
#pascalCasedName = IHMCCIPlugin
#kebabCasedName = ihmc-ci-plugin
#extraSourceSets = []
pascalCasedName = IHMCCI
kebabCasedName = ihmc-ci
extraSourceSets = ["core-api", "generator", "generator-test"]
groupDependencyVersion = source
compositeSearchHeight = 0
excludeFromCompositeBuild = true
```

Run:

`gradle publish -PpublishUrl=ihmcRelease`
