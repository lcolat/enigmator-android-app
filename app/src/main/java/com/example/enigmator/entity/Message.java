package com.example.enigmator.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Message implements Serializable {
    private String content;
    private Date messageDate;
    private boolean received;

    public String getHour() {
        return new SimpleDateFormat("hh:mm", Locale.getDefault()).format(messageDate);
    }
}
