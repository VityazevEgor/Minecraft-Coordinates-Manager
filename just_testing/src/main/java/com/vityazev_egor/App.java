package com.vityazev_egor;

import java.util.Scanner;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class App 
{
    public static void main( String[] args ) throws NativeHookException, AWTException, InterruptedException, HeadlessException, UnsupportedFlavorException, IOException
    {
        emulateTest("good vibes only");
        //testKeyLogger();
    }

    private static void testKeyLogger() throws NativeHookException{
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(new KeyListener());
        Scanner sc = new Scanner(System.in);
        sc.nextLine();
        sc.close();
        GlobalScreen.unregisterNativeHook();
    }

    public static void emulateTest(String textToWrite) throws AWTException, InterruptedException, HeadlessException, UnsupportedFlavorException, IOException{
        
        var emulator = new KeyEmulator();
        emulator.writeText(textToWrite, 3000);
        // Thread.sleep(3000);
        // emulator.twoKeys();
        // Thread.sleep(1000);
        // System.out.println(emulator.GetClipBoard());
    }
}
