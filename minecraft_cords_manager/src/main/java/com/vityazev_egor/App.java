package com.vityazev_egor;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.Getter;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.vityazev_egor.Modules.DataManager;
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
    private final DataManager dataManager = new DataManager(serverApi);
    @Getter
    private final Dimension defaultSize = new Dimension(720+50, 524+10);
    private Stage currentStage;
    private final Map<String, ICustomScene> scenes = Map.of(
        SettingsPage.class.getName(), new SettingsPage(this),
        MyCordsPage.class.getName(), new MyCordsPage(this),
        AddCordsPage.class.getName(), new AddCordsPage(this)
    );

    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public App(){
        super();
    }

    @SuppressWarnings({ "unused", "resource" })
    private void dumpCSS(){
        String stylesheet = new PrimerDark().getUserAgentStylesheet();
        System.out.println("Stylesheet path: " + stylesheet);

        try {
            // Получаем путь к теме
            System.out.println("Stylesheet path: " + stylesheet);

            // Загружаем ресурс как поток
            InputStream inputStream = EntryPoint.class.getResourceAsStream(stylesheet);
            if (inputStream == null) {
                System.err.println("Failed to load stylesheet.");
                return;
            }

            // Читаем файл в список строк
            List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.toList());

            // 🔹 Сохраняем измененный файл
            Path outputFile = Path.of("primer-dark-modified.css");
            Files.write(outputFile, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("Modified stylesheet saved as: " + outputFile.toAbsolutePath());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        //Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        Application.setUserAgentStylesheet(EntryPoint.class.getResource("primer-dark-modified.css").toExternalForm());

        currentStage = stage;
        currentStage.setScene(scenes.get(SettingsPage.class.getName()).getScene());
        currentStage.setResizable(true);
//        currentStage.setMinWidth(720+50);
//        currentStage.setMinHeight(524+10);

        currentStage.setTitle("MC Manager");
        currentStage.setOnCloseRequest(arg0 -> {
            System.out.println("Form closed so i going to kill app");
            dataManager.shutdown();
            System.exit(0);
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
        var newScene = scenes.get(pageClassName);
        if (newScene.hideWindowBeforeSwitch())
            setVisible(false);
        currentStage.setScene(newScene.getScene());
        executor.submit(() -> {
            newScene.beforeShow();
            Platform.runLater(() -> {
                if (newScene.hideWindowBeforeSwitch()) {
                    setVisible(true);
                    newScene.getMinSize().ifPresent(size ->{
                        currentStage.setMinWidth(size.getWidth());
                        currentStage.setMinHeight(size.getHeight());
                    });
                }
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