package com.vityazev_egor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
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


import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("exports")
public class ServerApi {

    public static class CordsModel{
        public Integer id;

        public String title;

        public String cords;

        public String imageName;
    }

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
                System.out.println("Can't desiaralize");
                ex.printStackTrace();
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
        HttpClient client = HttpClients.createDefault();
        HttpPost request = new HttpPost("http://127.0.0.1:8080/create");

        try {
            ImageIO.write(preview, "png", new File("preview.png"));
        } catch (IOException e) {
            e.printStackTrace();
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

    public static CordsModel[] getCords(){
        HttpClient client = HttpClients.createDefault();
        HttpGet request = new HttpGet("http://127.0.0.1:8080/getall");
        
        try {
            CordsModel[] result = client.execute(request, new CordsListHandler());
            return result;
        } catch (IOException e) {
            System.out.println("Error in senging rs");
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage getImage(String imageName){
        HttpClient client = HttpClients.createDefault();
        HttpGet reques = new HttpGet("http://127.0.0.1:8080/image/" + imageName);

        try{
            BufferedImage image = client.execute(reques, new ImageHandles());
            return image;
        }
        catch (Exception ex){
            System.out.println("Can't get image");
            ex.printStackTrace();
            return null;
        }
    }
}
