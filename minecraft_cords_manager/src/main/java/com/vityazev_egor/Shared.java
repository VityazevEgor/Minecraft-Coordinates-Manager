package com.vityazev_egor;

import java.awt.image.BufferedImage;
import java.io.File;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Shared {

    private static final String redStart = "\u001B[31m";
    private static final String redEnd = "\u001B[0m";

    @SuppressWarnings("exports")
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
        System.out.println(redStart +"ERROR: "+message+" : "+ex.getMessage()+redEnd);
        // записывает все данные из ex.strackTrace и сохраняет их в файл lastError.txt в папке с программой
        try {
            java.io.PrintWriter pw = new java.io.PrintWriter(new File("lastError.txt"));
            ex.printStackTrace(pw);
            pw.close();
        } catch (Exception e) {}

    }
}
