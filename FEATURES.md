# FEATURES.md: funkcje KEY2 (athena) na LineageOS 24.0

Statusy: `not started` / `in progress` / `implemented – needs device test` / `verified` / `blocked (powód)`.
Wyniki testów zbieram przy najbliższym buildzie. Logi zapisuj do `~/logs/<funkcja>.txt`.

| # | Funkcja | Status | Źródło (PORTING.md) |
|---|---|---|---|
| E2a | EROFS w kernelu (GMS APEX z MindTheGapps) | not started | Etap 2a |
| E2b | Miejsce na system pod MindTheGapps | not started | Etap 2b |
| E2c | Recovery: sideload niepodpisanego add-onu, reboot do recovery | not started | Etap 2c, `1048d83` |
| 1 | NFC | not started | – |
| 2 | GPS | not started | – |
| 3 | Ekran (panel, jasność, obrót, CABL) | not started | display: import = czubek Lineage-BBKey2 |
| 4 | Bluetooth (A2DP/HFP/LE, poprawki Lineage-BBKey2) | not started | `01d8bdc`, `cc2ba40`, `6560862`, `72f2d1e7`, `9aa4ece`, `db18583` |
| 5a | Klawisze fizyczne (Alt, Shift, Sym, Convenience) | in progress (overlay KCM, Alt w BB Keyboard: implemented – needs device test) | `0319a4ee`, `8013a1ed`, native EventHub, IME switcher |
| 5b | Touch na klawiaturze: scroll | implemented – needs device test | `628fd7e7`, `ddf8eec1`, `dc4ea330`, `76605e9e`, `790cd2ba`, native `8eeea09` |
| 5c | Podświetlenie klawiatury i przycisków | implemented – needs device test (adaptacja wg czujnika światła: Etap 3 pkt 6) | seria PMS (12 commitów), kernel `656bd38`, `92a1143` |
| 6 | Kafelek QS „Dotyk klawiatury” + Ustawienia → System → „BlackBerry Keyboard” | not started | DeviceSettings (istnieje), `0f15676a` (replaced) |
| 7 | Automatyczny wybór KCM (QWERTY/QWERTZ/AZERTY) | not started | `0319a4ee`, native `fb827f8` |
| 8 | Fingerprint na spacji | not started | vendor athena (Goodix AIDL) |
| 9 | Latarka dual-tone (+ panel suwaków) | not started | – |
| 10 | PIN SIM z klawiatury | not started | `c87cf219`, `ba9b79d1` (adapted) |
| 11 | PIN / hasło blokady z klawiatury | not started | `c87cf219`, `ba9b79d1` (adapted) |
| 12 | Bateria (power_profile, deep sleep, cpufreq, ChargingControl) | not started | `0693b591` |
| 13 | Aperture: spacja = migawka, swipe na klawiaturze = zoom | not started | + 10 commitów Aperture Lineage-BBKey2 |

## Plan testów (komendy uzupełniam w miarę implementacji)

### E2: instalacja z GApps i bez
Procedura zostanie opisana po Etapie 2, zgodnie z raportem.

### 5a. Klawisze / KCM
Oczekiwane: Alt+klawisz daje znak z warstwy Alt aktywnego KCM (QWERTZ), Sym działa w athenaKB,
a w logcat przy zmianie układu widać `BlackBerry KCM: native=... action=apply|native`.
```
adb logcat -c; # zmień układ w Ustawienia → System → Klawiatura fizyczna
adb logcat -d | grep -i "BlackBerry KCM" > ~/logs/kcm.txt
adb shell dumpsys input | grep -A30 -i stmpe >> ~/logs/kcm.txt
```

### 5b. Touch-scroll
Oczekiwane: pionowy i poziomy scroll w Ustawieniach, Chrome, PDF; brak podświetlania
pierwszej ikony/elementu po dotknięciu klawiatury; fling po szybkim ruchu; brak scrolla
przy `persist.vendor.touchkeypad.scrolling_disabled=true`.
```
adb shell dumpsys input | grep -B5 -A25 -i "touch_keypad\|touchPad" > ~/logs/touchscroll.txt
adb shell setprop persist.vendor.touchkeypad.scrolling_disabled true   # scroll ma zniknąć
adb shell setprop persist.vendor.touchkeypad.scrolling_disabled false  # scroll wraca
adb logcat -d >> ~/logs/touchscroll.txt
```

### 5c. Podświetlenie
Oczekiwane: klawiatura i przyciski gasną po 10 s bez aktywności, zapalają się po
naciśnięciu klawisza (i po dotyku klawiatury, gdy włączone „tylko przy naciśnięciu” +
„przy dotyku”), skalują się z suwakiem jasności ekranu, klawiatura gaśnie przy jasności
ekranu ≥ 100/255, obie gasną natychmiast po wyłączeniu ekranu (także w AOD/doze).
```
adb shell ls /sys/class/leds/ > ~/logs/backlight.txt
adb shell dumpsys power | grep -E "mKeyboard|mButton|mScreenBrightnessSetting" >> ~/logs/backlight.txt
adb shell settings put secure keyboard_backlight_timeout 3000   # zmiana działa od razu
adb shell dumpsys power | grep -i wake_lock >> ~/logs/backlight.txt   # brak wakelocków od podświetlenia
```

### 12. Bateria
```
adb shell dumpsys batterystats --reset
# ~8 h przy wyłączonym ekranie
adb shell cat /sys/power/suspend_stats/success /sys/power/suspend_stats/fail
adb shell dumpsys batterystats > ~/logs/batterystats.txt
```
