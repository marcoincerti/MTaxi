package Simulators;

import MQTT.MTaxi;
import Simulators.Measurement;
import Simulators.MeasurementsBuffer;
import Simulators.PM10Simulator;

import java.util.ArrayList;
import java.util.List;

public class PollutionSensor extends Thread{

    private MTaxi mTaxi;
    private MeasurementsBuffer sensorBuffer;
    private PM10Simulator simulator;
    private ArrayList<Measurement> droneBuffer;
    private final Object droneBufferLock;

    public PollutionSensor(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
        sensorBuffer = new MeasurementsBuffer();
        simulator = new PM10Simulator(sensorBuffer);
        droneBuffer = new ArrayList<>();
        droneBufferLock = new Object();
    }

    public void start() {
        simulator.start();
        try {
            while(true){
                synchronized (sensorBuffer.bufferLock) {
                    sensorBuffer.bufferLock.wait();
                }
                addMeasurement(computeAvg(sensorBuffer.readAllAndClean()));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Measurement computeAvg(List<Measurement> l){
        double avgValue = 0;
        String type = "";
        long timestamp = 0;
        for (Measurement m : l) {
            avgValue += m.getValue();
            type = m.getType();
            timestamp += m.getTimestamp();
        }
        return new Measurement("" + mTaxi.getId(), type, avgValue, timestamp);
    }

    private void addMeasurement(Measurement m) {
        synchronized (droneBufferLock) {
            droneBuffer.add(m);
        }
    }

    public ArrayList<Measurement> getDeliveryPollution() {
        ArrayList<Measurement> ret;
        synchronized (droneBufferLock) {
            ret = new ArrayList<>(droneBuffer);
            droneBuffer = new ArrayList<>();
        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "DRONE MEASUREMENTS BUFFER";
        for (Measurement m : droneBuffer) {
            ret += m + "\n";
        }
        return ret;
    }
}
