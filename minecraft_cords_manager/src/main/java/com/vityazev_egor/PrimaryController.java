package com.vityazev_egor;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PrimaryController extends CustomInit implements Initializable{

    @FXML
    private Button createNewButton;

    @FXML
    private VBox tableBox;

    private TableView<cordsData> table = new TableView<>();

    private final Emulator emu = new Emulator();
    private final ScheduledExecutorService shPool = Executors.newScheduledThreadPool(1);
    private static Boolean isThreadRunning = false;

    // класс который представляет элемент в таблице
    private class cordsData{
        private final ObjectProperty<ImageView> image;
        private final StringProperty title;
        private final StringProperty cords;
        private final ObjectProperty<HBox> buttons;

        public cordsData(ImageView image, String title, String cords, HBox buttons){
            this.image = new SimpleObjectProperty<ImageView>(image);
            this.title = new SimpleStringProperty(title);
            this.cords = new SimpleStringProperty(cords);
            this.buttons = new SimpleObjectProperty<HBox>(buttons);
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
    
        public ObjectProperty<HBox> buttonsProperty() {
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

        TableColumn<cordsData, HBox> buttonsColumns = new TableColumn<>("Actions");
        buttonsColumns.setCellValueFactory(cellData -> cellData.getValue().buttonsProperty());
        buttonsColumns.setMinWidth(250);

        table.getColumns().addAll(imageColumn, titleColumn, cordsColumn, buttonsColumns);
        table.setItems(data);

        // сделать так чтобы таблица растягивалось на всю ширину родительского блока
        table.prefWidthProperty().bind(tableBox.widthProperty());
        
        // сделать так чтобы таблица растягивалось на всю высоту родительского блока
        table.prefHeightProperty().bind(tableBox.heightProperty());


        tableBox.getChildren().add(table);

        setUpInitTask("primary", 50);
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

    // класс который отвечает за обновление таблицы с координатами
    private class Updater implements Runnable{

        @Override
        public void run() {
            var data = ServerApi.getCords();
            if (data!=null){
                
                for (CordsModel model : data) {
                    // Добовляю те координаты которые отсуствуют в таблице
                    if (table.getItems().filtered(item->item.titleProperty().get().equals(model.title)).isEmpty()){

                        BufferedImage image = ServerApi.getImage(model.imageName);

                        if (image != null){

                            // создаём превью
                            ImageView view = new ImageView();
                            view.setFitWidth(200);
                            view.setFitHeight(150);
                            view.setImage(Shared.convertBufferedImage(image));

                            Button button = new Button();
                            button.setText("Teleport");
                            // устанавливаю цвет фона кнопки на зёлёный
                            button.setStyle("-fx-background-color: green;");

                            button.setOnAction(new EventHandler<ActionEvent>() {

                                @Override
                                public void handle(ActionEvent arg0) {
                                    EnterCords(model.cords);
                                }
                                
                            });
                            var deleteButton = new Button();
                            deleteButton.setText("Delete");
                            // установить цвет фона кнопки красным
                            deleteButton.setStyle("-fx-background-color: red;");
                            deleteButton.setOnAction(new EventHandler<ActionEvent>() {

                                @Override
                                public void handle(ActionEvent arg0) {
                                    // вывести сообщение с вопросом пользователю уверен ли он что он хочет удалить координаты
                                    Alert al = new Alert(AlertType.CONFIRMATION);
                                    al.setTitle("Confirmation");
                                    al.setContentText("Are you sure you want to delete this coordinates?");
                                    al.setHeaderText(null);
                                    Optional<ButtonType> result = al.showAndWait();
                                    if (result.get() == ButtonType.OK){
                                        Boolean rsResult = ServerApi.deleteCords(model.id);
                                        // если запрос был выполнен успешно то вывести сообщение об успехе
                                        if (rsResult){
                                            Alert al2 = new Alert(AlertType.INFORMATION);
                                            al2.setTitle("Information");
                                            al2.setContentText("Coordinates was deleted");
                                            al2.setHeaderText(null);
                                            al2.show();
                                        } else {
                                            Alert al2 = new Alert(AlertType.ERROR);
                                            al2.setTitle("Error");
                                            al2.setContentText("Can't delete coordinates");
                                            al2.setHeaderText(null);
                                            al2.show();
                                        }

                                    }
                                }
                                
                            });

                            var clipboardButton = new Button();
                            clipboardButton.setText("Copy to clipboard");
                            // установить цвет кнопки на оранжевы
                            clipboardButton.setStyle("-fx-background-color: orange;");
                            clipboardButton.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent arg0) {
                                    emu.setClipBoard("/tp " + model.cords);
                                    Alert al = new Alert(AlertType.INFORMATION);
                                    al.setTitle("Information");
                                    al.setContentText("Coordinates was copied to your clipboard");
                                    al.setHeaderText(null);
                                    al.show();
                                }
                                
                            });

                            HBox buttons = new HBox();
                            buttons.getChildren().addAll(button, deleteButton, clipboardButton);
                            buttons.setSpacing(5);

                            table.getItems().add(new cordsData(view, model.title, model.cords, buttons));
                        }
                    }
                }

                // удалаем те элементы который есть в таблице, но которое отсуствуют на сервере
                List<CordsModel> listData = Arrays.asList(data);
                for (cordsData item : table.getItems()) {
                    if (listData.stream().filter(model->model.title.equals(item.titleProperty().get())).count() == 0){
                        table.getItems().remove(item);
                    }
                }
            }
        }


        
    }

    @Override
    public void init() {
        if (isThreadRunning == false){
            // запускаю поток который обновляет таблицу с коордианатами каждые 2 секунды
            shPool.scheduleAtFixedRate(new Updater(), 0, 10, TimeUnit.SECONDS);
            isThreadRunning = true;
        }
    }
}
