package com.example.enigmator.entity;

import android.content.Context;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoreMedia {
    private final String url;
    private final Context context;
    private final String mediaType;
}
