# Monkey Island

A multiplayer take on *Monkey Island*, built as a school project at ESEO (2016).
Pirates roam an island looking for the treasure. They drink rum to regain energy
and avoid the monkeys.

The repository contains the **game server** (Java, TCP on port `13579`). The
**Guybrush** Swing client (`client/Guybrush_2015_v2.jar`) was provided by the school.

## Run it

Requires Docker.

```sh
docker compose up --build
```

Then open **<http://localhost:6080/vnc.html?autoconnect=1&resize=scale>** in a browser.
The client runs in a container and is displayed through noVNC. Click the game window
and move your pirate with the **arrow keys**. Each move costs one energy, and rum
restores it. A monkey that reaches your pirate kills it, even while you are idle. A
dead pirate shows as a skull and can't move. Close the game window to respawn a new one.

To add more players, run the client natively (Java 8+ required). It connects to
`127.0.0.1:13579`, which the server container publishes:

```sh
java -jar client/Guybrush_2015_v2.jar
```

## Develop

```sh
mvn package -DskipTests        # target/monkey-island.jar
java -jar target/monkey-island.jar
mvn test
```

No local JDK? Run Maven in a container instead:

```sh
docker run --rm -v "$PWD":/w -w /w maven:3.9-eclipse-temurin-25 mvn test
```

The game settings are in `src/main/resources/config.properties`: island size,
treasure and rum positions, monkeys, energy and speeds. Rebuild after changing them.

### Layout

```
src/main/java/fr/eseo/
  controller/     Game: entry point, starts the TCP service
  communication/  sockets, broadcast channel, text protocol
  command/        Command pattern for client commands (register, move)
  model/          Island, Pirate, monkeys, Rhum, Treasure
client/           Guybrush client jar + Docker image (Xvfb, noVNC)
docs/             original report, design-pattern slides, test data
```

## Known issues (original code, left as is)

- **9 of 121 tests fail** with `mvn test`. Eight of them also fail in the original
  Java 8 / PowerMock setup. Some expect error messages that the code never produces.
  One asserts exact random counts. Others read stubs that were never set up.
- The ninth failure depends on test order. The tests share the `Island` and `Treasure`
  singletons, so a test can see state left over from an earlier one.
- `IslandHeigth` is ignored: the island is always square (`IslandWidth` is read twice).
- Hunter monkeys are not implemented (their timer callback is empty).
- Error messages are never sent to the client (`envoieMessageErreur` is a stub).
