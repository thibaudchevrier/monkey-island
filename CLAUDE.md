# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Multiplayer Monkey Island game **server** (Java 25, TCP port 13579), from a 2016 ESEO school
project. The **client** is `client/Guybrush_2015_v2.jar`, a Swing app provided by the school:
a binary we cannot change. The server must speak the protocol Guybrush expects.

## Commands

Gradle needs JDK 17+ to run; the build uses a Java 25 toolchain. Without a local JDK, prefix
commands with `docker run --rm -v "$PWD":/w -w /w eclipse-temurin:25-jdk`.

```sh
./gradlew check            # everything CI runs: tests + all quality gates
./gradlew spotlessApply    # fix formatting (run before check)
./gradlew test --tests 'fr.eseo.model.test.TestPirate'                         # one class
./gradlew test --tests 'fr.eseo.model.test.TestPirate.testMovementDeadPirate'  # one test
./gradlew run              # start the server

docker compose up --build                            # server + browser client on :6080
docker compose --profile duo up --build              # + second client on :6081
TAG=1.0.0 docker compose up --pull always --no-build   # a published version instead
```

`check` fails on any of: a javac warning (`-Xlint:all -Werror`), formatting
(google-java-format), Checkstyle (`config/checkstyle/`), SpotBugs (`config/spotbugs/exclude.xml`,
every exclusion is scoped and explained), Javadoc doclint, or line coverage under 70% (JaCoCo).
Reports go to `build/reports/`.

Releases: pushing a `vX.Y.Z` tag makes CI publish `ghcr.io/thibaudchevrier/monkey-island-{server,client}`
as `X.Y.Z`, `X.Y`, `X`, `latest`, and creates a GitHub release. `main` publishes `main` and `sha-<commit>`.

## Architecture

- `controller/Game` starts `communication/ServiceMonkeyIsland`, which loads `config.properties`
  (classpath) into the `Island`, starts the monkey timers, and accepts sockets. Each connection
  is a `ClientMonkeyIsland` thread with **one** `command/Manager` for its lifetime.
- Commands from the client (`/I` register, `/D dx dy` move) go through the Command pattern
  (`CommandControl` → `CommandInscription` / `CommandDeplacement`) to the `Manager`.
- The model (`model/`) uses the Observer pattern: every `Entity` (pirate, monkey, rum, treasure)
  notifies its `EntityObserver`s. The `Manager` observes the island and turns each change into
  protocol messages for its own client, and broadcasts to the others through
  `ServiceMonkeyIsland.diffuseAutres` / `CanalDiffusion`.
- `Island`, `Treasure` and `Configuration` are singletons (`getInstance()` / `getTreasure()`).

### Concurrency

Client threads (moves) and the Swing timer thread (monkeys, rum respawn, sobering up, new game)
all change the game. **Every mutation must hold `Island.LOCK`.** Lock order is always
`Island.LOCK` → the broadcast channel, never the reverse. Timers must not be started from
constructors: monkeys are started by `Island.startMonkeys()`, other timers are created lazily.

### Protocol quirks (Guybrush)

- Formats live in `communication/ProtocoleMonkeyIsland`; the decompiled client is the reference.
- Guybrush **locks its keyboard after every key press until it gets `/A` or `/R`**, even for keys
  that send nothing. So every `/D` must be answered, and the `Manager` resends `/A` on each
  monkey move to unlock a stuck keyboard.
- An entity change must notify observers **once**, with position, energy and state all set
  (see `Entity.moveTo`); intermediate notifications send wrong positions to clients.
- Errors are sent as `Erreur MonkeyIsland : <text>`, which Guybrush logs. A refused move still
  needs `/R` after the error.
- Guybrush has `127.0.0.1:13579` hard-coded; `client/entrypoint.sh` forwards it with socat.

## Tests

JUnit 4 + Mockito 5 (`Mockito.mockStatic` replaces the old PowerMock). Tests must not depend on
order or on the shared singletons:

- Stub `Island.getInstance()` with `mockStatic`, returning a mock or a fresh `new Island()`
  (`Fixtures.board(size)` builds a sea-bordered board).
- Call `Fixtures.resetTreasure()`, and stub `Configuration` when a test depends on its values.
- Randomness is injectable (`CrazyMonkey(..., Random)`, `Pirate.setRandom`) to keep tests
  deterministic.
- `TestServerIntegration` runs the real server on a free port and plays it over TCP.

## Game settings

`src/main/resources/config.properties` defines the island size, the positions of the treasure,
rum and monkeys, energy, speeds, and the drunk rule (`DrunkDuration`, `StumbleChance`).
`IslandHeigth` is ignored: the island is always square.
