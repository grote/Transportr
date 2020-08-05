---
name: New Release
about: Preparing a new release
title: ''
labels: ''
assignees: ''

---

Here are the steps to follow when preparing a new release. Please check the following boxes with an `x` when done:

* [ ] update PTE to the latest commit at [opentransitmap](https://gitlab.com/opentransitmap/public-transport-enabler/-/commits/master) and run `./update-dependency-pinning.sh`
* [ ] test the app (automatically using AndroidTest as well as manually), paying special attention to changes since the last release
* [ ] fix any bugs if necessary
* [ ] update translations from [Transifex](https://www.transifex.com/) using `tx pull --mode=developer -a` in the root folder (you need proper permissions to do that)
* [ ] add newly supported languages to the two arrays in [`app/src/main/res/values/arrays.xml`](https://github.com/grote/Transportr/blob/master/app/src/main/res/values/arrays.xml#L16)
* [ ] revise the commits to `master` since the last release and add interesting changes (as well as new languages) to the changelog at [`app/src/main/res/xml/changelog_master.xml`](https://github.com/grote/Transportr/blob/master/app/src/main/res/xml/changelog_master.xml), then run `python3 ./fastlane/generate_changelog.py`
* [ ] bump the [`versionCode`](https://github.com/grote/Transportr/blob/master/app/build.gradle#L14) and [`versionName`](https://github.com/grote/Transportr/blob/master/app/build.gradle#L15) in `app/build.gradle`
* [ ] add a last commit updating all dependencies right after the release

Please also refer to #689 as an example.

