package com.vityzev_egor.cords_manager_server.Controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vityzev_egor.cords_manager_server.CordsManagerServerApplication;

@RestController
public class FilesController {
    
    @RequestMapping(value = "/image/{imageName}", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName){
        Path filePath = Paths.get(CordsManagerServerApplication.imageDirPath.toString(), imageName);
        if (!Files.exists(filePath)){
            return ResponseEntity.badRequest().body(null);
        }
        
        try {
            byte[] result = Files.readAllBytes(filePath);
            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<byte[]>(result, headers, HttpStatus.CREATED);
        } catch (IOException e) {
            System.out.println("Can't read bytes of file");
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
