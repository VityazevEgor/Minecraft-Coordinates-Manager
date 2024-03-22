package com.vityazev_egor;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class KeyEmulator {
    private Robot r;

    private HashMap<String, Integer> unsuportedChrs = new HashMap<>(){
        {
            put(" ", KeyEvent.VK_SPACE);
            put("?", KeyEvent.VK_QUOTEDBL);
        }
    };

    public KeyEmulator(){
        try{
            r = new Robot();
        } catch (AWTException e){
            System.out.println("Can't creaete robot class");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void writeText(String textToWrite, Integer delay) throws InterruptedException{
        Thread.sleep(delay);

        for (int i=0; i<textToWrite.length(); i++){
            char currentChar = textToWrite.charAt(i);
            Integer keyCode = null;
            if (unsuportedChrs.containsKey(String.valueOf(currentChar))){
                keyCode = unsuportedChrs.get(String.valueOf(currentChar));
            }
            else{
                keyCode = KeyEvent.getExtendedKeyCodeForChar(currentChar);
            }

            if (keyCode !=null){
                try{
                    press(keyCode);
                    Thread.sleep(20);
                }
                catch (Exception ex){
                    System.out.println("Can't press this key: "+currentChar);
                    ex.printStackTrace();
                }
            }
            else{
                System.out.println("WTF?");
            }
        }
    }

    public void twoKeys() throws InterruptedException{
        r.keyPress(KeyEvent.VK_CONTROL);
        r.keyPress(KeyEvent.VK_F);
        r.keyRelease(KeyEvent.VK_CONTROL);
        r.keyRelease(KeyEvent.VK_F);
    }

    private void press(Integer keyCode){
        r.keyPress(keyCode);
        r.keyRelease(keyCode);
    }
}
