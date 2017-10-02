package com.example.minxuan.socialprojectv2.MQTT.service;

/**
 * Created by RMO on 2017/6/16.
 */

/**
 * Enumeration representing the success or failure of an operation
 */
public enum Status {
    /**
     * Indicates that the operation succeeded
     */
    OK,

    /**
     * Indicates that the operation failed
     */
    ERROR,

    /**
     * Indicates that the operation's result may be returned asynchronously
     */
    NO_RESULT
}
