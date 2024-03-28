package com.vityazev_egor;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.HashMap;

import javax.imageio.ImageIO;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Emulator {
    private Robot r;

    // символы, которые по какой-то причине не переводяться используя getExtendedKeyCodeForChar
    private HashMap<String, Integer> unsuportedChars = new HashMap<>(){
        {
            put(" ", KeyEvent.VK_SPACE);
            put("?", KeyEvent.VK_QUOTEDBL);
        }
    };

    public Emulator(){
        try {
            r = new Robot();
        } catch (AWTException e) {
            print("Can't create robot");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void writeText(String textToWrite, Integer delay){
        sleep(delay);

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
                    sleep(20);
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
        r.keyPress(KeyEvent.VK_F3);
        r.keyPress(KeyEvent.VK_C);
        r.keyRelease(KeyEvent.VK_F3);
        r.keyRelease(KeyEvent.VK_C);
        sleep(100);
        try{
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        }
        catch (Exception ex){
            print("Can't get data from clipboard");
            ex.printStackTrace();
            return "";
        }
    }

    public void setClipBoard(String text){
        var content = new StringSelection(text);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, content);
    }

    @SuppressWarnings("exports")
    public BufferedImage getScreenShot(){
        if (FakeMain.isWindows){
            var screenSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            return r.createScreenCapture(screenSize);
        }
        else{
            try {
                ProcessBuilder pb = new ProcessBuilder("shutter", "-f", "-e", "-n", "-o", "screenshot.png");
                Process p = pb.start();
                p.waitFor();

                print("Made screenshot using shutter");
                return ImageIO.read(new File("screenshot.png"));

            } catch (IOException | InterruptedException ex) {
                print("got error");
                ex.printStackTrace();
                return null;
            }
        }
    }

    private void sleep(Integer mSeconds){
        try{
            Thread.sleep(mSeconds);
        }
        catch (Exception ex){
            print("Can't pause thread");
            ex.printStackTrace();
        }
    }

    public void press(Integer keyCode){
        r.keyPress(keyCode);
        r.keyRelease(keyCode);
    }

    private void print(String text){
        System.out.println("[Emulator] "+text);
    }
}
