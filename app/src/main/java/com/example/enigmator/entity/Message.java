package com.example.enigmator.entity;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Message implements Serializable {
    private String content;
    private Date messageDate;

    public String getHour() {
        return new SimpleDateFormat("hh:mm").format(messageDate);
    }
}
