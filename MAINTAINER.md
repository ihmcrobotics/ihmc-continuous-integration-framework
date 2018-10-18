# Maintainer Guide

### Making a release

1. Increment version numbers in `build.gradle.kts` and `buildSrc/build.gradle.kts`
1. Upload to Bintray:
   ```
   ../ihmc-ci $ gradle publish -PpublishUrl=ihmcRelease
   ```
1. Publish to Gradle plugins site:
   ```
   ../ihmc-ci/buildSrc $ gradle publishPlugins
   ```
1. Click 'Publish' in Bintray: [https://bintray.com/ihmcrobotics/maven-release/ihmc-ci](https://bintray.com/ihmcrobotics/maven-release/ihmc-ci)
1. Tag commit with `:bookmark: X.X.X`
1. Document release notes at [https://github.com/ihmcrobotics/ihmc-ci/releases](https://github.com/ihmcrobotics/ihmc-ci/releases)
