package com.vityazev_egor.Modules;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServerApi {

    @NoArgsConstructor
    @Getter
    @Setter
    @AllArgsConstructor
    public static class CordsModel{
        private Integer id;
        private String title;
        private String cords;
        private String imageName;
    }

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path savePath = Paths.get(System.getProperty("user.home"), "Documents", "savedServerUrl.txt");
    @Getter
    @Setter
    private String serverUrl = "http://127.0.0.1:8080/";

    public ServerApi(String serverUrl){
        this.serverUrl = serverUrl;
        try {
            Files.write(savePath, serverUrl.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            Shared.printEr(e, "Can't save server url");
        }
    }

    public ServerApi(){
        try{
            serverUrl = new String(Files.readAllBytes(savePath), StandardCharsets.UTF_8);
        } catch (Exception e) {
            Shared.printEr(e, "Can't read server url");
        }
    }

    public Boolean isServerAlive(){
        Request request = new Request.Builder().url(serverUrl).build();
        try(Response response = client.newCall(request).execute()){
            return response.isSuccessful();
        }
        catch (Exception e){
            Shared.printEr(e, "Server is not avaibel");
            return false;
        }
    }

    public Boolean createCord(String title, String cords, BufferedImage preview){
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("title", title)
            .addFormDataPart("cords", cords);
        
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(preview, "png", baos); 
            byte[] imageBytes = baos.toByteArray();
            var imageBody = RequestBody.create(imageBytes, MediaType.parse("image/png"));
            requestBody.addFormDataPart("preview", "preview.png", imageBody);
        } catch (IOException e) {
            Shared.printEr(e, "Can't convert preview to array of bytes");
            return false;
        }

        Request request = new Request.Builder()
            .url(serverUrl + "create")
            .post(requestBody.build())
            .build();

        try(Response response = client.newCall(request).execute()){
            return response.isSuccessful();
        }
        catch (IOException e){
            Shared.printEr(e, "Can't create cord");
            return false;
        }
    }

    public Boolean deleteCord(Integer id){
        Request request = new Request.Builder()
            .url(serverUrl + "delete/" + id)
            .get()
            .build();

        try(Response response = client.newCall(request).execute()){
            return response.isSuccessful();
        }
        catch (IOException e){
            Shared.printEr(e, "Can't delete cord");
            return false;
        }
    }

    public List<CordsModel> getAllCords(){
        Request request = new Request.Builder()
            .url(serverUrl + "getall")
            .get()
            .build();

        try(Response response = client.newCall(request).execute()){
            String rawText = response.body().string();
            System.out.println(rawText);
            return Arrays.asList(mapper.readValue(rawText, CordsModel[].class));
        }
        catch (IOException e){
            Shared.printEr(e, "Error during request to the server");
            return new ArrayList<CordsModel>();
        }
        catch (Exception e){
            Shared.printEr(e, "Can't desiaralize answer from server");
            return new ArrayList<CordsModel>();
        }
    }

    public Optional<BufferedImage> getPreview(String fileName){
        Request request = new Request.Builder()
            .url(serverUrl + "image/" + fileName)
            .get()
            .build();

        try(Response response = client.newCall(request).execute()){
            System.out.print(response.code());
            return Optional.ofNullable(ImageIO.read(response.body().byteStream()));
        }
        catch (IOException e){
            Shared.printEr(e, "Can't get preview");
            return Optional.empty();
        }
    }    
}
