package com.vityazev_egor;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class PrimaryController implements Initializable{

    @FXML
    private void switchToSecondary() throws IOException {
        App.setVisible(false);
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        
    }
}
