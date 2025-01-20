package com.vityazev_egor;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Shared {

    // методы и классы которы отвечают за обмен сообщениями между контролерами
    private static class OpenMessage{
        public String fxmlName;
        public Boolean isProcessed = false;

        public OpenMessage(String fxmlName){
            this.fxmlName = fxmlName;
        }
    }

    private static List<OpenMessage> openMessages = new ArrayList<>();

    public static synchronized String getLastMessage(){
        if (openMessages.size() > 0){
            OpenMessage firtsNotDoneMessage = openMessages.stream().filter(x -> x.isProcessed == false).findFirst().orElse(null);
            if (firtsNotDoneMessage != null){
                //System.out.println("Last message is: "+firtsNotDoneMessage.fxmlName);
                return firtsNotDoneMessage.fxmlName;
            }
            else{
                return null;
            }
        }
        else{
            return null;
        }
    }

    public static synchronized void checkLastMessage(){
        if (openMessages.size() > 0){
            OpenMessage lastMessage = openMessages.get(openMessages.size() - 1);
            lastMessage.isProcessed = true;
        }
        if (openMessages.size() >=10 && openMessages.get(0).isProcessed == true){
            openMessages.remove(0);
        }
    }

    public static synchronized void addOpenMessage(String fxmlName){
        openMessages.add(new OpenMessage(fxmlName));
    }


    // остальное

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
        }
        else{
            System.out.println(redStart +"ERROR: "+message+" : "+redEnd);
        }
        // записывает все данные из ex.strackTrace и сохраняет их в файл lastError.txt в папке с программой
        if (ex != null){
            try {
                java.io.PrintWriter pw = new java.io.PrintWriter(new File("lastError.txt"));
                ex.printStackTrace(pw);
                pw.close();
            } catch (Exception e) {}
        }

    }
}
