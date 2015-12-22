#!/bin/sh

# OSMBonusPack: don't fail on lintErrors
echo "
android { lintOptions { abortOnError false } }" >> sublibs/OSMBonusPack/OSMBonusPack/build.gradle

# OSMBonusPack: don't use maven stuff (Travis fails)
sed -i "/apply from: '..\/maven.gradle'/d" sublibs/OSMBonusPack/OSMBonusPack/build.gradle


# PTE: add non-upstream providers
cd sublibs/public-transport-enabler/enabler
git checkout -- src/de/schildbach/pte/NetworkId.java
sed -i "s#SYDNEY, MET#SYDNEY, HSL, NZ, SPAIN, BR, MET#" src/de/schildbach/pte/NetworkId.java

