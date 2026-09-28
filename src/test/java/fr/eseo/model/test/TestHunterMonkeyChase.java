package fr.eseo.model.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import fr.eseo.model.Case;
import fr.eseo.model.CaseType;
import fr.eseo.model.HunterMonkey;
import fr.eseo.model.Island;
import fr.eseo.model.Pirate;
import fr.eseo.model.StatePirate;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Tests of the hunter monkeys chasing the closest pirate. */
public class TestHunterMonkeyChase {

  private static final int SIZE = 10;

  private MockedStatic<Island> islandStatic;

  private Island island;

  private ArrayList<Pirate> pirates;

  private HunterMonkey hunter;

  @Before
  public void setUp() {
    final Case[][] plateau = new Case[SIZE][SIZE];
    for (int i = 0; i < SIZE; i++) {
      for (int j = 0; j < SIZE; j++) {
        plateau[i][j] = new Case(i, j, CaseType.earth);
      }
    }
    this.pirates = new ArrayList<Pirate>();
    this.island = mock(Island.class);
    when(this.island.getnbLines()).thenReturn(SIZE);
    when(this.island.getnbRows()).thenReturn(SIZE);
    when(this.island.getCase()).thenReturn(plateau);
    when(this.island.getPirates()).thenReturn(this.pirates);
    this.islandStatic = Mockito.mockStatic(Island.class);
    this.islandStatic.when(Island::getInstance).thenReturn(this.island);

    this.hunter = new HunterMonkey(5, 5);
    this.hunter.getTimer().stop();
  }

  @After
  public void tearDown() {
    this.islandStatic.close();
  }

  @Test
  public void testCloserPirateIgnoresDeadPirates() {
    final Pirate far = new Pirate(9, 9);
    final Pirate dead = new Pirate(StatePirate.dead, 5, 6);
    final Pirate near = new Pirate(3, 5);
    assertSame(
        near, this.hunter.closerPirate(new ArrayList<Pirate>(Arrays.asList(far, dead, near))));
  }

  @Test
  public void testCloserPirateWithoutLivingPirate() {
    assertNull(this.hunter.closerPirate(new ArrayList<Pirate>()));
    assertNull(
        this.hunter.closerPirate(
            new ArrayList<Pirate>(Arrays.asList(new Pirate(StatePirate.dead, 1, 1)))));
  }

  @Test
  public void testHuntingPiratesStepsTowardThePirate() {
    assertArrayEquals(new int[] {-1, 0}, this.hunter.huntingPirates(new Pirate(1, 4)));
    assertArrayEquals(new int[] {1, 0}, this.hunter.huntingPirates(new Pirate(8, 6)));
    assertArrayEquals(new int[] {0, -1}, this.hunter.huntingPirates(new Pirate(5, 1)));
    assertArrayEquals(new int[] {0, 1}, this.hunter.huntingPirates(new Pirate(4, 8)));
    assertArrayEquals(new int[] {0, 0}, this.hunter.huntingPirates(new Pirate(5, 5)));
  }

  @Test
  public void testChaseMovesTowardTheClosestPirate() {
    this.pirates.add(new Pirate(2, 5));
    this.hunter.chase();
    assertEquals(4, this.hunter.getCoordinateX());
    assertEquals(5, this.hunter.getCoordinateY());
  }

  @Test
  public void testChaseTakesTheOtherAxisWhenBlocked() {
    this.pirates.add(new Pirate(2, 3));
    when(this.island.collisionMonkey(4, 5)).thenReturn(new HunterMonkey(4, 5));
    this.hunter.chase();
    assertEquals(5, this.hunter.getCoordinateX());
    assertEquals(4, this.hunter.getCoordinateY());
  }

  @Test
  public void testChaseKillsTheCaughtPirate() {
    final Pirate pirate = new Pirate(5, 6);
    this.pirates.add(pirate);
    when(this.island.collisionPirate(5, 6)).thenReturn(pirate);
    this.hunter.chase();
    assertEquals(StatePirate.dead, pirate.getState());
    assertEquals(0, pirate.getEnergy());
  }

  @Test
  public void testChaseWithoutPirateDoesNotMove() {
    this.hunter.chase();
    assertEquals(5, this.hunter.getCoordinateX());
    assertEquals(5, this.hunter.getCoordinateY());
  }
}
