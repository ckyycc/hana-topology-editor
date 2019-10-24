package com.sap.hana.topology.ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSnackbar;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeView;
import com.jfoenix.controls.JFXTreeViewPath;
import com.sap.hana.topology.tree.TTNode;
import com.sap.hana.topology.tree.controller.TTController;
import com.sap.hana.topology.tree.controller.TTControllerImpl;
import com.sap.hana.topology.ui.dialog.AboutDialogController;
import com.sap.hana.topology.ui.dialog.ConfirmationDialogController;
import com.sap.hana.topology.ui.dialog.DialogController;
import com.sap.hana.topology.ui.dialog.NodeEditDialogController;
import com.sap.hana.topology.ui.tree.FilterableTreeItem;
import com.sap.hana.topology.ui.tree.TreeCellImpl;
import com.sap.hana.topology.ui.tree.TreeItemPredicate;
import com.sap.hana.topology.util.CommonUtils;
import com.sap.hana.topology.util.Status;
import com.sap.hana.topology.util.TTException;
import com.sap.hana.topology.util.TreeUtils;
import com.sap.hana.topology.util.UIUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public final class TopologyEditorController {
  private File lastFileFolder;
  private TTNode<String> selectedNode;
  private String topologyFileName;
  private String topologyStr;
  private TTNode<String> topologyRootNode;
  private Map<TreeItem<String>, TTNode<String>> treeViewMap;
  private FilterableTreeItem<String> treeItemRootNode;
  private BooleanProperty isCurrentNodeLeaf;
  private BooleanProperty isCurrentNodeRoot;
  private BooleanProperty isCurrentTreeNotLoaded;
  private JFXTreeViewPath treeViewPath;
  private PseudoClass dragAreaPseudo;
  private JFXSnackbar snackbar;
  private FileChooser fileChooser;
  @FXML private StackPane spMain;
  @FXML private VBox vbMain;
  @FXML private JFXTextField txtFilter;
  @FXML private JFXButton btnImport;
  @FXML private VBox vbTree;
  @FXML private JFXButton btnExport;
  @FXML private JFXButton btnReload;
  @FXML private JFXButton btnAbout;
  @FXML private JFXTreeView<String> tvTopology;
  @FXML private ContextMenu contextMenu;
  @FXML private MenuItem miAdd;
  @FXML private MenuItem miEdit;
  @FXML private MenuItem miDelete;

  @FXML
  private void initialize() {

    vbMain.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> hideMessageBar());
    vbMain.addEventFilter(KeyEvent.KEY_PRESSED, event -> hideMessageBar());

    treeViewMap = new HashMap<>();
    isCurrentNodeLeaf = new SimpleBooleanProperty(true);
    isCurrentNodeRoot = new SimpleBooleanProperty(true);
    isCurrentTreeNotLoaded = new SimpleBooleanProperty(true);
    dragAreaPseudo = PseudoClass.getPseudoClass("ondrag");

    snackbar = new JFXSnackbar(spMain);
    /*----------- Tree View Setting -----------*/
    treeItemRootNode =
        new FilterableTreeItem<>(
            TreeUtils.TOPOLOGY_TREE_ROOT_NAME, TreeUtils.TOPOLOGY_TREE_ROOT_NAME); // , rootIcon);

    // bind filter
    treeItemRootNode
        .predicateProperty()
        .bind(
            Bindings.createObjectBinding(
                () -> {
                  if (CommonUtils.isNullOrEmpty(txtFilter.getText())) return null;
                  return TreeItemPredicate.create(
                      actor ->
                          actor.toLowerCase().contains(txtFilter.getText().toLowerCase().trim()));
                },
                txtFilter.textProperty()));

    treeItemRootNode.setExpanded(true);

    tvTopology.setRoot(treeItemRootNode);
    tvTopology.setShowRoot(false);

    // do not show context menu if tree is empty
    tvTopology
        .contextMenuProperty()
        .bind(
            Bindings.when(tvTopology.getRoot().leafProperty())
                .then((ContextMenu) null)
                .otherwise(contextMenu));

    // selection change listener
    tvTopology
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (newValue == null) {
                // reset selectNode if no selection
                selectedNode = null;
                isCurrentNodeRoot.setValue(true);
                isCurrentNodeLeaf.setValue(true);
              } else {
                if (treeViewMap.containsKey(newValue)) {
                  selectedNode = treeViewMap.get(newValue);
                  isCurrentNodeLeaf.setValue(selectedNode.isLeaf()); // update leaf status
                  isCurrentNodeRoot.setValue(selectedNode.isRoot()); // update root status
                }
              }
            });

    // tree tree cell
    tvTopology.setCellFactory(tc -> new TreeCellImpl(treeViewMap));

    /*----------- Button Setting -----------*/
    miAdd.disableProperty().bind(isCurrentNodeLeaf);
    miDelete.disableProperty().bind(isCurrentNodeRoot);
    miEdit.disableProperty().bind(isCurrentNodeRoot);

    // disable export when tree is empty
    // below will cause the button be disabled when search term does not hit anything.
    // btnExport.disableProperty().bind(tvTopology.getRoot().leafProperty());
    btnExport.disableProperty().bind(isCurrentTreeNotLoaded);
    // disable reload when tree is empty
    btnReload.disableProperty().bind(isCurrentTreeNotLoaded);
    // disable filter when tree is empty
    txtFilter.disableProperty().bind(isCurrentTreeNotLoaded);

    /*------------focus bindings-------------*/
    btnExport.focusTraversableProperty().bind(AboutDialogController.isDialogOpened().not());
    btnImport.focusTraversableProperty().bind(AboutDialogController.isDialogOpened().not());
    btnReload.focusTraversableProperty().bind(AboutDialogController.isDialogOpened().not());
    txtFilter.focusTraversableProperty().bind(AboutDialogController.isDialogOpened().not());
    btnAbout.focusTraversableProperty().bind(AboutDialogController.isDialogOpened().not());
    tvTopology.focusTraversableProperty().bind(AboutDialogController.isDialogOpened().not());
  }

  /**
   * Export the current topology to file
   *
   * @param event export button click event
   */
  @FXML
  private void onExport(ActionEvent event) {
    fileChooser = new FileChooser();

    fileChooser.setTitle("Save file");
    fileChooser.setInitialFileName("exp_" + topologyFileName);
    if (lastFileFolder != null) {
      fileChooser.setInitialDirectory(lastFileFolder);
    }

    File savedFile = fileChooser.showSaveDialog(tvTopology.getScene().getWindow());

    if (savedFile != null) {
      // update last processed file path
      lastFileFolder = savedFile.getParentFile();
      try {
        if (savedFile.exists() && !savedFile.delete()) {
          showMsg(
              "Failed to replace the file:"
                  + savedFile.toString()
                  + ", please check the access right.",
              Status.ERROR);
        } else if (savedFile.createNewFile()) {
          // Use StreamWriter instead, to make sure the UTF8 charset
          try (Writer writer =
              new OutputStreamWriter(new FileOutputStream(savedFile), StandardCharsets.UTF_8)) {
            writer.write(getController().exportTopology(this.topologyRootNode));
          }
          //                    try (FileWriter writer = new FileWriter(savedFile)) {
          //
          // writer.write(getController().exportTopology(this.topologyRootNode));
          //                    }
          showMsg("File saved: " + savedFile.toString(), Status.INFO);
        } else {
          showMsg(
              "Failed to save the file:"
                  + savedFile.toString()
                  + ", please check the access right.",
              Status.ERROR);
        }
      } catch (IOException | TTException e) {
        showMsg("An ERROR occurred while saving the file:" + savedFile.toString(), Status.ERROR);
      }
    } else {
      showMsg("File export is cancelled", Status.INFO);
    }

    // keep fileChooser null for checking whether fileChooser dialog is opening
    fileChooser = null;
    event.consume();
  }

  /**
   * Import the topology from topology file
   *
   * @param event import button click event
   */
  @FXML
  private void onImport(ActionEvent event) {
    fileChooser = new FileChooser();
    // set initial directory
    if (lastFileFolder != null) {
      fileChooser.setInitialDirectory(lastFileFolder);
    }

    File selectedFile = fileChooser.showOpenDialog(tvTopology.getScene().getWindow());

    if (selectedFile != null) {
      // update last processed file path
      lastFileFolder = selectedFile.getParentFile();
      loadTopologyFromFile(selectedFile.getAbsolutePath(), selectedFile.getName());
    } else {
      showMsg("File selection is cancelled", Status.INFO);
    }

    // keep fileChooser null for checking whether fileChooser dialog is opening
    fileChooser = null;
    event.consume();
  }

  /**
   * Reload the tree, resetting all the changes
   *
   * @param event reload button click event
   * @throws TTException the exception when building the tree
   */
  @FXML
  private void onReload(ActionEvent event) throws TTException {
    topologyRootNode = getController().loadTopology(topologyStr);
    treeItemRootNode.getInternalChildren().clear(); // reset whole tree
    txtFilter.setText("");

    UIUtils.buildTree(treeItemRootNode, topologyRootNode, treeViewMap);
    addTreeViewPathToContainer();

    if (event != null) {
      showMsg("Topology tree is reloaded.", Status.INFO);
      event.consume();
    }
  }

  /**
   * Display about dialog
   *
   * @param event about button click event
   */
  @FXML
  private void onAbout(ActionEvent event) {
    try {
      UIUtils.<AboutDialogController>showDialog(
          "/fxml/dialog/AboutDialog.fxml",
          (StackPane) spMain.getScene().getRoot(),
          controller -> {
            controller.setConsumer(m -> controller.close());
            controller.show();
          },
          true);
    } catch (Exception e) {
      showMsg("Error occurred when loading about dialog: " + e.getMessage(), Status.ERROR);
    }
    event.consume();
  }

  /**
   * Add a new node
   *
   * @param event add button click event
   */
  @FXML
  private void onAdd(ActionEvent event) {
    if (!checkForDisplayingEditDialog() || selectedNode == null) {
      return;
    }
    if (selectedNode.isLeaf()) {
      showMsg("Can not add sub node to leaf.", Status.WARN);
    } else {
      loadDialog(true, null, null, selectedNode.getId(), true, false);
    }
    event.consume();
  }

  /**
   * Delete current node
   *
   * @param event delete button click event
   */
  @FXML
  private void onDelete(ActionEvent event) {
    if (!checkForDisplayingEditDialog() || selectedNode == null) {
      return;
    }

    if (selectedNode.getParent() == null) {
      showMsg("Can not delete root node.", Status.WARN);
      return;
    }

    // delete confirmation
    try {
      UIUtils.<ConfirmationDialogController>showDialog(
          "/fxml/dialog/ConfirmationDialog.fxml",
          (StackPane) spMain.getScene().getRoot(),
          controller -> {
            controller.setContent1("Are you sure you want to delete this node?");
            controller.setContent2(selectedNode.getId());
            controller.setTitle("Confirm");
            controller.setConsumer(
                m -> {
                  if (m == ButtonType.OK) {
                    String nodeToDelete = selectedNode.getId();
                    // remove it from topology first, otherwise the selectedNode will be changed
                    // later
                    selectedNode.getParent().deleteChild(selectedNode);
                    // delete it from treeView
                    FilterableTreeItem<String> c =
                        (FilterableTreeItem<String>)
                            tvTopology.getSelectionModel().getSelectedItem();
                    treeViewMap.remove(c);
                    ((FilterableTreeItem<String>) c.getParent()).getInternalChildren().remove(c);
                    showMsg("'" + nodeToDelete + "' is deleted.", Status.SUCCESS);
                  }

                  controller.close();
                  tvTopology.requestFocus(); // set focus back to topology tree
                });
            controller.show();
          });
    } catch (Exception e) {
      showMsg("Error occurred when loading confirmation dialog: " + e.getMessage(), Status.ERROR);
    }
    event.consume();
  }

  /**
   * Edit current node
   *
   * @param event edit button click event
   */
  @FXML
  private void onEdit(ActionEvent event) {
    if (!checkForDisplayingEditDialog() || selectedNode == null) {
      return;
    }

    if (selectedNode.getParent() == null) {
      showMsg("Can not modify root node.", Status.WARN);
      return;
    }

    loadDialog(
        selectedNode.isLeaf(),
        selectedNode.getName(),
        selectedNode.getValue(),
        selectedNode.getParent().getId(),
        false,
        !CommonUtils.isNullOrEmpty(selectedNode.getChildren()));
    event.consume();
  }

  /**
   * The drag is dropped, processing the dropped file
   *
   * @param event drag event
   */
  @FXML
  private void OnDragDropped(DragEvent event) {
    if (!checkForDragDropCondition()) {
      event.consume();
      return;
    }
    Dragboard db = event.getDragboard();
    boolean success = false;
    if (db.hasFiles()) {
      List<File> files = db.getFiles();
      if (files.size() > 1) {
        showMsg(
            "This tool does not support multiple (" + files.size() + ") topology files.",
            Status.ERROR);
      } else if (files.size() == 1) {
        loadTopologyFromFile(files.get(0).getAbsolutePath(), files.get(0).getName());
      }
      success = true;
    }
    /* let the source know whether the string was successfully transferred and used */
    event.setDropCompleted(success);
    tvTopology.pseudoClassStateChanged(dragAreaPseudo, false);
    event.consume();
  }

  /**
   * Start dragging over, activating the drag css
   *
   * @param event drag event
   */
  @FXML
  private void OnDragOver(DragEvent event) {
    if (!checkForDragDropCondition()) {
      event.consume();
      return;
    }

    if (event.getDragboard().hasFiles()) {
      event.acceptTransferModes(TransferMode.LINK);
      tvTopology.pseudoClassStateChanged(dragAreaPseudo, true);
    }

    event.consume();
  }

  /**
   * Exit the drag, deactivating the drag css
   *
   * @param event drag event
   */
  @FXML
  private void OnDragExited(DragEvent event) {
    tvTopology.pseudoClassStateChanged(dragAreaPseudo, false);
    event.consume();
  }

  /** load topology from file */
  private void loadTopologyFromFile(String fullPath, String fileName) {
    String topologyStrBak = topologyStr;
    try {
      Path path = Paths.get(fullPath);
      if (Files.isDirectory(path)) {
        showMsg("'" + path + "' is a directory.", Status.ERROR);
        return;
      }
      if (Files.size(path) > 52428800) {
        showMsg(
            "File is bigger than 50MB, please make sure you chose the correct topology file.",
            Status.ERROR);
        return;
      }
      // Use UTF8 for those bytes, otherwise the first character will be replaced with ""
      topologyStr =
          (new String(Files.readAllBytes(path), StandardCharsets.UTF_8))
              .replaceAll("[^\\n\\r\\t\\p{Print}]", "");
      onReload(null);

      topologyFileName = fileName;
      showMsg("File imported: " + fileName, Status.INFO);
      isCurrentTreeNotLoaded.setValue(false); // set tree status to "loaded"
    } catch (Exception e) {
      showMsg("Error: " + e.getMessage(), Status.ERROR);
      topologyStr = topologyStrBak; // restore topology String if exception occurs
    }
  }

  /** load the dialog for editing tree node */
  private void loadDialog(
      boolean isLeaf,
      String name,
      String value,
      String path,
      boolean insertFlag,
      boolean childFlag) {
    try {
      UIUtils.<NodeEditDialogController>showDialog(
          "/fxml/dialog/NodeEditDialog.fxml",
          (StackPane) spMain.getScene().getRoot(),
          controller -> {
            // set info to the editor controller
            controller.setParentPath(path);
            controller.setLeaf(isLeaf);
            controller.setName(name);
            controller.setValue(value);
            controller.setInsertFlag(insertFlag);
            controller.setChildFlag(childFlag);
            controller.setConsumer(
                m -> {
                  boolean result = true;
                  if (m != null) {
                    try {
                      boolean insert =
                          Boolean.parseBoolean(m.get(NodeEditDialogController.INSERT_FLAG));
                      String nameNew = m.get(NodeEditDialogController.TXT_NAME);
                      String valueNew = m.get(NodeEditDialogController.TXT_VALUE);
                      boolean leaf = Boolean.parseBoolean(m.get(NodeEditDialogController.TB_LEAF));

                      String nameValueNew =
                          leaf ? nameNew + TreeUtils.NAME_VALUE_DELIMITER + valueNew : nameNew;
                      String nameValueOrg =
                          leaf ? name + TreeUtils.NAME_VALUE_DELIMITER + value : name;

                      if (insert) { // insert
                        result = insertNode(nameNew, valueNew, leaf);
                        if (result) {
                          showMsg(
                              "'" + nameValueNew + "' is added to " + path + ".", Status.SUCCESS);
                        }
                      } else { // update
                        result = updateCurrentNode(nameNew, valueNew, leaf);
                        if (result) {
                          showMsg(
                              "'" + nameValueOrg + "' is changed to '" + nameValueNew + "'.",
                              Status.SUCCESS);
                        }
                      }
                    } catch (Exception e) {
                      showMsg("Error occurred: " + e.getMessage(), Status.ERROR);
                    }
                  }
                  if (result) {
                    controller.close();
                  } else {
                    showMsg(
                        "Operation failed, check whether the name '"
                            + m.get(NodeEditDialogController.TXT_NAME)
                            + "' already exists.",
                        Status.ERROR);
                  }
                  tvTopology.requestFocus(); // set focus back to tree
                });
            controller.show();
          });
    } catch (Exception e) {
      showMsg("Error occurred when loading editor dialog: " + e.getMessage(), Status.ERROR);
    }
  }

  /**
   * Insert the node to current topology tree view if the node is not duplicated, and update tree
   * view map afterwards.
   *
   * @param name tree node name
   * @param value tree node value
   * @param isLeaf whether the node is a leaf
   * @return result for the inserting
   */
  private boolean insertNode(String name, String value, boolean isLeaf) {
    // check whether the node already exists
    if (TreeUtils.getTopologyNodeFromParent(selectedNode, name) != null) {
      return false;
    }

    // update topology
    TTNode<String> topologyNode =
        isLeaf ? new TTNode<>(selectedNode, name, value) : new TTNode<>(selectedNode, name);

    selectedNode.addChild(topologyNode);

    FilterableTreeItem<String> subNode =
        new FilterableTreeItem<>(
            UIUtils.getTreeNodeValue4Display(topologyNode),
            UIUtils.getId4FilterableTreeItem(topologyNode));
    treeViewMap.put(subNode, topologyNode); // update map

    FilterableTreeItem<String> treeNode =
        (FilterableTreeItem<String>) tvTopology.getSelectionModel().getSelectedItem();

    treeNode.getInternalChildren().add(subNode);
    return true;
  }

  /**
   * Update the node from current topology tree view if the new name is not duplicated, and update
   * tree view map afterwards.
   *
   * @param name the new name of the node
   * @param value the new value of the node
   * @return result of updating
   */
  private boolean updateCurrentNode(String name, String value, boolean isLeaf) {
    if (!name.equalsIgnoreCase(selectedNode.getName())) {
      // check whether the node already exists
      if (TreeUtils.getTopologyNodeFromParent(selectedNode.getParent(), name) != null) {
        return false;
      }
    }
    selectedNode.setName(name);
    if (isLeaf) {
      if (CommonUtils.isNullOrEmpty(selectedNode.getChildren())) {
        selectedNode.setValue(value);
        // current selected node is now leaf
        isCurrentNodeLeaf.set(true);
      } else {
        showMsg("Can't set value to a node which contains subNode", Status.WARN);
      }
    } else {
      selectedNode.setLeaf(false);
      // current selected node is now not leaf
      isCurrentNodeLeaf.set(false);
    }

    // update treeView item
    FilterableTreeItem<String> treeNode =
        (FilterableTreeItem<String>) tvTopology.getSelectionModel().getSelectedItem();
    // update value
    treeNode.update(UIUtils.getTreeNodeValue4Display(selectedNode));

    // update id for the selected node and all the sub-nodes recursively.
    Deque<FilterableTreeItem<String>> queue = new ArrayDeque<>();
    queue.offer(treeNode);
    while (!queue.isEmpty()) {
      FilterableTreeItem<String> node = queue.poll();
      node.updateId(UIUtils.getId4FilterableTreeItem(treeViewMap.get(node)));
      for (TreeItem<String> n : node.getInternalChildren()) {
        if (n instanceof FilterableTreeItem && treeViewMap.containsKey(n)) {
          queue.offer((FilterableTreeItem<String>) n);
        }
      }
    }

    return true;
  }

  /** create and add tree view path at the beginning of the container */
  private void addTreeViewPathToContainer() {
    // add tree path
    if (vbTree.getChildren().contains(treeViewPath)) {
      ((HBox) treeViewPath.getContent()).getChildren().clear();
      ((HBox) treeViewPath.getContent()).getChildren().add(new Label("Selection Path..."));
    } else { // init the tree view path
      treeViewPath = new JFXTreeViewPath(tvTopology);
      treeViewPath.setFocusTraversable(false);

      // change color + size for the path
      tvTopology
          .getSelectionModel()
          .selectedItemProperty()
          .addListener(
              observable -> {
                for (Node node : ((HBox) treeViewPath.getContent()).getChildren()) {
                  if (node instanceof StackPane) {
                    for (int i = 0; i < ((StackPane) node).getChildren().size(); i++) {
                      Node path = ((StackPane) node).getChildren().get(i);
                      if (path instanceof Button) {
                        // modify css all buttons, can't use .button directly.
                        // Because set traversable for .button affects all buttons
                        path.getStyleClass().add("tv-path-button");
                      }
                    }
                  }
                }
              });

      vbTree.getChildren().add(0, treeViewPath);
    }

    // fix the padding issue
    Label tvPathLabel = (Label) ((HBox) treeViewPath.getContent()).getChildren().get(0);
    tvPathLabel.getStyleClass().add("tv-path-init-label");
  }

  /**
   * Check the condition for displaying the node edit dialog.
   *
   * @return checking result
   */
  private boolean checkForDisplayingEditDialog() {
    // This is for fixing the bug: shortcut can keep opening all dialogs.
    // return false if some dialog was already opened or no node is selected.
    return !DialogController.isDialogOpened().get() && selectedNode != null;
  }

  /**
   * Check the condition for enabling the drag and drop
   *
   * @return checking result
   */
  private boolean checkForDragDropCondition() {
    return !DialogController.isDialogOpened().get() && fileChooser == null;
  }

  /** Hide the message bar */
  private void hideMessageBar() {
    showMsg("", Status.NORMAL);
  }

  /**
   * Display the message on message bar
   *
   * @param msg message to be displayed
   * @param status message status
   */
  private void showMsg(String msg, Status status) {
    UIUtils.setStatusText(snackbar, msg, status);
  }

  /**
   * Get topology tree controller
   *
   * @return topology tree controller
   * @throws TTException exception during the initialization of the controller
   */
  private TTController getController() throws TTException {
    return TTControllerImpl.getInstance();
  }
}
