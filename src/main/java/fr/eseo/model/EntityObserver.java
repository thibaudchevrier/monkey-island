package fr.eseo.model;

/**
 * Observer of the entities of the island (Observer pattern).
 *
 * <p>Replaces the deprecated {@code java.util.Observer}: the notification is typed, so observers
 * receive the entity that changed instead of an {@code Object} to cast.
 */
@FunctionalInterface
public interface EntityObserver {

  /**
   * Called after an entity has changed: moved, appeared, disappeared or changed state.
   *
   * @param entity the entity that changed
   */
  void update(Entity entity);
}
