#!/bin/sh

DIR=$PWD

cd sublibs/public-transport-enabler/enabler/
mkdir -p src/de/schildbach/pte/live

cp test/de/schildbach/pte/live/Secrets.java.template src/de/schildbach/pte/live/Secrets.java

# add non-upstream providers
sed -i "s#SYDNEY, MET#SYDNEY, HSL, USNY, MET#" src/de/schildbach/pte/NetworkId.java

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

