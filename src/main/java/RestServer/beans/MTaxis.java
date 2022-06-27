package RestServer.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.abs;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class MTaxis {

    @XmlElement(name="mtaxis")
    private ArrayList<MTaxi> mTaxisList;

    private static MTaxis instance;

    private MTaxis() {
        mTaxisList = new ArrayList<MTaxi>() ;
    }

    //singleton
    public synchronized static MTaxis getInstance(){
        if(instance==null)
            instance = new MTaxis();
        return instance;
    }

    public synchronized ArrayList<MTaxi> getmTaxisList() {
        return new ArrayList<MTaxi>(this.mTaxisList);
    }

    private int[] randomCoordinates() {
        Random rd = new Random();
        int x = abs(rd.nextInt()%10);
        int y = abs(rd.nextInt()%10);
        x = ((x  <= 4) ? 0 : 9);
        y = ((x  <= 4) ? 0 : 9);
        return new int[]{x, y};
    }

    public synchronized CoordMTaxiList add(MTaxi u){

        for ( MTaxi d : this.getmTaxisList() ) {
            if (u.getId() == d.getId())
                return null;
        }
        this.mTaxisList.add(u);
        return new CoordMTaxiList(getmTaxisList(), randomCoordinates());
    }

    public synchronized MTaxi getById(int id){
        for ( MTaxi d : this.getmTaxisList() ) {
            if ( id == d.getId())
                return d;
        }
        return null;
    }

    public synchronized MTaxi deleteById(int id) {
        for ( MTaxi d : this.mTaxisList ) {
            if (d.getId() == id) {
                this.mTaxisList.remove(d);
                return d;
            }
        }
        return null;
    }
}