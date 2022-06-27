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
    private ArrayList<Measurement> mTaxiBuffer;
    private final Object mTaxiBufferLock;

    public PollutionSensor(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
        sensorBuffer = new MeasurementsBuffer();
        simulator = new PM10Simulator(sensorBuffer);
        mTaxiBuffer = new ArrayList<>();
        mTaxiBufferLock = new Object();
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
        synchronized (mTaxiBufferLock) {
            mTaxiBuffer.add(m);
        }
    }

    public ArrayList<Measurement> getDeliveryPollution() {
        ArrayList<Measurement> ret;
        synchronized (mTaxiBufferLock) {
            ret = new ArrayList<>(mTaxiBuffer);
            mTaxiBuffer = new ArrayList<>();
        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "MTAXI MEASUREMENTS BUFFER";
        for (Measurement m : mTaxiBuffer) {
            ret += m + "\n";
        }
        return ret;
    }
}
