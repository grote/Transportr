#!/bin/sh

DIR=$PWD

cd sublibs/public-transport-enabler/enabler/
mkdir -p src/de/schildbach/pte/live

if [ ! -f src/de/schildbach/pte/live/Secrets.java ]; then
	cp test/de/schildbach/pte/live/Secrets.java.template src/de/schildbach/pte/live/Secrets.java
fi

# add non-upstream providers
git checkout -- src/de/schildbach/pte/NetworkId.java
sed -i "s#SYDNEY, MET#SYDNEY, HSL, USNY, NZ, SPAIN, MET#" src/de/schildbach/pte/NetworkId.java

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
  compileSdkVersion 21
  buildToolsVersion '21.1.2'

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

