package com.example.minxuan.socialprojectv2.MQTT.service;

/**
 * Created by RMO on 2017/6/16.
 */

interface MqttTraceHandler {

    public abstract void traceDebug(String source, String message);

    public abstract void traceError(String source, String message);

    public abstract void traceException(String source, String message, Exception e);

}
