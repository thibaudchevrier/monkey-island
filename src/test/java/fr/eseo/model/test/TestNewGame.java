package fr.eseo.model.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import fr.eseo.model.CaseType;
import fr.eseo.model.CrazyMonkey;
import fr.eseo.model.Island;
import fr.eseo.model.Pirate;
import fr.eseo.model.Rhum;
import fr.eseo.model.StatePirate;
import fr.eseo.model.Treasure;

/**
 * Tests of the reset of the island when the treasure has been found.
 */
public class TestNewGame {

	private Island island;

	private Pirate dead;

	private Pirate alive;

	private Rhum rhum;

	@Before
	public void setUp(){
		this.island = new Island();
		this.island.setIsland(8, 8);
		this.dead = new Pirate(1, 0, 2, 2);
		this.dead.setState(StatePirate.dead);
		this.alive = new Pirate(2, 40, 3, 3);
		this.rhum = new Rhum(5, 5, false);
		this.island.getPirates().add(this.dead);
		this.island.getPirates().add(this.alive);
		this.island.getRhums().add(this.rhum);
		Treasure.getTreasure().setVisibility(true);
	}

	@Test
	public void testNewGameRevivesEveryPirate(){
		this.island.newGame(100);
		for(Pirate pirate : this.island.getPirates()){
			assertEquals(StatePirate.sober, pirate.getState());
			assertEquals(100, pirate.getEnergy());
			assertEquals(CaseType.earth, this.island.getCase()[pirate.getCoordinateX()][pirate.getCoordinateY()].getCaseType());
		}
		assertFalse("two pirates on the same case",
				this.dead.getCoordinateX() == this.alive.getCoordinateX()
				&& this.dead.getCoordinateY() == this.alive.getCoordinateY());
	}

	@Test
	public void testPiratesRespawnAwayFromTheMonkeys(){
		final CrazyMonkey monkey = new CrazyMonkey(4, 4);
		monkey.getTimer().stop();
		this.island.getMonkeys().add(monkey);
		for(int i = 0; i < 20; i++){
			this.island.newGame(100);
			for(Pirate pirate : this.island.getPirates()){
				final int distance = Math.abs(pirate.getCoordinateX() - 4) + Math.abs(pirate.getCoordinateY() - 4);
				assertTrue("pirate spawned " + distance + " cases from a monkey", distance >= Island.SAFE_SPAWN_DISTANCE);
			}
		}
	}

	@Test
	public void testNewGameHidesTheTreasureAndRefillsTheRum(){
		this.island.newGame(100);
		final Treasure treasure = Treasure.getTreasure();
		assertFalse(treasure.getVisibility());
		assertEquals(CaseType.earth, this.island.getCase()[treasure.getCoordinateX()][treasure.getCoordinateY()].getCaseType());
		assertNotEquals("treasure under a pirate", this.alive.getCoordinateX() * 100 + this.alive.getCoordinateY(),
				treasure.getCoordinateX() * 100 + treasure.getCoordinateY());
		assertTrue(this.rhum.getVisibility());
	}
}
