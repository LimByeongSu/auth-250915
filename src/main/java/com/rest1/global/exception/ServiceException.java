package com.rest1.global.exception;

public class ServiceException extends RuntimeException {
    //ServiceException은 우리 서비스에서 일어나는 예외만 다룬다는 의미이다.

    private String resultCode;
    private String msg;

    public ServiceException(String resultCode, String msg) {
        super("%s : %s".formatted(resultCode, msg));
        this.resultCode = resultCode;
        this.msg = msg;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getMsg() {
        return msg;
    }
}
