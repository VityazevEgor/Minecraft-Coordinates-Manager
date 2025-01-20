package com.vityazev_egor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.HashMap;

public class App extends Application {

    private static Scene _scene;
    private static Stage _stage;

    private static HashMap<String, Parent> fxmls = new HashMap<>();

    @Override
    public void start(Stage stage) throws IOException {
        
        System.out.println("Loading forms in chache");
        fxmls.put("primary", loadFXML("primary")); 
        fxmls.put("settings", loadFXML("settings"));
        fxmls.put("secondary", loadFXML("secondary"));
        System.out.println("Loaded forms in chache");

        ServerApi.checkIfSavedServerUrlExists();
        System.out.println("\n\n\nPlease wait. I need to check if server is available");
        if (ServerApi.checkIfServerAvaible(ServerApi.serverUrl)) {
            System.out.println("\n\n\nServer is available!");
            _scene = new Scene(fxmls.get("primary"));
            Shared.addOpenMessage("primary");
        }
        else{
            System.out.println("\n\n\nServer is not available");
            _scene = new Scene(fxmls.get("settings"));
            Shared.addOpenMessage("settings");
        }

        _stage = stage;
        _stage.setScene(_scene);
        //_stage.setResizable(false);
        _stage.setMinWidth(720+50);
        _stage.setMinHeight(524+10);

        _stage.setTitle("Minecraft cords manager");
        _stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent arg0) {
                System.out.println("Form closed so i going to kill app");
                System.exit(0);
            }
            
        });
        _stage.show();
        
        // Это строчка делает так, чтобы даже после того как мы скрыли Stage приложение продолжало работать в фоновом режиме и Platform.runLater дальше работал
        Platform.setImplicitExit(false);

        
    }

    static void setRoot(String fxml) throws IOException {
        Shared.addOpenMessage(fxml);
        // если у нас открываеться вторая формы то мы в начале ждём пока она не скопирует координаты и только потом отображаем её. Иначе она не успеет их скопировать
        if (fxml.equals("secondary")){
            while (Shared.getLastMessage() != null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Shared.printEr(e, "Can't sleep");
                }
            }
        }
        _scene.setRoot(fxmls.get(fxml));
    }

    public static void setVisible(Boolean flag){
        if (_stage == null){
            return;
        }
        if (flag){
            _stage.show();
            _stage.toFront();
            _stage.requestFocus();
        }
        else{
            _stage.hide();
        }
        
    }

    public static Boolean getVisible(){
        return _stage.isShowing();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void init(String[] args) {
        launch();
    }

}