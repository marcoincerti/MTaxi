package MQTT;

import RestServer.beans.Statistic;
import Simulators.Measurement;
import com.mtaxi.grpc.MTaxisService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class StatisticsMonitor extends Thread{
    private MTaxi mTaxi;
    private HashMap<Integer, Integer> deliveries;
    private HashMap<Integer, Integer> batteries;
    private ArrayList<Double> kmList;
    private ArrayList<Measurement> pollutionList;

    protected final Object statisticLock;

    public StatisticsMonitor(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
        deliveries = new HashMap<Integer, Integer>();
        batteries = new HashMap<Integer, Integer>();
        kmList = new ArrayList<Double>();
        pollutionList = new ArrayList<Measurement>();
        statisticLock = new Object();
    }

    /*
        Add a statistic to then compute the average,
        the method is synchronized as it requires to lock everything
         */
    public synchronized void addStatistic(MTaxisService.RideResponse s){
        deliveries.put(s.getId(), (deliveries.getOrDefault(s.getId(), 0)) + 1);
        batteries.put(s.getId(), s.getResidualBattery());
        kmList.add(s.getKm());
        for ( MTaxisService.Measurement m : s.getMeasurementsList() )
            pollutionList.add(new Measurement("", "", m.getAvg(), 0));

    }

    /*
    Get the payload for the rest api call and clean all the lists and maps.
    The method is synchronized as it requires to lock everything
     */
    public synchronized Statistic getStatisticAndClean() {

        int totDeliveries = 0;
        for ( Integer i : deliveries.values() )
            totDeliveries += i;

        int totBatteries = 0;
        for ( Integer i : batteries.values() )
            totBatteries += i;

        double totKm = 0;
        for ( Double i : kmList )
            totKm += i;

        double totPollution = 0;
        for ( Measurement m : pollutionList )
            totPollution += m.getValue();

        int deliveringMTaxis = deliveries.size();


        Statistic ret;

        if (deliveringMTaxis == 0) {
            ret = new Statistic(
                    0, 0, 0, 0,
                    new Timestamp(System.currentTimeMillis()).getTime()
            );
        } else {
            ret = new Statistic(
                    (double) totDeliveries / (double) deliveringMTaxis,
                    totKm / deliveringMTaxis,
                    totPollution / pollutionList.size(),
                    (double) totBatteries / (double) deliveringMTaxis,
                    new Timestamp(System.currentTimeMillis()).getTime()
            );
        }

        deliveries = new HashMap<Integer, Integer>();
        batteries = new HashMap<Integer, Integer>();
        kmList = new ArrayList<Double>();
        pollutionList = new ArrayList<Measurement>();

        return ret;
    }

    public void start() {

        while (true) {
            synchronized (statisticLock) {
                try {
                    statisticLock.wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Statistic ret = getStatisticAndClean();
                if (ret.getAvgDelivery() > 0)
                    mTaxi.restMethods.sendStatistic(ret);
                statisticLock.notify();
            }
        }
    }
}
