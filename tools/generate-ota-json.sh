#!/bin/bash
#
# SPDX-FileCopyrightText: 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#
# Generate the LineageOS Updater entry (athena.json) for a built OTA package.
#
# Usage: generate-ota-json.sh <lineage-24.0-*-athena.zip> [release-tag] > athena.json
#
# The Updater compares "datetime" with ro.build.date.utc of the running build, so it is taken
# from the package metadata (post-timestamp) instead of being entered by hand. "id" must be
# unique per build; the SHA-256 of the package is used. The URL points at a GitHub release of
# Xmind404/distribution named after the tag (default: the package file name without .zip).

set -euo pipefail

zip=${1:?usage: $0 <ota.zip> [release-tag]}
filename=$(basename "$zip")
tag=${2:-${filename%.zip}}

metadata=$(unzip -p "$zip" META-INF/com/android/metadata)
datetime=$(sed -n 's/^post-timestamp=//p' <<< "$metadata")
if [ -z "$datetime" ]; then
    echo "post-timestamp missing from $zip metadata" >&2
    exit 1
fi

id=$(sha256sum "$zip" | cut -d' ' -f1)
size=$(stat -c %s "$zip")
url="https://github.com/Xmind404/distribution/releases/download/${tag}/${filename}"

cat <<JSON
{
  "response": [
    {
      "datetime": ${datetime},
      "filename": "${filename}",
      "id": "${id}",
      "romtype": "unofficial",
      "size": ${size},
      "url": "${url}",
      "version": "24.0"
    }
  ]
}
JSON
