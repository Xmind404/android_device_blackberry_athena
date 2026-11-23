# PORTING.md: LineageOS 24.0 dla BlackBerry KEY2 (athena)

Stan portu z Lineage-BBKey2 (22.2, kernel 4.19) na LineageOS 24.0 (Android 17).
Ostatnia aktualizacja: 2026-11-23 (Etap 0: inwentaryzacja).

## Metoda

- Repozytoria referencyjne (poza drzewem): `~/android/bbkey2-ref/<repo>`. Remote'y:
  `Lineage-BBKey2` (gałęzie 22.2 / 22.2-kernel-4.19) oraz `LineageOS` (lineage-22.2, lineage-24.0).
  Klony są blobless, więc `git show` dociąga pliki na żądanie.
- Delta = `git log LineageOS/lineage-22.2..Lineage-BBKey2/lineage-22.2`. We wszystkich
  nadpisywanych repo Lineage-BBKey2 jest tylko **ahead**, nigdy behind, więc delta jest kompletna.
  Gałęzie `lineage-23.2` / `lineage-24.0` we frameworkach Lineage-BBKey2 to nieaktualne
  mirrory bez własnych commitów. Gałęzie `lineage-23.2` device tree to osobne drzewa
  z kernelem 4.4 (selinux permissive, bez wspólnej historii) i służą tylko jako podpowiedź
  zmian buildu Androida 16.
- Repo w naszym drzewie to pojedyncze commity „Import …” bez wspólnej historii z LineageOS.
  Dlatego porty idą przez `git format-patch` → `git am -3` / `git apply -3`, a przy konflikcie
  przez ręczne przepisanie na API 24.0. Autor commita to zawsze Xmind404, a oryginał podaję
  w `Ported from` / `Original-Author`.
- Kolumna „Próba na 24.0” to wynik `git apply --check` pojedynczej łatki na czyste drzewo 24.0.
  Konflikty późniejszych commitów z serii wynikają częściowo z braku wcześniejszych łatek,
  więc ostateczna ocena jest w kolumnie „Plan / status”.
- Statusy: `ported` (przeszło 1:1), `adapted` (przepisane pod 24.0), `dropped` (z powodem),
  `planned` (do zrobienia), `deferred` (odłożone / czeka na decyzję), `replaced` (realizowane inaczej).

## Stan repozytoriów grupy A (device / vendor / kernel / hardware)

| Ścieżka | Import odpowiada commitowi Lineage-BBKey2 | Braki względem czubka Lineage-BBKey2 |
|---|---|---|
| device/blackberry/athena | `23b2f60` (lineage-22.2-kernel-4.19) minus Lokker/K12KB | `99ad210` (extract-files: poprawka HAL1) |
| device/blackberry/sdm660-common | `2d50db1` = czubek | brak (w imporcie zmieniono tylko ścieżki w common.mk) |
| vendor/blackberry/athena | `ca6bd7f` | `2903bf1` (spatchowany `camera.sdm660.so`) |
| vendor/blackberry/sdm660-common | pliki z czubka, ale `Android.bp` / `sdm660-common-vendor.mk` sprzed `db18583` | `db18583` + `f943718`: firmware BT (`bt_firmware/*.tlv`, `*.bin`), `btaudio_offload_if`, `libbluetooth_audio_session_qti*` nie są instalowane |
| kernel/blackberry/sdm660-4p19 | `43825c5` (lineage-22.2) | 5 commitów (3 dotyczą athena, 2 tylko luna) |
| hardware/blackberry | `0d25e65` = czubek lineage-22.2 | brak |
| hardware/qcom-caf/sdm660/display | `2b97ab4` = czubek lineage-22.2-caf-sdm660 | brak; 24.0 to nadal ta sama baza caf-sdm660 |
| hardware/qcom-caf/sdm660/audio | **nie odpowiada** Lineage-BBKey2 4.19: to LineageOS `lineage-2x.x-caf-msm8998` + starsza łatka routingu (`5d409a7`) | Lineage-BBKey2 4.19 używa `lineage-22.2-caf-sdm660` + 7 commitów. LineageOS nie ma `lineage-24.0-caf-msm8998`, ma za to `lineage-24.0-caf-sdm660` (zob. decyzja D1) |

Aplikacje Lineage-BBKey2 (zasada 9), w imporcie już usunięte:

| Commit Lineage-BBKey2 (athena) | Status |
|---|---|
| `c5ec388` add K12KB, Lokker prebuilt apps | dropped – app not wanted |
| `bd4a10c` athena: update Lokker to v1.2a | dropped – app not wanted |
| `2c6b440` athena: install Lokker privapp-permissions.xml to system_ext | dropped – app not wanted |
| `fef3cff` athena: update K12KB to v3.1c | dropped – app not wanted |
| `24164e4` athena: update K12KB prebuilt to v3.2a | dropped – app not wanted |

We frameworkach i pozostałych repo nie ma commitów pod Lokker / K12KB (sprawdzone grepem
w treści i diffach wszystkich łatek).

## Decyzje wymagające Twojej zgody

- **D1. Audio HAL: przejście z `caf-msm8998` na `caf-sdm660`.** Rekomendacja: TAK. Nowa gałąź
  `lineage-24.0-caf-sdm660` w `Xmind404/android_hardware_qcom_audio`, zbudowana z oficjalnego
  `LineageOS/lineage-24.0-caf-sdm660` + 7 commitów audio Lineage-BBKey2. Obecna gałąź
  `lineage-24.0-caf-msm8998` zostaje nietknięta. Uzasadnienie: device tree 4.19 był testowany
  z caf-sdm660, oficjalne 24.0 dla SDM660 używa caf-sdm660, a gałąź msm8998 nie dostaje już
  poprawek LineageOS. Wymaga zmiany `revision` w manifeście.
- **D2. Nowe forki oficjalnego 24.0 pod Xmind404** (strategia B): `android_frameworks_av`,
  `android_packages_apps_Settings`, `android_packages_apps_SettingsIntelligence`,
  `android_packages_apps_LineageParts`, `android_packages_apps_Launcher3`,
  `android_hardware_interfaces`, ewentualnie `android_bootable_recovery`. Mogę je założyć
  sam (import oficjalnego 24.0, jak przy frameworks_base) albo zrobisz to Ty. Diff manifestu niżej.
- **D3. Znak poziomego flingu w `SyntheticTouchKeypadHandler`** (zasady 7 i 8). Twoja wersja 22.2
  ma `hScroll = -deltaX * FLING_SCROLL_FACTOR`, a Lineage-BBKey2 `deltaX * …`. Pozostałe stałe
  (SCROLL_FACTOR 0.0117, TOUCH_SLOP 8, x → 1080, `enqueueInputEvent`) są identyczne. Domyślnie
  portuję wersję Lineage-BBKey2, a Twoją poprawkę dodam dopiero po Twojej zgodzie.
- **D4. `5fde4034` Domain Verification Auto-Approve.** Bez GMS zatwierdza App Links bez weryfikacji
  domeny, co jest kompromisem bezpieczeństwa (każda aplikacja może przejąć linki https dowolnej
  domeny). Rekomendacja: nie portować albo włączać tylko, gdy brak weryfikatora Google.
- **D5. Opcjonalne:** radio FM (fm-commonsys + RevampedFMRadio), LatinIME (SYM), DownloadProvider
  (paczki językowe BlackBerry Keyboard), kafelek Cast (`e43c3fd3`, `d4674016`). Domyślnie odłożone.

## Proponowany diff manifestu (`.repo/local_manifests/local_manifests.xml`)

Nie nanoszę go sam. Repo `Xmind404/...` z gałęzią `lineage-24.0` muszą najpierw istnieć (D2).

```diff
@@ Qualcomm CAF SDM660 @@
     <project
         name="Xmind404/android_hardware_qcom_audio"
         path="hardware/qcom-caf/sdm660/audio"
         remote="github"
-        revision="lineage-24.0-caf-msm8998" />
+        revision="lineage-24.0-caf-sdm660" />
@@ Framework @@
+    <remove-project path="frameworks/av" />
+    <project name="Xmind404/android_frameworks_av" path="frameworks/av" remote="github" revision="lineage-24.0" />
+
+    <!-- Hardware interfaces (QTI A2DP offload) -->
+    <remove-project path="hardware/interfaces" />
+    <project name="Xmind404/android_hardware_interfaces" path="hardware/interfaces" remote="github" revision="lineage-24.0" />
+
+    <!-- Settings / LineageParts / Launcher -->
+    <remove-project path="packages/apps/Settings" />
+    <project name="Xmind404/android_packages_apps_Settings" path="packages/apps/Settings" remote="github" revision="lineage-24.0" />
+    <remove-project path="packages/apps/SettingsIntelligence" />
+    <project name="Xmind404/android_packages_apps_SettingsIntelligence" path="packages/apps/SettingsIntelligence" remote="github" revision="lineage-24.0" />
+    <remove-project path="packages/apps/LineageParts" />
+    <project name="Xmind404/android_packages_apps_LineageParts" path="packages/apps/LineageParts" remote="github" revision="lineage-24.0" />
+    <remove-project path="packages/apps/Launcher3" />
+    <project name="Xmind404/android_packages_apps_Launcher3" path="packages/apps/Launcher3" remote="github" revision="lineage-24.0" />
```

Uwaga: Trebuchet z 22.2 w LineageOS 24.0 to `packages/apps/Launcher3`
(`LineageOS/android_packages_apps_Launcher3`). Recovery dojdzie do manifestu dopiero wtedy,
gdy w Etapie 2c okaże się, że 24.0 nie naprawia `1048d83`.

## Delta Lineage-BBKey2 → plan na 24.0

### android_frameworks_base

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [5a169864](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/5a1698649b0d3d4ce59c45d07d01f3b561183a48) | zapaca | frameworks_base: Change input method switcher shortcut from SYM key to Alt + Enter to avoid interfering with SYM key function in keyboard apps | czysto | IME-switch, krok 1/5 | adapted: łańcuch 5 commitów → 1 commit (stan końcowy: wybieralny skrót, domyślnie Alt+Enter); w 24.0 skróty idą przez KeyGestureController | – |
| [ad8b3997](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/ad8b3997e1e5965e83724b21d391a2345594640a) | zapaca | InputMethodManager: Change input switcher shortcut to Shift + Enter | konflikt 2 | IME-switch, krok 2/5 | adapted (scalone) | – |
| [f2b3168f](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/f2b3168feb970c8a5ffd051fc58a6ab9db0be92a) | zapaca | Revert "InputMethodManager: Change input switcher shortcut to Shift + Enter" | konflikt 2 | IME-switch, krok 3/5 (revert) | adapted (scalone, netto bez zmian) | – |
| [1464a496](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/1464a4964b48b010e3c252eae84d5f8656fd2b0a) | zapaca | IME Switcher: Enable choosing shortcut | konflikt 2 | IME-switch, krok 4/5 | adapted (scalone) | – |
| [9ae9bf08](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/9ae9bf0866eb75446850293a96fc9d6e2a2d9890) | tim-ecoder | PowerManagerService: Settings.Secure brightness overrides for DeviceSettings | konflikt 2 | Podświetlenie 1/12 | adapted: seria PMS (klawiatura+przyciski) przepisana na PMS 24.0, klucze Settings.Secure zgodne z DeviceSettings, gałęzie luna usunięte | – |
| [ef6d6627](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/ef6d6627ca1fb66c328bd2e887bc3350bac4ca2b) | zapaca | IME Switcher: Make Alt+Enter the default | konflikt 4 | IME-switch, krok 5/5 | adapted (scalone) | – |
| [e77da8f5](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/e77da8f59ecea58051a3594f1437af1ffa77036d) | tim-ecoder | adaptive keyboard and button backlight | konflikt 2 | Podświetlenie 2/12 | adapted (seria PMS) | – |
| [afe701ca](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/afe701cab69a0279aaa623ab35ad7d2d4df8004f) | zapaca | PowerManagerService: Adaptive backlight mod for Luna's dimmer keyboard backlight | konflikt 2 | Podświetlenie 3/12, luna | dropped – luna only (część wspólna wchodzi w serię PMS) | – |
| [5fde4034](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/5fde403493dd3c26ddc1fcf78dd2a3de8fa5e01b) | zapaca | frameworks_base: Domain Verification Auto-Approve | czysto | Bezpieczeństwo: auto-zatwierdzanie App Links bez GMS | deferred – wymaga Twojej decyzji (osłabia weryfikację domen; z GApps zbędne) | – |
| [0693b591](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/0693b5910498372fe40a7b27e01c1ac7a74228e0) | zapaca | hwui: disable bitmap decode telemetry | konflikt 2 | Bateria/telemetria | adapted: StatsUtils.cpp zmieniony w 24.0; port w etapie bateria | – |
| [72f2d1e7](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/72f2d1e7b83415e1a22e15c8eb81fde436f3a2cb) | zapaca | Bluetooth: car-bt-sco-a2dp-reroute patch | czysto | BT: SCO/A2DP w aucie | ported: przechodzi czysto; etap BT | – |
| [ebdd4d6f](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/ebdd4d6f4c633af3763df31c4d518e23ac82abc6) | zapaca | PowerManagerService: Keyboard backlight floor increase and off in daylight | konflikt 2 | Podświetlenie 4/12 | adapted (seria PMS) | – |
| [628fd7e7](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/628fd7e75556f5c975c70ed96613d07f455a6300) | zapaca | touchpad scroll handler | konflikt 2 | Touch-scroll 1/5 (zasada 7) | adapted: SyntheticTouchKeypadHandler 1:1 (stałe bez zmian), tylko dostosowanie do ViewRootImpl 24.0 | – |
| [ddf8eec1](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/ddf8eec1b2ea772342de75ec2d1baa04b31cbc6c) | tim-ecoder | touchpad scroll: reduce sensitivity 10% | konflikt 2 | Touch-scroll 2/5 | adapted (seria touch-scroll) | – |
| [0f15676a](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/0f15676ab97d13c3b242d061443e8ab14f89ff75) | tim-ecoder | TouchKeypad QS tile | konflikt 4 | Kafelek QS w SystemUI | replaced: Etap 3 pkt 6 – TileService w aplikacji urządzenia; z tego commitu tylko ikony | – |
| [dc4ea330](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/dc4ea33099938f7c614f519ff5d6dddc78768584) | tim-ecoder | touchpad scroll: reduce sensitivity and inertia 10% | konflikt 2 | Touch-scroll 3/5 | adapted (seria touch-scroll) | – |
| [c87cf219](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/c87cf2191e02443faaddeb82ec2fe40c3c62336f) | zapaca | keyguard: Enable keyboard PIN input on lockscreen | konflikt 4 | PIN z klawiatury 1/2 | adapted: KeyguardPinViewController 24.0 + przełącznik z pkt 6; mapowanie z KCM | – |
| [8d7ee75f](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/8d7ee75f2ca17391932899ae6c6b0b67888178cc) | zapaca | PowerManagerService: Add toggling capability to adaptive keyboard/buttons brightness | konflikt 2 | Podświetlenie 5/12 | adapted (seria PMS) | – |
| [0f1f282e](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/0f1f282e5a19f338713390cf09286fa35e3dc55f) | zapaca | PowerManagerService: Athena keyboard adaptive backlight floor decrease | konflikt 2 | Podświetlenie 6/12 | adapted (seria PMS) | – |
| [72e50692](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/72e50692e6a285d51ad9475d40dd35c7616a2557) | zapaca | frameworks_base: Compatibility for both Athena and Luna | konflikt 3 | Athena/Luna compat | adapted: tylko athena (zasada 1) | – |
| [55c8570e](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/55c8570e8dd2175fb60bf113c8ec4170ec564640) | zapaca | PowerManagerService: Adaptive backlights: Raise brightness threshold for keyboard shutoff, lower button floor | konflikt 2 | Podświetlenie 7/12 | adapted (seria PMS) | – |
| [8a223c98](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/8a223c985d1161f9a893cacec337512583c930b6) | zapaca | power: Add independent keyboard backlight controls, fix light state bugs | konflikt 2 | Podświetlenie 8/12 | adapted (seria PMS; zawiera poprawki alarmu timeoutu) | – |
| [76605e9e](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/76605e9eb117a24dd10d2384200e9c32bc539226) | zapaca | base: add light-on-touch and independent keyboard scrolling capability | konflikt 12 | Touch-scroll 4/5 + podświetlenie 9/12 | adapted: wyłączanie scrolla, light-on-touch, most JNI do PMS | – |
| [790cd2ba](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/790cd2bada637fa84cc810dc9bb49c991863cde1) | zapaca | ViewRootImpl: avoid focus changes from touch keypad input | czysto | Touch-scroll 5/5 | ported: czysto | – |
| [3a077a4a](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/3a077a4ac10f312945256d7157d2f7235e7cf2aa) | zapaca | base: Restore BlackBerry Hub message personalization access | konflikt 2 | BlackBerry Hub READ_MESSAGES | planned – niski priorytet (dla BlackBerry Keyboard; część systemowa) | – |
| [d44840d1](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/d44840d16bd33867e3d0b9c45cb9ecec1c434b67) | zapaca | NetworkPolicy: refresh rules for adopted-storage apps | czysto | NetworkPolicy, adopted storage | planned: czysto; sprawdzić, czy 24.0 nie naprawia | – |
| [ba9b79d1](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/ba9b79d1da0275157a5ba25c829dff5d105c78fb) | zapaca | keyguard: Fix intermittent keyboard PIN delete/enter, support QWERTZ/AZERTY PIN input | konflikt 6 | PIN z klawiatury 2/2 | adapted: zamiast QWERTZ/AZERTY na sztywno – mapowanie z aktywnego KCM (pkt 10) | – |
| [2625ccfd](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/2625ccfdbdddd258127733bc210991855f36c7a3) | zapaca | SettingsProvider: support configurable display density defaults | konflikt 2 | Gęstość ekranu | planned: wymaga forka Settings (bb7ef10) | – |
| [792be69c](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/792be69c111739652fd3fb1ec9c607f8b26e525a) | zapaca | PowerManager: Match stock keyboard and button backlight timeouts by default | konflikt 2 | Podświetlenie 10/12 | adapted (seria PMS) | – |
| [e43c3fd3](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/e43c3fd3e2d8bd1bf6d6ba8f3a42822bec92bede) | zapaca | SystemUI: Allow Cast tile with Wi-Fi enabled but disconnected | konflikt 4 | Kafelek Cast | deferred – poza listą funkcji, niski priorytet | – |
| [d6f4365a](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/d6f4365a8c8d131c97fa6034b24b2673a45476ea) | zapaca | PowerManager: Separate keyboard brightness scaling from daylight cutoff | konflikt 2 | Podświetlenie 11/12 | adapted (seria PMS) | – |
| [d4674016](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/d4674016a6fea37ad4e4770aa4f241d6460d528d) | zapaca | SystemUI: Enable Wi-Fi from Cast and show visible startup feedback | konflikt 4 | Kafelek Cast + Wi-Fi (99 plików) | deferred – poza listą funkcji, niski priorytet | – |
| [11e83906](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/11e83906fd2c86d8efe7de0e748f883260f793e9) | zapaca | PowerManager: avoid gating fixed-keyboard lighting on visibility | konflikt 2 | Podświetlenie 12/12 | adapted (seria PMS) | – |
| [3049eda0](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/3049eda0b6d45aa1c86d8f909ebdcc4be8391357) | zapaca | AnrTimer: prevent system_server from freezing itself | konflikt 2 | AnrTimer: system_server zamraża sam siebie | planned – wysoki priorytet (czarny ekran); sprawdzić kod 24.0 | – |
| [9df04ab0](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/9df04ab019fa4a318a88cd421504efa6887baeb1) | zapaca | audio: keep local call sliders and volume keys on native levels | konflikt 2 | Głośność rozmowy | planned: + Settings 34b8a16 | – |
| [dca385ec](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/dca385ec755460f3aa371e31a80f329378eec1c9) | zapaca | SystemUI: update navigation services when the on-screen bar changes | konflikt 2 | Navbar on-screen | planned: + LineageParts 860b45b + Launcher3 | – |
| [0319a4ee](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/0319a4eef14a60490132b2de683e4c3f00ad34f7) | zapaca | Input: Select family-compatible BlackBerry keyboard overlays | czysto | Wybór KCM BlackBerry | ported: czysto; podstawa pkt 7 | – |
| [8013a1ed](https://github.com/Lineage-BBKey2/android_frameworks_base/commit/8013a1ed450f6b473227d83a6a8102971cfe41c9) | zapaca | InputMethodService: Work around legacy BlackBerry Alt overrides | czysto | BlackBerry Keyboard Alt | ported: czysto | – |

### android_frameworks_native

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [6173eee](https://github.com/Lineage-BBKey2/android_frameworks_native/commit/6173eeeacde9ba0e1ca4d43949f908d4c946dafa) | zapaca | EventHub.cpp: Skip overlay application for stmpe_keypad | czysto | Overlay KCM STMPE 1/3 | adapted: 3 commity → stan końcowy (fb827f8) | – |
| [e626ef0](https://github.com/Lineage-BBKey2/android_frameworks_native/commit/e626ef0eac9a6cd8faa42e5f1af513ee4c80dc2a) | zapaca | EventHub.cpp: Overlay application skip for any stmpe keypad | konflikt 2 | Overlay KCM STMPE 2/3 | adapted (scalone) | – |
| [8eeea09](https://github.com/Lineage-BBKey2/android_frameworks_native/commit/8eeea093092355f11a12337c1566a9d36344cdf5) | zapaca | TouchInputMapper: TOUCH_PAD implementation patch | konflikt 2 | TOUCH_PAD w TouchInputMapper | adapted: kluczowe dla touch-scroll | – |
| [8f6bb31](https://github.com/Lineage-BBKey2/android_frameworks_native/commit/8f6bb31a9c4b95644e93db774cd286e5d7477293) | zapaca | inputflinger: notify policy of touchpad activity | konflikt 2 | Aktywność touchpada → policy | adapted: razem z 76605e9e | – |
| [fb827f8](https://github.com/Lineage-BBKey2/android_frameworks_native/commit/fb827f87d5d087a5022e3b72c0aa8a9ec1ed7dbf) | zapaca | EventHub: Allow matching BlackBerry overlays on STMPE keypads | konflikt 2 | Overlay KCM STMPE 3/3 | adapted (scalone) | – |

### android_packages_modules_Bluetooth

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [01d8bdc](https://github.com/Lineage-BBKey2/android_packages_modules_Bluetooth/commit/01d8bdcf169de139ee39a6ea814f81a4da6c55a7) | zapaca | Bluetooth: car-bt clcc-log-spam patch | konflikt 4 | CLCC log spam | adapted: etap BT | – |
| [cc2ba40](https://github.com/Lineage-BBKey2/android_packages_modules_Bluetooth/commit/cc2ba40403249036e3a5d65029f54a751d493398) | zapaca | Bluetooth: disable AAC for incompatible A2DP sinks (incl. new BMW address prefix) | konflikt 2 | AAC blacklist (BMW) | adapted: interop_database.conf czysto, reszta przepisana | – |
| [6560862](https://github.com/Lineage-BBKey2/android_packages_modules_Bluetooth/commit/6560862c3bf0254678bba97cae79e5cb4dfa86d9) | zapaca | Bluetooth: support the gated legacy QTI A2DP offload protocol | konflikt 12 | QTI A2DP offload (legacy) | planned: etap BT, razem z hardware/interfaces 9aa4ece i vendor db18583 | – |

### android_frameworks_av

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [0246cf0](https://github.com/Lineage-BBKey2/android_frameworks_av/commit/0246cf02f9b6b659eea5214ce7e41aadc6986369) | zapaca | Camera3Device: disable broken camera mute on BlackBerry devices | czysto | Camera mute | planned: czysto; wymaga forka frameworks/av | – |
| [c78af59](https://github.com/Lineage-BBKey2/android_frameworks_av/commit/c78af59682422085834ec929024ad753abd47bb1) | zapaca | audiopolicy: skip incompatible mono VoIP outputs | czysto | Mono VoIP outputs | planned: czysto; wymaga forka frameworks/av | – |

### android_hardware_interfaces

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [9aa4ece](https://github.com/Lineage-BBKey2/android_hardware_interfaces/commit/9aa4eceecefb5f22a7d3b9e6db491f1770877b54) | zapaca | bluetooth: expose gated 48 kHz QTI A2DP provider sessions | konflikt 4 | 48 kHz QTI A2DP | planned: etap BT; wymaga forka hardware/interfaces | – |

### android_packages_apps_Aperture

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [930a25c](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/930a25c30a0ca582e19fc0ab216d423060869f55) | zapaca | Aperture: gracefully handle cameras disabled by sensor privacy | konflikt 2 | Aperture | planned: Etap 3 pkt 13 | – |
| [bf29375](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/bf293759bc8bb7f68c27dcbeb2616d4f1b3e2b73) | zapaca | Aperture: Improve preview layout on compact BlackBerry displays | konflikt 4 | Aperture: kompaktowy ekran | planned: Etap 3 pkt 13 | – |
| [51f8932](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/51f8932105c3810ed69a88b3dd72ee8e4624fdd4) | zapaca | Aperture: Match 16:9 photo preview framing to captured images | konflikt 2 | Aperture | planned | – |
| [6bbafbe](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/6bbafbee319d31fe7b7d485ac947130040d75094) | zapaca | Aperture: Top-align the Scan preview using a 4:3 frame | konflikt 2 | Aperture | planned | – |
| [3b5c9e2](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/3b5c9e2cc5aec3113cf10c6f110ddfe9a1267988) | zapaca | Aperture: Size video previews from recording profiles | konflikt 3 | Aperture | planned | – |
| [d17e805](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/d17e80509d746fee046fdbea6148f80000d98864) | zapaca | Aperture: Prefer matching preview streams for SD video | konflikt 2 | Aperture | planned | – |
| [2b238bd](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/2b238bd7a341908d1372ce96d56a12a8be823add) | zapaca | Aperture: Defer camera binding until layout traversal completes | konflikt 2 | Aperture | planned | – |
| [4e562c7](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/4e562c79ad5828747b73369c57d17a435379f386) | zapaca | Aperture: Label video quality using actual profile dimensions | konflikt 4 | Aperture | planned | – |
| [a60b0a5](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/a60b0a5a7d35a09a718702019af1c00080bee2fe) | zapaca | Aperture: Add native Photo HDR OFF/ON control | konflikt 7 | Aperture: HDR | planned | – |
| [44b6709](https://github.com/Lineage-BBKey2/android_packages_apps_Aperture/commit/44b67097c29396d7d17c775387f59ce59b1ef0fe) | zapaca | Aperture: reapply camera settings across lifecycle transitions | konflikt 2 | Aperture | planned | – |

### android_packages_apps_Settings

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [bb7ef10](https://github.com/Lineage-BBKey2/android_packages_apps_Settings/commit/bb7ef10d0866ad2b5b166e8289e6c3c5dee60cad) | zapaca | Settings: honor device-specific display density defaults | 3-way | Settings: gęstość | planned: fork Settings | – |
| [9cf8de7](https://github.com/Lineage-BBKey2/android_packages_apps_Settings/commit/9cf8de776e5e56048fbdaa81738a72870ae99b7e) | zapaca | Settings: support physical SIM toggles on legacy radio implementations | konflikt 18 | Settings: SIM toggle (sevensim) | planned: fork Settings; duży port | – |
| [583c465](https://github.com/Lineage-BBKey2/android_packages_apps_Settings/commit/583c46528285632320eda53034ba84c52d6cecf4) | zapaca | Settings: stabilize SIM controls during legacy SIM power transitions | konflikt 11 | Settings: SIM toggle | planned: fork Settings | – |
| [34b8a16](https://github.com/Lineage-BBKey2/android_packages_apps_Settings/commit/34b8a161813847f1cceb806cb3e946d752b5fe09) | zapaca | Settings: display the configured local call volume range | czysto | Settings: głośność rozmowy | planned: fork Settings | – |
| [87fda3c](https://github.com/Lineage-BBKey2/android_packages_apps_Settings/commit/87fda3c13b89aab1e45cdb63afca11462b801e2e) | zapaca | Settings: start search from physical keyboard input | 3-way | Settings: szukanie z klawiatury | planned: fork Settings + SettingsIntelligence | – |
| [5b3b60d](https://github.com/Lineage-BBKey2/android_packages_apps_Settings/commit/5b3b60dc6f8d48496619c9f1fb55beb6b805ebdd) | zapaca | Settings: Preview BlackBerry physical keyboard layouts | 3-way | Settings: podgląd KCM | planned: fork Settings | – |

### android_packages_apps_SettingsIntelligence

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [cd73cf7](https://github.com/Lineage-BBKey2/android_packages_apps_SettingsIntelligence/commit/cd73cf75c2fd3b42e98e0b894d4beed5fba739ff) | zapaca | SettingsIntelligence: initialize search from the launch query | 3-way | SettingsIntelligence | planned: fork | – |

### android_packages_apps_LineageParts

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [860b45b](https://github.com/Lineage-BBKey2/android_packages_apps_LineageParts/commit/860b45b41f27a05c261cb805e6276decb31d9e2b) | zapaca | LineageParts: preserve the selected on-screen navigation style | 3-way | LineageParts navbar | planned: fork LineageParts | – |

### android_packages_apps_Trebuchet

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [7b616a7](https://github.com/Lineage-BBKey2/android_packages_apps_Trebuchet/commit/7b616a7e360234b876e5dff12f933721b71b5560) | zapaca | Trebuchet: Don't reserve navbar space without a navbar | konflikt 4 | Launcher3 navbar | planned: Trebuchet → w 24.0 packages/apps/Launcher3; fork | – |
| [c95ab00](https://github.com/Lineage-BBKey2/android_packages_apps_Trebuchet/commit/c95ab00110c8384b4cd829b64fb4ded9d3c52d67) | zapaca | Trebuchet: refresh navigation mode on configuration changes | konflikt 1 | Launcher3 navbar | planned: fork Launcher3 | – |

### android_packages_inputmethods_LatinIME

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [fa275a0](https://github.com/Lineage-BBKey2/android_packages_inputmethods_LatinIME/commit/fa275a0ea244de968556b7ea338eb3e2034d4282) | zapaca | LatinIME: Add physical SYM key symbol layout cycling | 3-way | LatinIME SYM | deferred – opcjonalne (używasz athenaKB/Gboard) | – |

### android_packages_providers_DownloadProvider

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [28fc312](https://github.com/Lineage-BBKey2/android_packages_providers_DownloadProvider/commit/28fc31207d23781bda478adc69d1d29911a06a24) | zapaca | DownloadProvider: redirect legacy BlackBerry language packs | czysto | Paczki językowe BB | deferred – opcjonalne | – |

### android_bootable_recovery

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [1048d83](https://github.com/Lineage-BBKey2/android_bootable_recovery/commit/1048d83649bf0f7b386bfd25a909231dd266bd39) | zapaca | recovery: create the locale directory before saving | czysto | Recovery: katalog locale | planned: Etap 2c; fork bootable/recovery jeśli 24.0 nie naprawia | – |

### android_vendor_qcom_opensource_fm-commonsys

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [24201ff](https://github.com/Lineage-BBKey2/android_vendor_qcom_opensource_fm-commonsys/commit/24201ffbdc1c819fe2b4f32971c0d505fe68a918) | zapaca | qcom.fmradio: fix RadioText buffer parsing | czysto | FM | deferred – radio FM poza listą funkcji | – |
| [132bfbd](https://github.com/Lineage-BBKey2/android_vendor_qcom_opensource_fm-commonsys/commit/132bfbd08e742331e23b978e460d4667869f1d01) | zapaca | fm: Add API to disable RDS group processing | czysto | FM | deferred | – |
| [65b081c](https://github.com/Lineage-BBKey2/android_vendor_qcom_opensource_fm-commonsys/commit/65b081c694bc5adea69cb6bffea24221bf6844db) | zapaca | FM2: Rename app to FM Radio Legacy | czysto | FM | deferred | – |

### android_frameworks_opt_telephony

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [9a36a7d](https://github.com/Lineage-BBKey2/android_frameworks_opt_telephony/commit/9a36a7d746ef23bde87a9e25f41e3abfcbf60ee8) | zapaca | telephony: add opt-in support for modem-controlled IWLAN | konflikt 2 | IWLAN | dropped – repo nieużywane w manifeście 4.19 Lineage-BBKey2 | – |

### android_hardware_qcom_audio

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [3793efa](https://github.com/Lineage-BBKey2/android_hardware_qcom_audio/commit/3793efa60d59cba2fbd0194e6fde8294be07e2d6) | zapaca | sdm660-audio: Routing bugs fix and VOIP call fix | – | Audio sdm660 | planned: po przejściu na caf-sdm660 (decyzja D1) | – |
| [1ef12c4](https://github.com/Lineage-BBKey2/android_hardware_qcom_audio/commit/1ef12c4ca7a4670f5ad2fdcdcd4b8d4cd352ae0a) | zapaca | sdm660-audio: prevent software VA capture from blocking voice calls | – | Audio | planned (D1) | – |
| [2cb0097](https://github.com/Lineage-BBKey2/android_hardware_qcom_audio/commit/2cb009757369223fb8ef168db89d610715f2a3b9) | zapaca | audio: Handle FM tuner capture latency queries | – | Audio FM | planned (D1) | – |
| [4c5d031](https://github.com/Lineage-BBKey2/android_hardware_qcom_audio/commit/4c5d031c5932b07de5802d444fb2424ca316ce4d) | zapaca | audio: use the default capture app type for PCM VoIP | – | Audio VoIP | planned (D1) | – |
| [1b0c042](https://github.com/Lineage-BBKey2/android_hardware_qcom_audio/commit/1b0c04202168fc9acf66f34a3ee99a0d58321d50) | zapaca | audio: support wideband VoIP with calibrated PCM fallback | – | Audio VoIP WB | planned (D1) | – |
| [20e54f5](https://github.com/Lineage-BBKey2/android_hardware_qcom_audio/commit/20e54f52a7c6dba1e7369359171ae35555413eff) | zapaca | audio: keep normal handset gain within its calibration range | – | Audio słuchawka | planned (D1) | – |
| [5304c3d](https://github.com/Lineage-BBKey2/android_hardware_qcom_audio/commit/5304c3df974ce5b1e8453bb46d6a62e4296c2c42) | zapaca | audio: avoid unsupported AAC frame controls on the QTI DSP path | – | Audio AAC | planned (D1) | – |

### android_hardware_qcom_display

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [0dd8504](https://github.com/Lineage-BBKey2/android_hardware_qcom_display/commit/0dd85049b909798d69792246c816aafa719669a5) | zapaca | gralloc: disable UBWC for RGBA application buffers | czysto | gralloc | already in tree (import) | – |
| [9fe70ff](https://github.com/Lineage-BBKey2/android_hardware_qcom_display/commit/9fe70ff810ab8fd2df249f92d9ba457b3a141308) | zapaca | Revert "gralloc: disable UBWC for RGBA application buffers" | 3-way | gralloc revert | already in tree | – |
| [7499cbe](https://github.com/Lineage-BBKey2/android_hardware_qcom_display/commit/7499cbe7d3529557c4587bd7947248d748eae70a) | zapaca | gralloc: track imported buffer CPU lock state | 3-way | gralloc | already in tree | – |
| [2b97ab4](https://github.com/Lineage-BBKey2/android_hardware_qcom_display/commit/2b97ab449dd10a66aea2eae1ed9aed9ef5dc4933) | zapaca | sdm: handle HDMI initialization failures without stale interfaces | 3-way | sdm HDMI | already in tree | – |

### android_device_blackberry_athena (22.2-kernel-4.19, brak w imporcie)

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [99ad210](https://github.com/Lineage-BBKey2/android_device_blackberry_athena/commit/99ad210c17fecf85409b3bf99a71fb420786b53a) | zapaca | athena: preserve the HAL1 close fix during blob extraction | – | device athena | planned: Etap 1 | – |

### android_vendor_blackberry_athena (brak w imporcie)

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [2903bf1](https://github.com/Lineage-BBKey2/android_vendor_blackberry_athena/commit/2903bf1b8cff1215ff16b8be3cffc15799bce0d4) | zapaca | athena: finish queued HAL1 work before worker shutdown | – | vendor athena blob | planned: Etap 1 (razem z 99ad210) | – |

### android_vendor_blackberry_sdm660-common (import ma pliki, ale stare Android.bp/.mk)

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [f943718](https://github.com/Lineage-BBKey2/android_vendor_blackberry_sdm660-common/commit/f943718aa6076d81a5e459db57db06f1e7370a52) | zapaca | sdm660-common: regenerate vendor build files | – | vendor common | planned: Etap 1 | – |
| [db18583](https://github.com/Lineage-BBKey2/android_vendor_blackberry_sdm660-common/commit/db18583e058a138dc8fa52ba4c3e146a25fe98e4) | zapaca | sdm660-common: package QTI A2DP clients and Bluetooth firmware | – | vendor common BT fw + QTI A2DP | planned: Etap 1 (firmware BT), A2DP w etapie BT | – |

### android_kernel_blackberry_sdm660-4p19 (lineage-22.2, nowsze niż nasz kernel)

| Źródło | Autor | Tytuł | Próba na 24.0 | Obszar | Plan / status | Commit docelowy |
|---|---|---|---|---|---|---|
| [656bd3853](https://github.com/Lineage-BBKey2/android_kernel_blackberry_sdm660-4p19/commit/656bd38536a291a9c32788af9f2c47b2a412c72a) | zapaca | input: athena/luna: guard capacitive navigation during top-row typing | – | kernel: nawigacja vs pisanie | planned: Etap 1 | – |
| [92a1143bc](https://github.com/Lineage-BBKey2/android_kernel_blackberry_sdm660-4p19/commit/92a1143bc535152e9f35e3fe02270b9455f37467) | zapaca | input: blackberry: guard Athena navigation keys during keyboard swipes | – | kernel: nawigacja vs swipe | planned: Etap 1 | – |
| [36b013cfc](https://github.com/Lineage-BBKey2/android_kernel_blackberry_sdm660-4p19/commit/36b013cfc7c5a8962c06d9fa3da2565a65f2018e) | zapaca | arm64: dts: luna: select the retail BYD battery profile | – | kernel: luna dts | dropped – luna only | – |
| [60391bf1c](https://github.com/Lineage-BBKey2/android_kernel_blackberry_sdm660-4p19/commit/60391bf1cd11d2270e0f7f5d234e2d3533418bdd) | zapaca | arm64: dts: qcom: luna: smooth the notification LED fade | – | kernel: luna dts | dropped – luna only | – |
| [50bcc42c7](https://github.com/Lineage-BBKey2/android_kernel_blackberry_sdm660-4p19/commit/50bcc42c746b2842884da43badbe5740cf039662) | zapaca | media: msm: cpp: let devres release the reset control | – | kernel: cpp reset | planned: Etap 1 | – |

## Twoje własne zmiany z forków 22.2 (zasada 8, NIE portowane bez zgody)

Porównałem `Xmind404/*@lineage-22.2` z Lineage-BBKey2. Twoje gałęzie 22.2 to głównie import
starszego stanu Lineage-BBKey2. Faktycznie własne różnice:

| Repo / commit | Zmiana | Status |
|---|---|---|
| android_frameworks_base `927208e`, `ba53999`, `477cfcc` | `SyntheticTouchKeypadHandler`: `hScroll = -deltaX * FLING_SCROLL_FACTOR` (odwrócony poziomy fling). Reszta handlera jest identyczna z Lineage-BBKey2. | czeka na zgodę (D3) |
| android_frameworks_base `c89fb21`, `8c8ad74`, `b286c6f` | PIN z klawiatury (QWERTY/QWERTZ/AZERTY): stan końcowy identyczny z Lineage-BBKey2 `ba9b79d1` | nic do portowania (pokrywa się z deltą) |
| android_frameworks_base `c7ffb02` | skala jasności przycisków 0.1–1.0 (Lineage-BBKey2 `55c8570e` ma później 0.01–1.0) | czeka na zgodę; domyślnie wartość Lineage-BBKey2 |
| android_frameworks_base `477cfcc` | pliki `*:Zone.Identifier` (artefakty Windows) | nie portować (śmieci) |
| android_device_blackberry_athena `6429925` | URI OTA updatera dla dystrybucji Xmind404 | czeka na zgodę |
| android_hardware_qcom_audio `lineage-22.2-caf-msm8998` | LineageOS msm8998 + łatka routingu Lineage-BBKey2 `5d409a7` | zastępowane przez D1 |
| android_kernel_blackberry_sdm660 (4.4) | tylko INIT + README | nic do portowania |

## Uwagi techniczne zebrane w Etapie 0

- Defconfig athena: `TARGET_KERNEL_CONFIG := athena-perf_defconfig` (`kernel/blackberry/sdm660-4p19/arch/arm64/configs/athena-perf_defconfig`).
  EROFS jest w drzewie jako backport `fs/erofs`, ale defconfig go nie włącza (`CONFIG_STAGING=y` jest). Do zrobienia w Etapie 2a.
- Partycje **statyczne** (brak dynamic partitions): `BOARD_SYSTEMIMAGE_PARTITION_SIZE := 2684354560` (2,5 GiB),
  vendor 661 MiB, brak osobnych product/system_ext. Rezerwę na MindTheGapps trzeba policzyć na system (Etap 2b).
- `device/blackberry/athena/DeviceSettings` (`BlackBerrySettings_athena`, podpis platform) już istnieje i jest
  wstrzyknięta do Settings przez `IA_SETTINGS` / `ia.system`. Ma przełączniki touchpada, PIN-u,
  podświetlenia klawiatury i przycisków oraz CABL. Brakuje tłumaczeń `pl` i TileService. Etap 3 pkt 6
  rozbuduje tę aplikację zamiast tworzyć nową.
- `sdm660-common/key2_iwlan_patch.py` binarnie łata `libril-qc-hal-qmi.so` przy ekstrakcji blobów.
  Nie wymaga forka telephony (`9a36a7d` dropped).
- Hook `commit-msg` (Gerrit Change-Id) jest aktywny we wszystkich 12 repo z tabeli (symlink z `repo`).

## Plan Etapu 1

1. Group A: `99ad210` + `2903bf1` (HAL1), `db18583` + `f943718` (firmware BT), kernel `656bd38`, `92a1143`, `50bcc42`.
2. Zielony `m nothing`: zmienne buildu 24.0, sepolicy, VINTF, zdeprecjonowane HAL-e.
3. frameworks/native: TOUCH_PAD, overlay KCM, aktywność touchpada.
4. frameworks/base: touch-scroll (zasada 7), seria PMS podświetlenia, PIN z klawiatury, KCM BlackBerry, IME switcher, AnrTimer, reszta wg tabeli.
5. Bluetooth: `01d8bdc`, `cc2ba40` (+ `72f2d1e7` w base), offload QTI po D2.
