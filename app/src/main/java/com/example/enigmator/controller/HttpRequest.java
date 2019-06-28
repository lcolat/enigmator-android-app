package com.example.enigmator.controller;

import android.support.annotation.Nullable;

import com.example.enigmator.entity.Response;

import lombok.Getter;

@Getter
public class HttpRequest {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";

    private final String route;
    private final String method;
    private String requestBody;
    private final HttpRequestListener listener;

    public HttpRequest(String method, String route, @Nullable String requestBody, HttpRequestListener listener) {
        this.route = route;
        this.method = method;
        this.listener = listener;

        if (POST.equals(method) || PUT.equals(method)) {
           if (requestBody == null) {
               throw new IllegalArgumentException("Cannot perform Post or Put without a Request Body");
           } else {
               this.requestBody = requestBody;
           }
        } else if (!GET.equals(method) && !DELETE.equals(method)) {
            throw new UnsupportedOperationException("HTTP Method '" + method + "' is not allowed.");
        }
    }

    public interface HttpRequestListener {
        /**
         * Tasks to perform before an HTTP request to tell the user to wait for the response
         */
        void prepareRequest();

        /**
         * Tasks to perform to handle the result of a successful HTTP request
         * @param response  The HTTP response as a {@link Response}
         */
        void handleSuccess(Response response);

        /**
         * Tasks to perform when an error occurs on an HTTP request
         * @param error  The error as a {@link Response}
         */
        void handleError(Response error);
    }
}
