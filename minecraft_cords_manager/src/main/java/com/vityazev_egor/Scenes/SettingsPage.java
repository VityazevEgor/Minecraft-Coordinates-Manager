package com.vityazev_egor.Scenes;

import java.awt.Dimension;
import java.time.LocalDateTime;
import java.util.Optional;

import com.vityazev_egor.App;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class SettingsPage implements ICustomScene{
    
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

        var createNewButton = new Button("Test and save!");
        createNewButton.setMnemonicParsing(false);
        createNewButton.setOnAction(event -> app.openPage(MyCordsPage.class.getName())); // Метод нужно реализовать

        label = new Label("Enter server url");
        textField = new TextField();

        var tableBox = new VBox();
        tableBox.setSpacing(10.0);
        AnchorPane.setRightAnchor(tableBox, 10.0);
        AnchorPane.setTopAnchor(tableBox, 10.0);
        AnchorPane.setLeftAnchor(tableBox, 10.0);
        tableBox.getChildren().addAll(label, textField, createNewButton);

        root.getChildren().add(tableBox);
        this.scene = new Scene(root);

        new Thread(() -> {
            while (true) {
                Platform.runLater(()-> label.setText(LocalDateTime.now().toString()));
                try{
                    Thread.sleep(100);
                }catch (Exception ex){};   
            }
        }).start();
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
