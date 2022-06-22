package MQTT;

import RestServer.beans.MTaxi;
import RestServer.beans.MTaxis;
import org.eclipse.paho.client.mqttv3.*;
import java.sql.Timestamp;
import java.util.Arrays;

public class MQTTBroker extends Thread{
    private MTaxi mTaxi;
    private MqttClient client;
    private static String broker = "tcp://localhost:1883";
    //private final String clientId;
    private static String topic = "seta/smartcity/rides";
    //private OrderQueue queue;

    public MQTTBroker(MTaxi mTaxi, RideQueue queue) {
        this.mTaxi = mTaxi;
        this.clientId = MqttClient.generateClientId();
        this.queue = queue;
    }
}
