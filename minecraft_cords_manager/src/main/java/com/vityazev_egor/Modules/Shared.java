package com.vityazev_egor.Modules;

import java.awt.image.BufferedImage;
import java.io.File;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Shared {

    public static class CustomException extends Exception {
        public CustomException(String message){
            super(message);
        }
    }

    private static final String redStart = "\u001B[31m";
    private static final String redEnd = "\u001B[0m";

    public static Image convertBufferedImage(BufferedImage toConvert){
        WritableImage wi = new WritableImage(toConvert.getWidth(), toConvert.getHeight());

        PixelWriter pw = wi.getPixelWriter();
        for (int x = 0; x < toConvert.getWidth(); x++){
            for (int y = 0; y < toConvert.getHeight(); y++){
                pw.setArgb(x, y, toConvert.getRGB(x, y));
            }
        }

        return new ImageView(wi).getImage();
    }

    public static void printEr(Exception ex, String message){
        if (ex!= null) {
            System.out.println(redStart +"ERROR: "+message+" : "+ex.getMessage()+redEnd);
            try {
                java.io.PrintWriter pw = new java.io.PrintWriter(new File("lastError.txt"));
                ex.printStackTrace(pw);
                pw.close();
            } catch (Exception e) {}
        }
        else{
            System.out.println(redStart +"ERROR: "+message+" : "+redEnd);
        }
    }

    public static void sleep(long milis){
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }    
    }
}
