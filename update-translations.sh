#!/bin/bash

# pull translations
tx -d pull --force --mode=developer

# remove empty strings
sed -i "/><\/string>/d" res/values-*/strings.xml
