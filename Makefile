# Monkey Island: shortcuts for Docker Compose, Gradle and releases.
# Run `make` (or `make help`) to list the targets.

.DEFAULT_GOAL := help
.PHONY: help up duo release-up down restart logs ps open \
        check test format jar run clean release

# Published version used by release-up: 1.0.0, 1.0, 1, latest, main or sha-<commit>.
TAG ?= latest

CLIENT_URL  := http://localhost:6080/vnc.html?autoconnect=1&resize=scale
CLIENT2_URL := http://localhost:6081/vnc.html?autoconnect=1&resize=scale

# Gradle needs JDK 17+. Use the local JDK when it is recent enough, otherwise Docker.
JAVA_MAJOR := $(shell java -version 2>&1 | awk -F'"' '/version/ { split($$2, v, "."); print (v[1] == "1" ? v[2] : v[1]) }')
DOCKER_GRADLE := docker run --rm -v "$(CURDIR)":/w -v monkey-gradle:/root/.gradle -w /w eclipse-temurin:25-jdk ./gradlew
GRADLE ?= $(if $(shell [ "$(JAVA_MAJOR)" -ge 17 ] 2>/dev/null && echo yes),./gradlew,$(DOCKER_GRADLE))

help: ## List the targets
	@awk 'BEGIN { FS = ":.*## " } /^## ---- / { gsub(/(## )?-+ ?/, ""); printf "\n%s\n", $$0 } /^[a-z-]+:.*## / { printf "  \033[36m%-11s\033[0m %s\n", $$1, $$2 }' $(MAKEFILE_LIST)

## ---- Play (Docker Compose) ----

up: ## Build from source and start the server and one browser client
	TAG=dev docker compose up -d --build
	@echo "Play at $(CLIENT_URL)"

duo: ## Same as up, with a second browser client for a second player
	TAG=dev docker compose --profile duo up -d --build
	@echo "Player 1: $(CLIENT_URL)"
	@echo "Player 2: $(CLIENT2_URL)"

release-up: ## Start a published version instead of the source (make release-up TAG=1.0.0)
	TAG=$(TAG) docker compose up -d --pull always --no-build
	@echo "Playing $(TAG) at $(CLIENT_URL)"

down: ## Stop and remove every container (including the second client)
	docker compose --profile duo down

restart: ## Restart the server (new island, new game)
	docker compose restart server

logs: ## Follow the logs
	docker compose --profile duo logs -f

ps: ## Show the running containers and their images
	docker compose --profile duo ps

open: ## Open the browser client
	@open "$(CLIENT_URL)" 2>/dev/null || xdg-open "$(CLIENT_URL)"

## ---- Develop (Gradle) ----

check: ## Run the tests and every quality gate, like CI
	$(GRADLE) check

test: ## Run the tests, or one class/method: make test TEST=fr.eseo.model.test.TestPirate
	$(GRADLE) test $(if $(TEST),--tests '$(TEST)')

format: ## Format the code with google-java-format
	$(GRADLE) spotlessApply

jar: ## Build build/libs/monkey-island.jar
	$(GRADLE) jar

run: ## Run the server from source, without Docker (needs JDK 17+)
	./gradlew run

clean: ## Delete the build output
	$(GRADLE) clean

## ---- Release ----

release: ## Tag and push a version; CI publishes the images: make release VERSION=1.1.0
	@echo "$(VERSION)" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$$' \
		|| { echo "Usage: make release VERSION=x.y.z"; exit 1; }
	@[ "$$(git branch --show-current)" = main ] || { echo "Release from main."; exit 1; }
	@git diff --quiet HEAD || { echo "Commit or stash your changes first."; exit 1; }
	@git fetch -q origin main && [ "$$(git rev-parse HEAD)" = "$$(git rev-parse origin/main)" ] \
		|| { echo "main is not in sync with origin/main."; exit 1; }
	git tag -a v$(VERSION) -m "Monkey Island $(VERSION)"
	git push origin refs/tags/v$(VERSION)
	@echo "CI is publishing $(VERSION): https://github.com/thibaudchevrier/monkey-island/actions"
