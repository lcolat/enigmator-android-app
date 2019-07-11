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

    public static RequestBody buildRequestBody(Media media) {
        MultipartBuilder builder = new MultipartBuilder();
        builder.type(MultipartBuilder.FORM);
        builder.addFormDataPart("mediaType", media.getType());
        builder.addFormDataPart("file", media.getFileName(), RequestBody.create(mediaTypeFromMedia(media), new File(media.getFileName())));

        return builder.build();
    }

    private static MediaType mediaTypeFromMedia(Media media) {
        String extension = media.getFileName().substring(media.getFileName().lastIndexOf(".") + 1);
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
            default:
                throw new UnsupportedOperationException("File extension not supported: " + extension);
        }

        return MediaType.parse(type);
    }
}
