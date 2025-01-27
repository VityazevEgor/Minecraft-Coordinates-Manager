package com.vityazev_egor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vityazev_egor.Modules.ServerApi;
import com.vityazev_egor.Modules.Shared;
import com.vityazev_egor.Scenes.AddCordsPage;
import com.vityazev_egor.Scenes.ICustomScene;
import com.vityazev_egor.Scenes.MyCordsPage;
import com.vityazev_egor.Scenes.SettingsPage;

import atlantafx.base.theme.PrimerDark;

public class App extends Application {

    @Getter
    private final ServerApi serverApi = new ServerApi();
    @Getter
    private final Dimension defaultSize = new Dimension(720+50, 524+10);
    private Stage currentStage;
    private final Map<String, ICustomScene> fxmls = Map.of(
        SettingsPage.class.getName(), new SettingsPage(this),
        MyCordsPage.class.getName(), new MyCordsPage(this),
        AddCordsPage.class.getName(), new AddCordsPage(this)
    );

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        currentStage = stage;
        currentStage.setScene(fxmls.get(SettingsPage.class.getName()).getScene());
        //_stage.setResizable(false);
        // currentStage.setMinWidth(720+50);
        // currentStage.setMinHeight(524+10);

        currentStage.setTitle("MC Manager");
        currentStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent arg0) {
                System.out.println("Form closed so i going to kill app");
                System.exit(0);
            }
            
        });
        currentStage.show();
        // Это строчка делает так, чтобы даже после того как мы скрыли Stage приложение продолжало работать в фоновом режиме и Platform.runLater дальше работал
        Platform.setImplicitExit(false);
    }

    public void openPage(String pageClassName){
        if (!fxmls.containsKey(pageClassName)){
            return;
        }
        currentStage.hide();
        var page = fxmls.get(pageClassName);
        currentStage.setScene(page.getScene());
        executor.submit(() -> {
            page.beforeShow();
            Platform.runLater(() -> {
                page.getMinSize().ifPresentOrElse(size ->{
                    currentStage.setMinWidth(size.getWidth());
                    currentStage.setMinHeight(size.getHeight());
                },
                () -> {
                    currentStage.setMinWidth(defaultSize.getWidth());
                    currentStage.setMinHeight(defaultSize.getHeight());
                });

                currentStage.show();
                currentStage.toFront();
                currentStage.requestFocus();
            });
        });
        // page.beforeShow();
        // page.getMinSize().ifPresentOrElse(size ->{
        //     currentStage.setMinWidth(size.getWidth());
        //     currentStage.setMinHeight(size.getHeight());
        // },
        // () -> {
        //     currentStage.setMinWidth(defaultSize.getWidth());
        //     currentStage.setMinHeight(defaultSize.getHeight());
        // });
        // currentStage.show();
        // currentStage.toFront();
        // currentStage.requestFocus();
    }

    public static void setRoot(String fxml){
        setVisible(false);
        Shared.addOpenMessage(fxml);
        executor.submit(() -> {
            while (Shared.getLastMessage() != null) {
                try{
                    Thread.sleep(50);
                }
                catch (InterruptedException e){}
            }
            //SCENE.setRoot(fxmls.get(fxml));
            Platform.runLater(() -> setVisible(true));
        });
    }

    public static void setVisible(Boolean flag){
        // if (currentStage == null){
        //     return;
        // }
        // if (flag){
        //     currentStage.show();
        //     currentStage.toFront();
        //     currentStage.requestFocus();
        // }
        // else{
        //     currentStage.hide();
        // }
        return;
    }

    public static Boolean getVisible(){
        return true;
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void init(String[] args) {
        launch();
    }

}