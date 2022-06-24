package MQTT;

import SETA.Ride;
import org.eclipse.paho.client.mqttv3.*;
import java.sql.Timestamp;
import java.util.Arrays;

public class MQTTBroker extends Thread{
    private MTaxi mTaxi;
    private MqttClient client;
    private static String broker = "tcp://localhost:1883";
    private final String clientId;
    private static String topic = "seta/smartcity/rides";
    private RideQueue queue;

    public MQTTBroker(MTaxi mTaxi, RideQueue queue) {
        this.mTaxi = mTaxi;
        this.clientId = MqttClient.generateClientId();
        this.queue = queue;
    }
    /*
        Monitor MQTT topic,
        produce on the queue when receiving one
         */
    public void run() {
        int qos = 2;
        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // Connect the client
            //System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts);
            //System.out.println(clientId + " Connected - Thread PID: " + Thread.currentThread().getId());

            // Callback
            client.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    // Called when a message arrives from the server that matches any subscription made by the client
                    String time = new Timestamp(System.currentTimeMillis()).toString();
                    String receivedMessage = new String(message.getPayload());

                    Ride r = Ride.unpackJson(receivedMessage);
                    if (r.id == -1){
                        System.out.println("ERROR while unpacking order json");
                    } else {
                        queue.produce(r);
                    }
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId + " Connection lost! cause:" + cause.getMessage()+ "-  Thread PID: " + Thread.currentThread().getId());
                    System.out.println(Arrays.toString(cause.getStackTrace()));
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Not used here
                }

            });
            System.out.println("MQTT CLIENT: \n\t- Subscribing ... - Thread PID: " + Thread.currentThread().getId());
            client.subscribe(topic,qos);
            System.out.println("\t- Subscribed to topics : " + topic);

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }

    }

    /*
    Stop the MQTT client
    TODO STOP QUEUE PRODUCE
     */
    public void disconnect() {
        try {
            System.out.println("Disconnecting mqtt client");
            client.disconnect();
        } catch (MqttException me ) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }

}
