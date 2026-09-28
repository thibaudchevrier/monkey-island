package fr.eseo.communication.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import fr.eseo.communication.ServiceMonkeyIsland;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import org.junit.BeforeClass;
import org.junit.Test;

/** Plays the real server over TCP, the way the Guybrush client does. */
public class TestServerIntegration {

  private static final int TIMEOUT_MS = 5000;

  private static int port;

  /** A raw client speaking the Monkey Island protocol. */
  private static final class Player implements AutoCloseable {
    private final Socket socket;
    private final BufferedReader in;
    private final BufferedWriter out;
    private int id;
    private int x;
    private int y;

    Player() throws IOException {
      this.socket = connect();
      this.socket.setSoTimeout(TIMEOUT_MS);
      this.in =
          new BufferedReader(
              new InputStreamReader(this.socket.getInputStream(), StandardCharsets.UTF_8));
      this.out =
          new BufferedWriter(
              new OutputStreamWriter(this.socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    void send(String message) throws IOException {
      this.out.write(message);
      this.out.newLine();
      this.out.flush();
    }

    /** Reads messages until one starts with the prefix, or fails after the timeout. */
    String await(String prefix) throws IOException {
      final long end = System.currentTimeMillis() + TIMEOUT_MS;
      while (System.currentTimeMillis() < end) {
        final String line;
        try {
          line = this.in.readLine();
        } catch (SocketTimeoutException e) {
          break;
        }
        if (line == null) {
          break;
        }
        if (line.startsWith(prefix)) {
          return line;
        }
      }
      return null;
    }

    /** Registers the pirate and returns the map message. */
    String register() throws IOException {
      this.send("/I ");
      final String map = this.await("/C ");
      final String identity = this.await("/i ");
      assertNotNull("no pirate identity", identity);
      final String[] fields = identity.substring(3).split("-");
      this.id = Integer.parseInt(fields[0]);
      this.x = Integer.parseInt(fields[1]);
      this.y = Integer.parseInt(fields[2]);
      return map;
    }

    /**
     * Moves the pirate and waits for the answer: refused (/R), or accepted (/A) at the new
     * position. The server also resends the position when monkeys move; those are skipped.
     *
     * @return true if the move was accepted
     */
    boolean move(int dx, int dy) throws IOException {
      this.send("/D " + dx + " " + dy);
      while (true) {
        final String line = this.await("/");
        assertNotNull("no answer to the move " + dx + "," + dy, line);
        if (line.startsWith("/R")) {
          return false;
        }
        if (line.startsWith("/A ")) {
          final String[] fields = line.substring(3).split("-");
          final int newX = Integer.parseInt(fields[0]);
          final int newY = Integer.parseInt(fields[1]);
          final boolean dead = "0".equals(fields[2]);
          if (dead || newX == this.x + dx && newY == this.y + dy) {
            this.x = newX;
            this.y = newY;
            return true;
          }
        }
      }
    }

    @Override
    public void close() throws IOException {
      this.socket.close();
    }
  }

  private static Socket connect() throws IOException {
    final long end = System.currentTimeMillis() + TIMEOUT_MS;
    while (true) {
      try {
        return new Socket("127.0.0.1", port);
      } catch (IOException notYetListening) {
        if (System.currentTimeMillis() > end) {
          throw notYetListening;
        }
        Thread.onSpinWait();
      }
    }
  }

  @BeforeClass
  public static void startServer() throws Exception {
    try (ServerSocket probe = new ServerSocket(0)) {
      port = probe.getLocalPort();
    }
    final ServiceMonkeyIsland service = new ServiceMonkeyIsland();
    final Thread server =
        new Thread(
            () -> {
              try {
                service.lanceService(port);
              } catch (Exception e) {
                throw new IllegalStateException(e);
              }
            },
            "monkey-island-test-server");
    server.setDaemon(true);
    server.start();
  }

  @Test
  public void testRegistrationSendsTheIsland() throws IOException {
    try (Player player = new Player()) {
      final String map = player.register();
      assertNotNull("no map", map);
      assertTrue(map, map.startsWith("/C 20 20 "));
      assertNotNull("no rum", player.await("/B "));
      assertNotNull("no crazy monkeys", player.await("/e "));
      assertNotNull("no hunter monkeys", player.await("/c "));
      assertNotNull("no other pirates", player.await("/P "));
    }
  }

  @Test
  public void testEveryMoveIsAnswered() throws IOException {
    try (Player player = new Player()) {
      player.register();
      // Guybrush keeps its keyboard locked until the server answers each move.
      player.move(1, 0);
      player.move(-1, 0);
      player.move(0, 1);
      player.move(0, -1);
    }
  }

  @Test
  public void testPlayersSeeEachOtherJoinAndLeave() throws IOException {
    try (Player first = new Player()) {
      first.register();
      final int secondId;
      try (Player second = new Player()) {
        second.register();
        secondId = second.id;
        final String others = second.await("/P ");
        assertNotNull("the second player does not get the others", others);
        assertTrue(others, others.contains(first.id + "-"));
        final String joined = first.await("/n " + secondId + "-");
        assertNotNull("the first player is not told about the second", joined);
      }
      assertEquals("/s " + secondId, first.await("/s " + secondId));
    }
  }
}
