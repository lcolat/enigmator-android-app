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

    public static final String MEDIA_VIDEO = "video";
    public static final String MEDIA_IMAGE = "image";

    private final String route;
    private final String method;
    private String requestBody;
    private String filePath;
    private String mediaType;
    private final HttpRequestListener listener;

    public HttpRequest(String method, String route, @Nullable String requestBody, @Nullable HttpRequestListener listener) {
        this.route = route;
        this.method = method;
        this.listener = listener;
        this.requestBody = requestBody;

        if (!isSupportedOperation(method)) {
            throw new UnsupportedOperationException("HTTP Method '" + method + "' is not allowed.");
        }
    }

    HttpRequest(String method, String route, String filePath, String mediaType, @Nullable HttpRequestListener listener) {
        this.method = method;
        this.route = route;
        this.listener = listener;
        this.mediaType = mediaType;
        this.filePath = filePath;
    }

    private static boolean isSupportedOperation(String method) {
        return GET.equals(method) || POST.equals(method) || PUT.equals(method)
                || DELETE.equals(method);
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
