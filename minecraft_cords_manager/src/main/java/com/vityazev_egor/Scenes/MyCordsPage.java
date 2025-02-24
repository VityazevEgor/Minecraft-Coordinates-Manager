package com.vityazev_egor.Scenes;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import com.vityazev_egor.App;
import com.vityazev_egor.Models.TableEntity;
import com.vityazev_egor.Modules.Emulator;
import com.vityazev_egor.Modules.NativeWindowsManager;
import com.vityazev_egor.Modules.ServerApi;
import com.vityazev_egor.Modules.Shared;

import atlantafx.base.theme.Styles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class MyCordsPage implements ICustomScene{

    @Getter
    private final Scene scene;
    private final TableView<TableEntity> table = new TableView<>();
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final Emulator emulator = new Emulator();
    private final ServerApi api;

    @SuppressWarnings("unchecked")
    public MyCordsPage(App app) {
        this.api = app.getServerApi();

        final var root = new VBox();

        final var deleteButton = new Button("Delete", new FontIcon(Feather.DELETE));
        deleteButton.getStyleClass().add(Styles.DANGER);
        deleteButton.setOnAction(event -> deleteCordinates());

        final var enterCordsButton = new Button("Enter cords", new FontIcon(Feather.EXTERNAL_LINK));
        enterCordsButton.setOnAction(event -> enterCoordinates());

        final var copyButton = new Button("Copy to clipboard", new FontIcon(Feather.CLIPBOARD));
        copyButton.setOnAction(event -> copyCoordinates());

        final var addCordsButton = new Button("Add new coordinates", new FontIcon(Feather.PLUS));
        addCordsButton.setOnAction(event -> app.openPage(AddCordsPage.class.getName()));

        final var searchField = new TextField();
        searchField.setPromptText("Enter text to search");

        final var toolbar = new ToolBar(
            addCordsButton,
            new Separator(Orientation.VERTICAL),
            searchField,
            enterCordsButton,
            copyButton,
            deleteButton
        );

        // set up table data
        ObservableList<TableEntity> data = FXCollections.observableArrayList();

        TableColumn<TableEntity, ImageView> imageColumn = new TableColumn<>("Image");
        imageColumn.setCellValueFactory(cellData -> cellData.getValue().getImage());
        imageColumn.setMinWidth(210);

        TableColumn<TableEntity, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
        titleColumn.setMinWidth(100);

        TableColumn<TableEntity, String> cordsColumn = new TableColumn<>("Cords");
        cordsColumn.setCellValueFactory(cellData -> cellData.getValue().getCords());
        cordsColumn.setMinWidth(150);

        table.getColumns().addAll(imageColumn, titleColumn, cordsColumn);
        table.setItems(data);

        // сделать так чтобы таблица растягивалось на всю ширину родительского блока
        table.prefWidthProperty().bind(root.widthProperty());
        // сделать так чтобы таблица растягивалось на всю высоту родительского блока
        table.prefHeightProperty().bind(root.heightProperty());
        table.getStyleClass().add(Styles.BORDERED);
        // end of table setup

        root.getChildren().addAll(toolbar, table);
        service.scheduleWithFixedDelay(
            () -> updateData(), 
            0, 
            5,
            TimeUnit.SECONDS
        );
        this.scene = new Scene(root);
    }

    private void deleteCordinates(){
        TableEntity selectedRecord = table.getSelectionModel().getSelectedItem();
        if (api.deleteCord(selectedRecord.getId())){
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success!");
            alert.setHeaderText("Coordinates were deleted succesfully");
            alert.showAndWait();
        }
        else{
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Coordinates were not deleted");
            alert.showAndWait();
        }
    }

    private void copyCoordinates(){
        String tpCommand = String.format("/tp %s", table.getSelectionModel().getSelectedItem().getCords().get());
        emulator.setClipBoard(tpCommand);
    }

    private void enterCoordinates(){
        String tpCommand = String.format("/tp %s", table.getSelectionModel().getSelectedItem().getCords().get());
        if (NativeWindowsManager.activateWindow("Minecraft ") || NativeWindowsManager.activateWindow("Fear Nightfall")){
            Shared.sleep(2000);
            emulator.press(KeyEvent.VK_T);
            emulator.writeText(tpCommand, 500);
            emulator.press(KeyEvent.VK_ENTER);
        }
        else{
            Alert al = new Alert(Alert.AlertType.ERROR);
            al.setTitle("Error");
            al.setContentText("Can't activate Minecraft window");
            al.setHeaderText(null);
            al.show();
        }
    }

    private void updateData(){
        var data = api.getAllCords();
        if (data.size() == 0) return;

        data.forEach(model ->{
            if (table.getItems().stream().noneMatch(item->item.getTitle().get().equals(model.getTitle()))){
                api.getPreview(model.getImageName()).ifPresentOrElse(
                    image ->{
                        // создаём превью
                        ImageView view = new ImageView();
                        view.setFitWidth(200);
                        view.setFitHeight(150);
                        view.setImage(Shared.convertBufferedImage(image));
                        table.getItems().add(new TableEntity(
                                model.getId(),
                                view, 
                                model.getTitle(), 
                                model.getCords()
                            )
                        );
                    }, 
                    () -> Shared.printEr(null, "Can't get preview")
                );
            }            
        });
        
        // удаляем те записи, которых нету в ответе от сервера
        table.getItems().removeIf(item -> data.stream().noneMatch(model->model.getTitle().equals(item.getTitle().get())));
    }

    @Override
    public void beforeShow() {
        return;
    }

    @Override
    public Optional<Dimension> getMinSize() {
        return Optional.empty();
    }
    
}
