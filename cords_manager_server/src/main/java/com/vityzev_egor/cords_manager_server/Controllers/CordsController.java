package com.vityzev_egor.cords_manager_server.Controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    
    private static final int FILENAME_LENGTH = 16;
    private static final String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateFileName(){
        StringBuilder sb = new StringBuilder(FILENAME_LENGTH);
        for (int i=0; i<FILENAME_LENGTH; i++){
            sb.append(CHARSET.charAt( RANDOM.nextInt(CHARSET.length())));
        }
        return sb.toString();
    }

    public String getExtension(String fileName){
        if (!fileName.contains(".")){
            return null;
        }
        var ext = fileName.substring(fileName.lastIndexOf('.'));
        return ext;
    }

    @Autowired
    private CordsRepo context;

    @GetMapping(path = "/")
    public String index(){
        return "i'm working!";
    }

    @GetMapping(path = "/getall")
    public List<CordsModel> getAll(){
        return context.findAll();
    }

    @SuppressWarnings("null")
    @PostMapping("create")
    public ResponseEntity<Boolean> create(@RequestParam("preview") MultipartFile file, @RequestParam("title") String title, @RequestParam("cords") String cords){
        if (file==null || file.isEmpty() || !file.getContentType().startsWith("image") || file.getSize() > 6 * 1024 * 1024){
            System.out.println("Filtered file");
            return ResponseEntity.badRequest().body(false);
        }
        if (validateString(cords) || validateString(title) || !validateCords(cords)){
            System.out.println("Not valid params");
            return ResponseEntity.badRequest().body(false);
        }

        try{
            var newFileName = generateFileName() + ".png";
            Path filePath =  Paths.get(CordsManagerServerApplication.imageDirPath.toString(), newFileName);
            System.out.println(filePath.toString());
            file.transferTo(filePath.toFile());

            var model = new CordsModel();
            model.title = title;
            model.cords = cords;
            model.imageName = newFileName;
            context.save(model);
            return ResponseEntity.ok(true);
        }
        catch (Exception ex){
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body(false);
        }
    }

    @RequestMapping(value =  "/delete/{id}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> delete(@PathVariable("id") Integer id){
        if (id == null){
            return ResponseEntity.badRequest().body(false);
        }
        
        CordsModel toDelete = context.findById(id).orElse(null);
        if (toDelete == null){
            return ResponseEntity.badRequest().body(false);
        }

        Path fileToDelete = Paths.get(CordsManagerServerApplication.imageDirPath.toString(), toDelete.imageName);
        try {
            Files.deleteIfExists(fileToDelete);
        } catch (IOException e) {
            System.out.println("Can't delete file, but i'm going to ignore it");
        }

        context.delete(toDelete);
        return ResponseEntity.ok(true);
    }

    private Boolean validateString(String s){
        return s==null || s.isEmpty() || s.isBlank(); 
    }

    private Boolean validateCords(String s){
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
}
