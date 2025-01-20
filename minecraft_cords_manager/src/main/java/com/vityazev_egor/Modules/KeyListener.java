package com.vityazev_egor.Modules;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.vityazev_egor.App;

import javafx.application.Platform;

public class KeyListener implements NativeKeyListener {
    
    @Override
    public void nativeKeyPressed(NativeKeyEvent e){
        print(("Key Pressed: " + NativeKeyEvent.getKeyText(e.getKeyCode())));
        // Если нажата кнопка хоме то открыть главное меню
        if (e.getKeyCode() == NativeKeyEvent.VC_HOME){

            System.out.println("Dected HOME press");
            Platform.runLater(()->{
                //App.setRoot("primary");
                print("Visible state = " + App.getVisible());
                App.setVisible(!App.getVisible());
            });

        }

        if (e.getKeyCode() == NativeKeyEvent.VC_INSERT || e.getKeyCode() == NativeKeyEvent.VC_F13){

            print("Detcted insert key");
            Platform.runLater(()->{
                //App.setRoot("secondary");
                App.setVisible(true);
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
