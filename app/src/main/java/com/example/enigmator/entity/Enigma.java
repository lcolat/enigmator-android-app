package com.example.enigmator.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Enigma implements Serializable {
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 100;
    public static final int MEDIUM_THRESHOLD = 25;
    public static final int HARD_THRESHOLD = 50;
    public static final int EXTREME_THRESHOLD = 75;

    private int id;
    private String name;
    private String question;
    private int likes;
    private Date creationDate;
    private boolean status;
    private int scoreReward;
    private int UserID;
    private int topicId;
    private String answer;
}
