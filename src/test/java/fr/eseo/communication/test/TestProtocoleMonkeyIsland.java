package fr.eseo.communication.test;

import static org.junit.Assert.assertEquals;

import fr.eseo.communication.ProtocoleMonkeyIsland;
import fr.eseo.model.Pirate;
import fr.eseo.model.Rhum;
import fr.eseo.model.StatePirate;
import java.awt.Point;
import java.util.Arrays;
import org.junit.Test;

/** Tests of the messages sent to the Guybrush client. */
public class TestProtocoleMonkeyIsland {

  @Test
  public void testTousPiratesSkipsTheClientAndTheDead() {
    final Pirate mine = new Pirate(1, 100, 2, 2);
    final Pirate other = new Pirate(2, 100, 3, 4);
    final Pirate dead = new Pirate(3, 100, 5, 6);
    dead.setState(StatePirate.dead);
    final Pirate another = new Pirate(4, 100, 7, 8);
    assertEquals(
        "/P 2-3-4___4-7-8",
        ProtocoleMonkeyIsland.formaterTousPirates(Arrays.asList(mine, other, dead, another), mine));
    assertEquals("/P ", ProtocoleMonkeyIsland.formaterTousPirates(Arrays.asList(mine), mine));
  }

  @Test
  public void testPirateMessages() {
    final Pirate pirate = new Pirate(7, 100, 2, 3);
    assertEquals("/n 7-2-3", ProtocoleMonkeyIsland.formaterNouveauPirate(pirate));
    assertEquals("/p 7-2-3", ProtocoleMonkeyIsland.formaterDeplacementPirate(pirate));
    assertEquals("/s 7", ProtocoleMonkeyIsland.formaterSuppressionPirate(pirate));
    assertEquals("/N ", ProtocoleMonkeyIsland.formaterNouvellePartie());
  }

  @Test
  public void testPositionRhumSendsTheVisibility() {
    assertEquals(
        "/B 4-4-1___5-6-0",
        ProtocoleMonkeyIsland.formaterPositionRhum(
            Arrays.asList(new Rhum(4, 4, true), new Rhum(5, 6, false))));
  }

  @Test
  public void testMoveParsing() {
    assertEquals(new Point(-1, 0), ProtocoleMonkeyIsland.commandeDuDeplacement("/D -1 0"));
    assertEquals(new Point(0, 1), ProtocoleMonkeyIsland.commandeDuDeplacement("/D 0 1"));
    assertEquals(new Point(0, 0), ProtocoleMonkeyIsland.commandeDuDeplacement("/D 0 0"));
  }

  @Test(expected = NumberFormatException.class)
  public void testMoveParsingRejectsText() {
    ProtocoleMonkeyIsland.commandeDuDeplacement("/D a b");
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testMoveParsingRejectsMissingValues() {
    ProtocoleMonkeyIsland.commandeDuDeplacement("/D 1");
  }
}
