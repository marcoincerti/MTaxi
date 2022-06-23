package MQTT;

import SETA.Ride;
import com.mtaxi.grpc.MTaxisService;
import java.util.ArrayList;

public class MTaxisList {
    protected MTaxi mTaxi;
    protected ArrayList<MTaxi> mTaxisList;

    public MTaxisList(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
        this.mTaxisList = new ArrayList<MTaxi>();
    }

    /*
    Create a thread for each drone and start requesting infos,
    it's done when a Drone becomes Master
    after receiving the response each thread proceeds to star the updateDrone
    procedure, to update the drone information in the droneslist
     */
//    public void requestMTaxisInfo() {
//        // list of threads to then stop them
//        ArrayList<GetInfoClient> threadList = new ArrayList<>();
//
//        int i = 0;
//        for ( Drone d : getDronesList() ) {
//            GetInfoClient c = new GetInfoClient(drone, d, i);
//            threadList.add(c);
//            c.start();
//            i++;
//        }
//
//        for ( GetInfoClient t : threadList) {
//            try {
//                t.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /*
    Create a thread for each drone in the list
    and start sending infos
    after receiving the response each thread proceeds to star the updateMaster
    to find out who's the master drone, it will come in handy in case
    the master fails
     */
//    public void sendDroneInfo(){
//        // list of threads to then stop them
//        ArrayList<SendInfoClient> threadList = new ArrayList<>();
//
//        for ( Drone d : getDronesList() ) {
//            SendInfoClient c = new SendInfoClient(drone, d);
//            threadList.add(c);
//            c.start();
//        }
//
//        for ( SendInfoClient t : threadList) {
//            try {
//                t.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        System.out.println(drone);
//    }

    /*
    Add a new Drone to the list
    If I am not the master, I set the coordinates to -1, -1, so
    during the order assignment I can stop
     */
    public synchronized void addNewMTaxi(MTaxisService.SenderInfoRequest value){
        mTaxisList.add(new MTaxi(
                value.getId(),
                value.getIp(),
                value.getPort(),
                ((mTaxi.isMaster())?
                        new int[]{value.getPosition().getX(), value.getPosition().getY()} :
                        new int[]{-1, -1}),
                value.getResidualBattery(),
                value.getIsMaster(),
                value.getAvailable()
        ));
    }

    /*
    Remove a drone from the list
    called when master get a response error
     */
    public synchronized void remove(MTaxi d){
        mTaxisList.remove(d);
    }


    /*
    Update drone info after a master request
     */
//    public void updateDrone(DroneService.InfoResponse value, int listIndex) {
//        // concurrent access to the drone list, need sync
//        ArrayList<Drone> copy = getDronesList();
//        Drone d = copy.get(listIndex);
//        d.coordinates[0] = value.getPosition().getX();
//        d.coordinates[1] = value.getPosition().getY();
//        d.battery = value.getResidualBattery();
//        d.isMaster = value.getIsMaster();
//        d.isAvailable = value.getAvailable();
//    }

    /*
    Called when sending info to others,
    it update the master
     */
//    public synchronized void updateMasterDrone(DroneService.SenderInfoResponse value){
//        int id = value.getId();
//        boolean isMaster = value.getIsMaster();
//        for ( Drone d : dronesList ) {
//            if (d.getId() == id)
//                d.isMaster = isMaster;
//        }
//    }


//    public synchronized void setNewMaster(int id){
//        System.out.println("Setting the new master: " + id);
//        for ( Drone d : dronesList ) {
//            if (d.getId() == id)
//                d.isMaster = true;
//        }
//
//    }

    /*
    Distance function to find closest drone
     */
    static Double distance(int[]v1, int[] v2){
        return Math.sqrt(
                Math.pow(v2[0] - v1[0], 2) +
                        Math.pow(v2[1] - v1[1], 2)
        );
    }

    /*
    Find the closest Drone,
    synchronized as multiple concurrent deliveries are possible
     */
    public synchronized MTaxi findClosest(Ride r) {

        MTaxi closest = null;
        Double dist = Double.MAX_VALUE;
        int maxBattery = 0;

        ArrayList<MTaxi> list = getmTaxisList();
        list.add(mTaxi);

        for ( MTaxi d : list ) {
            if(d.getX() == -1){
                return null;
            }
            Double currentDistance = distance(r.startCoordinates, d.getCoordinates());
            if ((d.isAvailable() && d.getBattery() > 15) && (closest == null || currentDistance.compareTo(dist) < 0 ||
                    (currentDistance.compareTo(dist) == 0 && d.getBattery() > maxBattery))) {
                dist = currentDistance;
                maxBattery = d.getBattery();
                closest = d;
            }
        }
        if (closest != null) {
            closest.setAvailable(false);
            if (closest != this.mTaxi)
                closest.decreaseBattery();
        }

        return closest;
    }

    /*
    Getter that returns a copy so I can unlock the list
     */
    public synchronized ArrayList<MTaxi> getmTaxisList() {
        return new ArrayList<MTaxi>(mTaxisList);
    }
}
