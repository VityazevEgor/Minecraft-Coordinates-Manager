package com.vityazev_egor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.vityazev_egor.Modules.KeyListener;
import com.vityazev_egor.Modules.ServerApi;
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
    private final Map<String, ICustomScene> scenes = Map.of(
        SettingsPage.class.getName(), new SettingsPage(this),
        MyCordsPage.class.getName(), new MyCordsPage(this),
        AddCordsPage.class.getName(), new AddCordsPage(this)
    );

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        currentStage = stage;
        currentStage.setScene(scenes.get(SettingsPage.class.getName()).getScene());
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
        try{
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new KeyListener(this));
        }catch (Exception e){
            System.out.println("Can't register global hook");
            e.printStackTrace();
            System.exit(1);
        }
        // Это строчка делает так, чтобы даже после того как мы скрыли Stage приложение продолжало работать в фоновом режиме и Platform.runLater дальше работал
        Platform.setImplicitExit(false);
    }

    public void openPage(String pageClassName){
        if (!scenes.containsKey(pageClassName)){
            return;
        }
        currentStage.hide();
        var page = scenes.get(pageClassName);
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
    }

    public void setVisible(Boolean flag){
        if (flag){
            currentStage.show();
            currentStage.toFront();
            currentStage.requestFocus();
        }
        else{
            currentStage.hide();
        }
    }

    public Boolean getVisible(){
        return currentStage.isShowing();
    }

    public static void init(String[] args) {
        launch();
    }

}