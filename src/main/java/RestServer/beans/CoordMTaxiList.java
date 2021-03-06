package RestServer.beans;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;

//Class for coordinates and taxi
@XmlRootElement
public class CoordMTaxiList {
    private ArrayList<MTaxi> mTaxisList;
    private int[] coordinates;

    public CoordMTaxiList() {
    }

    public CoordMTaxiList(ArrayList<MTaxi> mTaxisList, int[] coordinates) {
        this.mTaxisList = mTaxisList;
        this.coordinates = coordinates;
    }

    public ArrayList<MTaxi> getmTaxisList() {return mTaxisList;}

    public void setmTaxisList(ArrayList<MTaxi> mTaxisList) {
        this.mTaxisList = mTaxisList;
    }

    public int[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(int[] coordinates) {
        this.coordinates = coordinates;
    }
}
