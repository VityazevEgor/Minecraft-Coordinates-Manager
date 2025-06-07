package com.vityazev_egor.Modules;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vityazev_egor.Models.TableEntity;
import com.vityazev_egor.Modules.ServerApi.CordsModel;

import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalStorage {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path storageDir;
    private final Path dataFile;
    private final Path imagesDir;
    private final AtomicInteger nextId = new AtomicInteger(1);
    
    private Map<Integer, CordsModel> coordinates = new HashMap<>();
    private Map<Integer, BufferedImage> images = new HashMap<>();
    
    public LocalStorage() {
        String userHome = System.getProperty("user.home");
        this.storageDir = Paths.get(userHome, "Documents", "MinecraftCordsManager");
        this.dataFile = storageDir.resolve("coordinates.json");
        this.imagesDir = storageDir.resolve("images");
        
        initializeStorage();
        loadData();
    }
    
    private void initializeStorage() {
        try {
            Files.createDirectories(storageDir);
            Files.createDirectories(imagesDir);
            
            if (!Files.exists(dataFile)) {
                Files.createFile(dataFile);
                saveData();
            }
        } catch (IOException e) {
            Shared.printEr(e, "Failed to initialize local storage");
        }
    }
    
    private void loadData() {
        try {
            if (Files.size(dataFile) > 0) {
                coordinates = objectMapper.readValue(dataFile.toFile(), 
                    new TypeReference<Map<Integer, CordsModel>>() {});
                
                if (!coordinates.isEmpty()) {
                    nextId.set(coordinates.keySet().stream().max(Integer::compare).orElse(0) + 1);
                }
                
                loadImages();
            }
        } catch (IOException e) {
            Shared.printEr(e, "Failed to load local data");
            coordinates = new HashMap<>();
        }
    }
    
    private void loadImages() {
        for (CordsModel cord : coordinates.values()) {
            if (cord.getImageName() != null) {
                Path imagePath = imagesDir.resolve(cord.getImageName());
                if (Files.exists(imagePath)) {
                    try {
                        BufferedImage image = ImageIO.read(imagePath.toFile());
                        images.put(cord.getId(), image);
                    } catch (IOException e) {
                        Shared.printEr(e, "Failed to load image: " + cord.getImageName());
                    }
                }
            }
        }
    }
    
    private void saveData() {
        try {
            objectMapper.writeValue(dataFile.toFile(), coordinates);
        } catch (IOException e) {
            Shared.printEr(e, "Failed to save local data");
        }
    }
    
    public boolean createCord(String title, String cords, BufferedImage image) {
        try {
            int id = nextId.getAndIncrement();
            String imageName = "img_" + id + "_" + System.currentTimeMillis() + ".png";
            
            CordsModel cord = new CordsModel();
            cord.setId(id);
            cord.setTitle(title);
            cord.setCords(cords);
            cord.setImageName(imageName);
            
            Path imagePath = imagesDir.resolve(imageName);
            ImageIO.write(image, "PNG", imagePath.toFile());
            
            coordinates.put(id, cord);
            images.put(id, image);
            
            saveData();
            return true;
        } catch (IOException e) {
            Shared.printEr(e, "Failed to create coordinate locally");
            return false;
        }
    }
    
    public boolean deleteCord(int id) {
        CordsModel cord = coordinates.remove(id);
        if (cord != null) {
            images.remove(id);
            
            if (cord.getImageName() != null) {
                try {
                    Path imagePath = imagesDir.resolve(cord.getImageName());
                    Files.deleteIfExists(imagePath);
                } catch (IOException e) {
                    Shared.printEr(e, "Failed to delete image file: " + cord.getImageName());
                }
            }
            
            saveData();
            return true;
        }
        return false;
    }
    
    public List<CordsModel> getAllCords() {
        return new ArrayList<>(coordinates.values());
    }
    
    public Optional<BufferedImage> getPreview(String imageName) {
        return coordinates.values().stream()
            .filter(cord -> Objects.equals(cord.getImageName(), imageName))
            .findFirst()
            .map(cord -> images.get(cord.getId()));
    }
    
    public void clearAll() {
        coordinates.clear();
        images.clear();
        
        try {
            if (Files.exists(imagesDir)) {
                Files.walk(imagesDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            Shared.printEr(e, "Failed to delete image: " + path);
                        }
                    });
            }
            saveData();
        } catch (IOException e) {
            Shared.printEr(e, "Failed to clear local storage");
        }
    }
    
    public int getLocalCoordinatesCount() {
        return coordinates.size();
    }
    
    public boolean hasLocalData() {
        return !coordinates.isEmpty();
    }
    
    public Map<Integer, CordsModel> getCoordinatesMap() {
        return new HashMap<>(coordinates);
    }
    
    public void updateFromServer(List<CordsModel> serverData, Map<String, BufferedImage> serverImages) {
        coordinates.clear();
        images.clear();
        
        try {
            if (Files.exists(imagesDir)) {
                Files.walk(imagesDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            Shared.printEr(e, "Failed to delete old image: " + path);
                        }
                    });
            }
            
            int maxId = 0;
            for (CordsModel cord : serverData) {
                coordinates.put(cord.getId(), cord);
                maxId = Math.max(maxId, cord.getId());
                
                BufferedImage image = serverImages.get(cord.getImageName());
                if (image != null) {
                    images.put(cord.getId(), image);
                    
                    Path imagePath = imagesDir.resolve(cord.getImageName());
                    ImageIO.write(image, "PNG", imagePath.toFile());
                }
            }
            
            nextId.set(maxId + 1);
            saveData();
        } catch (IOException e) {
            Shared.printEr(e, "Failed to update from server");
        }
    }
}