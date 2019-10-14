package com.sap.hana.tools.topology.util;

import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXSnackbarLayout;
import com.sap.hana.tools.topology.exception.TTException;
import com.sap.hana.tools.topology.tree.TTNode;
import com.sap.hana.tools.topology.ui.tree.FilterableTreeItem;
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
public class UIUtils {
    /**
     * Key for getting HostServices object
     */
    public final static String HOST_SERVICES = "hostServices";

    // Declaration of PseudoClasses
    private final static PseudoClass INFO_PSEUDO = PseudoClass.getPseudoClass("info");
    private final static PseudoClass WARN_PSEUDO = PseudoClass.getPseudoClass("warn");
    private final static PseudoClass ERROR_PSEUDO = PseudoClass.getPseudoClass("error");

    /**
     * Build the tree for tree view
     * @param treeNode treeNode of tree view
     * @param topologyNode tree node from topology
     * @param treeViewMap map between treeNode of tree view and treeNode of topology.
     */
    public static void buildTree (FilterableTreeItem<String> treeNode, TTNode topologyNode, Map<TreeItem<String>, TTNode> treeViewMap) throws TTException {
        //tree view map should be never null
        if (treeViewMap == null) {
            throw new TTException("Internal error occurred! The tree map is empty.");
        }
        //does nothing if related tree node is null
        if (topologyNode != null && treeNode != null) {
            FilterableTreeItem<String> subNode = new FilterableTreeItem<>(topologyNode.getName());

            //update map
            treeViewMap.put(subNode, topologyNode);

            //only expand the second level tree node
            if (topologyNode.getLevel() < 2) {
                treeNode.setExpanded(true);
            }

            treeNode.getInternalChildren().add(subNode);
            if (topologyNode.getChildren() != null && topologyNode.getChildren().size() > 0) {
                for (TTNode node : topologyNode.getChildren())
                    buildTree(subNode, node, treeViewMap);
            }
        }
    }

    /**
     * Get the string value of one tree node
     * @param node tree node
     * @return the string value of the tree node
     */
    public static String getTreeNodeValue4Display(TTNode node) {
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

        int lenRestriction = (int) ((statusBar.getPopupContainer().getWidth() - 55) / 6.2);
        if (text.length() > lenRestriction) {
            text = "..." + text.substring(text.length() - lenRestriction);
        }

        JFXSnackbarLayout layout = new JFXSnackbarLayout(text, "x", action -> statusBar.close());
        if (layout.getLeft() instanceof  StackPane) {
            Node label = ((StackPane)layout.getLeft()).getChildren().get(0);
            switch (status) {
                case INFO:
                    label.pseudoClassStateChanged(INFO_PSEUDO, true);
                    label.pseudoClassStateChanged(WARN_PSEUDO, false);
                    label.pseudoClassStateChanged(ERROR_PSEUDO, false);
                    break;
                case WARN:
                    label.pseudoClassStateChanged(WARN_PSEUDO, true);
                    label.pseudoClassStateChanged(INFO_PSEUDO, false);
                    label.pseudoClassStateChanged(ERROR_PSEUDO, false);
                    break;
                case ERROR:
                    label.pseudoClassStateChanged(ERROR_PSEUDO, true);
                    label.pseudoClassStateChanged(WARN_PSEUDO, false);
                    label.pseudoClassStateChanged(INFO_PSEUDO, false);
                    break;
                default:
                    layout.pseudoClassStateChanged(INFO_PSEUDO, false);
                    layout.pseudoClassStateChanged(WARN_PSEUDO, false);
                    layout.pseudoClassStateChanged(ERROR_PSEUDO, false);
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
