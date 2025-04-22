# 🎨 Battle Draw 🖌️

[![Java Version](https://img.shields.io/badge/Java-21-blue)](https://www.java.com/de/)
[![Gradle Version](https://img.shields.io/badge/Gradle-8.6-green)](https://gradle.org)
[![JavaFX Version](https://img.shields.io/badge/JavaFX-21-orange)](https://openjfx.io)

A competitive drawing game where players create artwork based on themes and vote on each other's creations!

## Table of Contents
- [Features](#-features)
- [Installation](#-installation)
- [How to Play](#-how-to-play)
- [Important Note](#important-note)
- [Authors](#-authors)

## ✨ Features
- 🎮 3+ player online multiplayer
- ⏳ Timed drawing rounds
- 🎲 A selection of random creative themes
- ✨ Anonymous voting system
- 📊 Score tracking across rounds

## 📦 Installation

### Requirements
- Java 21+
- Gradle 8.6+

1. Clone repository:
   ```sh
   git clone ssh://git@git.scicore.unibas.ch:2222/cs108-fs25/Gruppe-23.git
   ```

## 🎮 How to Play

### 🛠️ Compile and Run the Project

1. Open a terminal or command prompt.
2. Navigate to the project directory:
3. create the jar file using gradle:
   ```sh
   ./gradlew build
   ```
4. Navigate to the `build/libs` directory:
   ```sh
   cd build/libs
   ```
5. Start the server (run this once):
   ```sh
   java -jar Battle_Draw-v1.jar server <port>
   ```
6. Start a new player instance for each player:
   ```sh
   java -jar Battle_Draw-v1.jar client <ip>:<port> [username]
   ```
7. Have fun! 🎨🎮🖌️

## ❗Important Note❗
The database file must be located in the same directory as the JAR file, inside a folder named database. 
By default, this setup is already configured after running "./gradlew build-cs108".

If you wish to use the JAR file outside of the project directory, simply copy the libs folder to your 
desired location and rename it to reflect its contents. For example, you can rename the folder to something 
like Battle Draw to clearly indicate its purpose.

## 💻 Authors
* Frédéric Weyssow
* Alex Marggraf
* Noah Nellen
* Simon Zeugin