#!/bin/sh

# PTE: add non-upstream providers
cd public-transport-enabler/enabler
git checkout -- src/de/schildbach/pte/NetworkId.java
sed -i "s#ONTARIO, QUEBEC#ONTARIO, NZ, SPAIN, BR, BRFLORIPA, QUEBEC#" src/de/schildbach/pte/NetworkId.java

