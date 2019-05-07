package com.example.enigmator.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Enigme implements Serializable {
    private int id;
    private String question;
    private String response;
    private boolean etats;
    private int UserID;
}
