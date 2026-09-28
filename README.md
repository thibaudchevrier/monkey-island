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
- Rum also makes you drunk for 5 seconds: each move then has a 1 in 3 chance to go in a
  random direction, maybe straight into a monkey.
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

## Published images

Every release publishes both images to the GitHub Container Registry, for amd64 and arm64.
The same `compose.yaml` runs either your local build or a published version:

```sh
docker compose up --build                            # build from source (images tagged dev)
TAG=1.0.0 docker compose up --pull always --no-build   # a published version
```

`TAG` can be a version (`1.0.0`, `1.0`, `1`, `latest`), `main`, or `sha-<commit>`.
`--no-build` makes a missing tag fail instead of silently building from source.

## Develop

Requires JDK 17+ to run Gradle. The build uses a Java 25 toolchain, which Gradle downloads
if it's missing.

```sh
./gradlew build                 # compile, test, run every quality check, build the jar
./gradlew run                   # start the server on port 13579
./gradlew spotlessApply         # format the code
```

No local JDK? Run Gradle in a container instead:

```sh
docker run --rm -v "$PWD":/w -w /w eclipse-temurin:25-jdk ./gradlew build
```

The game settings are in `src/main/resources/config.properties`: island size,
treasure and rum positions, monkeys, energy and speeds. Rebuild after changing them.

### Code quality

`./gradlew check` fails on any of these:

| Tool | Checks | Config |
| --- | --- | --- |
| javac `-Xlint:all -Werror` | every compiler warning | `build.gradle.kts` |
| [Spotless](https://github.com/diffplug/spotless) + google-java-format | formatting | `build.gradle.kts` |
| [Checkstyle](https://checkstyle.org) | naming, imports, braces, Javadoc on the public API | `config/checkstyle/` |
| [SpotBugs](https://spotbugs.github.io) | bug patterns (successor of FindBugs) | `config/spotbugs/exclude.xml` |
| Javadoc doclint | broken documentation | `build.gradle.kts` |
| JUnit + Mockito | 152 tests, including a TCP integration test | `src/test/` |
| [JaCoCo](https://www.jacoco.org) | at least 70% line coverage (successor of Emma) | `build.gradle.kts` |

Reports land in `build/reports/`.

### CI and releases

GitHub Actions (`.github/workflows/ci.yml`) runs `./gradlew check` on every push and
pull request, then builds both Docker images. On `main`, the images are pushed with the
`main` and `sha-<commit>` tags. Pull requests only build them.

To release, push a [semantic version](https://semver.org) tag:

```sh
git tag v1.2.0
git push origin v1.2.0
```

The pipeline then publishes the images as `1.2.0`, `1.2`, `1` and `latest`, and creates
a GitHub release with generated notes. Dependabot keeps Gradle dependencies, Docker
base images and actions up to date.

### Layout

```
src/main/java/fr/eseo/
  controller/     Game: entry point, starts the TCP service
  communication/  sockets, broadcast channel, text protocol
  command/        Command pattern for client commands (register, move)
  model/          Island, Pirate, monkeys, Rhum, Treasure
config/           Checkstyle and SpotBugs configuration
client/           Guybrush client jar + Docker image (Xvfb, noVNC)
docs/             original report, design-pattern slides, test data
```

## Known issues

- Guybrush locks its keyboard on every key press until the server replies, even for keys
  that send nothing (Shift, Cmd...). The server works around this by resending your
  position on every monkey move, so a stuck keyboard unlocks within a second.
- `IslandHeigth` is ignored: the island is always square (`IslandWidth` is read twice).
