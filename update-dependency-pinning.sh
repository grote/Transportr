#!/bin/bash

WITNESS=app/witness.gradle

echo "" > $WITNESS
./gradlew -q calculateChecksums | grep -Ev "^(Skipping|Verifying)" | grep -Ev "files-2.1:|caches:transforms-3:|:build-tools:core-lambda-stubs.jar:|:platforms:core-for-system-modules.jar:|-linux.jar:" > $WITNESS

