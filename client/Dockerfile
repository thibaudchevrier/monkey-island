# Guybrush, the Swing game client, served in the browser through noVNC.
FROM eclipse-temurin:25-jre

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
      xvfb x11vnc fluxbox novnc websockify socat \
      libxext6 libxrender1 libxtst6 libxi6 fonts-dejavu-core \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY Guybrush_2015_v2.jar entrypoint.sh ./
RUN chmod +x entrypoint.sh

ENV DISPLAY=:0 \
    SERVER_HOST=server \
    SERVER_PORT=13579

EXPOSE 6080
ENTRYPOINT ["./entrypoint.sh"]
