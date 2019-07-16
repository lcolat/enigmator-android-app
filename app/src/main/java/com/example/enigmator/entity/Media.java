package com.example.enigmator.entity;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
  
@Getter
@Setter
@AllArgsConstructor
public class Media {
    private int id;
    private String fileName;
    private String type;

    public static RequestBody buildRequestBody(String fileName, String type) {
        MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.FORM);
        builder.addFormDataPart("mediaType", type);
        builder.addFormDataPart("file", fileName, RequestBody.create(mediaTypeFromFilename(fileName), new File(fileName)));

        return builder.build();
    }

    private static MediaType mediaTypeFromFilename(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        String type;
        switch (extension.toLowerCase()) {
            case "png":
                type = "image/png";
                break;
            case "jpeg":
                type = "image/jpeg";
                break;
            case "jpg":
                type = "image/jpeg";
                break;
            case "mp4":
                type = "video/mp4";
                break;
            case "3gp":
                type = "video/3gpp";
                break;
            default:
                throw new UnsupportedOperationException("File extension not supported: " + extension);
        }

        return MediaType.parse(type);
    }
}
