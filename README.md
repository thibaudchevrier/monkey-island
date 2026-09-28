# Monkey Island

A multiplayer take on *Monkey Island*, built as a school project at ESEO (2016).
Pirates roam an island looking for the treasure. They drink rum to regain energy
and avoid the monkeys. The first pirate to find the treasure wins the round, and a
new game starts a few seconds later.

The repository contains the **game server** (Java, TCP on port `13579`). The
**Guybrush** Swing client (`client/Guybrush_2015_v2.jar`) was provided by the school.

## Run it

Requires Docker.

```sh
docker compose up --build
```

Then open **<http://localhost:6080/vnc.html?autoconnect=1&resize=scale>** in a browser.
The client runs in a container and is displayed through noVNC. Click the game window
and move your pirate with the **arrow keys**.

### Rules

- Each move costs one energy. A pirate with no energy left dies.
- Rum gives back 15 energy. The bottle comes back after 10 seconds.
- Crazy monkeys wander at random. The hunter monkey chases the closest living pirate.
  A monkey that reaches a pirate kills it, even while its player is idle.
- The treasure is hidden. Step on it to reveal it and win the round. Three seconds
  later, a new game starts: every pirate comes back to life with full energy, the rum
  is refilled, and the treasure is hidden somewhere else.
- A dead pirate shows as a skull and can't move until the next game. To respawn right
  away, close the game window.

### More players

Open a second browser client on port 6081:

```sh
docker compose --profile duo up --build
```

Then go to <http://localhost:6081/vnc.html?autoconnect=1&resize=scale>. You can also run
the client natively (Java 8+ required). It connects to `127.0.0.1:13579`, which the
server container publishes:

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

## Known issues

- **9 of 139 tests fail** with `mvn test`, all in the original test suite. Eight of them
  also fail in the original Java 8 / PowerMock setup. Some expect error messages that the
  code never produces. One asserts exact random counts. Others read stubs that were never
  set up. The ninth depends on test order: the tests share the `Island` and `Treasure`
  singletons, so a test can see state left over from an earlier one.
- Guybrush locks its keyboard on every key press until the server replies, even for keys
  that send nothing (Shift, Cmd...). The server works around this by resending your
  position on every monkey move, so a stuck keyboard unlocks within a second.
- `IslandHeigth` is ignored: the island is always square (`IslandWidth` is read twice).
- Error messages are never sent to the client (`envoieMessageErreur` is a stub).
- The drunk pirate state (`StatePirate.drunk`) exists but is never used.
