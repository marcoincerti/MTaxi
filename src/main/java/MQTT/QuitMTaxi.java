package MQTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class QuitMTaxi extends Thread{
    private MTaxi mTaxi;
    private BufferedReader inFromUser;


    public QuitMTaxi(MTaxi mTaxi) {
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
        } while(!message.equals("quit") && !message.equals("recharge"));

        if(message.equals("quit")) {
            mTaxi.stop();
        }else{
            try {
                mTaxi.recharge();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
