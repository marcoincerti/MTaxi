package RestServer.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Statistics {
    @XmlElement(name="statistics")
    private ArrayList<Statistic> statsList;

    private static Statistics instance;

    private Statistics() { statsList = new ArrayList<Statistic>(); }

    public synchronized static Statistics getInstance(){
        if(instance==null)
            instance = new Statistics();
        return instance;
    }

    public synchronized ArrayList<Statistic> getStatsList() {
        return new ArrayList<Statistic>(this.statsList);
    }

    public synchronized void add(Statistic s){
        statsList.add(s);
    }

    public synchronized ArrayList<Statistic> getLastStats(int n){
        ArrayList<Statistic> last = getStatsList();
        int end = last.size();
        int start = Math.max(end - n, 0);
        return new ArrayList<>(last.subList(start, end));
    }

    public synchronized double avgDelivery(long t1, long t2){
        ArrayList<Statistic> stats = getStatsList();

        double sum = 0;
        int tot = 0;
        for (int i = 0; stats.get(i).getTimestamp() < t2; i++) {
            Statistic s = stats.get(i);
            if (s.getTimestamp() > t1) {
                sum += 1;
                tot += s.getAvgDelivery();
            }
        }
        return (tot > 0)? sum / tot : 0;
    }

    public synchronized double avgKm(long t1, long t2){
        ArrayList<Statistic> stats = getStatsList();

        double sum = 0;
        int tot = 0;
        for (int i = 0; stats.get(i).getTimestamp() < t2; i++) {
            Statistic s = stats.get(i);
            if (s.getTimestamp() > t1) {
                sum += 1;
                tot += s.getAvgKm();
            }
        }
        return (tot > 0)? sum / tot : 0;
    }

}

