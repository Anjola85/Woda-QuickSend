package com.example.quicksend.util.dto;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

// used annotation in order to be able to pass this class as a param in service class
@Configuration
public class ServiceResult<T> {
    private HttpStatus status = null;
    private String message = null;

    private List<T> data = null;

    public ServiceResult() {}

    public ServiceResult(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public ServiceResult(HttpStatus status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = new ArrayList<T>();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ServiceResult<T> setStatus(HttpStatus status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ServiceResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public List<T> getData() {
        return data;
    }

    /**
     * Accepts a single data
     * @param data
     * @return
     */
    public ServiceResult<T> setData(T data) {
        if(data == null) {
            this.data = new ArrayList<T>();
        }
        this.data.add(data);
        return this;
    }

    /**
     * Accepts a list of data
     * @param data
     * @return
     */
    public ServiceResult<T> setData(List<T> data) {
        data.addAll(data);
        return this;
    }

}
