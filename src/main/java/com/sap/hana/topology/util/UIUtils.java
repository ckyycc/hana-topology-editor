package com.sap.hana.topology.util;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;
import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.ui.tree.FilterableTreeItem;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Utilities for UI
 */
public final class UIUtils {
    /**
     * Key for getting HostServices object
     */
    public final static String HOST_SERVICES = "hostServices";

    // Declaration of PseudoClasses
    private final static PseudoClass SUCCESS_PSEUDO = PseudoClass.getPseudoClass("success");
    private final static PseudoClass INFO_PSEUDO = PseudoClass.getPseudoClass("info");
    private final static PseudoClass WARN_PSEUDO = PseudoClass.getPseudoClass("warn");
    private final static PseudoClass ERROR_PSEUDO = PseudoClass.getPseudoClass("error");

    /**
     * Build the tree for tree view
     * @param treeNode treeNode of tree view
     * @param topologyNode tree node from topology
     * @param treeViewMap map between treeNode of tree view and treeNode of topology.
     */
    public static void buildTree (FilterableTreeItem<String> treeNode, TTNode<String> topologyNode, Map<TreeItem<String>, TTNode<String>> treeViewMap) throws TTException {
        //tree view map should be never null
        if (treeViewMap == null) {
            throw new TTException("Internal error occurred! The tree map is empty.");
        }
        //does nothing if related tree node is null
        if (topologyNode != null && treeNode != null) {
            //create tree item. For search purpose, set value to tree item if it is leaf node
            FilterableTreeItem<String> subNode = new FilterableTreeItem<>(
                    topologyNode.getValue() != null ? topologyNode.getName() + "/" + topologyNode.getValue() : topologyNode.getName());

            //update map
            treeViewMap.put(subNode, topologyNode);

            //only expand the second level tree node
            if (topologyNode.getLevel() < 2) {
                treeNode.setExpanded(true);
            }

            treeNode.getInternalChildren().add(subNode);
            if (topologyNode.getChildren() != null && topologyNode.getChildren().size() > 0) {
                for (TTNode<String> node : topologyNode.getChildren())
                    buildTree(subNode, node, treeViewMap);
            }
        }
    }

    /**
     * Get the string value of one tree node
     * @param node tree node
     * @return the string value of the tree node
     */
    public static String getTreeNodeValue4Display(TTNode<String> node) {
        return node.getName(); // only display name
    }

    /**
     * Set text to status bar for NORMAL status
     * @param statusBar object for displaying the message
     * @param text text that needs to be set
     */
    public static void setStatusText(JFXSnackbar statusBar, String text) {
        setStatusText(statusBar, text, Status.NORMAL);
    }

    /**
     * Set text to status bar
     * @param statusBar object for displaying the message
     * @param text text that needs to be set
     * @param status the status
     */
    public static void setStatusText(JFXSnackbar statusBar, String text, Status status) {
        if (statusBar.isVisible()) {
            statusBar.close(); //always close the previous one if have
        }

        if (CommonUtils.isNullOrEmpty(text)) {
            return; //not to display empty text
        }

        //set width for status bar base on the container width and font size
        int lenRestriction = (int) ((statusBar.getPopupContainer().getWidth() - 55) / 6.2);
        if (text.length() > lenRestriction) {
            text = "..." + text.substring(text.length() - lenRestriction);
        }

        JFXSnackbarLayout layout = new JFXSnackbarLayout(text, "x", action -> statusBar.close());

        if (statusBar.getChildren().size() > 0 && statusBar.getChildren().get(0) instanceof StackPane) {
            StackPane sbPane = (StackPane) statusBar.getChildren().get(0);
            switch (status) {
                case SUCCESS:
                    sbPane.pseudoClassStateChanged(SUCCESS_PSEUDO, true);
                    sbPane.pseudoClassStateChanged(INFO_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(WARN_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(ERROR_PSEUDO, false);
                    break;
                case INFO:
                    sbPane.pseudoClassStateChanged(SUCCESS_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(INFO_PSEUDO, true);
                    sbPane.pseudoClassStateChanged(WARN_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(ERROR_PSEUDO, false);
                    break;
                case WARN:
                    sbPane.pseudoClassStateChanged(SUCCESS_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(WARN_PSEUDO, true);
                    sbPane.pseudoClassStateChanged(INFO_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(ERROR_PSEUDO, false);
                    break;
                case ERROR:
                    sbPane.pseudoClassStateChanged(SUCCESS_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(ERROR_PSEUDO, true);
                    sbPane.pseudoClassStateChanged(WARN_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(INFO_PSEUDO, false);
                    break;
                default:
                    sbPane.pseudoClassStateChanged(SUCCESS_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(INFO_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(WARN_PSEUDO, false);
                    sbPane.pseudoClassStateChanged(ERROR_PSEUDO, false);
            }
        }
        statusBar.fireEvent(new JFXSnackbar.SnackbarEvent(layout, Duration.INDEFINITE, null));
    }

    /**
     * General function for showing the dialog
     * @param resource FXML path
     * @param pane dialog container
     * @param consumer consumer for delivering message (via controller)
     * @param <T> Controller
     */
    public static <T> void showDialog(String resource, StackPane pane, Consumer<T> consumer) throws
            IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        showDialog(resource, pane, consumer, false);
    }

    /**
     * General function for showing the dialog
     * @param resource FXML path
     * @param pane dialog container
     * @param consumer consumer for delivering message (via controller)
     * @param overlayClose Overlay Close flag, to prevent from being closed when click outside, set it to false
     * @param <T> Controller
     */
    public static <T> void showDialog(String resource, StackPane pane, Consumer<T> consumer, boolean overlayClose) throws
            IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        FXMLLoader loader = new FXMLLoader(UIUtils.class.getResource(resource));
        Parent parent = loader.load();

        T controller = loader.getController();

        VBox dlgContent = new VBox();
        dlgContent.getChildren().add(parent);

        JFXDialog dialog = new JFXDialog(pane, dlgContent, JFXDialog.DialogTransition.CENTER);

        dialog.setOverlayClose(overlayClose);  //prevent from being closed when click outside

        //set default focus to the dialog if setDefaultFocus is implemented
        dialog.setOnDialogOpened((e) -> {
            try {
                //try to invoke setDefaultFocus, exception will be ignored if it is not implemented
                Method setFocusMethod = controller.getClass().getMethod("setDefaultFocus");
                setFocusMethod.invoke(controller);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        Method setDialogMethod = controller.getClass().getMethod("setDialog", JFXDialog.class);
        setDialogMethod.invoke(controller, dialog);

        consumer.accept(controller);
    }
}
