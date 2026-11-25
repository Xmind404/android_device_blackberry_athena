# HANDOFF.md: stan prac i zadania dla następnej sesji Claude

Stan na 2026-11-25. Drzewo `~/android/LineageOS_24` jest kasowane i synchronizowane od nowa.
Wszystkie commity są na GitHubie (Xmind404). Ten plik mówi, **co odtworzyć, co sprawdzić i co
zrobić dalej**. Szczegóły: `PORTING.md` (delta Lineage-BBKey2, decyzje D1–D6) i `FEATURES.md`
(funkcje, statusy, testy na telefonie).

> **Najważniejsze: nic z tego kodu nie zostało jeszcze skompilowane.** Żaden `m nothing` ani
> `m bacon` nie doszedł do końca, bo analiza Soong nie mieści się w pamięci tego komputera
> (sekcja 2). Pierwszy udany build na pewno pokaże błędy kompilacji do poprawienia (sekcja 3).

---

## 0. Pierwsze kroki nowej sesji Claude

1. Przeczytaj `docs/CLAUDE.md` (kopia instrukcji projektu z korzenia drzewa), ten plik,
   `PORTING.md` i `FEATURES.md`.
2. Sprawdź konto: `git config user.name`, `git config user.email`, `gh auth status` (Xmind404).
3. Pamiętaj o pułapce powłoki narzędzia Bash: **przed `source build/envsetup.sh` zawsze
   `unset -f grep find pkill;`**, inaczej `breakfast`/`lunch` się wywraca.
4. Nie uruchamiaj `m bacon` ani `repo sync`. Robi to właściciel. Wolno ci uruchamiać `m nothing`
   i buildy pojedynczych modułów, ale lokalnie i tak nie przejdą analizy (sekcja 2).

## 1. Odtworzenie drzewa (robi właściciel)

```
mkdir -p ~/android/LineageOS_24 && cd ~/android/LineageOS_24
repo init -u https://github.com/LineageOS/android.git -b lineage-24.0 --git-lfs --no-clone-bundle
mkdir -p .repo/local_manifests
cp <kopia z GitHuba>/docs/local_manifests.xml .repo/local_manifests/local_manifests.xml
repo sync -c --force-sync --no-clone-bundle --no-tags -j4
cp device/blackberry/athena/docs/CLAUDE.md ./CLAUDE.md
```

- `docs/local_manifests.xml` ma już poprawione audio: `revision="lineage-24.0-caf-sdm660"` (D1).
  Plik pobierzesz przed syncem z
  `https://raw.githubusercontent.com/Xmind404/android_device_blackberry_athena/lineage-24.0/docs/local_manifests.xml`.
- `CLAUDE.md` z korzenia drzewa nie jest w żadnym repo. Kopia jest w `docs/CLAUDE.md`
  (stan z 2026-11-23). Jeśli zmieniasz `CLAUDE.md`, zaktualizuj też kopię.
- `repo init` instaluje hook `commit-msg` (Change-Id) w każdym projekcie. Sprawdź, czy jest
  (`ls .git/hooks/commit-msg` w danym repo). Change-Id nigdy nie wpisujemy ręcznie.
- Po syncu projekty są w detached HEAD. Przed commitami: `repo start <gałąź> <ścieżka>` albo
  `git push github HEAD:refs/heads/<gałąź>` (gałęzie w tabeli w `docs/CLAUDE.md`).
- Poza drzewem (nie są kasowane): `~/android/bbkey2-ref/` (klony referencyjne Lineage-BBKey2
  z remote'ami `Lineage-BBKey2` i `LineageOS`, klon własnego 22.2 `xmind_fb`, robocze klony
  `work/audio-sdm660`, `tools/portmsg.sh` do treści commitów portów) i pamięć Claude
  w `~/.claude/projects/-home-franek-android-LineageOS-24/memory/`.

## 2. Build na tym komputerze (13,7 GB RAM): ustawienia i stan

Analiza Soong (`analyzing Android.bp files`) całego drzewa Androida 17 zajmuje **ponad 42 GB**.
Dwa przebiegi skończyły się OOM-killem (`exit=137`, `Free swap = 0kB` w `dmesg`), także
z `SOONG_INCREMENTAL_ANALYSIS=false`. Wcześniej WSL padał całkiem, bo miał limit 14 GB przy
13,7 GB RAM w Windowsie.

**Ustawienia hosta** (`C:\Users\fkarb\.wslconfig`, po zmianie `wsl --shutdown`):
```ini
[wsl2]
memory=10GB
processors=8
swap=64GB
```

**Przełącznik `ATHENA_LOW_MEMORY_BUILD=true`** (athena `41d0a07`, `21b8c56`, `369a2fb`):
`lineage_athena.mk` pomija w `PRODUCT_SOURCE_ROOT_DIRS` testy, CTS, automotive, inne urządzenia
i nieużywane VNDK (ok. 27% modułów mniej), a `BoardConfig.mk` ustawia
`ALLOW_MISSING_DEPENDENCIES := true`. Bez tej zmiennej nic się nie zmienia.

**Komenda builda** (właściciel):
```
cd ~/android/LineageOS_24/
source build/envsetup.sh
export ATHENA_LOW_MEMORY_BUILD=true
export SOONG_INCREMENTAL_ANALYSIS=false
breakfast athena userdebug
set -o pipefail
m bacon -j2 2>&1 | tee ~/logs/bacon-$(date +%H%M%S).txt
```
Sukces = kod wyjścia 0 **i** `build completed successfully` w tym samym logu.

**Do sprawdzenia przy pierwszym buildzie z przełącznikiem:**
- [ ] Czy analiza przechodzi wczesne mutatory. Dotąd naprawione: brak pluginu `csuite_test`
      (przywrócony katalog pluginu) i odwołania `default_team` do `packages/services/Car/teams`
      (przywrócone pliki z defaults/teams/licencjami).
      Gdy pojawi się `depends on skipped module` albo `unrecognized module type`:
      ```
      python3 device/blackberry/athena/tools/low-memory-closure.py "$(get_build_var PRODUCT_SOURCE_ROOT_DIRS)"
      ```
      Ścieżki z wyniku dopisz (bez `-`) do listy w `lineage_athena.mk`. Plugin (`unrecognized
      module type`) znajdziesz przez `grep -rl 'pluginFor' <wykluczony katalog>`.
- [ ] Ile pamięci zajmuje `soong_build` (`ps -o rss,etime -C soong_build`, `free -m`, swap).
      Jeśli i tak przekroczy RAM + 64 GB swapu, zostaje build na maszynie z ≥ 64 GB RAM
      (np. serwer w chmurze na kilka godzin).
- [ ] Czy po analizie Kati/Make nie wywraca się na `Android.mk` z pominiętych katalogów.
- [ ] Czy w fazie kompilacji nie brakuje modułu, który faktycznie trafia do obrazu
      (`ALLOW_MISSING_DEPENDENCIES` przesuwa taki błąd na moment budowania). Wtedy usuń
      odpowiedni wpis z listy wykluczeń.

## 3. Pierwsza kompilacja: gdzie spodziewać się błędów

Kod pisany bez kompilatora. Kolejność sprawdzania (od największego ryzyka). Po każdej
poprawce, jeśli się da, buduj pojedynczy moduł.

| Repo | Co | Moduł do budowania |
|---|---|---|
| frameworks/base | SystemUI: `flashlight/dualtone/DualToneTorch.kt`, `DualToneTorchDialogDelegate.kt` (Compose, `SystemUIDialogFactory.create`), `FlashlightTileWithLevel`, `FlashlightTileUserActionInteractor`, testy Kosmos | `SystemUI` |
| frameworks/base | Keyguard: `PhysicalKeyboardEntry.java`, `KeyguardPinBasedInputView`, kontrolery PinBased/Password, `KeyguardKeyEventInteractor` | `SystemUI` |
| frameworks/base | PowerManagerService (podświetlenie klawiatury/przycisków, listener jasności), JNI `com_android_server_power_PowerManagerService.cpp` | `services` |
| frameworks/base | `ViewRootImpl` (SyntheticTouchKeypadHandler + czułość), `View`, `InputMethodManager` (IME switcher), `Settings.java` + walidatory, `KeyboardLayoutManager`, `BlackBerryKeyboardLayoutPolicy`, `AudioDeviceBroker`, NetworkPolicy, hwui `StatsUtils` | `framework-minus-apex`, `services`, `libhwui` |
| frameworks/native | `TouchInputMapper` (TOUCH_PAD), `EventHub` (filtr STMPE), `InputDispatcher::notifyTouchpadActivity` | `libinputflinger`, `libinputreader` |
| packages/modules/Bluetooth | interop AAC (RawAddress przez wartość), log CLCC | `com.android.btservices` |
| packages/apps/Aperture | tryb PRO (`ProSettings.kt`, `CaptureRequestOptionsBuilder.setProSettings`, panel PRO), spacja, zoom z touchpada, zapis na microSD | `Aperture` |
| device/blackberry/athena | `DeviceSettings` (BlackBerrySettings_athena: KeyHandler, TileService, ustawienia) | `BlackBerrySettings_athena` |
| device/blackberry/sdm660-common | Lights HAL (BATTERY + NOTIFICATIONS), health HAL, sepolicy (torch, touchkeypad), `init.torch.sh` | `m nothing` / `selinux_policy` |
| hardware/blackberry | AIDL touch HAL `vendor.lineage.touch-service.blackberry` | ten moduł |
| hardware/qcom-caf/sdm660/audio | gałąź `caf-sdm660` + 7 poprawek (konflikt w `compress_voip.c` rozwiązany ręcznie) | `audio.primary.sdm660` |
| kernel | EROFS w `athena-perf_defconfig`, `gf_spi.c` (wakelock 2000 ms), porty 656bd38/92a1143/50bcc42 | kernel (w ramach bacon) |

Zasady przy poprawkach: osobny commit na logiczną zmianę, styl z `docs/CLAUDE.md` (zasada 3),
push od razu. Nie zmieniaj zachowania `SyntheticTouchKeypadHandler` (zasada 7: TOUCH_SLOP 8,
0.0117, x→1080, `enqueueInputEvent`; zgoda jest tylko na D3 i mnożnik czułości).

## 4. Po pierwszym flashu: testy na telefonie

Procedura instalacji i komendy adb są w `FEATURES.md` (E2, 5a–21). Logi zapisuj do
`~/logs/<funkcja>.txt`. Najważniejsze:

- [ ] Start systemu, `adb logcat -b all -d > ~/logs/boot.txt`, `adb shell dmesg > ~/logs/dmesg.txt`
      (SELinux denials: `grep avc`).
- [ ] Klawiatura, touch-scroll, podświetlenie, kafelek QS, strona „BlackBerry Keyboard” (5a–6, 17–18).
- [ ] **Latarka (9):** suwak biały na 0 i ciepły na max. Powinna świecić żółta dioda. Jeśli
      świeci biała, zamień `torch_0` i `torch_1` w `sdm660-common/init/init.torch.sh`.
- [ ] PIN SIM/PUK/blokady z klawiatury, także przy losowym układzie PIN (10–11).
- [ ] Wykrywanie układu QWERTZ (7), dioda RGB (19), wybudzanie czytnikiem (20).
- [ ] MindTheGapps przez sideload (E2a, E2c), OTA bez utraty danych (21).
- [ ] Bateria: deep sleep, `dumpsys batterystats` (12).

## 5. Do zrobienia (kod)

Kolejność z `docs/CLAUDE.md` („Kolejność priorytetów”):

1. [ ] Zielony build (sekcje 2–3).
2. [ ] **E2b:** miejsce na system/product pod MindTheGapps (`BOARD_*IMAGE_PARTITION_RESERVED_SIZE`
       lub rozmiar grupy dynamicznej). Sprawdź po pierwszym buildzie `out/target/product/athena/*.img`
       i `misc_info.txt`.
3. [ ] **8. Fingerprint na spacji:** HAL Goodix (AIDL) z vendor athena, sepolicy, enrollment.
       Sprawdź, czy touch klawiatury nie koliduje z czytnikiem.
4. [ ] **3. Ekran:** display HAL caf-sdm660, jasność, obrót, CABL (`cabl-mode`).
5. [ ] **1. NFC, 2. GPS, 4. Bluetooth:** HAL-e, konfiguracja, sepolicy, testy z `FEATURES.md`.
       `gps.conf` zmieniaj tylko w niezbędnym zakresie i opisz każdą zmianę.
6. [ ] **13. Aperture:** przepisać pozostałe 7 commitów Lineage-BBKey2 (`bf29375`, `51f8932`,
       `6bbafbe`, `3b5c9e2`, `d17e805`, `4e562c7`, `a60b0a5` HDR) na strukturę Aperture 24.0,
       z weryfikacją na ekranie KEY2 (podgląd 3:2). HDR scalić z PRO (scene mode HDR nie łączy
       się z ręczną ekspozycją).
7. [ ] Uzupełnić `FEATURES.md` wynikami testów i statusami `verified`.

## 6. Decyzje czekające na właściciela

- **D2:** nowe forki oficjalnego 24.0 (Settings, SettingsIntelligence, LineageParts, Launcher3,
  frameworks/av, hardware/interfaces, ewentualnie recovery). Opis i diff manifestu w `PORTING.md`.
- **D6:** uprawnienie BlackBerry Hub dla BlackBerry Keyboard (rekomendacja: nie portować).
- Własna zmiana 22.2 `c7ffb02` (skala jasności przycisków 0.1–1.0 zamiast 0.01–1.0): czeka na zgodę.

Rozstrzygnięte (nie wracać): D1 przyjęte (audio caf-sdm660), D3 przyjęte (fling `-deltaX`),
D4 odrzucone (bez auto-zatwierdzania App Links), D5 odrzucone (bez FM, Cast, LatinIME,
DownloadProvider). Bez Lokker/K12KB, bez wbudowanych GApps, athenaKB i BlackBerry Keyboard
instalowane ręcznie przez użytkownika (obejście Alt usunięte, base `19fdb6e`).

## 7. Stan repozytoriów (czubki gałęzi na GitHubie)

| Repo (Xmind404/…) | Gałąź | Czubek |
|---|---|---|
| android_device_blackberry_athena | lineage-24.0 | ten commit (po `369a2fb`) |
| android_device_blackberry_sdm660-common | lineage-24.0 | `dbab284` |
| android_vendor_blackberry_athena | lineage-24.0 | `d6320b9` |
| android_vendor_blackberry_sdm660-common | lineage-24.0 | `efa4f81` |
| android_vendor_blackberry_media (publiczne) | lineage-24.0 | `0b007b8` |
| android_kernel_blackberry_sdm660-4p19 | lineage-24.0-athena | `b420eb2` |
| android_hardware_blackberry | lineage-24.0-athena | `5f7c623` |
| android_hardware_qcom_audio | lineage-24.0-caf-sdm660 | `eb8b11e` |
| android_hardware_qcom_display | lineage-24.0-caf-sdm660 | `2b97ab4` |
| android_frameworks_base | lineage-24.0-athena | `ede3b42` |
| android_frameworks_native | lineage-24.0-athena | `a475b9f` |
| android_packages_modules_Bluetooth | lineage-24.0 | `afb7888` |
| android_packages_apps_Aperture | lineage-24.0 | `be84f72` |
