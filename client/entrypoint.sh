#!/bin/sh
set -e

# Guybrush has 127.0.0.1:13579 hard-coded in its jar, so forward that
# local port to the server container.
socat TCP-LISTEN:13579,bind=127.0.0.1,fork,reuseaddr "TCP:${SERVER_HOST}:${SERVER_PORT}" &

Xvfb "$DISPLAY" -screen 0 1024x768x24 -nolisten tcp &
until [ -e /tmp/.X11-unix/X0 ]; do sleep 0.1; done
fluxbox >/dev/null 2>&1 &
x11vnc -display "$DISPLAY" -forever -shared -nopw -quiet -rfbport 5900 &
websockify --web /usr/share/novnc 6080 localhost:5900 >/dev/null 2>&1 &

# Relaunch the client when its window is closed, to start a new pirate.
while true; do
  java -jar Guybrush_2015_v2.jar || true
  sleep 1
done
