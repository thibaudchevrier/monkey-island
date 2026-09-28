package fr.eseo.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

/**
 * The Class Island.
 */
public class Island {
	
	/** The nb rows. */
	private int nbRows;
	
	/** The nb lines. */
	private int nbLines;
	
	/** The cases. */
	private Case[][] cases;
	
	/** The Constant DEFAULT_NB_ROWS. */
	public static final int DEFAULT_NB_ROWS = 10;
	
	/** The Constant DEFAULT_NB_LINES. */
	public static final int DEFAULT_NB_LINES = 10;

	/** The pirates. */
	private ArrayList<Pirate> pirates;
	
	/** The monkeys. */
	private ArrayList<Monkey> monkeys;

	/** The rhum bottles. */
	private ArrayList<Rhum> rhums;
	
	/** The island. */
	private static Island island = null;

	/**
	 * Lock held by every thread that changes the game: client threads (pirates)
	 * and the Swing timer thread (monkeys, rum, new game).
	 */
	public static final Object LOCK = new Object();

	/** Minimal distance between a new pirate and the monkeys. */
	public static final int SAFE_SPAWN_DISTANCE = 4;

	/** Attempts to find a position away from the monkeys before giving up. */
	private static final int MAX_SPAWN_ATTEMPTS = 100;
	
	/**
	 * Gets the single instance of Island.
	 *
	 * @return single instance of Island
	 */
	public static Island getInstance(){
		if(island == null){
			island = new Island();
		}
		return island;
	}
	
	/**
	 * Instantiates a new island.
	 */
	public Island(){
		this.nbRows = DEFAULT_NB_ROWS;
		this.nbLines = DEFAULT_NB_LINES;
		this.cases = new Case[DEFAULT_NB_ROWS][DEFAULT_NB_LINES];
		this.pirates = new ArrayList<Pirate>();
		this.monkeys = new ArrayList<Monkey>();
		this.rhums = new ArrayList<Rhum>();
	}
	
	/**
	 * Set a new dimension of the island.
	 * @param nbLines the number of lines of the island
	 * @param nbRows the number of rows of the island
	 */
	public void setIsland(int nbLines, int nbRows){
		this.nbRows = nbRows;
		this.nbLines = nbLines;
		this.cases = new Case[nbRows][nbLines];
		this.initIsland();
	}
	
	/**
	 * Gets the nb rows.
	 *
	 * @return the nb rows
	 */
	public int getnbRows(){
		return this.nbRows;
	}
	
	/**
	 * Sets the nb rows.
	 *
	 * @param nbRows the new nb rows
	 */
	public void setnbRows(int nbRows){
		this.nbRows = nbRows;
	}
	
	/**
	 * Gets the nb lines.
	 *
	 * @return the nb lines
	 */
	public int getnbLines(){
		return this.nbLines;
	}
	
	
	/**
	 * Sets the nb lines.
	 *
	 * @param nbLines the new nb lines
	 */
	public void setnbLines(int nbLines){
		this.nbLines = nbLines;
	}
	

	/**
	 * Gets the pirates.
	 *
	 * @return the pirates
	 */
	public ArrayList<Pirate> getPirates(){
		return this.pirates;
	}
	
		
	/**
	 * Gets the monkeys.
	 *
	 * @return the monkeys
	 */
	public ArrayList<Monkey> getMonkeys(){
		return this.monkeys;
	}
	
	/**
	 * Gets the rhum bottles.
	 *
	 * @return the rhum bottles
	 */
	public ArrayList<Rhum> getRhums(){
		return this.rhums;
	}
	
	/**
	 * Get the cases of the island.
	 * @return cases the cases of the island
	 */
	public Case[][] getCase(){
		return this.cases;
	}

	/**
	 * Initialize the island, dimension and type of each case.
	 */
	private void initIsland(){
		for(int i = 0; i< getnbLines(); i++){
			for(int j = 0; j< getnbRows(); j++){
				if(((i == 0 || i == getnbLines()-1) && j < getnbRows()) || ((j == 0 || j == getnbRows()-1)&& i < getnbLines())){
					this.cases[i][j] = new Case(i, j, CaseType.sea);
				}else{
					this.cases[i][j] = new Case(i, j, CaseType.earth);
				}
			}
		}
	}
	
	/**
	 * Notice if the current entity have the same coordinates that a pirate.
	 *
	 * @param x the coordinate x of the entity
	 * @param y the y
	 * @return the pirate
	 */
	public Pirate collisionPirate(int x, int y){
		Pirate p = null;
		if(this.pirates.isEmpty() == false){
			for(Pirate pirate: this.pirates){
				// A live pirate can stand on a dead one: report the live one first.
				if(pirate.getCoordinateX() == x && pirate.getCoordinateY() == y
						&& (p == null || p.getState() == StatePirate.dead || pirate.getState() != StatePirate.dead)){
					p = pirate;
				}
			}
		}
		return p;
	}
	
	/**
	 * Notice if the current entity have the same coordinates that a monkey.
	 *
	 * @param x the x
	 * @param y the y
	 * @return the monkey
	 */
	public Monkey collisionMonkey(int x, int y){
		Monkey m = null;
		if(this.monkeys.isEmpty() == false){
			for(Monkey monkey : this.monkeys){
				if(monkey.getCoordinateX() == x && monkey.getCoordinateY() == y ){
					m = monkey;
				}
			} 
		}
		return m;
	}
	
	/**
	 * Notice if the current entity have the same coordinates that a rhum.
	 * 
	 * @param x position x
	 * @param y position y  
	 * @return the rhum.
	 */
	public Rhum collisionRhum(int x, int y){
		Rhum r = null;
		if(this.rhums.isEmpty() == false){
			for(Rhum rhum : this.rhums){
				if(rhum.getCoordinateX() == x && rhum.getCoordinateY() == y){
					r = rhum;
				}
			}
		}
		return r;
	}
	
	/**
	 * Handle the exceptions when the coordinates of the pirate are set.
	 * @param x the coordinate x of the entity
	 * @param y the coordinate y of the entity
	 * @throws CollisionException 
	 */
	public void position(int x, int y) throws CollisionException{
		if(this == null || this.getCase() == null){
			 throw new NullPointerException("Island doesn't exsist !!");
		}else if(x < 0 || x > this.getnbLines()-1 || y < 0 || y > this.getnbRows()-1){
			 throw new CollisionException("exit island!!",
			         CollisionException.COLLISION_EXIT_ISLAND);
		}else if(this.getCase()[x][y].getCaseType() == CaseType.sea){
			 throw new CollisionException("Case sea not allowed",
			         CollisionException.COLLISION_SEA);
		}else if(this.collisionPirate(x, y) != null){
			 throw new CollisionException("Case pirate not allowed",
			         CollisionException.COLLISION_PIRATE);
		}else if(this.collisionMonkey(x, y) != null){
			 throw new CollisionException("Case monkey not allowed",
			         CollisionException.COLLISION_MONKEY);
		}else if(this.collisionRhum(x, y) != null){
			 throw new CollisionException("Case rhum not allowed",
			         CollisionException.COLLISION_RHUM);
		}else if( x == Treasure.getTreasure().getCoordinateX() && y == Treasure.getTreasure().getCoordinateY()){
			 throw new CollisionException("Case treasure not allowed",
			         CollisionException.COLLISION_TREASURE);
		}
	}
	
	/**
	 * Add a new entity randomly to the island.
	 * @return p a point 
	 */
	public Point addEntity(){
		Point p = new Point();
		int erreur = 0;
		do{
			try{
				final Random rand = new Random();
				p.x = rand.nextInt(this.getnbLines() -2)+1;
				p.y = rand.nextInt(this.getnbRows() - 2)+1;
				this.position(p.x, p.y);
				erreur = 0;
			}catch(CollisionException e){
				erreur = 1;
			}
		}while(erreur == 1);
		return p;
	}

	/**
	 * Pick a random free position for a pirate, away from the monkeys when possible,
	 * so that a new pirate is not caught before its player can react.
	 * @return p a point
	 */
	public Point addPirateEntity(){
		Point p = this.addEntity();
		for(int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS && this.nearMonkey(p); attempt++){
			p = this.addEntity();
		}
		return p;
	}

	/**
	 * Tell if a position is within SAFE_SPAWN_DISTANCE of a monkey.
	 * @param p the position
	 * @return true if a monkey is too close
	 */
	private boolean nearMonkey(Point p){
		for(Monkey monkey : this.monkeys){
			if(Math.abs(monkey.getCoordinateX() - p.x) + Math.abs(monkey.getCoordinateY() - p.y) < SAFE_SPAWN_DISTANCE){
				return true;
			}
		}
		return false;
	}

	/**
	 * Start a new game on the same island: hide the treasure somewhere else,
	 * put the rum back and bring every pirate back to life at a random position.
	 *
	 * @param energy the energy given back to every pirate
	 */
	public void newGame(int energy){
		final Treasure treasure = Treasure.getTreasure();
		treasure.setVisibility(false);
		final Point treasurePosition = this.addEntity();
		treasure.setCoordinateX(treasurePosition.x);
		treasure.setCoordinateY(treasurePosition.y);
		for(Rhum rhum : this.rhums){
			rhum.getTimer().stop();
			rhum.setVisibility(true);
		}
		for(Pirate pirate : this.pirates){
			final Point p = this.addPirateEntity();
			pirate.respawn(p.x, p.y, energy);
		}
	}
}
