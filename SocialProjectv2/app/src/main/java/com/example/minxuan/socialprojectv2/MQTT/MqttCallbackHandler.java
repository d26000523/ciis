package com.example.minxuan.socialprojectv2.MQTT;

/**
 * Created by RMO on 2017/6/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.minxuan.socialprojectv2.MQTT.Connection.ConnectionStatus;
import com.example.minxuan.socialprojectv2.R;
import com.example.minxuan.socialprojectv2.SharedSocket;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * 當接收到MQTT傳來的訊息
 *
 */
public class MqttCallbackHandler implements MqttCallback {

    private String mess="";
    static public String ue_info = "";

    /** {@link Context} for the application used to format and import external strings**/
    private Context context;
    /** Client handle to reference the connection that this handler is attached to**/
    private String clientHandle;

    /**
     * Creates an <code>MqttCallbackHandler</code> object
     * @param context The application's context
     * @param clientHandle The handle to a {@link Connection} object
     */
    public MqttCallbackHandler(Context context, String clientHandle)
    {
        this.context = context;
        this.clientHandle = clientHandle;
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(Throwable)
     */
    @Override
    public void connectionLost(Throwable cause) {
        if (cause != null) {
            Connection c = Connections.getInstance(context).getConnection(clientHandle);
            c.addAction("Connection Lost");
            c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

            //format string to use a notification text
            Object[] args = new Object[2];
            args[0] = c.getId();
            args[1] = c.getHostName();

            String message = context.getString(R.string.connection_lost, args);

            //build intent
            Intent intent = new Intent();
            intent.setClassName(context, "com.ibm.msg.android.ConnectionDetails");
            intent.putExtra("handle", clientHandle);

            //notify the user
            Notify.notifcation(context, message, intent, context.getString(R.string.notifyTitle_connectionLost));
        }
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(String, org.eclipse.paho.client.mqttv3.MqttMessage)
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {//


        //Get connection object associated with this object
        Connection c = Connections.getInstance(context).getConnection(clientHandle);

        //create arguments to format message arrived notification string
        String[] args = new String[2];
        args[0] = new String(message.getPayload());
        args[1] = topic;

        //get the string from strings.xml and format
        String messageString = context.getString(R.string.messageRecieved, args);

        //create intent to start activity
        Intent intent = new Intent();
        intent.setClassName(context, "com.ibm.msg.android.ConnectionDetails");
        intent.putExtra("handle", clientHandle);

        //format string args
        Object[] notifyArgs = new String[3];
        notifyArgs[0] = c.getId();
        notifyArgs[1] = new String(message.getPayload());
        notifyArgs[2] = topic;

        mess = (String) notifyArgs[1];
        Activity activity = (Activity)context;
        final SharedSocket sh = (SharedSocket)activity.getApplication();

        Log.d("msgarrive",topic+"   "+mess);

        /**接收到使用者列表**/
        if(topic.compareTo("MQTT_UE_LIST")==0){
        /*******如果failed這裡會爆*******/
            sh.LIST_msg = mess;
        }
        /**接收到多人類型訊息**/
        else if(topic.compareTo("MQTT_SMS_SEND")==0)
        {   //收到廣播簡訊
            /**先切割字串，因為收到的形式為 MQTTAudioBroadcast_10.58.0.2**/
            String ifbroadcasting[] = mess.split("_");
            /**接收到語音廣播**/
            if(ifbroadcasting[0].compareTo("MQTTAudioBroadcast")==0)
            {
                Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, "ip:"+ifbroadcasting[1]+"is streaming now");
                sh.BroadcastMember.add(ifbroadcasting[1]);
            }
            /**接收到訊文廣播**/
            else
            {
                sh.USER_msg.add(mess);
            }
        }
        /**接收到單人類型訊息**/
        else if(topic.compareTo("MQTT_SMS_SEND_$(" + sh.id + ")")==0)
        {
            String ifacceptbroadcasting[] = mess.split("_");
            /**單人語音通話**/
            if(ifacceptbroadcasting[0].compareTo("VoiceCall")==0)
            {
                Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, "ip:"+ifacceptbroadcasting[1]+"call you!");
            }
            /**單人視訊通話**/
            else if(ifacceptbroadcasting[0].compareTo("VideoCall")==0)
            {
                Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, "ip:"+ifacceptbroadcasting[1]+"call you!");
            }
            /**加入語音通話**/
            else if(ifacceptbroadcasting[0].compareTo("MQTTAcceptAudio")==0)
            {
                InetAddress clientaddr;
                clientaddr = InetAddress.getByName(ifacceptbroadcasting[1]);
                final InetSocketAddress ClientAddress = new InetSocketAddress(clientaddr, 10004);
                sh.OnlineListener.add(ClientAddress);
            }
            else if(ifacceptbroadcasting[0].compareTo("MQTTLeaveAudio")==0)
            {

            }
            else
            {
                Log.d("msgarrive",topic+"   "+mess);
                sh.USER_msg.add(mess);
            }
        }
        /**接收使用者資訊**/
        else if(topic.compareTo("MQTT_UE_INFO")==0)
        {
            ue_info = mess;
        }
        c.addAction(messageString);
    }

    /**
     * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Do nothing
    }
}
