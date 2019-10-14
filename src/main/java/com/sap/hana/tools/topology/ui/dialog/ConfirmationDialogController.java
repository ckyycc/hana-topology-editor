package com.sap.hana.tools.topology.ui.dialog;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ConfirmationDialogController extends DialogController<ButtonType> {
    @FXML
    private JFXButton btnCancel;

    @FXML
    private Label lbTitle;

    @FXML
    private Label lbContent1;

    @FXML
    private Label lbContent2;

    @FXML
    private void onCancel(ActionEvent event) {
        accept(ButtonType.CANCEL);
        if (event != null) event.consume();
    }

    @FXML
    private void onKeyPressed(KeyEvent event) {
        if (KeyCode.ESCAPE == event.getCode()) {
            onCancel(null);
        } else if (KeyCode.ENTER == event.getCode()) {
            if (btnCancel.isFocused()) {
                onCancel(null);
            } else {
                onOK(null);
            }
        }
    }

    @FXML
    private void onOK(ActionEvent event) {
        accept(ButtonType.OK);
        if (event != null) event.consume();
    }

    /**
     * Set default focus to cancel button after dialog is loaded
     */
    @Override
    public void setDefaultFocus() {
        btnCancel.requestFocus();
    }

    public void setTitle(String title) {
        lbTitle.setText(title);
    }
    public void setContent1(String content) {
        lbContent1.setText(content);
    }
    public void setContent2(String content) {
        lbContent2.setText(content);
    }
}
