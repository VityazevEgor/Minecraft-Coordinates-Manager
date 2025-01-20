package com.vityazev_egor.Scenes;

import java.awt.Dimension;
import java.util.Optional;

import javafx.scene.Scene;

public interface ICustomScene {
    public Scene getScene();
    public Optional<Dimension> getMinSize();

    // this is going to be executed in another thread!
    public void beforeShow();
}
