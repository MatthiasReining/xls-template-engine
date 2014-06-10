/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sourcecoding.xte;

/**
 * @author Matthias Reining
 */
public class XlsTemplateEngineException extends RuntimeException {

    public XlsTemplateEngineException() {
        super();
    }

    public XlsTemplateEngineException(String message) {
        super(message);
    }

    public XlsTemplateEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public XlsTemplateEngineException(Throwable cause) {
        super(cause);
    }

    protected XlsTemplateEngineException(String message, Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
