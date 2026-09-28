package fr.eseo.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

//
/** The Class Rhum. */
public class Rhum extends Item implements ActionListener {

  /** The time before the bottle appears again, in milliseconds. */
  private int apparitionTime;

  /** The energy quantity. */
  private int energyQuantity;

  /** The timer that makes the bottle reappear. */
  private Timer timer;

  /** The Constant DEFAULT_ENERGY_QUANTITY. */
  public static final int DEFAULT_ENERGY_QUANTITY = 15;

  /** The Constant DEFAULT_ENERGY_QUANTITY. */
  public static final int DEFAULT_TMP_RHUM = 100;

  /** Instantiates a new rhum. */
  public Rhum() {
    super();
    this.energyQuantity = DEFAULT_ENERGY_QUANTITY;
    this.apparitionTime = DEFAULT_TMP_RHUM;
  }

  /**
   * Instantiates a new rhum.
   *
   * @param vis the visibility of the rhum
   */
  public Rhum(boolean vis) {
    super(vis);
    this.energyQuantity = DEFAULT_ENERGY_QUANTITY;
    this.apparitionTime = DEFAULT_TMP_RHUM;
  }

  /**
   * Instantiates a new rhum.
   *
   * @param x the coordinate x of the rhum
   * @param y the coordinate y of the rhum
   * @param vis the visibility of the rhum
   */
  public Rhum(int x, int y, boolean vis) {
    super(x, y, vis);
    this.energyQuantity = DEFAULT_ENERGY_QUANTITY;
    this.apparitionTime = DEFAULT_TMP_RHUM;
  }

  /**
   * Instantiates a new rhum.
   *
   * @param x the coordinate x of the rhum
   * @param y the coordinate y of the rhum
   * @param visibility the visibility
   * @param energyQuantity the energy quantity
   * @param apparitionTime the time before the rhum appears again
   */
  public Rhum(int x, int y, boolean visibility, int energyQuantity, int apparitionTime) {
    super(x, y, visibility);
    this.energyQuantity = energyQuantity;
    this.apparitionTime = apparitionTime;
  }

  /**
   * Gets the energy quantity.
   *
   * @return the energy quantity
   */
  public int getEnergyQuantity() {
    return this.energyQuantity;
  }

  /**
   * Gets the timer that makes the bottle reappear, created on first use.
   *
   * @return the timer
   */
  public Timer getTimer() {
    if (this.timer == null) {
      this.timer = new Timer(this.apparitionTime, this);
    }
    return this.timer;
  }

  @Override
  public void actionPerformed(ActionEvent arg0) {
    synchronized (Island.LOCK) {
      this.setVisibility(true);
      this.getTimer().stop();
    }
  }
}
