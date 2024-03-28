package com.vityzev_egor.cords_manager_server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CordsManagerServerApplication {

	public static final Path imageDirPath = Paths.get("images").toAbsolutePath();
	public static void main(String[] args) {
		// before we run app we need to create dir where we going to store images
		if (!Files.exists(imageDirPath)){
			try {
				Files.createDirectories(imageDirPath);
			} catch (IOException e) {
				System.out.println("Can't create directory");
				System.exit(1);
			}
		}
		SpringApplication.run(CordsManagerServerApplication.class, args);
	}

}
