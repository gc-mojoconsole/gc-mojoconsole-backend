package com.mojo.consoleplus.forms;

public class ResponseAuth {
    int code = 0;
    String message = "";
    String key = "";

    public ResponseAuth(int code, String message, String key) {
        this.code = code;
        this.message = message;
        this.key = key;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getKey() {
        return key;
    }
}
