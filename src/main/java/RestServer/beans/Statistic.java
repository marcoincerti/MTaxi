package RestServer.beans;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Statistic {

    private double avgDelivery;
    private double avgKm;
    private double avgPollution;
    private double avgBattery;
    private long timestamp;

    public Statistic(){
        this.avgDelivery = 0;
        this.avgKm = 0;
        this.avgPollution = 0;
        this.avgBattery = 0;
        this.timestamp = 0;
    }

    public Statistic(double avgDelivery, double avgKm, double avgPollution, double avgBattery, long timestamp) {
        this.avgDelivery = avgDelivery;
        this.avgKm = avgKm;
        this.avgPollution = avgPollution;
        this.avgBattery = avgBattery;
        this.timestamp = timestamp;
    }

    public double getAvgDelivery() {
        return avgDelivery;
    }

    public void setAvgDelivery(double avgDelivery) {
        this.avgDelivery = avgDelivery;
    }

    public double getAvgKm() {
        return avgKm;
    }

    public void setAvgKm(double avgKm) {
        this.avgKm = avgKm;
    }

    public double getAvgPollution() {
        return avgPollution;
    }

    public void setAvgPollution(double avgPollution) {
        this.avgPollution = avgPollution;
    }

    public double getAvgBattery() {
        return avgBattery;
    }

    public void setAvgBattery(double avgBattery) {
        this.avgBattery = avgBattery;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
