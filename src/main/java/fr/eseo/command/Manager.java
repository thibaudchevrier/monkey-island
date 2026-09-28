package fr.eseo.command;

import fr.eseo.communication.Client;
import fr.eseo.communication.ProtocoleMonkeyIsland;
import fr.eseo.model.CollisionException;
import fr.eseo.model.Configuration;
import fr.eseo.model.CrazyMonkey;
import fr.eseo.model.Entity;
import fr.eseo.model.EntityObserver;
import fr.eseo.model.HunterMonkey;
import fr.eseo.model.Island;
import fr.eseo.model.Monkey;
import fr.eseo.model.Pirate;
import fr.eseo.model.Rhum;
import fr.eseo.model.StatePirate;
import fr.eseo.model.Treasure;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * The class Action for the design pattern Command.
 *
 * <p>There is one Manager per connected client, for the whole connection. It owns the client's
 * pirate and observes the entities of the island.
 */
public class Manager implements EntityObserver, KeyListener {

  private String message;

  private int id;

  private Client client;

  private Pirate pirate;

  /**
   * Instantiates a new Action.
   *
   * @param client the client of the game
   */
  public Manager(Client client) {
    this.client = client;
    this.id = ProtocoleMonkeyIsland.formaterIdentificationPort(client);
  }

  /**
   * Set the last message received from the client.
   *
   * @param message the message of the action
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /** Observe the rum bottles, the monkeys and the treasure of the island. */
  private void addObserver() {
    for (Rhum rhum : Island.getInstance().getRhums()) {
      rhum.addObserver(this);
    }
    for (Monkey monkey : Island.getInstance().getMonkeys()) {
      monkey.addObserver(this);
    }
    Treasure.getTreasure().addObserver(this);
  }

  /** Stop observing the island. */
  private void deleteObserver() {
    for (Rhum rhum : Island.getInstance().getRhums()) {
      rhum.deleteObserver(this);
    }
    for (Monkey monkey : Island.getInstance().getMonkeys()) {
      monkey.deleteObserver(this);
    }
    Treasure.getTreasure().deleteObserver(this);
  }

  /**
   * Send a message to every other registered client.
   *
   * @param broadcast the message
   */
  private void diffuser(String broadcast) {
    this.client.getMonkeyIsland().diffuseAutres(broadcast, this.client);
  }

  /** Send the rum bottles and the monkeys to the client. */
  private void envoieElements() {
    final Island island = Island.getInstance();
    this.client.envoieMessage(ProtocoleMonkeyIsland.formaterPositionRhum(island.getRhums()));
    this.client.envoieMessage(
        ProtocoleMonkeyIsland.formaterPositionSingeCrazy(island.getMonkeys()));
    this.client.envoieMessage(
        ProtocoleMonkeyIsland.formaterPositionSingeHunter(island.getMonkeys()));
  }

  /** Register a new client for the game. */
  public void inscription() {
    synchronized (Island.LOCK) {
      if (this.pirate != null) {
        return;
      }
      final Island island = Island.getInstance();
      final Point p = island.addPirateEntity();
      this.pirate = new Pirate(this.id, Configuration.getInstance().getNRJMax(), p.x, p.y);
      island.getPirates().add(this.pirate);
      this.pirate.addObserver(this);
      this.addObserver();
      this.client.getMonkeyIsland().inscriptionCanal(this.client);

      this.client.envoieMessage(
          ProtocoleMonkeyIsland.formatteMessageIndicationCarte(island.getCase()));
      this.client.envoieMessage(ProtocoleMonkeyIsland.MESSAGE_INSCRIPTION);
      this.client.envoieMessage(ProtocoleMonkeyIsland.formaterIdentificationPirate(this.pirate));
      this.envoieElements();
      this.client.envoieMessage(
          ProtocoleMonkeyIsland.formaterTousPirates(island.getPirates(), this.pirate));
      if (Treasure.getTreasure().getVisibility()) {
        this.client.envoieMessage(ProtocoleMonkeyIsland.formaterIdentificationTreasure());
      }
      this.diffuser(ProtocoleMonkeyIsland.formaterNouveauPirate(this.pirate));
    }
  }

  /** Handle the movement of the pirate of the client. */
  public void movePirate() {
    synchronized (Island.LOCK) {
      // Every move gets an answer (/A or /R): Guybrush locks its keyboard until then.
      if (this.pirate == null) {
        this.client.envoieMessageErreur("Pirate non inscrit : envoyer /I avant de se déplacer.");
        this.client.envoieMessage(ProtocoleMonkeyIsland.formaterRefusDeplacementPirate());
        return;
      }
      if (this.pirate.getState() == StatePirate.dead) {
        this.client.envoieMessage(ProtocoleMonkeyIsland.formaterRefusDeplacementPirate());
        return;
      }
      try {
        final Point p = ProtocoleMonkeyIsland.commandeDuDeplacement(message);
        this.pirate.movementPirate(p.x, p.y);
      } catch (CollisionException e) {
        this.client.envoieMessage(ProtocoleMonkeyIsland.formaterRefusDeplacementPirate());
      } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
        this.client.envoieMessageErreur("Déplacement invalide : " + message);
        this.client.envoieMessage(ProtocoleMonkeyIsland.formaterRefusDeplacementPirate());
      }
      final Treasure treasure = Treasure.getTreasure();
      if (treasure.getVisibility()
          && this.pirate.getCoordinateX() == treasure.getCoordinateX()
          && this.pirate.getCoordinateY() == treasure.getCoordinateY()) {
        this.client.getMonkeyIsland().programmeNouvellePartie();
      }
    }
  }

  /**
   * A new game starts: the client clears its island and receives the rum and the monkeys. The
   * respawn of the pirates is sent afterwards, through the observers.
   */
  public void nouvellePartie() {
    this.client.envoieMessage(ProtocoleMonkeyIsland.formaterNouvellePartie());
    this.envoieElements();
  }

  /** The client leaves: remove its pirate from the island and stop observing it. */
  public void quitte() {
    synchronized (Island.LOCK) {
      this.deleteObserver();
      if (this.pirate != null) {
        this.pirate.deleteObserver(this);
        Island.getInstance().getPirates().remove(this.pirate);
        this.diffuser(ProtocoleMonkeyIsland.formaterSuppressionPirate(this.pirate));
        this.pirate = null;
      }
    }
  }

  /**
   * Resend the position of the pirate to the client.
   *
   * <p>Guybrush locks its keyboard on every key press until the server answers, even for keys that
   * send nothing (Shift, Cmd...). Any acceptation unlocks it.
   */
  private void liberationClavier() {
    if (this.pirate != null && this.pirate.getState() != StatePirate.dead) {
      this.client.envoieMessage(
          ProtocoleMonkeyIsland.formaterAcceptationDeplacementPirate(this.pirate));
    }
  }

  @Override
  public void keyTyped(KeyEvent e) {

    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
      try {
        this.client.envoieMessage("/D -1 0");
      } catch (Exception ex) {
        System.out.println("wrong key type");
      }
    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
      try {
        this.client.envoieMessage("/D 1 0");
      } catch (Exception ex) {
        System.out.println("wrong key type");
      }
    } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
      try {
        this.client.envoieMessage("/D 0 1");
      } catch (Exception ex) {
        System.out.println("wrong key type");
      }
    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
      try {
        this.client.envoieMessage("/D -1 0");
      } catch (Exception ex) {
        System.out.println("wrong key type");
      }
    }
  }

  @Override
  public void update(Entity entity) {
    if (entity == this.pirate) {
      this.client.envoieMessage(
          ProtocoleMonkeyIsland.formaterAcceptationDeplacementPirate(this.pirate));
      if (this.pirate.getState() == StatePirate.dead) {
        this.diffuser(ProtocoleMonkeyIsland.formaterSuppressionPirate(this.pirate));
      } else {
        this.diffuser(ProtocoleMonkeyIsland.formaterDeplacementPirate(this.pirate));
      }
    } else if (entity instanceof Rhum rhum) {
      this.client.envoieMessage(
          ProtocoleMonkeyIsland.formaterIdentificationRhum(
              rhum, Island.getInstance().getRhums().indexOf(rhum)));
    } else if (entity instanceof Treasure) {
      if (Treasure.getTreasure().getVisibility()) {
        this.client.envoieMessage(ProtocoleMonkeyIsland.formaterIdentificationTreasure());
      }
    } else if (entity instanceof CrazyMonkey) {
      this.client.envoieMessage(
          ProtocoleMonkeyIsland.formaterPositionSingeCrazy(Island.getInstance().getMonkeys()));
      this.liberationClavier();
    } else if (entity instanceof HunterMonkey) {
      this.client.envoieMessage(
          ProtocoleMonkeyIsland.formaterPositionSingeHunter(Island.getInstance().getMonkeys()));
      this.liberationClavier();
    }
  }

  @Override
  public void keyPressed(KeyEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void keyReleased(KeyEvent arg0) {
    // TODO Auto-generated method stub

  }
}
