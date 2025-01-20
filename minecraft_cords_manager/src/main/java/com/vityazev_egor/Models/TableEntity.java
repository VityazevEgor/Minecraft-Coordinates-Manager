package com.vityazev_egor.Models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;
import lombok.Getter;

@Getter
public class TableEntity {
    private final ObjectProperty<ImageView> image;
    private final StringProperty title;
    private final StringProperty cords;
    private final Integer id;

    public TableEntity(Integer id, ImageView image, String title, String cords){
        this.image = new SimpleObjectProperty<ImageView>(image);
        this.title = new SimpleStringProperty(title);
        this.cords = new SimpleStringProperty(cords);
        this.id = id;
    }
}
