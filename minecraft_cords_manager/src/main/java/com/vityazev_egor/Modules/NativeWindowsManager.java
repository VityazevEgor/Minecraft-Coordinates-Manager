package com.vityazev_egor.Modules;


import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import com.vityazev_egor.FakeMain;

// Manages windows on Windows and Linux systems
public class NativeWindowsManager {

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);
        boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);
        int GetWindowTextA(Pointer hWnd, byte[] lpString, int nMaxCount);
        boolean ShowWindow(Pointer hWnd, int nCmdShow);
        boolean SetForegroundWindow(Pointer hWnd);
    }

    public static class ProcessInfo {
        public final HWND handle;
        public final String title;
        
        public ProcessInfo(HWND handle, String title){
            this.handle = handle;
            this.title = title;
        }
    }

    public static List<ProcessInfo> getAllProcess(){
        var result = new ArrayList<ProcessInfo>();
        final User32 user32 = User32.INSTANCE;
        user32.EnumWindows(new WinUser.WNDENUMPROC() {
            int count = 0;
            @Override
            public boolean callback(HWND hWnd, Pointer arg1) {
                byte[] windowText = new byte[512];
                user32.GetWindowTextA(hWnd.getPointer(), windowText, 512);
                String wText = Native.toString(windowText).trim();
                if (!wText.isEmpty()) {
                    System.out.println("Found window with pointer " + hWnd.getPointer() + ", total " + ++count + " Text: " + wText);
                    result.add(new ProcessInfo(hWnd, wText));
                } 
                return true;
            }
        }, null);

        return result;
    }

    public static Boolean activateWindow(ProcessInfo pr){
        final User32 user32 = User32.INSTANCE;
        return user32.ShowWindow(pr.handle.getPointer(), 9) && user32.SetForegroundWindow(pr.handle.getPointer());
    }

    public static Boolean activateWindow(String title){
        if (FakeMain.isWindows){
            return activateWindowWin(title);
        }else{
            return activateWindowLinux(title);
        }
        
    }

    // for Linux only
    private static Boolean activateWindowLinux(String title){
        var runner = new ConsoleRunner(String.format("xdotool search \"%s\"", title));
        var exitStatus = runner.runAndWaitForExit();
        if (!exitStatus || runner.getOutput().size() < 2){
            Shared.printEr(null, "Got less windows that expectect");
            return false;
        }
        runner.setProcessBuilder(String.format("xdotool windowactivate %s", runner.getOutput().get(1)));
        runner.runAndWaitForExit();
        return true;
    }

    private static Boolean activateWindowWin(String title){
        return getAllProcess().stream().filter(pr -> pr.title.contains(title))
            .findFirst()
            .map(process -> activateWindow(process))
            .orElse(false);
    }
}
