package com.vityazev_egor;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.vityazev_egor.Modules.KeyListener;

// фейковый главный класс просто для того, чтобы программа работала при компиляции в jar
public class FakeMain {

    public static final Boolean isWindows = System.getProperties().getProperty("os.name").toLowerCase().contains("windows");

    public static void main(String[] args) {        
        System.out.println("I'm running on windows = " + isWindows);
        System.out.println(System.getProperties().getProperty("os.name"));
        if (isWindows){
            try {
                GlobalScreen.registerNativeHook();
            } catch (NativeHookException e) {
                System.out.println("Can't register global hook");
                e.printStackTrace();
                System.exit(1);
            }
            GlobalScreen.addNativeKeyListener(new KeyListener());
        }
        App.init(args);
    }
}
