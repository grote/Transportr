#!/bin/bash

WITNESS=app/witness.gradle

echo "" > $WITNESS
gradle -q calculateChecksums | grep -v '^\(Skipping\|Verifying\)' > $WITNESS

