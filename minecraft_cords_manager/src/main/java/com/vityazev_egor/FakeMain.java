package com.vityazev_egor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

// фейковый главный класс просто для того, чтобы программа работала при компиляции в jar
public class FakeMain {

    public static final Boolean isWindows = System.getProperties().getProperty("os.name").toLowerCase().contains("windows");

    public static void main(String[] args) {
        // try {
        //     tetsRs();
        // } catch (IOException e) {
        //     // TODO Auto-generated catch block
        //     e.printStackTrace();
        // }
        System.out.println("I'm running on windows = "+isWindows);
        System.out.println(System.getProperties().getProperty("os.name"));
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            System.out.println("Can't register global hook");
            e.printStackTrace();
            System.exit(1);
        }
        GlobalScreen.addNativeKeyListener(new KeyListener());
        App.main(args);
    }

    private static void tetsRs() throws IOException{
        // BufferedImage pr = ImageIO.read(new File("screenshot.png"));
        // System.out.println(ServerApi.createCord("test", "127.0 127.0", pr));
        var result = ServerApi.getCords();
        if (result !=null){
            for (int i=0; i<result.length; i++){
                System.out.println(result[i].title);
                var image = ServerApi.getImage(result[i].imageName);
                if (image != null){
                    System.out.print("Downloaded image");
                    ImageIO.write(image, "png", new File(result[i].imageName));
                }
            }
        }
        System.exit(0);
    }
}
