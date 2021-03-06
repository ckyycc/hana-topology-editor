package com.sap.hana.topology.ui.tree;

import java.lang.reflect.Field;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;

public final class FilterableTreeItem<T> extends TreeItem<T> {
  private ObservableList<TreeItem<T>> sourceList;
  private ObjectProperty<TreeItemPredicate<T>> predicate = new SimpleObjectProperty<>();
  private T id;

  /**
   * Constructor for FilterableTreeItem
   *
   * @param value value of the node
   * @param id id (full path) of the node
   */
  public FilterableTreeItem(T value, T id) {
    super(value);
    this.id = id;
    sourceList = FXCollections.observableArrayList();
    FilteredList<TreeItem<T>> filteredList = new FilteredList<>(this.sourceList);
    // Otherwise ask the TreeItemPredicate
    filteredList
        .predicateProperty()
        .bind(
            Bindings.createObjectBinding(
                () ->
                    child -> {
                      // Set the predicate of child items to force filtering
                      FilterableTreeItem<T> filterableChild = null;
                      if (child instanceof FilterableTreeItem) {
                        filterableChild = (FilterableTreeItem<T>) child;
                        filterableChild.setPredicate(predicate.get());
                      }
                      // If there is no predicate, keep this tree item
                      if (predicate.get() == null) {
                        return true;
                      }
                      // If there are children, keep this tree item
                      if (child.getChildren().size() > 0) {
                        return true;
                      }

                      // If it is filterable item, filter it with id,
                      // which means when hitting parent node, all sub-nodes will be shown as well)
                      if (filterableChild != null && filterableChild.getId() != null) {
                        return predicate.get().test(this, filterableChild.getId());
                      }
                      // normal item return getValue instead
                      return predicate.get().test(this, child.getValue());
                    },
                predicate));
    setHiddenFieldChildren(filteredList);
  }

  @SuppressWarnings("unchecked")
  private void setHiddenFieldChildren(ObservableList<TreeItem<T>> list) {
    try {
      Field childrenField = TreeItem.class.getDeclaredField("children"); // $NON-NLS-1$
      childrenField.setAccessible(true);
      childrenField.set(this, list);

      Field declaredField = TreeItem.class.getDeclaredField("childrenListener"); // $NON-NLS-1$
      declaredField.setAccessible(true);
      list.addListener((ListChangeListener<? super TreeItem<T>>) declaredField.get(this));
    } catch (NoSuchFieldException
        | SecurityException
        | IllegalArgumentException
        | IllegalAccessException e) {
      throw new RuntimeException("Could not set TreeItem.children", e); // $NON-NLS-1$
    }
  }

  /**
   * Get children
   *
   * @return the children of current node
   */
  public ObservableList<TreeItem<T>> getInternalChildren() {
    return this.sourceList;
  }

  private void setPredicate(TreeItemPredicate<T> predicate) {
    this.predicate.set(predicate);
  }

  public TreeItemPredicate getPredicate() {
    return predicate.get();
  }

  public ObjectProperty<TreeItemPredicate<T>> predicateProperty() {
    return predicate;
  }

  private void setId(T id) {
    this.id = id;
  }

  private T getId() {
    return this.id;
  }

  /**
   * Update value and id
   *
   * @param value value of the node
   */
  public void update(T value) {
    // A little big ugly,
    // set it to empty first, otherwise it won't trigger the update of tree cell if value is not
    // changed
    if (value != null && value.equals(this.getValue())) {
      this.setValue(null);
    }
    this.setValue(value);
  }

  /**
   * Update id
   *
   * @param id id (full path) fo the node
   */
  public void updateId(T id) {
    this.setId(id);
  }
}
