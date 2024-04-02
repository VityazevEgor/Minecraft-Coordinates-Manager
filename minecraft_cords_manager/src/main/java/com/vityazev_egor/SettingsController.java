package com.vityazev_egor;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class SettingsController implements Initializable {
    @FXML
    private Button testSaveButton;

    @FXML
    private TextField urlBox;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        testSaveButton.setOnAction(e -> {
            if(ServerApi.checkIfServerAvaible(urlBox.getText())){
                // если url не оканчивается на символ '/' то сообщить пользователю о том что формат ссылке не правильны и показать правильный пример ссылке
                if(!urlBox.getText().endsWith("/")){
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Incorrect URL");
                    alert.setHeaderText("Incorrect URL");
                    alert.setContentText("URL must end with '/'");
                    alert.showAndWait();
                    return;
                }
                try {
                    App.setRoot("primary");
                } catch (IOException e1) {
                    Shared.printEr(e1, "Can't open primary form");
                }
            }
            else{
                var alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Server is not available");
                alert.setContentText("Please try another url");
                alert.showAndWait();
            }
        });
    }
    
}
