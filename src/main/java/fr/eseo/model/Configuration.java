package fr.eseo.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** The Class Configuration. */
public final class Configuration {

  /** Classpath location of the game configuration. */
  private static final String CONFIG_RESOURCE = "/config.properties";

  private int nrjPirateMax = 0;

  /** How long a pirate stays drunk after drinking rum, in milliseconds. */
  private int drunkDuration = Pirate.DEFAULT_DRUNK_DURATION;

  /** Chance, in percent, that a drunk pirate stumbles in a random direction. */
  private int stumbleChance = Pirate.DEFAULT_STUMBLE_CHANCE;

  private static final Configuration CONFIG = new Configuration();

  /**
   * Gets the single instance of Island.
   *
   * @return single instance of Island
   */
  public static Configuration getInstance() {
    return CONFIG;
  }

  /** Instantiates a new Configuration. */
  private Configuration() {}

  /**
   * Get the energy of the pirate for the configuration.
   *
   * @return nrjPirateMax the energy of the pirate
   */
  public int getNRJMax() {
    return this.nrjPirateMax;
  }

  /**
   * How long a pirate stays drunk after drinking rum.
   *
   * @return the duration in milliseconds
   */
  public int getDrunkDuration() {
    return this.drunkDuration;
  }

  /**
   * Chance that a drunk pirate stumbles in a random direction.
   *
   * @return the chance in percent
   */
  public int getStumbleChance() {
    return this.stumbleChance;
  }

  /** Configure the data from the configuration file. */
  public void loading() {
    try {
      final Island isl = Island.getInstance();
      final InputStream fileInput = Configuration.class.getResourceAsStream(CONFIG_RESOURCE);
      if (fileInput == null) {
        throw new FileNotFoundException(CONFIG_RESOURCE + " not found on the classpath");
      }
      final Properties properties = new Properties();
      try (fileInput) {
        properties.load(fileInput);
      }

      final int xIsl = Integer.parseInt(properties.getProperty("IslandWidth"));
      final int yIsl = Integer.parseInt(properties.getProperty("IslandWidth"));
      isl.setIsland(xIsl, yIsl);

      if (properties.getProperty("TreasureX") != null
          && properties.getProperty("TreasureY") != null) {
        final int xTre = Integer.parseInt(properties.getProperty("TreasureX"));
        final int yTre = Integer.parseInt(properties.getProperty("TreasureY"));
        Treasure.getTreasure().setCoordinateX(xTre);
        Treasure.getTreasure().setCoordinateY(yTre);
      }

      if (properties.getProperty("nbRhum") != null) {
        for (int i = 0; i < Integer.parseInt(properties.getProperty("nbRhum")); i++) {
          isl.getRhums()
              .add(
                  new Rhum(
                      Integer.parseInt(properties.getProperty("Rhumx" + i + "")),
                      Integer.parseInt(properties.getProperty("Rhumy" + i + "")),
                      true,
                      Integer.parseInt(properties.getProperty("NRJRhum")),
                      Integer.parseInt(properties.getProperty("TMPRhum"))));
        }
      }

      if (properties.getProperty("nbCrazyMonkey") != null) {
        for (int i = 0; i < Integer.parseInt(properties.getProperty("nbCrazyMonkey")); i++) {
          isl.getMonkeys()
              .add(
                  new CrazyMonkey(
                      Integer.parseInt(properties.getProperty("VitesseSingeErratique")),
                      Integer.parseInt(properties.getProperty("CrazyMonkeyx" + i + "")),
                      Integer.parseInt(properties.getProperty("CrazyMonkeyy" + i + ""))));
        }
      }

      if (properties.getProperty("nbHunterMonkey") != null) {
        for (int i = 0; i < Integer.parseInt(properties.getProperty("nbHunterMonkey")); i++) {
          isl.getMonkeys()
              .add(
                  new HunterMonkey(
                      Integer.parseInt(properties.getProperty("VitesseSingeChasseur")),
                      Integer.parseInt(properties.getProperty("HunterMonkeyx" + i + "")),
                      Integer.parseInt(properties.getProperty("HunterMonkeyy" + i + ""))));
        }
      }

      if (properties.getProperty("NRJMaxPirate") != null) {
        this.nrjPirateMax = Integer.parseInt(properties.getProperty("NRJMaxPirate"));
      }

      if (properties.getProperty("DrunkDuration") != null) {
        this.drunkDuration = Integer.parseInt(properties.getProperty("DrunkDuration"));
      }

      if (properties.getProperty("StumbleChance") != null) {
        this.stumbleChance = Integer.parseInt(properties.getProperty("StumbleChance"));
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
