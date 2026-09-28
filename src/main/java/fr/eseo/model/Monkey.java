package fr.eseo.model;

import java.awt.event.ActionListener;
import javax.swing.Timer;

//
/** The Class Monkey. */
public abstract class Monkey extends Entity implements ActionListener {

  /** The timer that moves the monkey, created on first use and started by Island.startMonkeys(). */
  private Timer timer = null;

  /** The speed. */
  private int speed;

  /** The Constant DEFAULT_SPEED. */
  public static final int DEFAULT_SPEED = 75;

  /** Instantiates a new monkey. */
  public Monkey() {
    super();
    this.speed = DEFAULT_SPEED;
  }

  /**
   * Instantiates a new monkey.
   *
   * @param x the coordinate x of the monkey
   * @param y the coordinate y of the monkey
   */
  public Monkey(int x, int y) {
    super(x, y);
    this.speed = DEFAULT_SPEED;
  }

  /**
   * Instantiates a new monkey.
   *
   * @param speed the speed
   * @param x the coordinate x of the monkey
   * @param y the coordinate y of the monkey
   */
  public Monkey(int speed, int x, int y) {
    super(x, y);
    this.speed = speed;
  }

  /**
   * Gets the timer that moves the monkey.
   *
   * @return the timer
   */
  public Timer getTimer() {
    if (this.timer == null) {
      this.timer = new Timer(this.speed, this);
    }
    return this.timer;
  }

  /**
   * Gets the speed.
   *
   * @return the speed
   */
  public int getSpeed() {
    return this.speed;
  }

  /**
   * Sets the speed.
   *
   * @param speed the new speed
   */
  public void setSpeed(int speed) {
    this.speed = speed;
    this.getTimer().setDelay(speed);
  }

  /**
   * Tell if the island this monkey lives on has been built.
   *
   * @return true when the island and its cases exist
   */
  protected boolean onIsland() {
    final Island island = Island.getInstance();
    return island != null && island.getCase() != null;
  }

  /**
   * Set the coordinates of an item and handle the exception of creation.
   *
   * @param x the coordinate x of the item
   * @param y the coordinate y of the item
   * @throws CollisionException collision with an entity
   * @throws IllegalArgumentException movement not allowed
   * @throws NullPointerException the island
   */
  public void setPositionMonk(int x, int y)
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
    this.moveTo(newX, newY);
    final Pirate pirate = Island.getInstance().collisionPirate(newX, newY);
    if (pirate != null && pirate.getState() != StatePirate.dead) {
      pirate.setEnergy(0);
      pirate.setState(StatePirate.dead);
    }
  }
}
