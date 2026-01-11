# Volla Hub 📱

Eine native Android-App für die Volla-Community, die alle wichtigen Volla-Ressourcen an einem Ort vereint.

![Android](https://img.shields.io/badge/Android-24%2B-green.svg)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-purple.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)

## 📋 Über die App

Volla Hub ist eine umfassende Android-App, die Zugriff auf alle wichtigen Volla-Plattformen bietet:

- 🌐 **Volla Online** - Alle Seiten von volla.online hierarchisch organisiert
- 📝 **Volla Blog** - Die neuesten Blogbeiträge mit "Weitere laden"-Funktion
- 📚 **Volla Wiki** - Mehrsprachiges Wiki (DE, EN, ES, IT, CS, DA, NO, SV)
- 💬 **Volla Forum** - Direktzugriff auf Unterforen in verschiedenen Sprachen

## ✨ Features

- ✅ **100% ohne Google-Dienste** - Perfekt für Volla-Geräte
- 🌓 **Dark/Light Mode** - Umschaltbar über das Menü
- 🔍 **Integrierte Suche** - Durchsuche alle Bereiche
- 📱 **Responsive Design** - Optimiert für mobile Nutzung
- 🔄 **Pull-to-Refresh** - Aktualisiere Inhalte durch Herunterziehen
- 🗂️ **Hierarchische Navigation** - Übersichtliche Darstellung von Seiten und Artikeln
- 🌍 **Mehrsprachig** - Wiki und Forum in 8 Sprachen

## 🖼️ Screenshots

<img width="1080" height="2400" alt="Screenshot_20260111-210225_Volla Hub" src="https://github.com/user-attachments/assets/f76a7bd9-9574-4dcd-aec8-7e2aeda267e7" />

<img width="1080" height="2400" alt="Screenshot_20260111-210235_Volla Hub" src="https://github.com/user-attachments/assets/9b122015-0351-46fa-bc6d-ac887034c4a5" />

<img width="1080" height="2400" alt="Screenshot_20260111-210248_Volla Hub" src="https://github.com/user-attachments/assets/96052c98-c9c7-4a84-a035-981f4293c727" />

<img width="1080" height="2400" alt="Screenshot_20260111-210258_Volla Hub" src="https://github.com/user-attachments/assets/893f6f83-4fe8-4cf2-8651-8db24d2cc854" />


## 🛠️ Technologie-Stack

- **Sprache:** Kotlin
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Build-System:** Gradle (KTS)
- **UI:** Android Views mit ViewBinding
- **Architektur:** MVVM mit Kotlin Coroutines
- **HTML-Parsing:** Jsoup 1.17.2
- **Networking:** OkHttp (via Jsoup)

## 📦 Installation

### Aus den Releases

1. Lade die neueste APK aus den [Releases](https://github.com/USERNAME/volla-hub/releases) herunter
2. Aktiviere "Installation aus unbekannten Quellen" in den Android-Einstellungen
3. Installiere die APK

### Selbst kompilieren
```bash
# Repository klonen
git clone https://github.com/USERNAME/volla-hub.git
cd volla-hub

# In Android Studio öffnen
# Build > Build Bundle(s) / APK(s) > Build APK(s)
```

## 🏗️ Projekt-Struktur
```
app/src/main/
├── java/com/volla/hub/
│   ├── MainActivity.kt          # Hauptbildschirm mit 4 Tabs
│   ├── ContentActivity.kt       # WebView für Artikel/Seiten
│   ├── VollaParser.kt          # Parser für Volla-Webseiten
│   └── ContentAdapter.kt        # RecyclerView Adapter
├── res/
│   ├── layout/                  # XML-Layouts
│   ├── menu/                    # Menü-Definitionen
│   ├── values/                  # Strings, Themes (Light)
│   ├── values-night/            # Dark Theme
│   └── xml/                     # Network Security Config
└── AndroidManifest.xml
```

## 🎨 Features im Detail

### Volla Online
- Hierarchische Darstellung aller Seiten von volla.online/de/
- Ausschluss des Blog-Bereichs (eigener Tab)
- Einrückung zur Visualisierung der Seitenstruktur

### Volla Blog
- Zeigt die 20 neuesten Blogbeiträge
- "Weitere Beiträge laden"-Button für ältere Artikel
- Anzeige von Titel, Datum und Excerpt

### Volla Wiki
- 8 Sprach-Buttons für verschiedene Wiki-Sprachen
- Hierarchische Darstellung aller Wiki-Artikel
- Mobile-optimierte Darstellung mit responsivem Layout

### Volla Forum
- 5 Sprach-Buttons für Unterforen:
  - 🇩🇪 Deutsch
  - 🇬🇧 English
  - 🇪🇸 Español
  - 🇨🇿 Česky Slovenská
  - 🇮🇹 Italiano

## 🔧 Konfiguration

### Network Security

Die App verwendet HTTP für das Wiki (wiki.volla.online). Die Konfiguration befindet sich in:
```xml
res/xml/network_security_config.xml
```

### Themes

- Light Theme: `res/values/themes.xml`
- Dark Theme: `res/values-night/themes.xml`
- Hauptfarbe: Rot

## 🤝 Beitragen

Beiträge sind willkommen! Bitte beachte:

1. Forke das Repository
2. Erstelle einen Feature-Branch (`git checkout -b feature/AmazingFeature`)
3. Committe deine Änderungen (`git commit -m 'Add some AmazingFeature'`)
4. Pushe zum Branch (`git push origin feature/AmazingFeature`)
5. Öffne einen Pull Request

## 📝 Lizenz

Dieses Projekt steht unter der MIT-Lizenz - siehe [LICENSE](LICENSE) Datei für Details.

## 🙏 Danksagungen

- [Volla](https://volla.online) für die großartigen Produkte und die offene Community
- [Jsoup](https://jsoup.org/) für das HTML-Parsing
- Alle Mitwirkenden am Volla Wiki und Forum

## 📧 Kontakt

Bei Fragen oder Problemen:
- Öffne ein [Issue](https://github.com/tux4us/volla-hub/issues)
- Kontaktiere mich über [tux4us@online.de]

## 🗺️ Roadmap

- [ ] Offline-Modus für Wiki-Artikel
- [ ] Lesezeichen-Funktion
- [ ] Push-Benachrichtigungen für neue Blog-Posts
- [ ] Teilen-Funktion für Artikel
- [ ] Download-Manager für Wiki-PDFs
- [ ] Erweiterte Suchfilter

---

**Hinweis:** Diese App ist ein inoffizielles Projekt von [tux4us](https://github.com/tux4us) und steht in keiner offiziellen, geschäftlichen Verbindung mit Volla.

Made with ❤️ for the Volla Community
