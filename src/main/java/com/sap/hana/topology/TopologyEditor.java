package com.sap.hana.topology;

import com.sap.hana.topology.util.UIUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class TopologyEditor extends Application {

  @Override
  public void start(Stage stage) throws Exception {
    Parent root = FXMLLoader.load(getClass().getResource("/fxml/TopologyEditor.fxml"));
    Scene scene = new Scene(root);
    scene.getStylesheets().add(getClass().getResource("/css/TopologyEditor.css").toExternalForm());
    stage.setTitle("HANA Topology Editor");
    stage.setScene(scene);
    stage.getIcons().add(new Image(getClass().getResourceAsStream("/img/icon.png")));
    stage.getProperties().put(UIUtils.HOST_SERVICES, this.getHostServices());
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
