# Manual Battle Draw

## 🔧 Technische Voraussetzungen

Da aktuell noch keine ausführbare Datei vorhanden ist, wird zur Ausführung des Spiels eine Entwicklungsumgebung (IDE) mit Java-Unterstützung benötigt – zum Beispiel **IntelliJ IDEA** oder **Visual Studio Code**.

Das Spiel verfügt bereits über eine grafische Benutzeroberfläche, weshalb die meisten Befehle durch Buttons ersetzt wurden und bequem per Klick ausführbar sind.

### Schritte zur Ausführung:

1. Öffne ein Terminal oder die Eingabeaufforderung.
2. Navigiere zum Projektverzeichnis.
3. Erstelle die JAR-Datei mit Gradle:

   ```bash
   ./gradlew build
   ```

4. Wechsle in das Verzeichnis `build/libs`:

   ```bash
   cd build/libs
   ```

5. Starte den Server (einmalig):

   ```bash
   java -jar Battle_Draw-v1.jar server <port>
   ```

   **Beispiel:**

   ```bash
   java -jar Battle_Draw-v1.jar server 8989
   ```

6. Starte eine neue Spielerinstanz:

   ```bash
   java -jar Battle_Draw-v1.jar client <ip>:<port> [username]
   ```

   **Beispiel:**

   ```bash
   java -jar Battle_Draw-v1.jar client localhost:8989 Username
   ```

---

## 👤 Account

Nachdem du dich erfolgreich mit einem Benutzernamen mit dem Server verbunden hast, erscheint ein Fenster.

- Der Server fragt, ob du diesen Namen behalten möchtest.
- Du kannst dich **anmelden** oder ein **neues Konto erstellen**.
- Falls der Benutzername bereits vergeben ist, wird automatisch eine Zahl angehängt.

---

## 🏠 Lobby

- Du kannst eine **neue Lobby erstellen** und Host werden – Name: `Lobby_von_Benutzername`.
- Oder du trittst einer **bestehenden Lobby** bei.
- Nach dem Betreten kann der Host das Spiel starten.

---

## 🎮 Spiel

- Über dem Zeichenbereich siehst du das **Thema** und die **verbleibende Zeit**.
- Dir stehen **verschiedene Farben und Pinselgrößen** zur Verfügung.
- Wenn du fertig bist oder die Zeit abläuft, klicke auf **„Proceed“**.
- Danach kommt die **Bewertungsseite**:  
  → Bewerte andere Zeichnungen auf einer Skala von **1 bis 5**.

Wenn alle bewertet und auf **„Send Vote“** geklickt haben:

- Wird die nächste Zeichnung angezeigt.
- Am Ende gewinnt der Spieler mit der **höchsten Gesamtbewertung**.
- Du kannst dann eine weitere Runde spielen oder zur Startseite zurückkehren.

---

## 💬 Wichtige Befehle (Chat-Commands)


/whisper [Name] [Nachricht]
  → Private Nachricht an einen Spieler.
  Beispiel: /whisper Max Hey, gute Zeichnung!

/online
  → Zeigt die Anzahl der aktuell online Spieler.

/listPlayers
  → Listet alle Spieler in deiner Lobby.

/games
  → Zeigt alle offenen Lobbys an.

/nick [NeuerName]
  → Ändert deinen Spielernamen.
  Beispiel: /nick SuperZeichner

