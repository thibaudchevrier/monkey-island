package fr.eseo.model.test;

import fr.eseo.model.Case;
import fr.eseo.model.CaseType;
import fr.eseo.model.Treasure;

/**
 * Shared test fixtures. The game keeps its state in singletons, so the tests reset them explicitly
 * instead of depending on the order they run in.
 */
final class Fixtures {

  private Fixtures() {}

  /** Hide the treasure in a sea corner and forget its observers. */
  static void resetTreasure() {
    final Treasure treasure = Treasure.getTreasure();
    treasure.deleteObservers();
    treasure.setVisibility(false);
    treasure.setCoordinateX(0);
    treasure.setCoordinateY(0);
  }

  /**
   * A square board with sea on the border, like the island built by Island.setIsland.
   *
   * @param size the number of cases on each side
   * @return the board
   */
  static Case[][] board(int size) {
    final Case[][] cases = new Case[size][size];
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        final boolean border = i == 0 || j == 0 || i == size - 1 || j == size - 1;
        cases[i][j] = new Case(i, j, border ? CaseType.sea : CaseType.earth);
      }
    }
    return cases;
  }
}
