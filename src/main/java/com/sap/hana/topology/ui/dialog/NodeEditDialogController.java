package com.sap.hana.topology.ui.dialog;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public class NodeEditDialogController extends DialogController<Map<String, String>> {
    @FXML
    private JFXTextField txtName;
    @FXML
    private JFXTextField txtValue;
    @FXML
    private JFXButton btnOK;
    @FXML
    private JFXButton btnCancel;
    @FXML
    private JFXToggleButton tbLeaf;
    @FXML
    private Label lbParentPath;

    @FXML
    private void onCancel(ActionEvent event) {
        accept(null);
        if (event != null) event.consume();
    }

    @FXML
    private void onOK(ActionEvent event) {
        Map<String, String> map = new HashMap<>();
        map.put(TXT_NAME, txtName.getText());
        map.put(TXT_VALUE, txtValue.getText());
        map.put(TB_LEAF, String.valueOf(tbLeaf.isSelected()));
        map.put(INSERT_FLAG, String.valueOf(!editFlag.getValue()));
        accept(map);
        if (event != null) event.consume();
    }

    @FXML
    private void onKeyPressed(KeyEvent event) {
        if (KeyCode.ESCAPE == event.getCode()) {
            onCancel(null);
        } else if (KeyCode.ENTER == event.getCode()) {
            if (btnCancel.isFocused()) {
                onCancel(null);
            } else if (!btnOK.isDisable()) {
                onOK(null);
            }
        }
    }

    @FXML
    private void initialize() {
        // disable if name is empty or name and value are not changed
        btnOK.disableProperty().bind(Bindings.or(txtName.textProperty().isEmpty(), Bindings.and(nameEqualFlag, valueEqualFlag)));
        txtValue.disableProperty().bind(Bindings.createBooleanBinding(() -> !tbLeaf.isSelected(), tbLeaf.selectedProperty()));
        tbLeaf.disableProperty().bind(editFlag);
    }

    /**
     * Set parent path to path label
     * @param path path of parent node
     */
    public void setParentPath(String path) {
        lbParentPath.setText(path);
    }

    /**
     * Set the leaf flag, indicating whether current node is leaf node or not.
     * @param isLeaf leaf flag
     */
    public void setLeaf(boolean isLeaf) {
        tbLeaf.setSelected(isLeaf);
    }

    /**
     * Set node name to the name text field
     * @param name name of the node
     */
    public void setName(String name) {
        txtName.setText(name == null ? "" : name);
        nameEqualFlag.bind(txtName.textProperty().isEqualTo(txtName.getText()));
    }

    /**
     * Set node value to the value text field
     * @param value value of the node
     */
    public void setValue(String value) {
        txtValue.setText(value == null ? "" : value);
        valueEqualFlag.bind(txtValue.textProperty().isEqualTo(txtValue.getText()));
    }

    /**
     * Set the edit flag, indicating whether this dialog is for editing or inserting
     * @param editFlag edit flag
     */
    public void setEditFlag(boolean editFlag) {
        this.editFlag.setValue(!editFlag);
    }

    /**
     * Set default focus to name text field after dialog is loaded
     */
    @Override
    public void setDefaultFocus() {
        txtName.requestFocus();
    }
    /**
     * Key - NAME for delivering information via map
     */
    public final static String TXT_NAME = "name";
    /**
     * Key - VALUE for delivering information via map
     */
    public final static String TXT_VALUE = "value";
    /**
     * Key - LEAF for delivering information via map
     */
    public final static String TB_LEAF = "leaf";
    /**
     * Key - INSERT for delivering information via map
     */
    public final static String INSERT_FLAG = "insert";
    private BooleanProperty editFlag = new SimpleBooleanProperty(true);
    private BooleanProperty nameEqualFlag = new SimpleBooleanProperty(true);
    private BooleanProperty valueEqualFlag = new SimpleBooleanProperty(true);
}
