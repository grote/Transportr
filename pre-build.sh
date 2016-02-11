#!/bin/sh

# PTE: add non-upstream providers
cd sublibs/public-transport-enabler/enabler
git checkout -- src/de/schildbach/pte/NetworkId.java
sed -i "s#SYDNEY, MET#SYDNEY, HSL, NZ, SPAIN, BR, BRFLORIPA, MET#" src/de/schildbach/pte/NetworkId.java

