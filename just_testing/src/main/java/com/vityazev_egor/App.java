package com.vityazev_egor;

import java.util.Scanner;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

public class App 
{
    public static void main( String[] args ) throws NativeHookException, AWTException, InterruptedException
    {
        emulateTest("How to poop");
        
    }

    private static void testKeyLogger() throws NativeHookException{
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(new KeyListener());
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        GlobalScreen.unregisterNativeHook();
    }

    public static void emulateTest(String textToWrite) throws AWTException, InterruptedException{
        
        var emulator = new KeyEmulator();
        emulator.writeText(textToWrite, 3000);
        // Thread.sleep(3000);
        // emulator.twoKeys();
    }
}
