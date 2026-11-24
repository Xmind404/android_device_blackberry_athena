#
# Copyright (C) 2026 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

# BlackBerry stock sounds and wallpapers (vendor/blackberry/media). Must come
# before the AOSP and Lineage audio packages so its PRODUCT_COPY_FILES win for
# shared names.
$(call inherit-product-if-exists, vendor/blackberry/media/media.mk)

# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Device
$(call inherit-product, device/blackberry/athena/device.mk)

# Inherit some common Lineage stuff.
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

PRODUCT_BRAND := blackberry
PRODUCT_DEVICE := athena
PRODUCT_MANUFACTURER := BlackBerry
PRODUCT_MODEL := KEY2
PRODUCT_NAME := lineage_athena

# need to keep old FPs from oreo for play certification
PRODUCT_BUILD_PROP_OVERRIDES += \
    BuildDesc="bbf100dsglobal 8 OPM1.171019.026 ACQ160 release-keys" \
    BuildFingerprint=blackberry/bbf100dsglobal/bbf100:8.1.0/OPM1.171019.026/ACQ160:user/release-keys \
    DeviceProduct=bbf100dsglobal \
    DeviceName=bbf100 \
    SystemDevice=bbf100

# OTA Updater
PRODUCT_SYSTEM_PROPERTIES += \
    lineage.updater.uri=https://raw.githubusercontent.com/Xmind404/distribution/lineage-24.0/{device}.json

# Low-memory build hosts (opt-in, ATHENA_LOW_MEMORY_BUILD=true in the environment).
# Soong analysis of the whole tree needs well over 40 GB of RAM+swap. Skip the
# Android.bp files of test suites, automotive and other devices, none of which end
# up in the athena images. Requires ALLOW_MISSING_DEPENDENCIES (BoardConfig.mk),
# since test modules elsewhere still name modules from these directories.
ifeq ($(ATHENA_LOW_MEMORY_BUILD),true)
PRODUCT_SOURCE_ROOT_DIRS += \
    -bionic/benchmarks \
    -bionic/tests \
    -cts \
    -device/generic/car \
    -device/generic/goldfish \
    -device/generic/trusty \
    -device/google \
    -frameworks/base/core/tests \
    -frameworks/base/libs/WindowManager/Shell/tests \
    -frameworks/base/services/tests \
    -frameworks/base/tests \
    -frameworks/rs/tests \
    -packages/modules/AdServices/adservices/tests \
    -packages/modules/AdServices/sdksandbox/tests \
    -packages/modules/Permission/tests \
    -packages/modules/Virtualization/tests \
    -packages/services/Car \
    -platform_testing \
    -prebuilts/vndk \
    -system/software_defined_vehicle \
    -test \
    -trusty

# Keep the soong_build plugins from the skipped directories: modules elsewhere
# (e.g. art/test) use the module types they register. A longer prefix wins.
PRODUCT_SOURCE_ROOT_DIRS += \
    device/google/cuttlefish/build \
    test/app_compat/csuite/tools/csuite_test

# Keep the files that define defaults, teams and licenses still referenced from
# parsed directories; those references are not covered by
# ALLOW_MISSING_DEPENDENCIES.
PRODUCT_SOURCE_ROOT_DIRS += \
    cts/Android.bp \
    packages/modules/AdServices/adservices/tests/test-util/Android.bp \
    packages/services/Car/cpp/car_binder_lib/largeParcelable/Android.bp \
    packages/services/Car/cpp/power/client/Android.bp \
    packages/services/Car/cpp/vhal/client/Android.bp \
    packages/services/Car/cpp/watchdog/stable_server/Android.bp \
    packages/services/Car/teams/Android.bp \
    platform_testing/libraries/motion/Android.bp \
    platform_testing/libraries/rdroidtest/Android.bp \
    system/software_defined_vehicle/platform/tracing/Android.bp \
    trusty/kernel/Android.bp
endif
