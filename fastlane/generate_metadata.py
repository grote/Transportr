#!/usr/bin/env python3

# Author: Torsten Grote
# License: GPLv3 or later

import os
import shutil
from xml.etree import ElementTree


XML_PATH = '../app/src/main/res'
METADATA_PATH = 'metadata/android'
DEFAULT_LANG = 'en-US'
LANG_MAP = {
    'values': 'en-US',
    'values-ca': 'ca',
    'values-cs': 'cs-CZ',
    'values-de': 'de-DE',
    'values-es': 'es-ES',
    'values-eu': 'eu-ES',
    'values-fr': 'fr-FR',
    'values-hu': 'hu-HU',
    'values-it': 'it-IT',
    'values-ja': 'ja-JP',
    'values-nl': 'nl-NL',
    'values-pl': 'pl-PL',
    'values-pt-rBR': 'pt-BR',
    'values-ta': 'ta-IN',
    'values-tr': 'tr-TR',
}
PATH = os.path.dirname(os.path.realpath(__file__))


def main():
    path = os.path.join(PATH, XML_PATH)
    for entry in os.listdir(path):
        directory = os.path.join(path, entry)
        if not os.path.isdir(directory) or entry not in LANG_MAP.keys():
            continue
        strings_file = os.path.join(directory, 'strings.xml')
        if not os.path.isfile(strings_file):
            print("Error: %s does not exist" % strings_file)
            continue

        print()
        print(LANG_MAP[entry])
        print("Parsing %s..." % strings_file)
        e = ElementTree.parse(strings_file).getroot()
        short_desc = e.find('.//string[@name="google_play_short_desc"]')
        full_desc = e.find('.//string[@name="google_play_full_desc"]')
        if short_desc is None or full_desc is None:
            print("Warning: Skipping %s because of incomplete translation" % entry)
            continue
        save_file(short_desc.text, LANG_MAP[entry], 'short_description.txt')
        save_file(full_desc.text, LANG_MAP[entry], 'full_description.txt')


def save_file(text, directory, filename):
    directory_path = os.path.join(PATH, METADATA_PATH, directory)
    if not os.path.exists(directory_path):
        os.makedirs(directory_path)
    if filename == 'short_description.txt':
        limit = 80
    else:
        limit = 0
    text = clean_text(text, limit)
    check_title(directory_path)
    file_path = os.path.join(directory_path, filename)
    print("Writing %s..." % file_path)
    with open(file_path, 'w') as f:
        f.write(text)


def clean_text(text, limit=0):
    text = text.replace('\\\'', '\'')
    if limit != 0 and len(text) > limit:
        print("Warning: Short Description longer than 80 characters, truncating...")
        text = text[:limit]
    return text


def check_title(directory):
    title_path = os.path.join(directory, 'title.txt')
    if os.path.exists(title_path):
        return
    default_title_path = os.path.join(directory, '..', DEFAULT_LANG, 'title.txt')
    shutil.copy(default_title_path, title_path)


if __name__ == "__main__":
    main()
