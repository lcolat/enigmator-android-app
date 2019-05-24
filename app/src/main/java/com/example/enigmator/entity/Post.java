package com.example.enigmator.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Post implements Serializable {
    private final UserEnigmator author;
    private final String content;
    private final Date postDate;
}
