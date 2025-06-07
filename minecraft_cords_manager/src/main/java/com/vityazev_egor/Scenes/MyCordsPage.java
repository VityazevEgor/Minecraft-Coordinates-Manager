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
import com.vityazev_egor.Modules.DataManager;
import com.vityazev_egor.Modules.ServerApi;
import com.vityazev_egor.Modules.Shared;

import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Label;
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;

public class MyCordsPage extends ICustomScene{

    @Getter
    private final Scene scene;
    private final TableView<TableEntity> table = new TableView<>();
    private final ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private final Emulator emulator = new Emulator();
    private final DataManager dataManager;
    private final VBox root;
    private final ObservableList<TableEntity> tableEntities = FXCollections.observableArrayList();

    @SuppressWarnings("unchecked")
    public MyCordsPage(App app) {
        this.dataManager = app.getDataManager();

        root = new VBox();
        root.setMinHeight(524);
        root.setMinWidth(820);

        final var deleteButton = new Button("Delete", new FontIcon(Feather.DELETE));
        deleteButton.getStyleClass().add(Styles.DANGER);
        deleteButton.setOnAction(event -> deleteCoordinates());

        final var enterCordsButton = new Button("Enter cords", new FontIcon(Feather.EXTERNAL_LINK));
        enterCordsButton.setOnAction(event -> enterCoordinates());

        final var copyButton = new Button("Copy to clipboard", new FontIcon(Feather.CLIPBOARD));
        copyButton.setOnAction(event -> copyCoordinates());

        final var addCordsButton = new Button("Add new coordinates", new FontIcon(Feather.PLUS));
        addCordsButton.setOnAction(event -> app.openPage(AddCordsPage.class.getName()));
        
        final var syncButton = new Button("Sync", new FontIcon(Feather.REFRESH_CW));
        syncButton.setOnAction(event -> dataManager.forceSync());
        
        final var statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #ffffff;");
        updateStatusLabel(statusLabel);

        final var searchField = new TextField();
        searchField.setPromptText("Enter text to search");
        searchField.textProperty().addListener((observable, oldValue, newValue) ->{
            if (newValue.isBlank()){
                table.setItems(tableEntities);
                return;
            }
            var filteredResults =  FXCollections.observableArrayList(
                    tableEntities.stream()
                            .filter(tableEntity -> tableEntity.getTitle().getValue().contains(newValue)).toList()
            );
            table.setItems(filteredResults);
        });

        final var toolbar = new ToolBar(
            addCordsButton,
            new Separator(Orientation.VERTICAL),
            searchField,
            enterCordsButton,
            copyButton,
            deleteButton,
            new Separator(Orientation.VERTICAL),
            syncButton,
            statusLabel
        );
        Node[] buttonsForCustomDesign = new Node[]{addCordsButton, enterCordsButton, copyButton, deleteButton, syncButton};
        for (Node buttonNode : buttonsForCustomDesign){
            buttonNode.setStyle("-fx-background-color: #010409;");
            buttonNode.setOnMouseEntered(event -> buttonNode.setStyle("-fx-background-color: #A371F726; -fx-text-fill: #8957E5FF;"));
            buttonNode.setOnMouseExited(event -> buttonNode.setStyle("-fx-background-color: #010409;"));
        }
        toolbar.setStyle("-fx-background-color: #010409;");

        // set up table cells
        TableColumn<TableEntity, ImageView> imageColumn = new TableColumn<>("Image");
        imageColumn.setCellValueFactory(cellData -> cellData.getValue().getImage());
        imageColumn.setMinWidth(210);

        TableColumn<TableEntity, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().getTitle());
        titleColumn.setMinWidth(200);

        TableColumn<TableEntity, String> cordsColumn = new TableColumn<>("Cords");
        cordsColumn.setCellValueFactory(cellData -> cellData.getValue().getCords());
        cordsColumn.setMinWidth(150);

        table.getColumns().addAll(imageColumn, titleColumn, cordsColumn);
        table.setItems(tableEntities);

        // сделать так, чтобы таблица растягивалось на всю ширину родительского блока
        table.prefWidthProperty().bind(root.widthProperty());
        table.prefHeightProperty().bind(root.heightProperty());
        table.getStyleClass().add(Styles.BORDERED);
        // end of table setup

        root.getChildren().addAll(toolbar, table);
        service.scheduleWithFixedDelay(
            () -> {
                updateData();
                Platform.runLater(() -> updateStatusLabel(statusLabel));
            }, 
            0, 
            5,
            TimeUnit.SECONDS
        );
        service.scheduleWithFixedDelay(
            () -> pulseSelectedEntity(), 
            0, 
            1, 
            TimeUnit.SECONDS
        );

        this.scene = new Scene(root);
    }

    private void pulseSelectedEntity(){
        var selectedItem = table.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;
        try{
            Animations.pulse(selectedItem.getImage().get(), 1.02).playFromStart();
        } catch (Exception ex){
            Shared.printEr(ex, "Can't play animation");
        }
    }

    private void deleteCoordinates(){
        TableEntity selectedRecord = table.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) return;
        
        dataManager.deleteCord(selectedRecord.getId()).thenAccept(success -> {
            Platform.runLater(() -> {
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success!");
                    alert.setHeaderText("Coordinates were deleted successfully");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error!");
                    alert.setHeaderText("Coordinates were not deleted");
                    alert.showAndWait();
                }
            });
        });
    }

    private void copyCoordinates(){
        String tpCommand = String.format("/tp %s", table.getSelectionModel().getSelectedItem().getCords().get());
        emulator.setClipBoard(tpCommand);
    }

    private void enterCoordinates(){
        if (table.getSelectionModel().getSelectedItem() == null)
            return;
        String tpCommand = String.format("/tp %s", table.getSelectionModel().getSelectedItem().getCords().get());
        if (NativeWindowsManager.activateWindow("Minecraft ") || NativeWindowsManager.activateWindow("Fear Nightfall")){
            Shared.sleep(2000);
//            emulator.press(KeyEvent.VK_T);
//            emulator.press(KeyEvent.VK_BACK_SPACE);
//            emulator.press(KeyEvent.VK_BACK_SPACE);
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
        var data = dataManager.getAllCords();
        if (data.size() == 0) return;

        data.forEach(model ->{
            if (tableEntities.stream().noneMatch(item->item.getTitle().get().equals(model.getTitle()))){
                dataManager.getPreview(model.getImageName()).ifPresentOrElse(
                    image ->{
                        // создаём превью
                        ImageView view = new ImageView();
                        view.setFitWidth(200);
                        view.setFitHeight(150);
                        view.setImage(Shared.convertBufferedImage(image));
                        tableEntities.add(new TableEntity(
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
        
        // удаляем те записи, которых нет в ответе от сервера
        tableEntities.removeIf(item -> data.stream().noneMatch(model->model.getTitle().equals(item.getTitle().get())));
    }
    
    private void updateStatusLabel(Label statusLabel) {
        if (dataManager.isOnline()) {
            statusLabel.setText("Online " + dataManager.getLocalCoordinatesCount() + " coords");
            statusLabel.setStyle("-fx-text-fill: #4CAF50;");
        } else {
            statusLabel.setText("Offline " + dataManager.getLocalCoordinatesCount() + " coords");
            statusLabel.setStyle("-fx-text-fill: #FF9800;");
        }
    }

    @Override
    public void beforeShow() {
        for (Node node : root.getChildren()){
            Animations.fadeIn(node, Duration.seconds(2)).playFromStart();
        }
    }

    @Override
    public Optional<Dimension> getMinSize() {
        return Optional.of(new Dimension((int)root.getMinWidth(), (int) root.getMinHeight()));
    }
    
}
