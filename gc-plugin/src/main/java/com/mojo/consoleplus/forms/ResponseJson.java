package com.mojo.consoleplus.forms;

public final class ResponseJson {
    public String message = "success";
    public int code = 200;
    public String payload = "";

    public ResponseJson(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public ResponseJson(String message, int code, String payload) {
        this.message = message;
        this.code = code;
        this.payload = payload;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    
}
