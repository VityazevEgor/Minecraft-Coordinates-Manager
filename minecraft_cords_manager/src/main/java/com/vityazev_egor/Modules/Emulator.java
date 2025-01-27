package com.vityazev_egor.Modules;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.vityazev_egor.FakeMain;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Emulator {
    private Robot robot;

    // символы, которые по какой-то причине не переводяться используя getExtendedKeyCodeForChar
    private HashMap<String, Integer> unsuportedChars = new HashMap<>(){
        {
            put(" ", KeyEvent.VK_SPACE);
            put("?", KeyEvent.VK_QUOTEDBL);
        }
    };

    public Emulator(){
        try {
            robot = new Robot();
        } catch (AWTException e) {
            print("Can't create robot");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void writeText(String textToWrite, Integer delayMilis){
        Shared.sleep(delayMilis);

        for (int i=0; i<textToWrite.length(); i++){
            char currentChar = textToWrite.charAt(i);
            Integer keyCode = null;
            if (unsuportedChars.containsKey(String.valueOf(currentChar))){
                keyCode = unsuportedChars.get(String.valueOf(currentChar));
            }
            else{
                keyCode = KeyEvent.getExtendedKeyCodeForChar(currentChar);
            }

            if (keyCode !=null){
                try{
                    press(keyCode);
                    Shared.sleep(20);
                }
                catch (Exception ex){
                    print("Can't press this key: " + currentChar);
                    ex.printStackTrace();
                }
            }
            else{
                print("WTF?");
            }
        }
    }

    // works only on windows
    public String getCords(){
        robot.keyPress(KeyEvent.VK_F3);
        robot.keyPress(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_F3);
        robot.keyRelease(KeyEvent.VK_C);
        Shared.sleep(100);
        try{
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        }
        catch (Exception ex){
            Shared.printEr(ex, "Can't get data from clipboard");
            return "";
        }
    }

    public void setClipBoard(String text){
        var content = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, content);
    }

    public BufferedImage getScreenShot(){
        if (FakeMain.isWindows){
            var screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            return robot.createScreenCapture(screenSize);
        }
        else{
            try {
                Files.deleteIfExists(Paths.get("screenshot.png"));
                ProcessBuilder pb = new ProcessBuilder("shutter", "-f", "-e", "-n", "-o", "screenshot.png");
                Process p = pb.start();
                p.waitFor(20, TimeUnit.SECONDS);

                print("Made screenshot using shutter");
                return ImageIO.read(new File("screenshot.png"));

            } catch (IOException | InterruptedException ex) {
                Shared.printEr(ex, "Got error while making screenshot");
                return null;
            }
        }
    }

    public Optional<BufferedImage> getScreenShotWinX11(){
        try{
            var screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            return Optional.of(robot.createScreenCapture(screenSize));
        } catch (Exception ex){
            Shared.printEr(ex, "Got error while making screenshot");
            return Optional.empty();
        }
    }

    public void press(Integer keyCode){
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
    }

    private void print(String text){
        System.out.println("[Emulator] "+text);
    }
}
