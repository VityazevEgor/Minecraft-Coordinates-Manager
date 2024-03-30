package com.vityazev_egor;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.vityazev_egor.ServerApi.CordsModel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class PrimaryController implements Initializable{

    @FXML
    private Button createNewButton;

    @FXML
    private VBox tableBox;

    private TableView<cordsData> table = new TableView<>();

    private final Emulator emu = new Emulator();
    private final ScheduledExecutorService shPool = Executors.newScheduledThreadPool(1);

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
            Shared.printEr(e, "Can't open secondary form");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {

        // Создаю таблицу и столбцы для таблицы
        ObservableList<cordsData> data = FXCollections.observableArrayList();

        TableColumn<cordsData, ImageView> imageColumn = new TableColumn<>("Image");
        imageColumn.setCellValueFactory(cellData -> cellData.getValue().imageProperty());
        imageColumn.setMinWidth(210);

        TableColumn<cordsData, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        titleColumn.setMinWidth(100);

        TableColumn<cordsData, String> cordsColumn = new TableColumn<>("Cords");
        cordsColumn.setCellValueFactory(cellData -> cellData.getValue().cordsProperty());
        cordsColumn.setMinWidth(150);

        TableColumn<cordsData, Button> buttonsColumns = new TableColumn<>("Actions");
        buttonsColumns.setCellValueFactory(cellData -> cellData.getValue().buttonsProperty());
        buttonsColumns.setMinWidth(150);

        table.getColumns().addAll(imageColumn, titleColumn, cordsColumn, buttonsColumns);
        table.setItems(data);

        tableBox.getChildren().add(table);

        // запускаю поток который обновляет таблицу с коордианатами каждые 2 секунды
        shPool.scheduleAtFixedRate(new Updater(), 0, 2, TimeUnit.SECONDS);
    }

    private void EnterCords(String cords){
        if (FakeMain.isWindows){
            var processList = NativeWindowsManager.getAllProcess();
            var minecraftWindow = processList.stream().filter(p-> p.title.toLowerCase().contains("minecraft") && !p.title.toLowerCase().contains("manager")).findFirst().orElse(null);
            if (minecraftWindow != null){
                if (NativeWindowsManager.ActivateWindow(minecraftWindow)){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Shared.printEr(e, "Can't sleep for some reason in {EnterCords}");
                    }

                    emu.press(KeyEvent.VK_T);
                    emu.writeText("/tp " + cords, 100);
                }
                emu.press(KeyEvent.VK_ENTER);
            }                    
        }
        else{
            emu.setClipBoard("/tp " + cords);
            Alert al = new Alert(AlertType.INFORMATION);
            al.setTitle("Information");
            al.setContentText("Command for teleport was copied to your clipboard");
            al.setHeaderText(null);
            al.show();
        }
    }

    private class Updater implements Runnable{

        @Override
        public void run() {
            var data = ServerApi.getCords();
            if (data!=null){
                
                for (CordsModel model : data) {
                    if (table.getItems().filtered(item->item.titleProperty().get().equals(model.title)).isEmpty()){

                        BufferedImage image = ServerApi.getImage(model.imageName);
                        if (image != null){

                            ImageView view = new ImageView();
                            view.setFitWidth(200);
                            view.setFitHeight(150);
                            Button button = new Button();
                            button.setText("Teleport");
                            button.setOnAction(new EventHandler<ActionEvent>() {

                                @Override
                                public void handle(ActionEvent arg0) {
                                    EnterCords(model.cords);
                                }
                                
                            });
                            view.setImage(Shared.convertBufferedImage(image));
                            table.getItems().add(new cordsData(view, model.title, model.cords, button));
                        }
                    }
                }
            }
        }


        
    }
}
