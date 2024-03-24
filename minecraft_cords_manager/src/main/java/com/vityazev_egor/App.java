package com.vityazev_egor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;
    private static Stage _stage;

    @SuppressWarnings("exports")
    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"), 640, 480);
        _stage = stage;
        _stage.setScene(scene);
        _stage.show();

        // Это строчка делает так, чтобы даже после того как мы скрыли Stage приложение продолжало работать в фоновом режиме и Platform.runLater дальше работал
        Platform.setImplicitExit(false);
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static void setVisible(Boolean flag){
        if (_stage == null){
            return;
        }
        if (flag){
            _stage.show();
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

    public static void main(String[] args) {
        launch();
    }

}