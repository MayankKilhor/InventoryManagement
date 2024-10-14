package com.project.inv.payload.response;

import java.util.HashMap;

public class ApiResponse {

    private Boolean status;

    private String message;

    private ApiErrorResponse error;

    private HashMap<String,Object> data = new HashMap<>();

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ApiErrorResponse getError() {
        return error;
    }

    public void setError(ApiErrorResponse error) {
        this.error = error;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
}

class ApiErrorResponse{
    private String statusCode;

    private String error_message;

    private HashMap<String,Object> data = new HashMap<>();

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
}
