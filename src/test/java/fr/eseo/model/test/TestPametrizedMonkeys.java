package fr.eseo.model.test;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import fr.eseo.model.Case;
import fr.eseo.model.CaseType;
import fr.eseo.model.HunterMonkey;
import fr.eseo.model.Island;
import fr.eseo.model.Pirate;
import fr.eseo.model.StatePirate;

@RunWith(Parameterized.class)

public class TestPametrizedMonkeys {

	/** Static mock of the Island singleton, released after each test. */
	private MockedStatic<Island> islandStatic;

	/** The isl. */
	private Island isl;
	private Case[][] plateau;
	
	@Parameter
	public int x;

	/**
	 * 2e param�tre, repr�sentant la longueur du deuxi�me c�t� du triangle.
	 */
	@Parameter (value = 1)
	public int y;
	
	/**
	 * 4e param�tre, repr�sentant le type attendu du triangle.
	 */
	@Parameter (value = 2)
	public Point deplacement;
	
	@Parameters
    public static Collection<Object[]> dt() {
    	Object[][] data = new Object[][]{
    		{3, 3, new Point(0, 1)}, 
    		{3, 3, new Point(0, -1)}, 
    		{3, 3, new Point(1, 0)}, 
    		{3, 3, new Point(-1, 0)}, 
    		{4, 4, new Point(0, 1)}, 
    		{1, 1, new Point(0, -1)}
    	};
    	return Arrays.asList(data);
    }
	
	@Before
	public void setUp(){	
		this.isl = mock(Island.class);
		this.plateau = new Case[6][6];
	    this.islandStatic = Mockito.mockStatic(Island.class);
		when(Island.getInstance()).thenReturn(isl);
		when(this.isl.getnbLines()).thenReturn(6);
		when(this.isl.getnbRows()).thenReturn(6);
		for(int i = 0; i<6; i++){
			for(int j = 0; j<6; j++){
				if(((i == 0 || i == 5) && j < 6) || ((j == 0 || j == 5)&& i < 6 )){
					this.plateau[i][j] = new Case(i, j, CaseType.sea);
				}else{
					this.plateau[i][j] = new Case(i, j, CaseType.earth);
				}
			}
		}
		when(this.isl.getCase()).thenReturn(plateau);
	}
	
	@Rule
	public ErrorCollector collector = new ErrorCollector();
	
	@Test
	public void testMovementSolo(){
		int erreur = 0;
		HunterMonkey monk = new HunterMonkey(x, y);
		
		try{
			monk.setPositionMonk(deplacement.x, deplacement.y);
			erreur = 0;
		}catch(Exception e){
			erreur = 1;
		}
		
		if(erreur == 0){
			assertNotEquals("error should move x ", x+y, monk.getCoordinateX()+ monk.getCoordinateY());
		}else 
			assertEquals("error shouldn't move x ", x+y, monk.getCoordinateX()+ monk.getCoordinateY());

	}
	
	@Test
	public void testMovementByMonkey(){
		int erreur = 0;
		HunterMonkey monk = new HunterMonkey(x, y);
		HunterMonkey monk1 = new HunterMonkey(3, 4);
		HunterMonkey monk2 = new HunterMonkey(2, 3);
		when(this.isl.collisionMonkey(3, 4)).thenReturn(monk1);
		when(this.isl.collisionMonkey(2, 3)).thenReturn(monk2);
		try{
			monk.setPositionMonk(deplacement.x, deplacement.y);
			erreur = 0;
		}catch(Exception e){
			erreur = 1;
		}
		if(erreur == 0){
			assertNotEquals("error should move x ", x+y, monk.getCoordinateX()+ monk.getCoordinateY());
		}else 
			assertEquals("error shouldn't move x ", x+y, monk.getCoordinateX()+ monk.getCoordinateY());
	}
	
	@Test
	public void testMovementByPirate(){
		int erreur = 0;
		HunterMonkey monk = new HunterMonkey(x, y);
		// One living pirate on each case next to the monkey.
		Pirate[] pirates = {
			new Pirate(StatePirate.sober, x+1, y),
			new Pirate(StatePirate.sober, x-1, y),
			new Pirate(StatePirate.sober, x, y+1),
			new Pirate(StatePirate.sober, x, y-1)
		};
		for(Pirate pirate : pirates){
			when(this.isl.collisionPirate(pirate.getCoordinateX(), pirate.getCoordinateY())).thenReturn(pirate);
		}
		try{
			monk.setPositionMonk(deplacement.x, deplacement.y);
			erreur = 0;
		}catch(Exception e){
			erreur = 1;
		}

		if(erreur == 0){
			assertNotEquals("error should move x ", x+y, monk.getCoordinateX()+ monk.getCoordinateY());
		}else
			assertEquals("error shouldn't move x ", x+y, monk.getCoordinateX()+ monk.getCoordinateY());
		// Only the pirate the monkey lands on dies.
		for(Pirate pirate : pirates){
			final boolean caught = pirate.getCoordinateX() == monk.getCoordinateX()
					&& pirate.getCoordinateY() == monk.getCoordinateY();
			assertEquals("error state", caught ? StatePirate.dead : StatePirate.sober, pirate.getState());
		}
	}

	@After
	public void tearDown(){
		this.islandStatic.close();
	}
}
