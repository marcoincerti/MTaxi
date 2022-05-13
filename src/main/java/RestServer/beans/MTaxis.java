package RestServer.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Random;

import static java.lang.Math.abs;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)

public class MTaxis {

    //@XmlRootElement(name = "mTaxis")
    private ArrayList<MTaxi> mTaxisList;

    private static MTaxis instance;

    private MTaxis() {
        mTaxisList = new ArrayList<MTaxi>();
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
        return new int[]{x, y};
    }

    public synchronized CoordMTaxiList add(MTaxi u){

        for ( MTaxi t : this.getmTaxisList() ) {
            if (u.getId() == t.getId())
                return null;
        }
        this.mTaxisList.add(u);
        return new CoordMTaxiList(getmTaxisList(), randomCoordinates());
    }

    public synchronized MTaxi getById(int id){
        for ( MTaxi t : this.getmTaxisList() ) {
            if ( id == t.getId())
                return t;
        }
        return null;
    }

    public synchronized MTaxi deleteById(int id) {
        for ( MTaxi t : this.mTaxisList ) {
            if (t.getId() == id) {
                this.mTaxisList.remove(t);
                return t;
            }
        }
        return null;
    }
}
