package com.vityazev_egor.Modules;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class ConsoleRunner {
    private ProcessBuilder processBuilder;
    @Getter
    private final List<String> output = new ArrayList<>();

    public ConsoleRunner(String rawCommand){
        setProcessBuilder(rawCommand);
    }

    public void setProcessBuilder(String rawCommand){
        processBuilder = new ProcessBuilder("bash", "-c", rawCommand)
            .redirectErrorStream(true);
    }

    public Boolean runAndWaitForExit(){
        output.clear();
        try{
            var process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                output.add(line);
            }
            int exitCode = process.waitFor();
            System.out.println("Process exited with code: " + exitCode);
            return exitCode == 0;
        } catch (Exception ex){
            Shared.printEr(ex, "Error while running command");
        }
        return false;
    }
}
