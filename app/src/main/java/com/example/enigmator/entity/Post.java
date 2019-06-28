package com.example.enigmator.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Post implements Serializable {
    private final int id;
    private final String content;
    private final Date postDate;
    private final UserEnigmator user;
}
