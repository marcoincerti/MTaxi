package SETA;

import org.codehaus.jettison.json.JSONException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;

import static java.lang.Math.abs;

public class SETA {

    static Ride generateRandomRide(Random rd, int id){
        int x1, y1, x2, y2;

        do {
            x1 = abs(rd.nextInt()%10);
            y1 = abs(rd.nextInt()%10);
            x2 = abs(rd.nextInt()%10);
            y2 = abs(rd.nextInt()%10);
        } while (x1 == x2 && y1 == y2);

        return new Ride(id, new int[]{x1, y1}, new int[]{x2, y2});
    }

    public static void main(String[] args) {
        MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = MqttClient.generateClientId();
        String topic = "seta/smartcity/rides";
        int qos = 2;

        try {
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            // Connect the client
            System.out.println(clientId + " Connecting Broker " + broker);
            client.connect(connOpts);
            System.out.println(clientId + " Connected");

            Random rd = new Random();

            Object sleep = new Object();
            synchronized (sleep) {
                for (int i = 0; i < 500; i = i + 2) {
                    Ride r = generateRandomRide(rd, i);
                    Ride r2 = generateRandomRide(rd, i+1);
                    String payload = r.getJson();
                    String payload2 = r2.getJson();
                    MqttMessage message = new MqttMessage(payload.getBytes());
                    MqttMessage message2 = new MqttMessage(payload2.getBytes());
                    message.setQos(qos);
                    message2.setQos(qos);
                    if (r.startCoordinates[0] <= 4){
                        if (r.startCoordinates[1] <= 4){
                            topic = topic + "/district1";
                        }else{
                            topic = topic + "/district4";
                        }
                    }else {
                        if (r.startCoordinates[1] <= 4) {
                            topic = topic + "/district2";
                        } else {
                            topic = topic + "/district3";
                        }
                    }
                    System.out.println(clientId + " Publishing ride 1: " + payload + " ...");
                    client.publish(topic, message);
                    System.out.println(clientId + " Ride published");
                    topic = "seta/smartcity/rides";
                    if (r2.startCoordinates[0] <= 4){
                        if (r2.startCoordinates[1] <= 4){
                            topic = topic + "/district1";
                        }else{
                            topic = topic + "/district4";
                        }
                    }else{
                        if (r2.startCoordinates[1] <= 4){
                            topic = topic + "/district2";
                        }else{
                            topic = topic + "/district3";
                        }
                    }
                    System.out.println(clientId + " Publishing ride 2: " + payload2 + " ...");
                    client.publish(topic, message2);
                    System.out.println(clientId + " Ride published");
                    topic = "seta/smartcity/rides";
                    sleep.wait(5000);
                }
            }

            if (client.isConnected())
                client.disconnect();
            System.out.println("Publisher " + clientId + " disconnected");



        } catch (MqttException | JSONException | InterruptedException me ) {
            // System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }
    }
}
