package com.vityazev_egor.Scenes;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;

import com.vityazev_egor.App;
import com.vityazev_egor.Modules.Emulator;
import com.vityazev_egor.Modules.NativeWindowsManager;
import com.vityazev_egor.Modules.Shared;
import com.vityazev_egor.Modules.Shared.CustomException;

import atlantafx.base.controls.Message;
import atlantafx.base.theme.Styles;
import atlantafx.base.util.Animations;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AddCordsPage implements ICustomScene {
    private final Scene scene;
    private final Emulator emulator = new Emulator();
    private final ImageView previewImage;
    private final TextField coordField;
    private final TextField nameField;
    private final Message errorMessage;
    private final App app;
    private final VBox root;

    private BufferedImage currentPreview;

    public AddCordsPage(App app){
        this.app = app;

        root = new VBox();
        root.setSpacing(5.0);

        final var goBackButton = new Button("Go back", new FontIcon(Feather.ARROW_LEFT));
        goBackButton.setOnAction(event -> app.openPage(MyCordsPage.class.getName()));
        final var addCordsButton = new Button("Save new coordinates", new FontIcon(Feather.SAVE));
        addCordsButton.setOnAction(event -> saveCoordinates());

        final var toolbar = new ToolBar(
            goBackButton,
            addCordsButton
        );
        toolbar.setStyle("-fx-background-color: #010409;");
        Node[] buttonsForCustomDesign = new Node[]{addCordsButton, goBackButton};
        for (Node buttonNode : buttonsForCustomDesign){
            buttonNode.setStyle("-fx-background-color: #010409;");
            buttonNode.setOnMouseEntered(event -> buttonNode.setStyle("-fx-background-color: #A371F726; -fx-text-fill: #8957E5FF;"));
            buttonNode.setOnMouseExited(event -> buttonNode.setStyle("-fx-background-color: #010409;"));
        }

        final var imageLabel = new Label("Preview of coordinats:");
        previewImage = new ImageView();
        previewImage.setFitWidth(app.getDefaultSize().getWidth());
        previewImage.setFitHeight(app.getDefaultSize().getHeight()/2);

        final var titleLabel = new Label("Name of new coordintaes:");
        nameField = new TextField();

        final var coordInputLabel = new Label("Coordinates:");
        coordField = new TextField();
        coordField.setEditable(false);
        coordField.setText("10 20 30");

        errorMessage = new Message("Error!", "Test", new FontIcon(Feather.ALERT_TRIANGLE));
        errorMessage.getStyleClass().add(Styles.DANGER);
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);

        root.getChildren().addAll(toolbar, errorMessage, imageLabel, previewImage, titleLabel, nameField, coordInputLabel, coordField);
        scene = new Scene(root);
    }

    @Override
    public Scene getScene() {
        return this.scene;
    }

    @Override
    public Optional<Dimension> getMinSize() {
        return Optional.empty();
    }

    private void saveCoordinates(){
        if (errorMessage.isVisible() || currentPreview == null) return;
        if (app.getServerApi().createCord(nameField.getText(), coordField.getText(), currentPreview)){
            var alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Coordinates saved");
            alert.showAndWait();
            app.openPage(MyCordsPage.class.getName());
        }
        else{
            var alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Can't save coordinates");
            alert.show();
        }
    }

    @Override
    public void beforeShow() {
        try{
            NativeWindowsManager.activateWindow("Minecraft ");
            Shared.sleep(100);
            var rawCords = emulator.getCords().orElseThrow(()-> new CustomException("Can't get coordinates from clipboard"));
            var filteredCords = filterCords(rawCords).orElseThrow(() -> new CustomException("Can't parse coordinates"));
            coordField.setText(filteredCords);
            Shared.sleep(100);
            var screenShot = emulator.getScreenShotWinX11().orElseThrow(()-> new CustomException("Can't get screenshot"));
            previewImage.setImage(Shared.convertBufferedImage(screenShot));
            currentPreview = screenShot;
            Platform.runLater(()-> {
                errorMessage.setVisible(false);
                errorMessage.setManaged(false);
            });
        }
        catch (CustomException ex){
            Shared.printEr(null, ex.getMessage());
            Platform.runLater(() -> {
                errorMessage.setDescription(ex.getMessage());
                errorMessage.setVisible(true);
                errorMessage.setManaged(true);
            });
        }
        catch (Exception ex){
            Shared.printEr(ex, "Unexpected error!");
            Platform.runLater(() -> {
                errorMessage.setDescription(ex.getMessage());
                errorMessage.setVisible(true);
            });
        }

        for (Node node : root.getChildren()){
            Animations.fadeIn(node, Duration.seconds(2)).playFromStart();
        }
    }

    private final List<String> dimensionCommands = Arrays.asList(
        "/execute in minecraft:overworld run tp @s ",
        "/execute in minecraft:the_nether run tp @s "
    );

    private Optional<String> filterCords(String rawText){
        if (dimensionCommands.stream().noneMatch(rawText::contains)){
            Shared.printEr(null, "Filter cords error");
            return Optional.empty();
        }
        for (String command : dimensionCommands){
            rawText = rawText.replace(command, "");
        }
        System.out.println(rawText);
        String[] nums = rawText.split(" ");
        if (nums.length<5) return Optional.empty();
        String result = "";
        for (int i=0; i<3; i++){
            if (isFloat(nums[i])){
                result+=nums[i]+" ";
            }
            else{
                return Optional.empty();
            }
        }
        return Optional.ofNullable(result);
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
