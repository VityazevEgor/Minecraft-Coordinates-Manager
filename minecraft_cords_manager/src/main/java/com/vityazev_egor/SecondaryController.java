package com.vityazev_egor;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;

public class SecondaryController extends CustomInit implements Initializable {

    @FXML
    private TextField cordsField;

    @FXML
    private TextField titleField;

    @FXML
    private ImageView previewView;

    @FXML
    private Button createButton;

    // пул потоков который могут выполнять с определённо задрежкой
    private ScheduledExecutorService shPool = Executors.newScheduledThreadPool(1);

    private BufferedImage preview  = null;
    
    private Emulator emu = new Emulator();

    // method that gets data about cords
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        setUpInitTask("secondary", 80);
    }

    @Override
    public void init() {
        // Emulator emu = new Emulator();
        String cords = filterCords(emu.getCords());

        if (cords != null && !cords.isEmpty() && !cords.isBlank()){
            cordsField.setText(cords);
        }
        else{
            cordsField.setText("Can't get cords. Check console for debug information");
        }

        if (!FakeMain.isWindows){
            // создаём поток который делает скриншот и устаналивает его в имдж ваев
            Runnable getSc = () ->{
                Platform.runLater(()->{
                    App.setVisible(false);  
                });

                preview = emu.getScreenShot();
                if (preview != null){
                    var screen = Shared.convertBufferedImage(preview);

                    Platform.runLater(()->{
                        previewView.setImage(screen);
                        App.setVisible(true); 
                    });
                }
                else{
                    Platform.runLater(()->{
                        App.setVisible(true); 
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Can't get screen shot");
                        alert.setContentText("Check console for debug information and lastError.txt file");
                        alert.show();
                    });
                
                }
            };

            // запускаем в отдельном потоке задачу для создания скриншота, которая выполниться через 200 секунд 
            shPool.schedule(getSc, 200, TimeUnit.MILLISECONDS);
            
        }
        else{
            preview = emu.getScreenShot();
            previewView.setImage(Shared.convertBufferedImage(preview));
        }
    }

    @FXML
    void loadPmForm(ActionEvent event) {
        try {
            App.setRoot("primary");
        } catch (IOException e) {
            System.out.println("Can't load primary form");
        }
    }

    @FXML
    void createCords(ActionEvent event){
        System.out.println("Sending cords");
        if (ServerApi.createCord(titleField.getText(), cordsField.getText(), preview)){
            var message = new Alert(AlertType.INFORMATION);
            message.setHeaderText(null);
            message.setContentText("The coordinates were successfully saved");
            message.setTitle("Done");
            message.show();
            try {
                App.setRoot("primary");
            } catch (IOException e) {
                Shared.printEr(e, "Can't load primary form");
            }
        }
        else{
            var message = new Alert(AlertType.ERROR);
            message.setHeaderText(null);
            message.setContentText("Can't save coordinates. Plaease check your fields and make sure that your server is up");
            message.setTitle("Error");
            message.show();
        }
    }

    

    private String filterCords(String rawText){
        if (!rawText.contains("/execute in minecraft:overworld run tp @s ") && !rawText.contains("/execute in minecraft:the_nether run tp @s ")){
            Shared.printEr(null, "Filter cords error");
        }
        rawText = rawText.replace("/execute in minecraft:overworld run tp @s ", "");
        rawText = rawText.replace("/execute in minecraft:the_nether run tp @s ", "");
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