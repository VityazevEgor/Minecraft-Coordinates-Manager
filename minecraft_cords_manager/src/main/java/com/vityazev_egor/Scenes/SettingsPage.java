package com.vityazev_egor.Scenes;

import java.awt.Dimension;
import java.util.Optional;

import com.vityazev_egor.App;

import atlantafx.base.util.Animations;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

public class SettingsPage extends ICustomScene{
    
    private final App app;
    private final Label label;
    private final TextField textField;
    @Getter
    private final Scene scene;

    public SettingsPage(App app) {
        this.app = app;
        
        AnchorPane root = new AnchorPane();
        root.setMinWidth(300.0);
        root.setMinHeight(200.0);

        var createNewButton = new Button("Connect to the server");
        createNewButton.setMnemonicParsing(false);
        createNewButton.setOnAction(event -> testServerButton()); // Метод нужно реализовать

        label = new Label("Enter server url");
        textField = new TextField();
        textField.setPromptText("Example: http://127.0.0.1:8080/");
        textField.setText(app.getServerApi().getServerUrl());

        var tableBox = new VBox();
        tableBox.setSpacing(10.0);
        AnchorPane.setRightAnchor(tableBox, 10.0);
        AnchorPane.setTopAnchor(tableBox, 10.0);
        AnchorPane.setLeftAnchor(tableBox, 10.0);
        tableBox.getChildren().addAll(label, textField, createNewButton);

        root.getChildren().add(tableBox);
        var animation = Animations.fadeIn(root, Duration.seconds(1));
        animation.playFromStart();
        this.scene = new Scene(root);
    }

    private void testServerButton(){
        app.getServerApi().setServerUrl(textField.getText());
        if (app.getServerApi().isServerAlive() && textField.getText().endsWith("/")){
            app.openPage(MyCordsPage.class.getName());
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR!");
            alert.setHeaderText("Server is not alive or URL is incorrect");
            //alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @Override
    public void beforeShow() {
        return;
    }

    @Override
    public Optional<Dimension> getMinSize() {
        return Optional.of(new Dimension(300, 200));
    }
}
