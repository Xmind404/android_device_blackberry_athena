# CLAUDE.md: LineageOS 24.0 (Android 17) dla BlackBerry KEY2, tylko `athena`

## Cel
Budujemy LineageOS 24.0 na kernelu 4.19 **wyłącznie dla BlackBerry KEY2 (athena)**. Nie dla KEY2 LE (bbf100) ani innych urządzeń. Telefon ma działać **jak oryginalne BlackBerry KEY2, tylko na Androidzie 17**, i ma być dopracowany. Mam wariant **QWERTZ**.

ROM ma być **czysty, bez Google**. GApps (MindTheGapps 17) **nie są wbudowane w ROM**. Użytkownik może je opcjonalnie doinstalować sam przez `adb sideload` w recovery podczas instalacji, jak w standardowej procedurze LineageOS. Szczegóły w Etapie 2.

Drzewo builda: `~/android/LineageOS_24/` (WSL2 Ubuntu 22.04, 16 GB RAM, dlatego `-j2`). Wszystkie komendy wykonujesz w tym katalogu.

## Tryb pracy: autonomiczny
- Pracujesz sam, etap po etapie, bez pytania o zgodę na każdy krok. Decyzje techniczne podejmujesz samodzielnie i uzasadniasz je w commitach oraz w PORTING.md / FEATURES.md.
- Zatrzymujesz się i czekasz na mnie **tylko** wtedy, gdy:
  1. potrzebny jest `repo sync` (zmiana manifestu lub nowe repo),
  2. potrzebny jest pełny build i test na telefonie (logi z urządzenia),
  3. decyzja jest nieodwracalna (force-push, usunięcie gałęzi, przepisanie historii),
  4. dochodzisz do rzeczy, która według zasad wymaga mojej zgody (zasady 7 i 8).
- Gdy funkcji nie da się zweryfikować bez telefonu, przygotuj implementację, popraw `m nothing`, opisz test w FEATURES.md i przejdź do następnej funkcji. Wyniki testów zbiorczo zbiorę przy najbliższym buildzie.
- Gdy wrócę z logami (podam ścieżkę do pliku), najpierw je przeanalizuj, napraw, co trzeba, i kontynuuj plan.
- Stan pracy między sesjami trzymasz w `PORTING.md` i `FEATURES.md`. Na początku każdej nowej sesji przeczytaj je i kontynuuj od miejsca, w którym skończyłeś.

## Repozytoria (local_manifest)
Wszystkie są moimi forkami pod `github.com/Xmind404`:

| Ścieżka | Repo | Gałąź | Pochodzenie |
|---|---|---|---|
| device/blackberry/sdm660-common | android_device_blackberry_sdm660-common | lineage-24.0 | Lineage-BBKey2 22.2 (kernel 4.19) |
| device/blackberry/athena | android_device_blackberry_athena | lineage-24.0 | Lineage-BBKey2 22.2 (kernel 4.19) |
| vendor/blackberry/sdm660-common | android_vendor_blackberry_sdm660-common | lineage-24.0 | Lineage-BBKey2 22.2 (kernel 4.19) |
| vendor/blackberry/athena | android_vendor_blackberry_athena | lineage-24.0 | Lineage-BBKey2 22.2 (kernel 4.19) |
| kernel/blackberry/sdm660-4p19 | android_kernel_blackberry_sdm660-4p19 | lineage-24.0-athena | Lineage-BBKey2 22.2 (kernel 4.19) |
| hardware/blackberry | android_hardware_blackberry | lineage-24.0-athena | Lineage-BBKey2 22.2 (kernel 4.19) |
| hardware/qcom-caf/sdm660/audio | android_hardware_qcom_audio | lineage-24.0-caf-msm8998 | Lineage-BBKey2 22.2 (kernel 4.19) |
| hardware/qcom-caf/sdm660/display | android_hardware_qcom_display | lineage-24.0-caf-sdm660 | Lineage-BBKey2 22.2 (kernel 4.19) |
| frameworks/base | android_frameworks_base | lineage-24.0-athena | **oficjalne LineageOS 24.0** |
| frameworks/native | android_frameworks_native | lineage-24.0-athena | **oficjalne LineageOS 24.0** |
| packages/modules/Bluetooth | android_packages_modules_Bluetooth | lineage-24.0 | **oficjalne LineageOS 24.0** |
| packages/apps/Aperture | android_packages_apps_Aperture | lineage-24.0 | **oficjalne LineageOS 24.0** |

### Dwie grupy repo, dwie różne strategie
**A. Device / vendor / kernel / hardware (pierwsze 8):** skopiowane z https://github.com/Lineage-BBKey2/ (gałęzie 22.2 z kernelem 4.19). To baza sprzętowa, która powinna działać. Trzeba ją tylko dostosować do 24.0: zmienne buildu, sepolicy, VINTF, HAL.

**B. frameworks/base, frameworks/native, packages/modules/Bluetooth, packages/apps/Aperture:** to **celowo czyste forki oficjalnego LineageOS 24.0**, żeby wszystkie zależności, API i interfejsy zgadzały się z resztą drzewa 24.0 (art, system/core, libcore, inne moduły mainline itd.).
- **NIE podmieniaj tych repo kodem z gałęzi 22.2 Lineage-BBKey2** i nie kopiuj całych plików z 22.2. Rozjechałyby się zależności.
- Zamiast tego **przepisz (przeportuj) logikę**, którą Lineage-BBKey2 dodało w swoich 22.2, **na kod 24.0**: commit po commicie, przez cherry-pick lub ręczne przepisanie, dostosowane do aktualnych API, klas i struktury 24.0.
- Cel: oficjalne 24.0 plus wszystkie BlackBerry-owe funkcje z Lineage-BBKey2, czyli touch na klawiaturze, obsługa klawiatury, podświetlenie, poprawki Bluetooth i cała reszta.
- Aperture nie ma odpowiednika w Lineage-BBKey2. Tutaj tylko dopisujemy własne funkcje (Etap 3, pkt 13) na oficjalnym kodzie 24.0.
- Każde kolejne repo, które trzeba będzie dodać (np. packages/apps/Settings, SystemUI poza frameworks/base, nfc, gps), też jest forkiem **oficjalnego 24.0** z przeportowaną deltą. Najpierw jednak sprawdź, czy da się to zrobić w device tree, bez forkowania (patrz Etap 3, pkt 6).

## Git i konto GitHub
- **Wszystkie commity i pushe idą z mojego konta GitHub `Xmind404`.** Używaj istniejącej konfiguracji (`git config user.name` / `user.email` oraz uwierzytelnienia `gh` / SSH) i jej nie zmieniaj. Na starcie sprawdź `git config user.name`, `git config user.email` i `gh auth status`. Jeśli to nie jest konto Xmind404 albo push nie działa, zatrzymaj się i daj znać.
- **Autor i committer każdego commita to moje konto**, także przy portach z Lineage-BBKey2. Oryginalnego autora i źródło podaj w treści commita:
  ```
  Ported from Lineage-BBKey2/<repo>@<sha>
  Original-Author: Name <email>
  ```
  Nie używaj `--author` z cudzymi danymi. Przy `git cherry-pick` przestaw autora na moje konto: `git commit --amend --reset-author --no-edit`, a potem dopisz powyższe linie.
- Push na gałęzie z tabeli w repo `Xmind404/...`. Bez force-push i bez przepisywania historii bez mojej zgody.
- Po `repo sync` projekty są w detached HEAD. Przed pracą w danym repo zrób `repo start <gałąź-z-tabeli> <ścieżka>` albo pushuj jawnie: `git push github HEAD:refs/heads/<gałąź-z-tabeli>`. Remote nazywa się `github` (z manifestu), protokół https przez `gh`.

## Zasady pracy (obowiązkowe)
1. **Tylko athena.** Nie dodawaj obsługi innych urządzeń. Jeśli coś w common przeszkadza, możesz to warunkować pod athena.
2. **Git do wszystkiego.** Każda logiczna zmiana to osobny commit w odpowiednim repo, od razu wypchnięty na GitHub (sekcja „Git i konto GitHub”).
3. **Styl commitów** (jak w AOSP/Gerrit):
   - tytuł `Komponent: Krótki opis w trybie rozkazującym` (≤ 72 znaki),
   - pusta linia, potem treść po angielsku, zawinięta do ~72 znaków: **jaki był problem, dlaczego, co zmieniono, czego NIE zmieniono i jak zachowuje się reszta systemu**,
   - przy portach: linie `Ported from ...` i `Original-Author: ...`, a jeśli port wymagał przepisania pod 24.0, także opis, co i dlaczego zmieniłeś względem oryginału,
   - stopka: `Co-authored-by:` z Twoją faktyczną nazwą modelu oraz `Change-Id:`. Change-Id generuje hook Gerrita: zainstaluj go w każdym repo (`commit-msg` hook), **nie wymyślaj go ręcznie**.

   Przykład stylu:
   ```
   InputMethodService: Work around legacy BlackBerry Alt overrides

   BlackBerry Keyboard 4.1904.3.16701 hardcodes several AZERTY/QWERTZ Alt
   characters instead of consulting the selected character map. This can
   produce duplicate parentheses in RTL layouts and override adapted maps.

   For the inspected package version and internal STMPE device, temporarily
   bypass the cached family correction when a verified hardcoded assignment
   disagrees with the active KCM. Let the IME retain its existing Unicode
   lookup and effective modifier handling, including sticky Alt.

   Restore the private family cache before completing event dispatch,
   including exceptional exits, without undoing a replacement cache value
   written by the IME itself.

   Leave APK bytes and signatures unchanged. Unsupported versions and
   unrelated devices retain their existing behavior.

   Co-authored-by: <model> <noreply@anthropic.com>
   Change-Id: I<generated by hook>
   ```
4. **Weryfikacja.** Po każdej serii zmian uruchom, **zawsze w jednym wywołaniu powłoki** (każde wywołanie Basha to nowa powłoka i stan `source` przepada):
   ```
   cd ~/android/LineageOS_24/ && source build/envsetup.sh && breakfast athena userdebug && m nothing -j2
   ```
   Każdy build uruchamiaj z `pipefail` i logiem z godziną w nazwie:
   ```
   cd ~/android/LineageOS_24/ && source build/envsetup.sh && breakfast athena userdebug && set -o pipefail && m nothing -j2 2>&1 | tee ~/logs/mnothing-$(date +%H%M%S).txt
   ```
   Sukces oceniaj po dwóch rzeczach naraz: exit code tego wywołania **oraz** „build completed successfully” w logu z tego samego przebiegu. W raporcie podaj nazwę pliku logu. Nie dopisuj niczego po komendzie builda, co mogłoby nadpisać jej kod wyjścia (np. `; echo ...`).

   Oprócz `m nothing` możesz budować pojedyncze moduły, które zmieniłeś, żeby sprawdzić kompilację (np. `framework-minus-apex`, `services`, `SystemUI`, `libinputflinger`, `libinputreader`, kernel), również w jednym wywołaniu powłoki:
   ```
   cd ~/android/LineageOS_24/ && source build/envsetup.sh && breakfast athena userdebug && m <moduł> -j2
   ```
   Pełnych buildów (`m bacon`) i `repo sync` **nie uruchamiasz**, robię to ja. Jeśli potrzebujesz synca albo builda, napisz dokładnie, jaką komendę mam wykonać i po co.
5. **Dokumentacja.** W `device/blackberry/athena` prowadź `PORTING.md` z tabelą: commit źródłowy (Lineage-BBKey2) → commit docelowy → status (ported / adapted / dropped + powód). Dopisz też `FEATURES.md`: każda funkcja z listy, status i sposób testu na telefonie. Oba pliki commituj i pushuj na bieżąco.
6. **Zmiany w manifeście** (nowe repo, remove-project) proponuj jako gotowy diff. Ja je nanoszę i robię `repo sync`.
7. **Nie zmieniaj bez pytania** `SyntheticTouchKeypadHandler` w `ViewRootImpl`, jeśli trafi do portu. Mój keyboard app (athenaKB) jest dopasowany do jego parametrów: wersja z `enqueueInputEvent`, TOUCH_SLOP 8, scroll factor 0.0117, x mapowane na 1080. Jeśli w 24.0 trzeba go zmienić, opisz dlaczego.
8. Moje własne zmiany z forków 22.2 (kernel 4.4) wypisz w PORTING.md w osobnej sekcji, ale **nie portuj ich bez mojej zgody**.
9. **Bez aplikacji z Lineage-BBKey2.** NIE dodajemy Lokker ani K12KB (ani innych community apps z Lineage-BBKey2). Przy portowaniu device/vendor/frameworks pomiń commity, które je dodają (PRODUCT_PACKAGES, prebuilty, privapp-permissions, overlaye, sepolicy pod te aplikacje), i oznacz je w PORTING.md jako `dropped – app not wanted`. Jeśli jakaś logika frameworka była napisana pod nie, przeportuj tylko część systemową, która przyda się innym aplikacjom (np. athenaKB), i opisz to w commicie.
10. **Bez wbudowanych GApps.** Nie dodawaj do drzewa MindTheGapps, `vendor/gapps`, `WITH_GMS` ani żadnych prebuiltów Google. ROM ma tylko umożliwiać poprawną instalację MindTheGapps z zewnętrznej paczki (Etap 2).
11. **Jakość.** Nie zostawiaj półśrodków, TODO bez wpisu w FEATURES.md ani logów debugowych w kodzie produkcyjnym. UI ma wyglądać i zachowywać się jak natywna część LineageOS: Material, tłumaczenia **pl** i **en**, poprawne zachowanie po restarcie i po zmianie użytkownika.

## Etap 0: inwentaryzacja (zacznij od tego)
- Sprawdź konto git / GitHub (sekcja „Git i konto GitHub”).
- Sklonuj do osobnego katalogu (poza drzewem `~/android/LineageOS_24/`) repozytoria Lineage-BBKey2: frameworks_base, frameworks_native, packages_modules_Bluetooth, device/vendor/kernel/hardware, a także ich `local_manifests` (gałąź `lineage-22.2-kernel-4.19`), żeby zobaczyć, które repo nadpisują.
- Dodaj też jako remote oficjalne LineageOS (`github.com/LineageOS`, gałęzie `lineage-22.2` i `lineage-24.0`), żeby mieć punkt odniesienia.
- Dla frameworks/base, frameworks/native i Bluetooth wyciągnij **tylko delty Lineage-BBKey2** względem oficjalnego upstreamu: `git log LineageOS/lineage-22.2..Lineage-BBKey2/lineage-22.2` (plus odpowiednie gałęzie 4.19). Właśnie te commity przepisujemy na 24.0. Sprawdź, czy któreś repo nie jest nadpisywane tylko w manifeście 4.19.
- Sprawdź też inne repo, które Lineage-BBKey2 nadpisuje (np. Settings, SystemUI, LineageParts, nfc, gps, hardware/interfaces). Jeśli takie są, przygotuj diff manifestu z ich dodaniem jako forków **oficjalnego 24.0** z przeportowaną deltą, według strategii B. Pomiń repo i commity związane z Lokker / K12KB (zasada 9).
- Dla każdego commita oceń: czy w 24.0 jest już zbędny (upstream to naprawił), czy przechodzi czysto, czy wymaga przepisania.
- Wynik: PORTING.md z pełną listą i planem, wypchnięty na GitHub. Potem od razu przechodzisz do Etapu 1. Jeśli potrzebne są nowe repo w manifeście, daj mi diff i komendę `repo sync`, a w międzyczasie pracuj nad tym, co już jest w drzewie.

## Etap 1: zielony `m nothing`
Napraw device/vendor/hardware pod 24.0: zmienne buildu, sepolicy, manifesty VINTF, zdeprecjonowane HAL-e i AIDL. Potem przeportuj deltę Lineage-BBKey2 na oficjalne 24.0 w frameworks/native, frameworks/base i Bluetooth. Konflikty rozwiązuj z zachowaniem intencji oryginału, dostosowując się do API 24.0 (nie cofaj kodu 24.0 do stanu z 22.2), i opisz je w commicie.

## Etap 2: zgodność z MindTheGapps instalowanym przez sideload
GApps **nie są częścią ROM-u**. Użytkownik, który chce Google, instaluje je sam w recovery zaraz po ROM-ie, przed pierwszym uruchomieniem. KEY2 (SDM660) to **arm64**, więc używamy paczki arm64:
- arm64 (KEY2): https://github.com/MindTheGapps/17.0.0-arm64/releases/latest
- arm (nie dotyczy KEY2, tylko informacyjnie): https://github.com/MindTheGapps/17.0.0-arm/releases/latest

Zadanie ROM-u to sprawić, żeby taka instalacja działała.

**a) Kernel, EROFS.** Na podstawie https://github.com/kurdworld/android_kernel_blackberry_sdm660/commit/4f89bd40626074166bcf9f3c292291515ad4b9c7:
MindTheGapps dostarcza GMS jako APEX oparty na EROFS (`com.google.android.gmssystem`). Bez `CONFIG_EROFS_FS` apexd nie aktywuje go (isFactory=true / isActive=false), `com.google.android.gms` nie istnieje, a Play Store pada na „Failed to find provider com.google.android.gsf.gservices”.
- Tamten commit dotyczy innego repo kernela. W `kernel/blackberry/sdm660-4p19` **znajdź defconfig faktycznie używany przez athena** (sprawdź `TARGET_KERNEL_CONFIG` w device tree).
- Na 4.19 sprawdź, gdzie jest EROFS: `fs/erofs` (backport ACK) albo `drivers/staging/erofs` (wtedy potrzebne `CONFIG_STAGING=y`). Włącz `CONFIG_EROFS_FS=y` oraz kompresję (`CONFIG_EROFS_FS_ZIP=y` albo odpowiednik w danym drzewie).
- Commit w stylu powyżej, z opisem problemu. Dla wariantu bez Google zmiana jest neutralna.

**b) Miejsce na partycjach.** Sprawdź, czy po instalacji ROM-u zostaje dość wolnego miejsca na system/product (dynamiczne partycje / reserved size, np. `BOARD_*IMAGE_PARTITION_RESERVED_SIZE`), żeby skrypt MindTheGapps się zmieścił. Jeśli trzeba, ustaw rezerwę i opisz to w commicie.

**c) Recovery.** Lineage Recovery musi pozwalać na sideload niepodpisanej paczki add-on (standardowe ostrzeżenie o weryfikacji podpisu + „Yes”) oraz na restart do recovery po wgraniu ROM-u bez uruchamiania systemu.

**d) Dokumentacja.** W FEATURES.md opisz procedurę instalacji wariantu z GApps i bez, dokładnie tymi krokami, które podajesz w raporcie (sekcja niżej).

## Etap 3: funkcje (wszystko ma działać idealnie)
Każdą funkcję opisz w FEATURES.md razem z komendami adb do testu, które ja wykonam.

1. **NFC.** HAL, konfiguracja libnfc-nci/nxp, sepolicy. Test: płatności zbliżeniowe i odczyt tagów.
2. **GPS.** HAL lokalizacji (Qualcomm loc / gnss), konfiguracja, sepolicy, szybki fix (A-GPS / XTRA), działanie z GApps i bez (wtedy z backendem sieciowym LineageOS). Prywatności `gps.conf` nie ruszaj ponad to, co niezbędne do działania, a każdą zmianę w nim opisz w commicie. Test: fix na zewnątrz, czas do fixa, liczba satelitów (`dumpsys location`, logcat `gnss`).
3. **Ekran.** Panel, jasność, obrót, brak artefaktów. Display HAL z hardware/qcom-caf/sdm660/display.
4. **Bluetooth.** Parowanie, audio (A2DP/HFP), LE, port poprawek z Lineage-BBKey2 na oficjalne 24.0.
5. **Klawiatura fizyczna i touch na klawiaturze.**
   - Wszystkie klawisze: Alt, Shift, Sym, klawisz Convenience, BlackBerry-owe zachowania przeportowane z Lineage-BBKey2 na frameworks 24.0.
   - **Touch na klawiaturze ma przewijać jak w oryginalnym BlackBerry KEY2:** płynny pionowy i poziomy scroll w listach, przeglądarce i dokumentach, bez przypadkowych przewinięć podczas pisania, z poprawnym współdziałaniem z IME (athenaKB, Gboard). Port logiki z Lineage-BBKey2 frameworks/base na 24.0, w tym ViewRootImpl (zasada 7).
   - **Podświetlenie klawiatury ma działać:** włączanie przy dotyku lub naciśnięciu klawisza, wygaszanie po timeoucie, poprawna regulacja jasności, wyłączanie przy wyłączonym ekranie. To samo dla podświetlenia **przycisków** (capacitive Back / Home / Recents nad klawiaturą), jeśli sprzęt je ma. Sprawdź sysfs LED-ów (`/sys/class/leds/...`), Lights HAL i obsługę w frameworks / LineageOS (`config_deviceHasVariableButtonBrightness`, keyboard backlight).
6. **Ustawienia klawiatury i kafelek QS.**
   Zaimplementuj możliwie w device tree, jako aplikację urządzenia (np. `BlackBerryParts` w `device/blackberry/athena` lub sdm660-common, systemowa, podpisana kluczem platform). Wstrzyknij ją do Settings przez standardowy mechanizm dashboardu (`com.android.settings.action.EXTRA_SETTINGS` z kategorią System), żeby **nie forkować packages/apps/Settings ani SystemUI**, jeśli się da. Jeśli się nie da, uzasadnij w commicie i zaproponuj fork oficjalnego 24.0.
   - **Kafelek w panelu szybkich ustawień** (tam, gdzie Wi-Fi, Bluetooth itd.): „Dotyk klawiatury”, czyli włącz / wyłącz touch na klawiaturze. Zrób go przez `TileService`, ze stanem aktywny / nieaktywny i ikoną. Długie przytrzymanie otwiera stronę ustawień poniżej. Stan przeżywa restart i jest spójny z przełącznikiem w ustawieniach.
     - **Ikona kafelka (wymóg użytkownika):** gdy touch na klawiaturze już działa, kafelek „Keyboard touchpad” / „Dotyk klawiatury” w panelu szybkich ustawień (ten sam rząd co Wi-Fi, Internet, Bluetooth) ma po lewej stronie ikonę z poniższego SVG. Przekonwertuj ją na wektorowy drawable Androida (stroke w kolorze `?android:attr/colorControlNormal` / tint kafelka, bez zmian w geometrii), z wariantem aktywnym i nieaktywnym zgodnym ze stylem kafelków QS:
       ```svg
       <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 48 48" fill="none">
         <g stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
           <rect x="8" y="14" width="32" height="20" rx="3" />
           <path d="
             M13 19.5h2.5 M19.5 19.5h2.5 M26 19.5h2.5 M32.5 19.5h2.5
             M13 24h2.5 M19.5 24h2.5 M26 24h2.5 M32.5 24h2.5
             M16 28.5h16
           " />
           <path d="M24 14V7m-2.5 2.5L24 7l2.5 2.5" />
           <path d="M24 34v7m-2.5-2.5L24 41l2.5-2.5" />
           <path d="M8 24H2m2.5-2.5L2 24l2.5 2.5" />
           <path d="M40 24h6m-2.5-2.5L46 24l-2.5 2.5" />
         </g>
       </svg>
       ```
   - **Ustawienia → System → „BlackBerry Keyboard”**, nowa strona z opcjami:
     - **Dotyk klawiatury**: on/off (to samo co kafelek),
     - **Podświetlenie klawiatury**: on/off,
     - **Wpisywanie PIN-u z klawiatury**: on/off (PIN SIM oraz PIN / hasło blokady ekranu z klawiatury fizycznej, pkt 10 i 11; domyślnie włączone),
     - **Adaptacyjna jasność klawiatury**: on/off (według czujnika światła),
     - **Jasność klawiatury**: suwak, aktywny, gdy adaptacyjna jasność jest wyłączona; przy włączonej działa jako korekta albo jest wyszarzony, wybierz to, co działa lepiej, i uzasadnij,
     - **Adaptacyjna jasność przycisków**: on/off (podświetlenie przycisków nawigacyjnych).
   - Ustawienia trzymaj w `Settings.System` / `LineageSettings` (lub w device-protected storage, jeśli muszą działać przed pierwszym odblokowaniem) i stosuj je od razu, bez restartu. Wymagane tłumaczenia **pl** i **en** oraz ikona strony spójna ze stylem Settings.
   - Jeśli LineageParts / LineageOS ma już część tych funkcji (np. Przyciski → podświetlenie), nie dubluj ich: podepnij się pod istniejące backendy, a w „BlackBerry Keyboard” zbierz wszystko w jednym miejscu.
7. **Automatyczne wykrywanie układu QWERTY / QWERTZ / AZERTY itd.** Ustal, skąd sprzęt lub bootloader zdradza wariant: właściwość `ro.boot.*`, ID z drivera klawiatury, SKU. Na tej podstawie automatycznie wybieraj właściwy KCM. Rozważ mechanizm IDC Androida 14+ (`keyboard.layoutType`, `keyboard.languageTag`) lub pliki KCM per wariant. Bez ręcznego wyboru w ustawieniach. Mój egzemplarz to QWERTZ, więc testuj na nim.
8. **Fingerprint na spacji.** HAL, sepolicy, enrollment, odblokowanie. Dotyk na klawiaturze nie może przeszkadzać czytnikowi i odwrotnie.
9. **Latarka biała + żółta (dual-tone).** Domyślnie świecą **obie diody jednocześnie** (mieszany kolor, nie sam żółty). Ustal, jak sterować `led:torch_0`, `led:torch_1` i `led:switch_*` (camera provider / torch HAL / sysfs) i gdzie najlepiej to wpiąć, żeby kafelek QS i `CameraManager.setTorchMode` dawały mieszany kolor.
   - **Na koniec (niski priorytet):** przytrzymanie kafelka latarki w QS otwiera rozmyte, przyciemnione tło z dwoma pionowymi suwakami: lewy dla białej diody, prawy dla żółtej, każdy z regulacją mocy od 0 do max. Ustawienie zapamiętywane. Do mocy wykorzystaj API torch strength (`turnOnTorchWithStrengthLevel` / `getTorchStrengthLevel`), jeśli HAL da się do tego dostosować.
10. **PIN karty SIM przy starcie wpisywany z klawiatury fizycznej.** KeyguardSimPinView / KeyguardSimPukView: cyfry z warstwy numerycznej klawiatury, jak w BlackBerry, czyli **bez trzymania Alt** w polach numerycznych. Mapowanie bierz z aktywnego KCM, nie hardcoduj. Enter zatwierdza, Backspace kasuje. Funkcję włącza i wyłącza opcja z pkt 6.
11. **PIN / hasło blokady ekranu z klawiatury fizycznej.** To samo dla KeyguardPINView. KeyguardPasswordView ma działać bez wysuwania klawiatury ekranowej, także zaraz po restarcie (przed pierwszym odblokowaniem / FBE). Funkcję włącza i wyłącza opcja z pkt 6.
12. **Bateria: maksymalny czas pracy.** Poprawny `power_profile.xml` (pojemność KEY2), działający deep sleep (sprawdź suspend_stats i wakelocki), tuning schedutil / cpufreq / devfreq pod SDM660, wyłączenie zbędnego debugu, nieaktywnych usług i polling. Podświetlenie klawiatury i przycisków nie może trzymać wakelocków ani świecić przy wyłączonym ekranie. Dodatkowo opcjonalny limit ładowania (Lineage Health / ChargingControl), żeby chronić baterię. Wszystkie decyzje uzasadnij w commitach i podaj mi komendy do pomiaru (`dumpsys batterystats`, `/sys/power/...`).
13. **Aperture (aparat).** W `packages/apps/Aperture` (oficjalny kod 24.0):
    - **Spacja** działa jak główny przycisk: zdjęcie / start-stop nagrywania (zależnie od trybu). Rozważ też klawisz Convenience.
    - **Touch na klawiaturze:** przesunięcie palcem w prawo przybliża, w lewo oddala. Płynnie, w zakresie zoomu kamery. Sprawdź, jakie zdarzenia faktycznie dostaje aplikacja z handlera touch-keypad (np. `ACTION_SCROLL` z `AXIS_HSCROLL` / `AXIS_VSCROLL`) i obsłuż je w `onGenericMotionEvent`.
    - Gesty nie mogą wywoływać zdjęcia przez przypadek.
    - **Zapis na kartę microSD:** w ustawieniach Aperture opcja „Zapisuj na karcie SD” (zdjęcia i filmy). Gdy karta jest dostępna, zapis idzie do `DCIM/Camera` na wolumenie karty (MediaStore z `getExternalVolumeNames`); gdy karty brak lub została wyjęta, automatyczny powrót do pamięci wewnętrznej z komunikatem. Ustawienie zapamiętywane, tłumaczenia pl/en.
    - **Tryby i tryb PRO:** pasek trybów od lewej do prawej: **PRO · ZDJĘCIE · WIDEO · SKANER**. Tryb PRO daje ręczną kontrolę wszystkiego, co udostępnia Camera2/CameraX na KEY2: ISO, czas naświetlania, kompensacja ekspozycji, balans bieli (preset i temperatura), ręczna ostrość, z wartościami ograniczonymi do zakresów zgłaszanych przez `CameraCharacteristics`. Opcje nieobsługiwane przez HAL są ukryte, nie wyszarzone na zawsze.
    - Przypomnienie: touchpad klawiatury = zoom w osi X (w prawo przybliża, w lewo oddala), spacja = zdjęcie (w WIDEO start/stop).

## Raport po każdym kroku (zawsze w tym formacie)
1. **Co zrobiłem:** lista commitów z linkami do GitHuba.
2. **Wynik `m nothing -j2`:** OK albo błąd, z diagnozą.
3. **Werdykt: co mam zrobić teraz.** Dokładne komendy, np.:
   - `repo sync`, jeśli zmienił się manifest albo trzeba dociągnąć zmiany:
     ```
     cd ~/android/LineageOS_24/
     repo sync -c --force-sync --no-clone-bundle --no-tags -j4
     ```
     (albo z nazwami konkretnych projektów na końcu, jeśli wystarczy zsynchronizować tylko część),
   - pełny build pod flashowanie, gdy uznasz, że jest sens testować na telefonie:
     ```
     cd ~/android/LineageOS_24/
     source build/envsetup.sh
     breakfast athena userdebug
     m bacon -j2
     ```
   - instalacja (podaj dokładnie, łącznie z tym, czy trzeba wgrać boot/dtbo/vendor_boot przez fastboot):
     ```
     # w Lineage Recovery: Apply update → Apply from ADB
     adb sideload out/target/product/athena/lineage-24.0-*-athena.zip

     # OPCJONALNIE GApps (przed pierwszym uruchomieniem):
     # Advanced → Reboot to recovery, potem znowu Apply update → Apply from ADB
     adb sideload MindTheGapps-17.0.0-arm64-*.zip
     # przy ostrzeżeniu o podpisie wybierz "Yes"

     # przy pierwszej instalacji / zmianie z/bez GApps: Factory reset → Format data
     # potem Reboot system now
     ```
   - jakie logi mam zebrać po flashu i do jakiego pliku je zapisać (np. `adb logcat -d > ~/logs/<funkcja>.txt`, `dmesg`, `dumpsys ...`), żeby zweryfikować daną funkcję.
4. **Co dalej:** następny krok z planu. Jeśli nie czekasz na mnie, kontynuuj od razu.

## Kolejność priorytetów
Etap 0 → 1 → 2 → klawiatura + touch (scroll) + podświetlenie + fingerprint + ekran → kafelek QS + Ustawienia → System → „BlackBerry Keyboard” → NFC + BT + GPS → SIM PIN / PIN blokady z klawiatury → wykrywanie układu → latarka dual-tone → bateria → Aperture → panel suwaków latarki.