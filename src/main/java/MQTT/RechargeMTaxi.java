package MQTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class RechargeMTaxi extends Thread{
    private MTaxi mTaxi;
    private BufferedReader inFromUser;


    public RechargeMTaxi(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
        this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
    }

    /*
    Monitor quit command
     */
    public void run(){
        String message = "";
        do {
            try {
                message = inFromUser.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while(!message.equals("recharge"));
        mTaxi.stop();
    }
}
