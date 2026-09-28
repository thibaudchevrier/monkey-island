package fr.eseo.model.test;

import static org.junit.Assert.assertEquals;

import fr.eseo.model.CaseType;
import fr.eseo.model.Configuration;
import fr.eseo.model.Island;
import fr.eseo.model.Treasure;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class TestConfig {

  private Configuration conf;

  private Island isl;

  private MockedStatic<Island> islandStatic;

  @Before
  public void setUp() throws Exception {

    // Load the configuration into a fresh island, not the shared singleton.
    isl = new Island();
    islandStatic = Mockito.mockStatic(Island.class);
    islandStatic.when(Island::getInstance).thenReturn(isl);
    Fixtures.resetTreasure();
    conf = Configuration.getInstance();
  }

  @After
  public void tearDown() {
    islandStatic.close();
  }

  @Test
  public void testInitIsland() {
    conf.loading();
    assertEquals("Error type case", isl.getnbLines(), 20);
    assertEquals("Error type case", Treasure.getTreasure().getCoordinateX(), 7);
    assertEquals("Error type case", isl.getCase()[18][18].getCaseType(), CaseType.earth);
    assertEquals("Error type case", isl.getRhums().get(0).getCoordinateX(), 4);
    assertEquals("Error type case", isl.getRhums().get(0).getCoordinateY(), 4);
    assertEquals("Error type case", isl.getRhums().get(0).getEnergyQuantity(), 15);
    assertEquals("Error type case", conf.getNRJMax(), 100);
  }
}
