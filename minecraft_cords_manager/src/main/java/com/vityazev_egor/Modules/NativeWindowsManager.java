package com.vityazev_egor.Modules;


import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;

// класс который может управлять окнами на виндовс
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

    public static boolean ActivateWindow(ProcessInfo pr){
        final User32 user32 = User32.INSTANCE;
        return user32.ShowWindow(pr.handle.getPointer(), 9) && user32.SetForegroundWindow(pr.handle.getPointer());
    }
}
