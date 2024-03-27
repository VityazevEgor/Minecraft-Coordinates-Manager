package com.vityazev_egor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PrimaryController implements Initializable{

    @FXML
    private Button createNewButton;

    @FXML
    private VBox tableBox;

    private class cordsData{
        private final ObjectProperty<ImageView> image;
        private final StringProperty title;
        private final StringProperty cords;
        private final ObjectProperty<Button> buttons;

        public cordsData(ImageView image, String title, String cords, Button buttons){
            this.image = new SimpleObjectProperty<ImageView>(image);
            this.title = new SimpleStringProperty(title);
            this.cords = new SimpleStringProperty(cords);
            this.buttons = new SimpleObjectProperty<Button>(buttons);
        }

        public ObjectProperty<ImageView> imageProperty() {
            return image;
        }
    
        public StringProperty titleProperty() {
            return title;
        }
    
        public StringProperty cordsProperty() {
            return cords;
        }
    
        public ObjectProperty<Button> buttonsProperty() {
            return buttons;
        }
    }

    @FXML
    void openScForm(ActionEvent event) {
        try {
            App.setRoot("secondary");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        TableView<cordsData> table = new TableView<>();
        ObservableList<cordsData> data = FXCollections.observableArrayList();

        TableColumn<cordsData, ImageView> imageColumn = new TableColumn<>("Image");
        imageColumn.setCellValueFactory(cellData -> cellData.getValue().imageProperty());

        TableColumn<cordsData, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());

        TableColumn<cordsData, String> cordsColumn = new TableColumn<>("Cords");
        cordsColumn.setCellValueFactory(cellData -> cellData.getValue().cordsProperty());

        TableColumn<cordsData, Button> buttonsColumns = new TableColumn<>("Actions");
        buttonsColumns.setCellValueFactory(cellData -> cellData.getValue().buttonsProperty());

        var b = new Button();
        b.setText("Test");
        b.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				b.setText("You've clicked!");
            }  
        });

        var image = new ImageView();
        image.setImage(new Image(new File("screenshot.png").toURI().toString()));
        image.setFitWidth(200);
        image.setFitHeight(150);
        data.add(new cordsData(image, "TetsTetsTetsTetsTetsTetsTetsTetsTetsTetsTetsTetsTetsTetsTetsTetsTets", "123 123 123", b));
        table.getColumns().addAll(imageColumn, titleColumn, cordsColumn, buttonsColumns);
        table.setItems(data);

        tableBox.getChildren().add(table);
    }
}
