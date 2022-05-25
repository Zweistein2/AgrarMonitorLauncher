# AgrarMonitorLauncher

---

#### Deutsch

### Was ist der AgrarMonitorLauncher?
Der AgrarMonitorLauncher ist eine lokale Anwendung, die dir im Zusammenspiel mit dem AgrarMonitor alle Infos und Statistiken zu deinen LS22-Spielständen anzeigen kann. Dazu liest die Anwendung deine lokalen Spielstände aus, bzw. entnimmt live die Daten aus der "Plumber"-Mod, sofern vorhanden (Dann auch im Multiplayer).

## Mitmachen
Pull-Requests sind gerne gesehen. Bei großen Umbauten eröffnet bitte eine Issue um im Vorfeld über die Änderungen zu diskutieren.

Bitte aktualisiert die Tests falls nötig.

### Bauen

Um das Projekt zu bauen, führe die "build-win.bat" im Verzeichnis "build-natives" mit folgenden Parametern aus "Build WindowsNatives". Daraufhin werden die Plumber64.dll und die Plumber.dll erstellt.

Mit den Gradle-Tasks "clean fatJar" baust du dann die ausführbare .jar-Datei. Diese kann dann beispielsweise mit Launch4J in eine .exe umgewandelt werden (Siehe dazu die "AgrarMonitor_launch4j_example.xml").

## Lizenz
Das Projekt wird unter der Apache Lizenz, Version 2.0 lizensiert. Siehe dazu auch LICENSE.

---

#### English

### What is the AgrarMonitorLauncher?
The AgrarMonitorLauncher is a local application that, in conjunction with the AgrarMonitor, can show you all the information and statistics about your LS22 savegames. To do this, the application reads your local saves or takes the data live from the "Plumber" mod, if available (then also in multiplayer).

### Build
To build the project, run the "build-win.bat" in the "build-natives" directory with the following parameters "Build WindowsNatives". The Plumber64.dll and the Plumber.dll are then created.

Then use the Gradle clean fatJar tasks to build the executable .jar file. This can then be converted into an .exe with Launch4J (see "AgrarMonitor_launch4j_example.xml").

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License
The project is licensed under the Apache License, Version 2.0. See LICENSE