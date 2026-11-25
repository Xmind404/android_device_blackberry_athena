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
| 5a | Klawisze fizyczne (Alt, Shift, Sym, Convenience) | implemented – needs device test (overlay KCM; obejście Alt dla BlackBerry Keyboard usunięte w base 19fdb6e) | `0319a4ee`, `8013a1ed`, native EventHub, IME switcher |
| 5b | Touch na klawiaturze: scroll | implemented – needs device test | `628fd7e7`, `ddf8eec1`, `dc4ea330`, `76605e9e`, `790cd2ba`, native `8eeea09` |
| 5c | Podświetlenie klawiatury i przycisków | implemented – needs device test (adaptacja wg czujnika światła: Etap 3 pkt 6) | seria PMS (12 commitów), kernel `656bd38`, `92a1143` |
| 6 | Kafelek QS „Dotyk klawiatury” (ikona SVG) + Ustawienia → System → „BlackBerry Keyboard” (pl/en), adaptacja wg jasności wyświetlacza (czujnik światła przy auto-jasności) | implemented – needs device test | device f364896, e73377b; base 040eebe9d |
| 7 | Automatyczny wybór KCM (QWERTY/QWERTZ/AZERTY) | implemented – needs device test: init.keypad.sh czyta wariant fabryczny z partycji trace (trace_util r 8) → sysfs kl → nazwa urządzenia → IDC → KCM; ręczne nadpisanie opcjonalne (domyślnie Automatyczny) | sdm660-common init.keypad.sh, IDC/KCM |
| 8 | Fingerprint na spacji | not started | vendor athena (Goodix AIDL) |
| 9 | Latarka dual-tone: dotknięcie kafelka = obie diody (domyślnie 50/50); przytrzymanie = panel z dwoma pionowymi suwakami (lewy biały, prawy ciepły) na przyciemnionym tle, cofnij/dotyk obok zamyka | implemented – needs device test (potwierdzić, że torch_0 = biała, torch_1 = żółta) | base 098e485fd, sdm660-common 0bd8451 |
| 10 | PIN SIM (i PUK) z klawiatury, także przy losowym układzie PIN | implemented – needs device test (cyfry z KCM bez Alt, przełącznik keyboard_pin_input) | base 059d23b16 |
| 11 | PIN / hasło blokady z klawiatury (pierwszy klawisz na ekranie blokady otwiera bouncer i nie przepada) | implemented – needs device test; Compose bouncer (scene_container) nieobsługiwany, flaga wyłączona w cp2a | base 059d23b16 |
| 12 | Bateria: power_profile 3500 mAh (OK), deep sleep włączany po starcie (post_boot, OK), schedutil 4.19 (OK), limit ładowania (Lineage ChargingControl), wyłączona telemetria hwui; podświetlenie gaśnie poza AWAKE | implemented – needs device measurement | sdm660-common d744aa2, base e44c4aebe, e5ca7961c |
| 13 | Aperture: spacja = migawka (start/stop w wideo), touchpad oś X = zoom, zapis na microSD, tryby PRO · ZDJĘCIE · WIDEO · SKANER (PRO: ISO, czas, EV, WB, K, MF z zakresów CameraCharacteristics) | implemented – needs compile + device test; 7 poprawek Lineage-BBKey2 jeszcze do portu (PORTING.md) | Aperture f0bfc79, 374888b, e294a0d |
| 14 | Dźwięki systemowe BlackBerry (klik, blokada/odblokowanie, słaba bateria, migawka/zrzut, ostrość, start/stop wideo, klawisze IME, dok, Trusted, ładowanie bezprzewodowe) + dzwonki, powiadomienia, alarmy | implemented – needs device test | vendor/blackberry/media 0b007b8, device 843ac5b |
| 15 | Tapety BlackBerry (138, 10 kategorii) w „Tapety i styl” | implemented – needs device test | vendor/blackberry/media 0b007b8 |
| 16 | Animacja rozruchu BlackBerry zamiast LineageOS | implemented – needs device test | vendor/blackberry/media 0b007b8, device 843ac5b |
| 17 | Klawisz Convenience (Asystent/AI domyślnie, Aparat, Latarka, Nic) i dwukrotne naciśnięcie zasilania (Aparat domyślnie, Portfel, Nic) w „BlackBerry Keyboard” | implemented – needs device test | athena 34cf33a, sdm660-common 0207c32 |
| 18 | Czułość dotyku klawiatury 50–200 % (100 % = stałe BlackBerry, bez zmian dla athenaKB) | implemented – needs device test | base 3e520add4, athena 34cf33a |
| 19 | Dioda RGB: kolory powiadomień per aplikacja / automatyczny kolor ikony, bateria: czerwona/pomarańczowa (ładowanie)/zielona | implemented – needs device test | sdm660-common dbab284 |
| 20 | Szybsze wybudzanie czytnikiem na spacji (wakelock IRQ 500 → 2000 ms) | implemented – needs device test | kernel b420eb2 |
| 21 | OTA przez Updater bez utraty danych + generator athena.json | implemented – needs release test | athena tools/generate-ota-json.sh |

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

### 7. Wykrywanie układu
Oczekiwane na QWERTZ: urządzenie `stmpe_qwertz_keypad`, KCM `stmpe_qwertz`, Y/Z na właściwych miejscach.
```
adb shell getprop ro.hwf.keypadlanguage > ~/logs/layout.txt          # oczekiwane: "3 qwertz"
adb shell getprop vendor.bb.keyboard.layout.applied >> ~/logs/layout.txt   # auto:qwertz
adb shell dumpsys input | grep -A12 -i "stmpe" >> ~/logs/layout.txt
```

### 10/11. PIN SIM, PIN i hasło blokady z klawiatury
Oczekiwane: na ekranie PIN-u (SIM lub blokady) litery W E R / S D F / Y X C i klawisz 0 wpisują
1–9 i 0 bez Alt, Del kasuje, Enter zatwierdza; na ekranie blokady (przed przesunięciem) pierwsza
wpisana cyfra/litera otwiera pole PIN/hasła i od razu się w nim pojawia; hasło wpisuje się bez
klawiatury ekranowej, także zaraz po restarcie. Po wyłączeniu „Wpisywanie PIN-u z klawiatury”
cyfry wymagają Alt, a ekran blokady nie reaguje na litery.
```
adb shell settings get secure keyboard_pin_input > ~/logs/pin.txt
# Test też z: Ustawienia → Bezpieczeństwo → Blokada ekranu → Losowy układ PIN-u (cyfra z klawiatury = ta sama cyfra)
adb logcat -d -b main | grep -iE "KeyguardKeyEvent|Bouncer|KeyguardPin" >> ~/logs/pin.txt
```

### 9. Latarka
Oczekiwane: dotknięcie kafelka włącza obie diody naraz; przytrzymanie otwiera panel z suwakami,
zmiana działa na żywo i zostaje po restarcie; suwak „Biała” steruje białą diodą, „Ciepła” żółtą
(jeśli odwrotnie – zamienić torch_0/torch_1 w init.torch.sh). Zdjęcia z lampą bez zmian.
```
adb root
adb shell 'for l in /sys/class/leds/led:torch_* /sys/class/leds/led:switch_* /sys/class/leds/led:flash_*; do echo "$l $(cat $l/brightness) max=$(cat $l/max_brightness)"; done' > ~/logs/torch.txt   # latarka WYŁĄCZONA
# włącz latarkę z QS:
adb shell 'for l in /sys/class/leds/led:torch_* /sys/class/leds/led:switch_*; do echo "$l $(cat $l/brightness)"; done' >> ~/logs/torch.txt
adb shell getprop | grep torch >> ~/logs/torch.txt
adb shell dumpsys media.camera | grep -iA5 torch >> ~/logs/torch.txt
```

### 13. Aperture
Oczekiwane: pasek trybów Pro · Zdjęcie · Wideo · Skaner; spacja robi zdjęcie / start-stop nagrywania
(Ustawienia aparatu → „Funkcja spacji”); przesunięcie palcem po klawiaturze w prawo przybliża, w lewo
oddala, bez wyzwalania zdjęcia; „Zapisuj na karcie SD” zapisuje do DCIM/Camera na karcie, a bez karty
pokazuje komunikat i zapisuje wewnętrznie; w trybie Pro widać tylko kontrolki obsługiwane przez HAL,
a zdjęcie z ręcznym ISO/czasem ma te wartości w EXIF.
```
adb shell dumpsys media.camera | grep -iE "android.sensor.info.sensitivityRange|exposureTimeRange|availableCapabilities" > ~/logs/aperture.txt
adb shell ls /storage/*/DCIM/Camera | tail -5 >> ~/logs/aperture.txt
adb logcat -d | grep -iE "Aperture|CameraX|Camera2" >> ~/logs/aperture.txt
```

### 17–18. Klawisze i czułość
Oczekiwane: Convenience = Asystent (z GApps: Gemini); po zmianie na Aparat otwiera aparat (także przy
zablokowanym ekranie, w trybie bezpiecznym), Latarka przełącza latarkę; dwukrotne naciśnięcie
zasilania otwiera aparat; suwak czułości zmienia szybkość przewijania od razu.
```
adb shell settings get secure convenience_key_action > ~/logs/keys.txt
adb shell settings get secure double_tap_power_button_gesture_enabled >> ~/logs/keys.txt
adb shell getprop persist.vendor.touchkeypad.sensitivity >> ~/logs/keys.txt
adb logcat -d | grep -iE "BlackBerryKeyHandler|device key handler" >> ~/logs/keys.txt
```

### 19. Dioda powiadomień
Ustawienia → Powiadomienia → Dioda powiadomień (kolor per aplikacja, „automatyczny” = kolor ikony)
i Ustawienia → Bateria → Dioda baterii. Przy ładowaniu bez powiadomień: pomarańczowa.
```
adb shell cat /sys/class/leds/red/brightness /sys/class/leds/green/brightness /sys/class/leds/blue/brightness > ~/logs/led.txt
adb shell dumpsys lights >> ~/logs/led.txt
```

### 20. Czytnik linii papilarnych
Oczekiwane: przy wyłączonym ekranie dotknięcie spacji odblokowuje bez ~1 s opóźnienia.
```
adb logcat -c   # wyłącz ekran, odblokuj palcem
adb logcat -d -v time | grep -iE "gx_fpd|fingerprint|goodix|PowerManagerService.*Waking" > ~/logs/fp.txt
adb shell cat /sys/kernel/debug/wakeup_sources 2>/dev/null | grep fp_wakelock >> ~/logs/fp.txt
```

### 21. OTA, aktualizacje i flashowanie
- **Dane użytkownika:** Updater LineageOS pobiera pełną paczkę i instaluje ją w recovery bez formatowania
  /data, więc aktualizacja OTA nie usuwa danych.
- **Aplikacje zamykające się po aktualizacji (problem z 22.2):** najczęstsza przyczyna to zmiana klucza
  podpisu między buildami (np. wydanie Lineage-BBKey2 podpisane jego kluczami → własny build z test-keys)
  bez czyszczenia danych; wtedy aplikacje systemowe z innym podpisem nie mogą przejąć swoich danych.
  Zasada: zawsze aktualizuj buildem podpisanym tym samym kluczem co zainstalowany system; przy zmianie
  źródła/klucza zrób Format data. Odcisk builda (stały, dla certyfikacji Play) nie blokuje wykrycia
  aktualizacji, bo PackageManager uwzględnia też ro.build.version.incremental, zmieniający się co build.
- **Wydanie OTA:** po `m bacon` wygeneruj wpis Updatera i wrzuć paczkę jako release o tej samej nazwie:
```
cd ~/android/LineageOS_24
Z=$(ls -t out/target/product/athena/lineage-24.0-*-athena.zip | head -1)
device/blackberry/athena/tools/generate-ota-json.sh "$Z" > ~/athena.json
# release: gh release create "$(basename "$Z" .zip)" "$Z" -R Xmind404/distribution
# potem podmień athena.json na gałęzi lineage-24.0 repo Xmind404/distribution
```
- **Flashowanie obrazów (fastboot / narzędzia WebUSB):** obrazy do wgrania przez fastboot
  (boot, recovery, dtbo, system, vendor) są w `out/target/product/athena/*.img`; paczkę z nimi daje
  `m updatepackage`. Narzędzia WebUSB oparte na fastboot wgrywają te same obrazy – bez
  `fastboot -w` dane zostają.

### 12. Bateria
Oczekiwane: Ustawienia → Bateria → Kontrola ładowania dostępna i zatrzymuje ładowanie na limicie;
suspend_stats rośnie przy wyłączonym ekranie; brak wakelocków od podświetlenia.
```
adb shell cat /sys/module/lpm_levels/parameters/sleep_disabled          # oczekiwane: N / 0
adb shell cat /sys/class/power_supply/battery/charging_enabled
adb shell dumpsys lineagehealth 2>/dev/null | head -20
adb shell dumpsys batterystats --reset
# ~8 h przy wyłączonym ekranie
adb shell cat /sys/power/suspend_stats/success /sys/power/suspend_stats/fail
adb shell dumpsys batterystats > ~/logs/batterystats.txt
```
