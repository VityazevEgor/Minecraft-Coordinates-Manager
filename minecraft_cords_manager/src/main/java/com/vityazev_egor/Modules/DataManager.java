package com.vityazev_egor.Modules;

import com.vityazev_egor.Modules.ServerApi.CordsModel;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataManager {
    private final ServerApi serverApi;
    private final LocalStorage localStorage;
    private final ScheduledExecutorService syncExecutor = Executors.newSingleThreadScheduledExecutor();
    
    private boolean isOnline = false;
    private boolean syncInProgress = false;
    
    public DataManager(ServerApi serverApi) {
        this.serverApi = serverApi;
        this.localStorage = new LocalStorage();
        
        startPeriodicSync();
    }
    
    private void startPeriodicSync() {
        syncExecutor.scheduleWithFixedDelay(this::attemptSync, 0, 30, TimeUnit.SECONDS);
    }
    
    private void attemptSync() {
        if (syncInProgress) return;
        
        syncInProgress = true;
        try {
            boolean wasOnline = isOnline;
            isOnline = serverApi.isServerAlive();
            
            if (isOnline && (!wasOnline || shouldSync())) {
                performSync();
            }
        } catch (Exception e) {
            Shared.printEr(e, "Error during sync attempt");
            isOnline = false;
        } finally {
            syncInProgress = false;
        }
    }
    
    private boolean shouldSync() {
        return localStorage.hasLocalData();
    }
    
    private void performSync() {
        try {
            List<CordsModel> serverData = serverApi.getAllCords();
            Map<String, BufferedImage> serverImages = new HashMap<>();
            
            for (CordsModel cord : serverData) {
                if (cord.getImageName() != null) {
                    serverApi.getPreview(cord.getImageName()).ifPresent(image -> 
                        serverImages.put(cord.getImageName(), image));
                }
            }
            
            Map<Integer, CordsModel> localData = localStorage.getCoordinatesMap();
            
            for (CordsModel localCord : localData.values()) {
                boolean existsOnServer = serverData.stream()
                    .anyMatch(serverCord -> Objects.equals(serverCord.getTitle(), localCord.getTitle()) &&
                                           Objects.equals(serverCord.getCords(), localCord.getCords()));
                
                if (!existsOnServer) {
                    Optional<BufferedImage> localImage = localStorage.getPreview(localCord.getImageName());
                    if (localImage.isPresent()) {
                        serverApi.createCord(localCord.getTitle(), localCord.getCords(), localImage.get());
                    }
                }
            }
            
            serverData = serverApi.getAllCords();
            serverImages.clear();
            for (CordsModel cord : serverData) {
                if (cord.getImageName() != null) {
                    serverApi.getPreview(cord.getImageName()).ifPresent(image -> 
                        serverImages.put(cord.getImageName(), image));
                }
            }
            
            localStorage.updateFromServer(serverData, serverImages);
            
        } catch (Exception e) {
            Shared.printEr(e, "Error during sync");
        }
    }
    
    public CompletableFuture<Boolean> createCord(String title, String cords, BufferedImage preview) {
        return CompletableFuture.supplyAsync(() -> {
            boolean localSuccess = localStorage.createCord(title, cords, preview);
            
            if (isOnline) {
                try {
                    boolean serverSuccess = serverApi.createCord(title, cords, preview);
                    return localSuccess && serverSuccess;
                } catch (Exception e) {
                    Shared.printEr(e, "Failed to create coordinate on server, but saved locally");
                    isOnline = false;
                    return localSuccess;
                }
            }
            
            return localSuccess;
        });
    }
    
    public CompletableFuture<Boolean> deleteCord(Integer id) {
        return CompletableFuture.supplyAsync(() -> {
            boolean localSuccess = localStorage.deleteCord(id);
            
            if (isOnline) {
                try {
                    boolean serverSuccess = serverApi.deleteCord(id);
                    return localSuccess && serverSuccess;
                } catch (Exception e) {
                    Shared.printEr(e, "Failed to delete coordinate on server, but deleted locally");
                    isOnline = false;
                    return localSuccess;
                }
            }
            
            return localSuccess;
        });
    }
    
    public List<CordsModel> getAllCords() {
        if (isOnline) {
            try {
                List<CordsModel> serverData = serverApi.getAllCords();
                if (!serverData.isEmpty()) {
                    return serverData;
                }
            } catch (Exception e) {
                Shared.printEr(e, "Failed to get coordinates from server, using local data");
                isOnline = false;
            }
        }
        
        return localStorage.getAllCords();
    }
    
    public Optional<BufferedImage> getPreview(String imageName) {
        if (isOnline) {
            try {
                Optional<BufferedImage> serverImage = serverApi.getPreview(imageName);
                if (serverImage.isPresent()) {
                    return serverImage;
                }
            } catch (Exception e) {
                Shared.printEr(e, "Failed to get preview from server, using local data");
                isOnline = false;
            }
        }
        
        return localStorage.getPreview(imageName);
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public boolean hasLocalData() {
        return localStorage.hasLocalData();
    }
    
    public int getLocalCoordinatesCount() {
        return localStorage.getLocalCoordinatesCount();
    }
    
    public void forceSync() {
        if (!syncInProgress && isOnline) {
            CompletableFuture.runAsync(this::performSync);
        }
    }
    
    public void shutdown() {
        syncExecutor.shutdown();
        try {
            if (!syncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                syncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            syncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}