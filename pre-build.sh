#!/bin/sh

# PTE: add non-upstream providers
cd sublibs/public-transport-enabler/enabler
git checkout -- src/de/schildbach/pte/NetworkId.java
sed -i "s#ONTARIO, QUEBEC#ONTARIO, BR, BRFLORIPA, QUEBEC#" src/de/schildbach/pte/NetworkId.java

