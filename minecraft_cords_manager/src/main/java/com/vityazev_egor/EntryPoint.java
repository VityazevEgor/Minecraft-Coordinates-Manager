package com.vityazev_egor;

// фейковый главный класс просто для того, чтобы программа работала при компиляции в jar
public class EntryPoint {

    public static final Boolean isWindows = System.getProperties().getProperty("os.name").toLowerCase().contains("windows");

    public static void main(String[] args) {        
        System.out.println("I'm running on windows = " + isWindows);
        System.out.println(System.getProperties().getProperty("os.name"));
        App.init(args);
    }
}
