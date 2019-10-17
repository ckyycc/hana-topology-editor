package com.sap.hana.topology.ui.tree;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;

import java.lang.reflect.Field;

public final class FilterableTreeItem<T> extends TreeItem<T> {
    final private ObservableList<TreeItem<T>> sourceList;
    private ObjectProperty<TreeItemPredicate<T>> predicate = new SimpleObjectProperty<>();

    public FilterableTreeItem(T value) {
        super(value);
        sourceList = FXCollections.observableArrayList();
        FilteredList<TreeItem<T>> filteredList = new FilteredList<>(this.sourceList);
        // Otherwise ask the TreeItemPredicate
        filteredList.predicateProperty().bind(Bindings.createObjectBinding(() -> child -> {
            // Set the predicate of child items to force filtering
            if (child instanceof FilterableTreeItem) {
                FilterableTreeItem<T> filterableChild = (FilterableTreeItem<T>) child;
                filterableChild.setPredicate(this.predicate.get());
            }
            // If there is no predicate, keep this tree item
            if (predicate.get() == null)
                return true;
            // If there are children, keep this tree item
            if (child.getChildren().size() > 0)
                return true;

            return predicate.get().test(this, child.getValue());
        }, predicate));
        setHiddenFieldChildren(filteredList);
    }

    @SuppressWarnings("unchecked")
    private void setHiddenFieldChildren(ObservableList<TreeItem<T>> list) {
        try {
            Field childrenField = TreeItem.class.getDeclaredField("children"); //$NON-NLS-1$
            childrenField.setAccessible(true);
            childrenField.set(this, list);

            Field declaredField = TreeItem.class.getDeclaredField("childrenListener"); //$NON-NLS-1$
            declaredField.setAccessible(true);
            list.addListener((ListChangeListener<? super TreeItem<T>>) declaredField.get(this));
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("Could not set TreeItem.children", e); //$NON-NLS-1$
        }
    }

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
}

