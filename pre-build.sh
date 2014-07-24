#!/bin/sh

DIR=$PWD

cd sublibs/public-transport-enabler/enabler/
mkdir -p src/de/schildbach/pte/live

cat <<EOF > src/de/schildbach/pte/live/Secrets.java
package de.schildbach.pte.live;

/**
 * @author Andreas Schildbach
 */
public final class Secrets
{
        public static final String SBB_ACCESS_ID = "I have no secret :(";
        public static final String VGN_API_BASE = "I didn't get an API base :(";
}
EOF

mvn clean package -DskipTests

cd $DIR
cp sublibs/public-transport-enabler/enabler/target/public-transport-enabler*.jar libs/public-transport-enabler.jar

##############
### Gradle ###
##############

### Android-PullToRefresh ###

cat > sublibs/Android-PullToRefresh/library/build.gradle <<EOF
apply plugin: 'android-library'

android {
  compileSdkVersion 19
  buildToolsVersion '19.1.0'

  sourceSets {
    main {
      manifest.srcFile 'AndroidManifest.xml'
      java.srcDirs = ['src']
      res.srcDirs = ['res']
    }
  }

  // Do not abort build if lint finds errors
  lintOptions {
    abortOnError false
  }
}

archivesBaseName = 'PullToRefresh'
EOF

