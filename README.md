# CS304 Air Hockey (JOGL)

Clean Java/JOGL project for the CS304 final game assignment.
No lab1 names, no extra classes.

## Structure

- `src/com/cs304/airhockey/AirHockeyGame.java` – main window and game loop
- `src/com/cs304/airhockey/MainMenuScreen.java` – main menu
- `src/com/cs304/airhockey/HighScoresScreen.java` – high scores
- `lib/` – put your JOGL JAR files here:
    - gluegen-rt-2.4.0.jar
    - gluegen-rt-2.4.0-natives-macosx-universal.jar
    - jogl-all-2.4.0.jar
    - jogl-all-2.4.0-natives-macosx-universal.jar

## Compile (from terminal)

On macOS / Linux:

    javac -cp "lib/*:src" src/com/cs304/airhockey/*.java

## Run

    java -cp "lib/*:src" com.cs304.airhockey.AirHockeyGame

## Controls

- Menu:
  - UP/DOWN – move selection
  - ENTER – select
  - ESC – quit

- Game:
  - W / S – move left paddle
  - ↑ / ↓ – move right paddle
  - P or Space – pause
  - ESC – back to menu
