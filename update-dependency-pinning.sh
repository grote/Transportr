#!/bin/bash

# this will just append new checksums and not remove unnecessary ones

# to clean up the file, remove ./gradle/verification-metadata.xml,
# run the command below and manually (re-)add checksums for missing operating systems windows, osx or linux for aapt2
# checksums can be computed after downloading the respective jars of https://maven.google.com/web/index.html?q=aapt2#com.android.tools.build:aapt2

./gradlew --write-verification-metadata sha256 build
