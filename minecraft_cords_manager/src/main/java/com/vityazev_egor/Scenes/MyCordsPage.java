package com.vityazev_egor.Scenes;

import java.awt.Dimension;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import com.vityazev_egor.App;
import com.vityazev_egor.Models.TableEntity;
import com.vityazev_egor.Modules.ServerApi;
import com.vityazev_egor.Modules.Shared;

import atlantafx.base.theme.Styles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.Getter;

public class MyCordsPage implements ICustomScene{

    private final App app;
    @Getter
    private final Scene scene;
    private final TableView<TableEntity> table = new TableView<>();
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final ServerApi api = new ServerApi("http://127.0.0.1:8080/");

    public MyCordsPage(App app) {
        this.app = app;

        final var root = new VBox();

        final var deleteButton = new Button("Delete", new FontIcon(Feather.DELETE));
        deleteButton.getStyleClass().add(Styles.DANGER);

        final var toolbar = new ToolBar(
            new Button("Add new coordinates", new FontIcon(Feather.PLUS)),
            new Separator(Orientation.VERTICAL),
            new Button("Copy to clipboard", new FontIcon(Feather.CLIPBOARD)),
            deleteButton
        );

        Button button = new Button("Go back");
        button.setOnAction(event -> {
            app.openPage(SettingsPage.class.getName());
        });

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

        root.getChildren().addAll(toolbar, table, button);
        service.scheduleWithFixedDelay(
            () -> updateData(), 
            0, 
            5,
            TimeUnit.SECONDS
        );
        this.scene = new Scene(root);
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
