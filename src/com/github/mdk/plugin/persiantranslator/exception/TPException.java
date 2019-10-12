package com.github.mdk.plugin.persiantranslator.exception;

public class TPException extends Exception {

    public TPException() {
    }

    public TPException(String message) {
        super(message);
    }

    public TPException(String message, Throwable cause) {
        super(message, cause);
    }

    public TPException(Throwable cause) {
        super(cause);
    }

    public TPException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
