package com.vityazev_egor.Scenes;

import java.awt.Dimension;
import java.util.Optional;

import javafx.scene.Scene;

public abstract class ICustomScene {
    public abstract Scene getScene();

    public Optional<Dimension> getMinSize(){
        return Optional.empty();
    }

    // this is going to be executed in another thread!
    public void beforeShow(){
        return;
    }

    public Boolean hideWindowBeforeSwitch(){
        return false;
    }
}
