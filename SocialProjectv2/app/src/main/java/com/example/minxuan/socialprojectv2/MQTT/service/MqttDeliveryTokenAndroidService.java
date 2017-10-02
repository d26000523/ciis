package com.example.minxuan.socialprojectv2.MQTT.service;

/**
 * Created by RMO on 2017/6/16.
 */

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * <p>
 * Implementation of the IMqttDeliveryToken interface for use from within the
 * MqttClientAndroidService implementation
 */
class MqttDeliveryTokenAndroidService extends MqttTokenAndroidService
        implements IMqttDeliveryToken {

    // The message which is being tracked by this token
    private MqttMessage message;

    MqttDeliveryTokenAndroidService(MqttClientAndroidService client,
                                    Object userContext, IMqttActionListener listener, MqttMessage message) {
        super(client, userContext, listener);
        this.message = message;
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.IMqttDeliveryToken#getMessage()
     */
    @Override
    public MqttMessage getMessage() throws MqttException {
        return message;
    }

    void setMessage(MqttMessage message) {
        this.message = message;
    }

    void notifyDelivery(MqttMessage delivered) {
        message = delivered;
        super.notifyComplete();
    }

}

