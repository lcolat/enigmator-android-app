package com.example.enigmator.activity;

public interface IHttpComponent {

    /**
     * Do some UI updates to show that a Http request will be performed
     */
    void prepareRequest();

    /**
     * Update the UI and handle the HTTP response.
     * @param result  The HTTP response
     */
    void handleSuccess(String result);
    void handleError(String error);
}
