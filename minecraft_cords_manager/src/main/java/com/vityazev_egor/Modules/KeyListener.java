package com.vityazev_egor.Modules;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.vityazev_egor.App;
import com.vityazev_egor.Scenes.AddCordsPage;

import javafx.application.Platform;

public class KeyListener implements NativeKeyListener {
    
    private final App app;
    public KeyListener(App app){
        this.app = app;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event){
        switch (event.getKeyCode()) {
            case NativeKeyEvent.VC_HOME:
                System.out.println("Detected HOME press");
                Platform.runLater(() -> app.setVisible(!app.getVisible()));
                break;
            case NativeKeyEvent.VC_INSERT:
                print("Detected insert key");
                Platform.runLater(()->app.openPage(AddCordsPage.class.getName()));
                break;
            case NativeKeyEvent.VC_END:
                print("Called exit");
                System.exit(1);
                break;
            default:
                break;
        }
    }

    private void print(String text){
        System.out.println("[KeyListener] " + text);
    }
}
