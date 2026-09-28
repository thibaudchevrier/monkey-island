package fr.eseo.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An entity of the island: pirate, monkey, rum bottle or treasure.
 *
 * <p>Entities are the subjects of the Observer pattern: the manager of each client observes them
 * and tells its client when they change.
 */
public abstract class Entity {

  /** The observers, notified after each change. Safe to modify while notifying. */
  private final List<EntityObserver> observers = new CopyOnWriteArrayList<EntityObserver>();

  /** The coordinate X. */
  private int coordinateX;

  /** The coordinate Y. */
  private int coordinateY;

  /** Instantiates a new character. */
  public Entity() {
    this.coordinateX = 0;
    this.coordinateY = 0;
  }

  /**
   * Instantiates a new character.
   *
   * @param x the coordinate x of the entity
   * @param y the coordinate y of the entity
   */
  public Entity(int x, int y) {
    this.coordinateX = x;
    this.coordinateY = y;
  }

  /**
   * Gets the case character.
   *
   * @return the case character
   */
  public int getCoordinateX() {
    return this.coordinateX;
  }

  /**
   * Gets the case character.
   *
   * @return the case character
   */
  public int getCoordinateY() {
    return this.coordinateY;
  }

  /**
   * Sets the case character.
   *
   * @param x the coordinate x of the entity
   */
  public void setCoordinateX(int x) {
    this.coordinateX = x;
    this.notifyObservers();
  }

  /**
   * Sets the case character.
   *
   * @param y the coordinate y of the entity
   */
  public void setCoordinateY(int y) {
    this.coordinateY = y;
    this.notifyObservers();
  }

  /**
   * Move the entity and notify its observers once, with both coordinates set.
   *
   * @param x the new coordinate x of the entity
   * @param y the new coordinate y of the entity
   */
  protected void moveTo(int x, int y) {
    this.coordinateX = x;
    this.coordinateY = y;
    this.notifyObservers();
  }

  /**
   * Set the coordinates of an item and handle the exception of creation.
   *
   * @param x the coordinate x of the item
   * @param y the coordinate y of the item
   * @throws CollisionException exception if collision with an entity or a sea case or limits of
   *     island
   * @throws NullPointerException the island
   */
  public void setPosition(int x, int y) throws CollisionException, NullPointerException {
    if (Island.getInstance() == null || Island.getInstance().getCase() == null) {
      throw new NullPointerException("Island doesn't exsist !!");
    } else if (x < 0
        || x > Island.getInstance().getnbLines() - 1
        || y < 0
        || y > Island.getInstance().getnbRows() - 1) {
      throw new CollisionException("exit island!!", CollisionException.COLLISION_EXIT_ISLAND);
    } else if (Island.getInstance().getCase()[x][y].getCaseType() == CaseType.sea) {
      throw new CollisionException("Case sea not allowed", CollisionException.COLLISION_SEA);
    } else if (Island.getInstance().collisionPirate(x, y) != null
        && Island.getInstance().collisionPirate(x, y).getState() != StatePirate.dead
        && this instanceof Pirate) {
      throw new CollisionException("Case pirate not allowed", CollisionException.COLLISION_PIRATE);
    } else if (Island.getInstance().collisionMonkey(x, y) != null && this instanceof Monkey) {
      throw new CollisionException("Case monkey not allowed", CollisionException.COLLISION_MONKEY);
    }
  }

  /**
   * Adds an observer, unless it is already registered.
   *
   * @param observer the observer to notify of changes
   */
  public void addObserver(EntityObserver observer) {
    if (!this.observers.contains(observer)) {
      this.observers.add(observer);
    }
  }

  /**
   * Removes an observer.
   *
   * @param observer the observer to remove
   */
  public void deleteObserver(EntityObserver observer) {
    this.observers.remove(observer);
  }

  /** Removes every observer. */
  public void deleteObservers() {
    this.observers.clear();
  }

  /**
   * Counts the observers.
   *
   * @return the number of observers
   */
  public int countObservers() {
    return this.observers.size();
  }

  /** Notifies every observer that this entity changed. */
  protected void notifyObservers() {
    for (EntityObserver observer : this.observers) {
      observer.update(this);
    }
  }
}
