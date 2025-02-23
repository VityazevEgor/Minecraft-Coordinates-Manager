package com.vityazev_egor.Modules;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;
import java.util.Optional;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Emulator {
    private Robot robot;

    // символы, которые по какой-то причине не переводяться используя getExtendedKeyCodeForChar
    private HashMap<Character, Integer> unsuportedChars = new HashMap<>(){
        {
            put(' ', KeyEvent.VK_SPACE);
            put('?', KeyEvent.VK_QUOTEDBL);
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
            Character currentChar = textToWrite.charAt(i);
            Integer keyCode = unsuportedChars.containsKey(currentChar) ? unsuportedChars.get(currentChar) : KeyEvent.getExtendedKeyCodeForChar(currentChar);

            if (keyCode == 0) continue;

            try{
                press(keyCode);
                Shared.sleep(50);
            }
            catch (Exception ex){
                print("Can't press this key: " + currentChar);
                ex.printStackTrace();
            }
        }
    }

    // works only on windows
    public Optional<String> getCords(){
        robot.keyPress(KeyEvent.VK_F3);
        robot.keyPress(KeyEvent.VK_C);
        robot.keyRelease(KeyEvent.VK_F3);
        robot.keyRelease(KeyEvent.VK_C);
        Shared.sleep(100);
        try{
            return Optional.ofNullable( (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
        }
        catch (Exception ex){
            Shared.printEr(ex, "Can't get data from clipboard");
            return Optional.empty();
        }
    }

    public Boolean setClipBoard(String text){
        try{
            var content = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, content);
            return true;
        }
        catch (Exception ex){
            Shared.printEr(ex, "Can't set clipboard");
            return false;
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
