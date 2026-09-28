package fr.eseo.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.eseo.model.Configuration;
import fr.eseo.model.Island;
import fr.eseo.model.Pirate;
import fr.eseo.model.Rhum;
import fr.eseo.model.StatePirate;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Tests of the drunk state: rum makes a pirate stumble for a while. */
public class TestDrunkPirate {

  private static final int SIZE = 10;

  private static final int DRUNK_DURATION = 50;

  private MockedStatic<Island> islandStatic;

  private MockedStatic<Configuration> configurationStatic;

  private Island island;

  private Pirate pirate;

  private Rhum rhum;

  /** A random source that returns a scripted sequence of values. */
  private static final class ScriptedRandom extends Random {
    private static final long serialVersionUID = 1L;

    private final transient Deque<Integer> values;

    ScriptedRandom(Integer... values) {
      this.values = new ArrayDeque<Integer>(Arrays.asList(values));
    }

    @Override
    public int nextInt(int bound) {
      if (this.values.isEmpty()) {
        fail("unexpected random draw");
      }
      return this.values.pop();
    }
  }

  @Before
  public void setUp() {
    this.island = mock(Island.class);
    when(this.island.getnbLines()).thenReturn(SIZE);
    when(this.island.getnbRows()).thenReturn(SIZE);
    when(this.island.getCase()).thenReturn(Fixtures.board(SIZE));
    this.islandStatic = Mockito.mockStatic(Island.class);
    this.islandStatic.when(Island::getInstance).thenReturn(this.island);

    final Configuration configuration = mock(Configuration.class);
    when(configuration.getDrunkDuration()).thenReturn(DRUNK_DURATION);
    when(configuration.getStumbleChance()).thenReturn(Pirate.DEFAULT_STUMBLE_CHANCE);
    this.configurationStatic = Mockito.mockStatic(Configuration.class);
    this.configurationStatic.when(Configuration::getInstance).thenReturn(configuration);

    Fixtures.resetTreasure();
    this.pirate = new Pirate(5, 5);
    this.rhum = new Rhum(6, 5, true, Rhum.DEFAULT_ENERGY_QUANTITY, 60_000);
    when(this.island.collisionRhum(6, 5)).thenReturn(this.rhum);
  }

  @After
  public void tearDown() {
    this.rhum.getTimer().stop();
    this.configurationStatic.close();
    this.islandStatic.close();
  }

  private void drink() throws Exception {
    this.pirate.movementPirate(1, 0);
    assertEquals(StatePirate.drunk, this.pirate.getState());
  }

  @Test
  public void testRumMakesThePirateDrunk() throws Exception {
    this.drink();
    assertEquals(Pirate.MAX_ENERGY + Rhum.DEFAULT_ENERGY_QUANTITY - 1, this.pirate.getEnergy());
  }

  @Test
  public void testDrunkPirateStumbles() throws Exception {
    this.drink();
    // Draws: 0 < 33, so it stumbles; then direction 3 = up, instead of the requested right.
    this.pirate.setRandom(new ScriptedRandom(0, 3));
    this.pirate.movementPirate(1, 0);
    assertEquals(6, this.pirate.getCoordinateX());
    assertEquals(4, this.pirate.getCoordinateY());
  }

  @Test
  public void testDrunkPirateSometimesWalksStraight() throws Exception {
    this.drink();
    // Draw: 99 >= 33, so it goes where it was asked.
    this.pirate.setRandom(new ScriptedRandom(99));
    this.pirate.movementPirate(1, 0);
    assertEquals(7, this.pirate.getCoordinateX());
    assertEquals(5, this.pirate.getCoordinateY());
  }

  @Test
  public void testSoberPirateNeverStumbles() throws Exception {
    // No draw allowed: a sober pirate does not use the random source.
    this.pirate.setRandom(new ScriptedRandom());
    this.pirate.movementPirate(0, 1);
    assertEquals(5, this.pirate.getCoordinateX());
    assertEquals(6, this.pirate.getCoordinateY());
  }

  @Test
  public void testPirateSobersUp() throws Exception {
    this.drink();
    final long end = System.currentTimeMillis() + 5_000;
    while (this.pirate.getState() == StatePirate.drunk && System.currentTimeMillis() < end) {
      Thread.sleep(10);
    }
    assertEquals(StatePirate.sober, this.pirate.getState());
  }

  @Test
  public void testRespawnSobersUp() throws Exception {
    this.drink();
    this.pirate.respawn(2, 2, Pirate.MAX_ENERGY);
    assertEquals(StatePirate.sober, this.pirate.getState());
  }
}
