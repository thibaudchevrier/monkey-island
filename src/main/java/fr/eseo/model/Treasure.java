package fr.eseo.model;

//
/** The Class Treasure. */
public final class Treasure extends Item {

  private static final Treasure TREASURE = new Treasure();

  /**
   * Gets the treasure.
   *
   * @return the treasure
   */
  public static Treasure getTreasure() {
    return TREASURE;
  }

  /** Instantiates a new treasure. */
  private Treasure() {
    super();
  }
}
