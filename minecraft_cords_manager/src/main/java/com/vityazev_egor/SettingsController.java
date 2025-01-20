package com.vityazev_egor;

import java.net.URL;
import java.util.ResourceBundle;

import com.vityazev_egor.Modules.ServerApi;
import com.vityazev_egor.Modules.Shared;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class SettingsController extends CustomInit implements Initializable {
    @FXML
    private Button testSaveButton;

    @FXML
    private TextField urlBox;

    public SettingsController() {
        super("settings");
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        testSaveButton.setOnAction(e -> {
            try {
                var api = new ServerApi(urlBox.getText());
                if (!api.isServerAlive()) throw new Exception("Server is not alive");
                if (!urlBox.getText().endsWith("/")) throw new Exception("Incorrect URL");
                App.setRoot("primary");
            } catch (Exception ex) {
                Shared.printEr(ex, "null");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("ERROR!");
                alert.setHeaderText("Incorrect URL");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });
    }

    @Override
    public void init() {
        return;
    }
    
}
