package MQTT;

import RestServer.beans.MTaxi;
import SETA.Ride;

public class RideAssignment extends Thread{
    private final MTaxi mTaxi;
    private final Ride ride;
    private final RideQueue queue;

    public RideAssignment(MTaxi mTaxi, Ride ride, RideQueue queue) {
        this.mTaxi = mTaxi;
        this.ride = ride;
        this.queue = queue;
    }
}
