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

    @XmlRootElement(name = "mTaxis")
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
}
