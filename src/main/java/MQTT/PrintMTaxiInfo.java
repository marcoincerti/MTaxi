package MQTT;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PrintMTaxiInfo extends Thread{
    MTaxi mTaxi;

    public PrintMTaxiInfo(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
    }

    public void start() {
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                System.out.println(mTaxi);
            };
        };
        t.scheduleAtFixedRate(tt,new Date(),10000);
    }
}
