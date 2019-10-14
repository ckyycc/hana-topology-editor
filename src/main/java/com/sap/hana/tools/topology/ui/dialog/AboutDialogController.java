package com.sap.hana.tools.topology.ui.dialog;

import com.jfoenix.controls.JFXButton;
import com.sap.hana.tools.topology.util.UIUtils;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AboutDialogController extends DialogController<ButtonType> {

    @FXML
    private JFXButton btnOK;

    @FXML
    private void onKeyPressed(KeyEvent event) {
        if (KeyCode.ESCAPE == event.getCode()) {
            onOK(new ActionEvent(event.getSource(), event.getTarget()));
        }
    }

    @FXML
    private void onOK(ActionEvent event) {
        accept(ButtonType.OK);
        event.consume();
    }

    @FXML
    private void onClick(MouseEvent event) {
        HostServices hostServices = (HostServices)this.getStage().getProperties().get(UIUtils.HOST_SERVICES);
        hostServices.showDocument("https://github.com/ckyycc/hana-topology-editor");
        event.consume();
    }

    /**
     * Set focus to OK Button by default
     */
    @Override
    public void setDefaultFocus() {
        btnOK.requestFocus();
    }

    private Stage getStage() {
        return (Stage) btnOK.getScene().getWindow();
    }
}
