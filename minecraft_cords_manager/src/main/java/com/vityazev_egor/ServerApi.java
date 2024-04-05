package com.vityazev_egor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("exports")
public class ServerApi {

    public static class CordsModel{
        public Integer id;

        public String title;

        public String cords;

        public String imageName;
    }

    private static final HttpClient client = HttpClients.createDefault();
    private static final Path savePath = Paths.get(System.getProperty("user.home"), "Documents", "savedServerUrl.txt");
    public static String serverUrl = "http://127.0.0.1:8080/";    

    public static class TextResponseHandler implements HttpClientResponseHandler<String>{

        @Override
        public String handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
        
    }

    public static class CordsListHandler implements HttpClientResponseHandler<CordsModel[]>{

        @Override
        public CordsModel[] handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
            String rawText = EntityUtils.toString(response.getEntity());
            System.out.println(rawText);
            ObjectMapper mapper = new ObjectMapper();

            try{
                CordsModel[] result = mapper.readValue(rawText, CordsModel[].class);
                return result;
            }
            catch (Exception ex){
                Shared.printEr(ex, "Can't desiaralize answer from server");
                return null;
            }
        }
    }

    public static class ImageHandles implements HttpClientResponseHandler<BufferedImage>{

        @Override
        public BufferedImage handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
            var inputStream = new ByteArrayInputStream(EntityUtils.toByteArray(response.getEntity()));
            return ImageIO.read(inputStream);
        }
        
    }

    public static Boolean createCord(String title, String cords, BufferedImage preview){
        HttpPost request = new HttpPost(serverUrl + "create");

        try {
            ImageIO.write(preview, "png", new File("preview.png"));
        } catch (IOException e) {
            Shared.printEr(e, "Can't save image of preview");
            return false;
        }

        HttpEntity multipartEntity = MultipartEntityBuilder.create()
            .addPart("preview", new FileBody(new File("preview.png"), ContentType.IMAGE_PNG, "preview.png"))
            .addPart("title", new StringBody(title, ContentType.TEXT_PLAIN))
            .addPart("cords", new StringBody(cords, ContentType.TEXT_PLAIN))
            .build();

        request.setEntity(multipartEntity);
        try {
            String response = client.execute(request, new TextResponseHandler());
            System.out.println(response);
            if (response.toLowerCase().contains("true")){
                
                return true;
            }
            else{
                return false;
            }
        } catch (IOException e) {
            Shared.printEr(e, "Error in sending request in {CreateCord}");
            return false;
        }
        
    }

    // метод который проверяет доступен сервер или нет
    public static Boolean checkIfServerAvaible(String testServerUrl){
        HttpGet request = new HttpGet(testServerUrl);

        // код который устаналивает максимально время ожидания ответа на сверера в 4 секунды
        RequestConfig rsConfig  = RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofSeconds(3)).build();
        request.setConfig(rsConfig);
        try {
            String response = client.execute(request, new TextResponseHandler());
            System.out.println(response);
            if (response.toLowerCase().contains("working")){
                // если севере доступен то сохранить информацию о нём в файл
                Files.writeString(savePath, testServerUrl);
                serverUrl = testServerUrl;
                return true;
            }
            else{
                return false;
            }
        } catch (IOException e) {
            Shared.printEr(e, "Error in sending request in {checkIfServerAvaible}");
            return false;
        }
    }

    public static CordsModel[] getCords(){
        HttpGet request = new HttpGet(URI.create(serverUrl + "getall"));
        
        try {
            CordsModel[] result = client.execute(request, new CordsListHandler());
            return result;
        } catch (IOException e) {
            Shared.printEr(e, "Can't get cords. Server url = "+serverUrl +". Request url = "+request.getRequestUri());
            return null;
        }
    }

    public static BufferedImage getImage(String imageName){
        HttpGet reques = new HttpGet(serverUrl + "image/" + imageName);

        try{
            BufferedImage image = client.execute(reques, new ImageHandles());
            return image;
        }
        catch (Exception ex){
            Shared.printEr(ex, "Can't get image from server");
            return null;
        }
    }

    // метод, который проверяет есть ли в папке "Documents" файл под названием "savedServerUrl.txt" и если он есть то, записывает его значение в serverUrl
    public static Boolean checkIfSavedServerUrlExists(){
        if (Files.exists(savePath)){
            try {
               serverUrl = Files.readString(savePath).replace("\n", "");
               return true;
           } catch (IOException e) {
               Shared.printEr(e, "Error in reading file in {checkIfSavedServerUrlExists}");
               return false;
           }
        }
        else{
            return false;
        }
    }
}
