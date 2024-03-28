package com.vityzev_egor.cords_manager_server.Controllers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vityzev_egor.cords_manager_server.CordsManagerServerApplication;
import com.vityzev_egor.cords_manager_server.Models.CordsModel;
import com.vityzev_egor.cords_manager_server.Repos.CordsRepo;

@RestController
public class CordsController {
    
    @Autowired
    private CordsRepo context;

    @GetMapping(path = "/")
    public String index(){
        return "i'm working!";
    }

    @GetMapping(path = "/getall")
    public List<CordsModel> getAll(){
        return context.findAll();
        //return null;
    }

    @PostMapping("create")
    public ResponseEntity<Boolean> create(@RequestParam("preview") MultipartFile file){
        if (file.isEmpty()){
            System.out.println("Filtered file");
            return ResponseEntity.badRequest().body(false);
        }
        try{
            Path filePath =  Paths.get(CordsManagerServerApplication.imageDirPath.toString(), file.getOriginalFilename());
            System.out.println(filePath.toString());
            file.transferTo(filePath.toFile());
            return ResponseEntity.ok(true);
        }
        catch (Exception ex){
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body(false);
        }
    }
}
