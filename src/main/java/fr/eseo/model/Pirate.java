package fr.eseo.model;

/** The Class Pirate. */
public class Pirate extends Entity {

  /** The id. */
  private int id;

  /** The state. */
  private StatePirate state;

  /** The energy. */
  private int energy;

  /** The Constant MAX_ENERGY. */
  public static final int DEFAULT_ID = 0;

  /** The Constant MAX_ENERGY. */
  public static final int MAX_ENERGY = 100;

  /** The Constant DEFAULT_STATE_PIRATE. */
  public static final StatePirate DEFAULT_STATE_PIRATE = StatePirate.sober;

  /**
   * Instantiates a new pirate.
   *
   * @param x Coordinate x of the pirate
   * @param y Coordinate y of the pirate
   */
  public Pirate(int x, int y) {
    super(x, y);
    this.id = DEFAULT_ID;
    this.state = DEFAULT_STATE_PIRATE;
    this.energy = MAX_ENERGY;
  }

  /**
   * Instantiates a new pirate.
   *
   * @param state the state of the pirate
   * @param x Coordinate x of the pirate
   * @param y Coordinate y of the pirate
   */
  public Pirate(StatePirate state, int x, int y) {
    super(x, y);
    this.id = DEFAULT_ID;
    this.state = state;
    this.energy = MAX_ENERGY;
  }

  /**
   * Instantiates a new pirate.
   *
   * @param id the id of the pirate
   * @param energy the energy
   * @param x Coordinate x of the pirate
   * @param y Coordinate y of the pirate
   */
  public Pirate(int id, int energy, int x, int y) {
    super(x, y);
    this.id = id;
    this.state = DEFAULT_STATE_PIRATE;
    this.energy = energy;
  }

  /**
   * Gets the state.
   *
   * @return Retourne l'état du pirate
   */
  public StatePirate getState() {
    return this.state;
  }

  /**
   * Sets the state.
   *
   * @param state Modifie l'état du pirate
   */
  public void setState(StatePirate state) {
    this.state = state;
    this.setChanged();
    this.notifyObservers(this.state);
  }

  /**
   * Gets the energy.
   *
   * @return Retourne l'énergie du pirate
   */
  public int getId() {
    return this.id;
  }

  /**
   * Gets the energy.
   *
   * @return Retourne l'énergie du pirate
   */
  public int getEnergy() {
    return this.energy;
  }

  /**
   * Sets the energy.
   *
   * @param energy Modifie l'énergie du pirate
   */
  public void setEnergy(int energy) {
    this.energy = energy;
  }

  /**
   * Méthode permettant de déplacer un pirate sur l'île.
   *
   * @param x the coordinate x of the pirate
   * @param y the coordinate y of the pirate
   * @throws CollisionException collision with an entity
   * @throws IllegalArgumentException movement not allowed
   * @throws NullPointerException the island
   */
  public void movementPirate(int x, int y)
      throws CollisionException, IllegalArgumentException, NullPointerException {
    if (!((x == -1 && y == 0)
        || (x == 1 && y == 0)
        || (x == 0 && y == 1)
        || (x == 0 && y == -1)
        || (x == 0 && y == 0))) {
      throw new IllegalArgumentException("MOVEMENT not allowed");
    }
    final int newX = this.getCoordinateX() + x;
    final int newY = this.getCoordinateY() + y;
    this.setPosition(newX, newY);
    if (this.getState() == StatePirate.dead) {
      return;
    }
    final Island island = Island.getInstance();
    final Rhum rhum = island.collisionRhum(newX, newY);
    if (island.collisionMonkey(newX, newY) != null) {
      this.energy = 0;
    } else if (rhum != null && rhum.getVisibility()) {
      this.energy = this.energy + rhum.getEnergyQuantity();
      rhum.setVisibility(false);
      rhum.getTimer().start();
    } else if (newX == Treasure.getTreasure().getCoordinateX()
        && newY == Treasure.getTreasure().getCoordinateY()) {
      Treasure.getTreasure().setVisibility(true);
    }
    if (this.energy != 0) {
      this.energy = this.energy - 1;
    }
    if (this.energy == 0) {
      this.state = StatePirate.dead;
    }
    // A single notification, once position, energy and state are all up to date.
    this.moveTo(newX, newY);
  }

  /**
   * Bring the pirate back to life at a new position, for a new game.
   *
   * @param x the coordinate x of the pirate
   * @param y the coordinate y of the pirate
   * @param energy the energy of the pirate
   */
  public void respawn(int x, int y, int energy) {
    this.energy = energy;
    this.state = DEFAULT_STATE_PIRATE;
    this.moveTo(x, y);
  }
}
