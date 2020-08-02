#!/bin/bash

WITNESS=app/witness.gradle

echo "" > $WITNESS
./gradlew -q calculateChecksums | grep -Ev "^(Skipping|Verifying)" | grep -Ev "files-2.1:|:build-tools:core-lambda-stubs.jar:|:platforms:android.jar:|-linux.jar:" > $WITNESS

