package com.sap.hana.topology.ui.tree;

import java.util.function.Predicate;
import javafx.scene.control.TreeItem;

@FunctionalInterface
public interface TreeItemPredicate<T> {

  /**
   * Test function for the predicate
   *
   * @param parent parent item
   * @param value value for checking
   * @return test result
   */
  boolean test(TreeItem<T> parent, T value);

  /**
   * Create the predicate
   *
   * @param predicate the predicate implementation
   * @param <T> type to check
   * @return created predicate
   */
  static <T> TreeItemPredicate<T> create(Predicate<T> predicate) {
    return (parent, value) -> predicate.test(value);
  }
}
