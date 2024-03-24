package com.vityazev_egor;

import java.io.IOException;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javafx.application.Platform;

public class KeyListener implements NativeKeyListener {
    
    @SuppressWarnings("exports")
    @Override
    public void nativeKeyPressed(NativeKeyEvent e){
        print(("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode())));
        // Если нажата кнопка хоме то открыть главное меню
        if (e.getKeyCode() == NativeKeyEvent.VC_HOME){

            System.out.println("Dected HOME press");
            Platform.runLater(()->{
                try {
                    App.setRoot("primary");
                    print("Visible state = " + App.getVisible());
                    App.setVisible(!App.getVisible());
                } catch (IOException e1) {
                    print("Can't load form");
                    e1.printStackTrace();
                }
            });

        }

        if (e.getKeyCode() == NativeKeyEvent.VC_INSERT || e.getKeyCode() == NativeKeyEvent.VC_F13){

            print("Detcted insert key");
            Platform.runLater(()->{
                try{
                    App.setRoot("secondary");
                    App.setVisible(true);
                }
                catch (IOException ex){
                    print("Can't load secondary form");
                }
            });
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_END){
            print("Called exit");
            System.exit(1);
        }
    }

    private void print(String text){
        System.out.println("[KeyListener] "+text);
    }
}
