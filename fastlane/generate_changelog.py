#!/usr/bin/env python3

# Author: Torsten Grote
# License: GPLv3 or later

import os
from xml.etree import ElementTree


PATH = os.path.dirname(os.path.realpath(__file__))
XML_PATH = os.path.join(PATH, '../app/src/main/res/xml')
CHANGELOG_PATH = os.path.join(PATH, 'metadata/android/en-US/changelogs')
START_VERSION_CODE = 100
LIMIT = 500


def main():
    changelog_file = os.path.join(XML_PATH, 'changelog_master.xml')
    if not os.path.isfile(changelog_file):
        print("Error: %s does not exist" % changelog_file)
        return

    print("Parsing %s..." % changelog_file)
    root = ElementTree.parse(changelog_file).getroot()
    for release in root:
        if release.tag == "release":
            store_release(release)


def store_release(release):
    version_code = release.attrib['versioncode']
    if int(version_code) < START_VERSION_CODE:
        return
    version_file = os.path.join(CHANGELOG_PATH, version_code + '.txt')
    print("Writing to %s ..." % version_file)
    count = 0
    with open(version_file, 'w') as f:
        for change in release:
            text = '* ' + change.text + '\n'
            count = count + len(text)
            if count > LIMIT:
                print("Warning: Maximum length exceeded. Truncating...")
                text = text[:(LIMIT - count)]
            f.write(text)


if __name__ == "__main__":
    main()
