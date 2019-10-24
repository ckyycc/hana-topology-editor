package com.sap.hana.topology.ui.dialog;

import com.jfoenix.controls.JFXDialog;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Abstract controller for dialog
 *
 * @param <T> object using for delivery message
 */
public abstract class DialogController<T> {
  private JFXDialog dialog;
  private Consumer<T> consumer;
  private static BooleanProperty dialogOpenedFlag;

  /** Set default focus after dialog is loaded */
  public abstract void setDefaultFocus();

  /**
   * Set consumer
   *
   * @param consumer the consumer
   */
  public void setConsumer(Consumer<T> consumer) {
    this.consumer = consumer;
  }

  /**
   * set dialog object
   *
   * @param dialog dialog object
   */
  public void setDialog(JFXDialog dialog) {
    this.dialog = dialog;
  }

  /** Close the dialog and update the dialog-open flag */
  public void close() {
    if (dialog != null) {
      dialog.close();
      if (dialogOpenedFlag != null) {
        dialogOpenedFlag.setValue(false);
      }
    }
  }

  /** Display the dialog and update the dialog-open flag */
  public void show() {
    if (dialog != null) {
      dialog.show();
      if (dialogOpenedFlag != null) {
        dialogOpenedFlag.setValue(true);
      }
    }
  }

  /**
   * Accept the consumer
   *
   * @param obj parameter for the consumer
   */
  void accept(T obj) {
    consumer.accept(obj);
  }

  /** Get the status that shows whether dialog is opened. */
  public static BooleanProperty isDialogOpened() {
    if (dialogOpenedFlag == null) {
      dialogOpenedFlag = new SimpleBooleanProperty(false);
    }
    return dialogOpenedFlag;
  }
}
