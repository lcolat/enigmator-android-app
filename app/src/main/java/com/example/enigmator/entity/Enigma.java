package com.example.enigmator.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Enigma implements Serializable {
    private int id;
    private String question;
    private int likes;
    private Date creationDate;
    private boolean status;
    private int UserID;
    private int scoreReward;
    private int topicId;
}
