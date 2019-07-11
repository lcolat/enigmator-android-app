package com.example.enigmator.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Topic {
    private String title;
    private int id;
    private int messageCount;
}
