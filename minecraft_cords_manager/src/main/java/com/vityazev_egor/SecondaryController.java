package com.vityazev_egor;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class SecondaryController implements Initializable {

    @FXML
    private TextField cordsField;

    @FXML
    private ImageView previewView;

    // method that gets data about cords
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        var emu = new Emulator();
        String cords = filterCords(emu.getCords());
        if (cords != null && !cords.isEmpty() && !cords.isBlank()){
            cordsField.setText(cords);
        }
        else{
            cordsField.setText("Can't get cords. Check console for debug information");
        }
        previewView.setImage(convertBufferedImage(emu.getScreenShot()));
        
    }

    private Image convertBufferedImage(BufferedImage toConvert){
        WritableImage wi = new WritableImage(toConvert.getWidth(), toConvert.getHeight());

        PixelWriter pw = wi.getPixelWriter();
        for (int x = 0; x < toConvert.getWidth(); x++){
            for (int y = 0; y < toConvert.getHeight(); y++){
                pw.setArgb(x, y, toConvert.getRGB(x, y));
            }
        }

        return new ImageView(wi).getImage();
    }

    private String filterCords(String rawText){
        if (!rawText.contains("/execute in minecraft:overworld run tp @s ")) return null;
        rawText = rawText.replace("/execute in minecraft:overworld run tp @s ", "");
        String[] nums = rawText.split(" ");
        if (nums.length<5) return null;
        String result = "";
        for (int i=0; i<3; i++){
            if (isFloat(nums[i])){
                result+=nums[i]+" ";
            }
            else{
                return null;
            }
        }
        return result;
    }

    private Boolean isFloat(String s){
        try{
            Float.parseFloat(s);
            return true;
        }
        catch (Exception ex){
            return false;
        }
    }
}