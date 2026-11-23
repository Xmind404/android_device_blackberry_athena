# FEATURES.md: funkcje KEY2 (athena) na LineageOS 24.0

Statusy: `not started` / `in progress` / `implemented – needs device test` / `verified` / `blocked (powód)`.
Wyniki testów zbieram przy najbliższym buildzie. Logi zapisuj do `~/logs/<funkcja>.txt`.

| # | Funkcja | Status | Źródło (PORTING.md) |
|---|---|---|---|
| E2a | EROFS w kernelu (GMS APEX z MindTheGapps) | implemented – needs device test (kernel 8996931) | Etap 2a |
| E2b | Miejsce na system pod MindTheGapps | not started | Etap 2b |
| E2c | Recovery: sideload niepodpisanego add-onu, reboot do recovery | supported by official 24.0 recovery (`ask_to_continue_unverified`, „Reboot to recovery”) – needs device test | Etap 2c |
| 1 | NFC | not started | – |
| 2 | GPS | not started | – |
| 3 | Ekran (panel, jasność, obrót, CABL) | not started | display: import = czubek Lineage-BBKey2 |
| 4 | Bluetooth (A2DP/HFP/LE, poprawki Lineage-BBKey2) | not started | `01d8bdc`, `cc2ba40`, `6560862`, `72f2d1e7`, `9aa4ece`, `db18583` |
| 5a | Klawisze fizyczne (Alt, Shift, Sym, Convenience) | in progress (overlay KCM, Alt w BB Keyboard: implemented – needs device test) | `0319a4ee`, `8013a1ed`, native EventHub, IME switcher |
| 5b | Touch na klawiaturze: scroll | implemented – needs device test | `628fd7e7`, `ddf8eec1`, `dc4ea330`, `76605e9e`, `790cd2ba`, native `8eeea09` |
| 5c | Podświetlenie klawiatury i przycisków | implemented – needs device test (adaptacja wg czujnika światła: Etap 3 pkt 6) | seria PMS (12 commitów), kernel `656bd38`, `92a1143` |
| 6 | Kafelek QS „Dotyk klawiatury” (ikona SVG) + Ustawienia → System → „BlackBerry Keyboard” (pl/en), adaptacja wg jasności wyświetlacza (czujnik światła przy auto-jasności) | implemented – needs device test | device f364896, e73377b; base 040eebe9d |
| 7 | Automatyczny wybór KCM (QWERTY/QWERTZ/AZERTY) | not started | `0319a4ee`, native `fb827f8` |
| 8 | Fingerprint na spacji | not started | vendor athena (Goodix AIDL) |
| 9 | Latarka dual-tone (+ panel suwaków) | not started | – |
| 10 | PIN SIM z klawiatury | not started | `c87cf219`, `ba9b79d1` (adapted) |
| 11 | PIN / hasło blokady z klawiatury | not started | `c87cf219`, `ba9b79d1` (adapted) |
| 12 | Bateria (power_profile, deep sleep, cpufreq, ChargingControl) | not started | `0693b591` |
| 13 | Aperture: spacja = migawka, touchpad oś X = zoom, zapis na microSD, tryby PRO · ZDJĘCIE · WIDEO · SKANER (PRO: ISO, czas, EV, WB, ostrość) | not started | + 10 commitów Aperture Lineage-BBKey2 |
| 14 | Dźwięki systemowe BlackBerry (klik, blokada/odblokowanie, słaba bateria, migawka/zrzut, ostrość, start/stop wideo, klawisze IME, dok, Trusted, ładowanie bezprzewodowe) + dzwonki, powiadomienia, alarmy | implemented – needs device test | vendor/blackberry/media 0b007b8, device 843ac5b |
| 15 | Tapety BlackBerry (138, 10 kategorii) w „Tapety i styl” | implemented – needs device test | vendor/blackberry/media 0b007b8 |
| 16 | Animacja rozruchu BlackBerry zamiast LineageOS | implemented – needs device test | vendor/blackberry/media 0b007b8, device 843ac5b |

## Plan testów (komendy uzupełniam w miarę implementacji)

### E2: instalacja z GApps i bez
Pełna procedura zostanie dopisana po Etapie 2b (miejsce na system). Weryfikacja EROFS / GMS po instalacji z GApps:
```
adb shell zcat /proc/config.gz | grep EROFS > ~/logs/gapps.txt   # jeśli dostępne; inaczej: adb shell cat /proc/filesystems | grep erofs
adb shell cat /proc/filesystems | grep erofs >> ~/logs/gapps.txt
adb shell pm list packages | grep -E "com.google.android.gms|com.android.vending" >> ~/logs/gapps.txt
adb shell dumpsys apexservice 2>/dev/null | grep -A3 gmssystem >> ~/logs/gapps.txt
adb logcat -d | grep -iE "apexd|gservices" >> ~/logs/gapps.txt
```

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

### 14. Dźwięki BlackBerry
Oczekiwane: kliknięcia (z włączonym „Dźwięk przy dotknięciu”), blokada/odblokowanie (włączony
„Dźwięk blokady ekranu”), zrzut ekranu i zdjęcie w Aperture, start/stop nagrywania grają dźwięki
BlackBerry; listy dzwonków/powiadomień/alarmów zawierają m.in. BBRY_*.
```
adb shell ls /product/media/audio/ui /product/media/audio/ringtones | head -40 > ~/logs/sounds.txt
adb shell md5sum /product/media/audio/ui/Effect_Tick.ogg /product/media/audio/ui/camera_click.ogg >> ~/logs/sounds.txt
adb shell settings get global lock_sound >> ~/logs/sounds.txt
adb shell settings put system sound_effects_enabled 1
```

### 15. Tapety BlackBerry
Oczekiwane: Ustawienia → Tapety i styl → Tapety systemowe pokazuje kategorie BlackBerry,
Ekran blokady, Krajobrazy… z nazwami po polsku; tapetę da się ustawić na ekran główny i blokady.
```
adb shell pm list packages | grep blackberry.wallpapers > ~/logs/wallpapers.txt
adb shell dumpsys package org.lineageos.blackberry.wallpapers | grep -E "codePath|flags" >> ~/logs/wallpapers.txt
```

### 16. Animacja rozruchu
Oczekiwane: przy starcie animacja BlackBerry wyśrodkowana, bez obcięcia po bokach, na czarnym tle;
w motywie ciemnym ta sama.
```
adb shell ls -l /product/media/bootanimation.zip /product/media/bootanimation-dark.zip > ~/logs/bootanim.txt
adb shell sha1sum /product/media/bootanimation.zip >> ~/logs/bootanim.txt   # 9802fee4b5e3f997f1872e51af3f709795d4aadf
```

### 6. Kafelek QS i strona BlackBerry Keyboard
Oczekiwane: w edycji panelu QS jest kafelek „Dotyk klawiatury” z ikoną klawiatury ze strzałkami;
dotknięcie włącza/wyłącza touchpad (scroll znika/wraca), długie przytrzymanie otwiera stronę
„BlackBerry Keyboard”; stan zgodny ze stroną i po restarcie. Przy auto-jasności podświetlenie
klawiatury ciemnieje w ciemnym pokoju i gaśnie w jasnym świetle (gdy włączone „Wyłączaj przy jasnym ekranie”).
```
adb shell getprop persist.vendor.touchkeypad.disabled > ~/logs/kbsettings.txt
adb shell cat /sys/devices/touch_keypad/turn_off >> ~/logs/kbsettings.txt
adb shell settings get secure keyboard_backlight_enabled >> ~/logs/kbsettings.txt
adb shell dumpsys power | grep -E "mDisplayBrightnessFraction|mKeyboard" >> ~/logs/kbsettings.txt
```

### 12. Bateria
```
adb shell dumpsys batterystats --reset
# ~8 h przy wyłączonym ekranie
adb shell cat /sys/power/suspend_stats/success /sys/power/suspend_stats/fail
adb shell dumpsys batterystats > ~/logs/batterystats.txt
```
