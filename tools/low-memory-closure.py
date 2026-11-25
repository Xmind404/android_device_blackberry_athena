#!/usr/bin/env python3
#
# SPDX-FileCopyrightText: 2026 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#
# Lists the Android.bp files inside directories skipped by
# PRODUCT_SOURCE_ROOT_DIRS (ATHENA_LOW_MEMORY_BUILD=true) that must be parsed
# anyway, because parsed files reference their defaults, teams or licenses.
# Those references are not covered by ALLOW_MISSING_DEPENDENCIES.
#
# Run from the top of the tree after a build has created
# out/.module_paths/Android.bp.list:
#   python3 device/blackberry/athena/tools/low-memory-closure.py \
#       "$(get_build_var PRODUCT_SOURCE_ROOT_DIRS)"
# Every printed path has to be added (without "-") to PRODUCT_SOURCE_ROOT_DIRS
# in lineage_athena.mk. An empty output means the list is complete.

import re
import sys

roots = sys.argv[1].split()
skipped = [r[1:] for r in roots if r.startswith('-')]
allowed = [r for r in roots if not r.startswith('-')]


def is_skipped(path):
    best = None
    for prefix in skipped + allowed:
        if path.startswith(prefix) and (best is None or len(prefix) > len(best)):
            best = prefix
    return best in skipped


files = [line.strip() for line in open('out/.module_paths/Android.bp.list')]
sources = {}
for f in files:
    try:
        sources[f] = open(f, errors='ignore').read()
    except OSError:
        sources[f] = ''

module_re = re.compile(r'^([a-z_0-9]+)\s*\{(.*?)^\}', re.S | re.M)
definitions = {}
for f, text in sources.items():
    for module in module_re.finditer(text):
        name = re.search(r'^\s{4}name:\s*"([^"]+)"', module.group(2), re.M)
        if name:
            definitions.setdefault(name.group(1), set()).add(f)

ref_re = re.compile(r'\b(defaults|default_team|team|licenses|default_applicable_licenses)'
                    r'\s*:\s*(\[[^\]]*\]|"[^"]*")')
parsed = {f for f in files if not is_skipped(f)}
needed = set()
work = list(parsed)
while work:
    f = work.pop()
    for ref in ref_re.finditer(sources[f]):
        for name in re.findall(r'"([^"]+)"', ref.group(2)):
            defined_in = definitions.get(name.split(':')[-1], set())
            if defined_in and not defined_in & parsed:
                for d in defined_in:
                    parsed.add(d)
                    needed.add(d)
                    work.append(d)

for f in sorted(needed):
    print(f)
