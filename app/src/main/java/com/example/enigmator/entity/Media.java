package com.example.enigmator.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Media {
    private int id;
    private String type;
    private String fileName;
}
