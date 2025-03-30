package com.vityzev_egor.cords_manager_server.Controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vityzev_egor.cords_manager_server.CordsManagerServerApplication;
import com.vityzev_egor.cords_manager_server.Models.CordsModel;
import com.vityzev_egor.cords_manager_server.Repos.CordsRepo;

@RestController
public class CordsController {
    private final Logger logger = LoggerFactory.getLogger(CordsController.class);
    @Autowired
    private CordsRepo cordsRepo;

    @GetMapping(path = "/")
    public String index(){
        return "i'm working!";
    }

    @GetMapping(path = "/getall")
    public List<CordsModel> getAll(){
        return cordsRepo.findAll();
    }

    private Boolean isInvalidString(String s){
        return s==null || s.isEmpty() || s.isBlank(); 
    }

    private Boolean isValidCords(String s){
        String[] nums = s.split(" ");
        if (nums.length!=3) return false;

        for (String num : nums) {
            try{
                Double.parseDouble(num);
            }
            catch (Exception ex){
                System.out.println("Can't filter this = "+num);
                return false;
            }
        }
        return true;
    }

    @PostMapping("create")
    public ResponseEntity<?> create(@RequestParam("preview") MultipartFile file, @RequestParam("title") String title, @RequestParam("cords") String cords) {
        if (file.isEmpty() || !file.getContentType().startsWith("image") || file.getSize() > 6 * 1024 * 1024) {
            logger.warn("Некорректный файл.");
            return ResponseEntity.badRequest().body("File didn't pass validation");
        }

        if (isInvalidString(title) || isInvalidString(cords) || !isValidCords(cords)) {
            logger.warn("Некорректные параметры: title={}, cords={}", title, cords);
            return ResponseEntity.badRequest().body("Parameters didn't pass validation");
        }

        try {
            String newFileName = UUID.randomUUID().toString() + ".png";
            Path filePath = Paths.get(CordsManagerServerApplication.imageDirPath.toString(), newFileName);
            file.transferTo(filePath.toFile());
            CordsModel model = new CordsModel(null, title, cords, newFileName);
            cordsRepo.save(model);

            return ResponseEntity.ok(true);
        } catch (Exception ex) {
            logger.error("Ошибка при создании записи", ex);
            return ResponseEntity.internalServerError().body(ex.getMessage());
        }
    }


    @RequestMapping(value =  "/delete/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> delete(@PathVariable("id") Integer id){        
        return cordsRepo.findById(id).map(toDelete ->{
            Path fileToDelete = Paths.get(CordsManagerServerApplication.imageDirPath.toString(), toDelete.getImageName());
            try {
                Files.deleteIfExists(fileToDelete);
            } catch (IOException e) {
                System.out.println("Can't delete file, but i'm going to ignore it");
            }
            cordsRepo.delete(toDelete);
            return ResponseEntity.ok().body(true);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(false));
    }
}
