package MQTT;

import Grpc.AliveClient;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PingService extends Thread{
    MTaxi mTaxi;

    public PingService(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
    }

    public void start() {
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                if(!mTaxi.isMaster()) {
                    AliveClient t = null;
                    for (MTaxi d : mTaxi.getMTAxisList().getMTaxiList()) {
                        if (d.isMaster()) {
                            t = new AliveClient(mTaxi, d);
                            t.start();
                            break;
                        }
                    }
                    if (t != null) {
                        try {
                            t.join();
                        } catch (InterruptedException e) {
                            System.out.println("Interrupted exception in join");
                        }
                    }
                }
            };
        };
        t.scheduleAtFixedRate(tt,new Date(),2000);
    }
}
