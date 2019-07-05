package com.example.enigmator.entity;

import android.support.annotation.NonNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class Response {
    private final int statusCode;
    private final String content;

    @NonNull
    @Override
    public String toString() {
        return "Error: StatusCode " + statusCode + ": " + content;
    }
}
