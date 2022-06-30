package MQTT;

import Grpc.ElectClient;
import Grpc.GrpcServer;
import Simulators.Measurement;
import Simulators.PollutionSensor;
import com.mtaxi.grpc.MTaxisService;

import java.sql.Timestamp;
import java.util.*;

public class MTaxi implements Comparable<MTaxi> {

    /*
    MTaxi fields and required locks
     */
    protected int id;
    protected String ip;
    protected int port;
    protected MTaxisList mTaxisList;
    protected RestMethods restMethods;

    protected int[] coordinates;
    private final Object coordinatesLock;

    protected int battery;
    private final Object batteryLock;

    protected boolean isAvailable;
    private final Object isAvailableLock;

    protected boolean isParticipant;
    private final Object participantLock;

    protected MTaxi successor;

    protected double totKm;
    private final Object totKmLock;

    protected int totDeliveries;
    private final Object totDeliveriesLock;

    protected boolean isQuitting;
    private final Object isQuittingLock;

    /*
    Master fields, and required locks
     */
    protected boolean isMaster;
    private final Object masterLock;

    private MQTTBroker mqttBroker;
    protected RideQueue rideQueue;
    protected StatisticsMonitor statisticsMonitor;

    /*
    GRPC server, ping, quit, print and pollution threads.
     */
    private GrpcServer grpcServer;
    private PingService pingService;
    private QuitMTaxi quitMTaxi;
    private PrintMTaxiInfo printMTaxiInfo;

    protected PollutionSensor pollutionSensor;

    public MTaxi(int id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        battery = 100;
        mTaxisList = new MTaxisList(this);
        coordinates = new int[2];
        restMethods = new RestMethods(this);
        isAvailable = true;
        isQuitting = false;
        successor = null;
        totKm = 0;
        totDeliveries = 0;
        batteryLock = new Object();
        coordinatesLock = new Object();
        isAvailableLock = new Object();
        participantLock = new Object();
        masterLock = new Object();
        totDeliveriesLock = new Object();
        totKmLock = new Object();
        isQuittingLock = new Object();
        isParticipant = false;
    }

    public MTaxi(int id, String ip, int port, int[] coordinates, int battery, boolean isMaster, boolean isAvailable) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.coordinates = coordinates;
        this.battery = battery;
        this.isMaster = isMaster;
        this.isAvailable = isAvailable;
        batteryLock = new Object();
        coordinatesLock = new Object();
        isAvailableLock = new Object();
        participantLock = new Object();
        masterLock = new Object();
        totDeliveriesLock = new Object();
        totKmLock = new Object();
        isQuittingLock = new Object();
    }

    /*
    Start function, the mtaxi initialize and send others
    it's info, if required it becomes master
     */
    public void run() {
        // make rest request
        if (!restMethods.initialize()) {
            System.out.println("An error occurred initializing drone with these specs " + getInfo());
            return;
        }

        // start quit service
        quitMTaxi = new QuitMTaxi(this);
        quitMTaxi.start();

        // start recharge service
        //rechargeMTaxi= new RechargeMTaxi(this);
        //rechargeMTaxi.start();

        // start grpc server to respond
        grpcServer = new GrpcServer(this);
        grpcServer.start();

        // send everyone my info
        mTaxisList.sendMTaxiInfo();


        // becomeMaster, it is a separate function
        // as one might become it later
        if (isMaster())
            becomeMaster();

        pingService = new PingService(this);
        pingService.start();

        printMTaxiInfo = new PrintMTaxiInfo(this);
        printMTaxiInfo.start();

        pollutionSensor = new PollutionSensor(this);
        pollutionSensor.start();
    }

    /*
    Calling this function a Drone becomes master, so it
    starts to monitor orders and manage the queue
     */
    public synchronized void becomeMaster() {
        setParticipant(false);
        setMaster(true);
        System.out.println("\nBECOMING THE NEW MASTER:");
        // request drones infos
        mTaxisList.requestMTaxisInfo();
        System.out.println("\t- Other mTaxis info requested");
        // start the order queue
        if (rideQueue == null) {
            rideQueue = new RideQueue(this);
            rideQueue.start();
            System.out.println("\t- Rider queue started");
        }
        if (mqttBroker == null) {
            mqttBroker = new MQTTBroker(this, rideQueue);
            // start the order monitor mqtt client
            mqttBroker.start();
            System.out.println("\t- MQTT client started\n\n");
        }
        if (statisticsMonitor == null) {
            statisticsMonitor = new StatisticsMonitor(this);
            statisticsMonitor.start();
        }
    }

    public void checkStatus() {
        setIsQuitting(true);
        System.out.println("\n\nCOMMAND RECEIVED:");

            /*
            Disconnect mqtt client to not receive new orders
             */
        if (isMaster()) {
            try {
                mqttBroker.disconnect();
            } catch (NullPointerException e) {
                System.out.println("Monitor rides was not initialized");
            }
        }
            /*
            Wait if there is an election in progress
             */
        while (isParticipant()) {
            //System.out.println("\t- Election in progress, can't quit now...");
            synchronized (participantLock) {

                try {
                    participantLock.wait(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

            /*
            A delivery is in progress, need to wait
             */
        while (!isAvailable()) {
            System.out.println("\t- Delivery in progress, can't quit now...");
            synchronized (isAvailableLock) {
                try {
                    isAvailableLock.wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (isMaster()) {
            // this make sure to run orderqueue until it's empty
            try {
                rideQueue.setExit(true);
                /*
                if orders are still in the queue, notifyAll, as
                there might be a produce that's stuck.
                Then wait on the queue, there will be a notify when all the
                current deliveries are finished
                 */
                if (!rideQueue.isEmpty()) {
                    System.out.println(rideQueue);
                    try {
                        synchronized (rideQueue.queueLock) {
                            rideQueue.queueLock.notifyAll();
                            rideQueue.queueLock.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("Ride queue was not initialized");
            }
            System.out.println("\t- All orders have been assigned\n" +
                    "\t- Sending statistics to the REST API...");
            try {
                synchronized (statisticsMonitor.statisticLock) {
                    statisticsMonitor.statisticLock.notify();
                }
                synchronized (statisticsMonitor.statisticLock) {
                    try {
                        statisticsMonitor.statisticLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("Statistic monitor was not initialized");
            }
            System.out.println("\t- STATS SENT");
        }
    }

    /*
    Called after a quit command, ti stops everything making sure
    an election nor a delivery is in progress, when a drone is
    master it also empty the order queue and send the stats to the REST API.
     */
    public void stop() {
        if (!isQuitting()) {
            checkStatus();

            grpcServer.interrupt();
            System.out.println("\t- GRPC server interrupted");
            restMethods.quit();
            System.out.println("\t- REST API delete sent");
            System.exit(0);
        } else {
            System.out.println("QUIT IS ALREADY IN PROGRESS");
        }
    }

    public void recharge() {
        if (!isQuitting()) {
            /*
            Wait if there is an election in progress
             */
            while (isParticipant()) {
                //System.out.println("\t- Election in progress, can't quit now...");
                synchronized (participantLock) {

                    try {
                        participantLock.wait(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            /*
            A delivery is in progress, need to wait
             */
            while (!isAvailable()) {
                System.out.println("\t- Delivery in progress, can't recharge now...");
                synchronized (isAvailableLock) {
                    try {
                        isAvailableLock.wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            setCoordinates(new int[]{0, 0});
            try {
                System.out.println("RECHARGING");
                Thread.sleep(5000);
                setBattery(100);
                System.out.println("FINISH RECHARGING");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setIsQuitting(false);
        } else {
            System.out.println("QUIT IS ALREADY IN PROGRESS");
        }
    }

    /*
    Enter the ring overlay network,
    The function computes the predecessor and successor,
    to simplify implementation it's called
    every time a drone enters the system
     */
    public void enterRing() {
        ArrayList<MTaxi> list = mTaxisList.getMTaxiList();
        list.add(this);
        Collections.sort(list);

        int i = list.indexOf(this);

        successor = (i == list.size() - 1) ? list.get(0) : list.get(i + 1);
    }

    public synchronized void forwardElection(MTaxisService.ElectionRequest electionRequest) {
        if (!isMaster()) {
            //System.out.println("Forwarding election");
            ElectClient c = new ElectClient(this, electionRequest);
            c.start();
        }
        //else {
        //System.out.println("Already master");
        //}
    }

    /*
    Start the election in case of a missed ping response
    by the master
     */
    public synchronized void startElection() {
        if (!isParticipant()) {
            setParticipant(true);
            forwardElection(MTaxisService.ElectionRequest.newBuilder()
                    .setId(getId())
                    .setBattery(getBattery())
                    .setElected(false)
                    .build());
        }
    }


    /*
    Delivery simulation, the Drone sleeps for 5 seconds,
    then it sends the delivery response,
     */
    public MTaxisService.RideResponse deliver(MTaxisService.RideRequest request) {
        setAvailable(false);
        int[] rideStartPosition = new int[]{request.getEnd().getX(), request.getEnd().getY()};
        int[] rideEndPosition = new int[]{request.getEnd().getX(), request.getEnd().getY()};
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        double deliveryKm = MTaxisList.distance(getCoordinates(), rideStartPosition) +
                MTaxisList.distance(rideStartPosition, rideEndPosition);

        decreaseBattery(deliveryKm);

        MTaxisService.RideResponse.Builder response = MTaxisService.RideResponse.newBuilder()
                .setId(getId())
                .setTimestamp(
                        new Timestamp(System.currentTimeMillis()).getTime()
                )
                .setNewPosition(
                        MTaxisService.Coordinates.newBuilder()
                                .setX(rideEndPosition[0])
                                .setY(rideEndPosition[1])
                                .build()
                )
                .setKm(deliveryKm)
                .setResidualBattery(getBattery());

        try {
            for (Measurement m : pollutionSensor.getDeliveryPollution()) {
                response.addMeasurements(MTaxisService.Measurement.newBuilder()
                        .setAvg(m.getValue()).build());
            }
        } catch (NullPointerException e) {
            response.addMeasurements(MTaxisService.Measurement.newBuilder()
                    .setAvg(0).build());
        }

        setCoordinates(rideEndPosition);
        incrementTotKm(deliveryKm);
        incrementTotDeliveries();

        System.out.println("\nDELIVERY COMPLETED: \n\t- New position: [" + rideEndPosition[0] + ", " + rideEndPosition[1] + "]");
        System.out.println("\t- Residual battery: " + getBattery() + "%\n");
        setAvailable(true);

        return response.build();
    }

    /*
    Used to sort the drone list in enter ring
     */
    @Override
    public int compareTo(MTaxi o) {
        return this.getId() - o.getId();
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    /*
    Synchronized methods as in delivery search
    the master access the fields concurrently
     */
    public int getBattery() {
        int ret;
        synchronized (batteryLock) {
            ret = battery;
        }
        return ret;
    }

    public void decreaseBattery(Double km) {
        synchronized (batteryLock) {
            battery -= km;
        }
    }
    public void setBattery(int bat) {
        synchronized (batteryLock) {
            battery = bat;
        }
    }

    public int[] getCoordinates() {
        int[] ret;
        synchronized (coordinatesLock) {
            ret = coordinates;
        }
        return ret;
    }

    public void setCoordinates(int[] cord) {
        synchronized (coordinatesLock) {
            coordinates = cord;
        }
    }

    public int getX() {
        int ret;
        synchronized (coordinatesLock) {
            ret = coordinates[0];
        }
        return ret;
    }

    public int getY() {
        int ret;
        synchronized (coordinatesLock) {
            ret = coordinates[1];
        }
        return ret;
    }

    public boolean isAvailable() {
        boolean ret;
        synchronized (isAvailableLock) {
            ret = (boolean) isAvailable;
        }
        return ret;
    }

    public void setAvailable(boolean b) {
        synchronized (isAvailableLock) {
            isAvailable = b;
        }
    }

    public void setParticipant(boolean b) {
        synchronized (participantLock) {
            isParticipant = b;
        }
    }

    public boolean isParticipant() {
        boolean ret;
        synchronized (participantLock) {
            ret = isParticipant;
        }
        return ret;
    }

    public boolean isMaster() {
        boolean ret;
        synchronized (masterLock) {
            ret = isMaster;
        }
        return ret;
    }

    public void setMaster(boolean b) {
        synchronized (masterLock) {
            isMaster = b;
        }
    }

    public MTaxisList getMTAxisList() {
        return mTaxisList;
    }

    public int getPort() {
        return port;
    }

    public void incrementTotKm(double d) {
        synchronized (totKmLock) {
            totKm += d;
        }
    }

    public double getTotKm() {
        double ret;
        synchronized (totKmLock) {
            ret = totKm;
        }
        return ret;
    }

    public void incrementTotDeliveries() {
        synchronized (totDeliveriesLock) {
            totDeliveries += 1;
        }
    }

    public int getTotDeliveries() {
        int ret;
        synchronized (totDeliveriesLock) {
            ret = totDeliveries;
        }
        return ret;
    }

    public boolean isQuitting() {
        boolean ret;
        synchronized (isQuittingLock) {
            ret = isQuitting;
        }
        return ret;
    }

    public void setIsQuitting(boolean b) {
        synchronized (isQuittingLock) {
            isQuitting = b;
        }
    }


    public String getInfo() {
        return (isMaster() ? "MASTER" : "WORKER") + "\n\t- Id: " + getId() +
                "\n\t- Address: " + getIp() + ":" + getPort();
    }

    public int getDistrict() {
        if (coordinates[0] <= 4) {
            if (coordinates[1] <= 4) {
                return 1;
            } else {
                return 4;
            }
        } else {
            if (coordinates[1] <= 4) {
                return 2;
            } else {
                return 3;
            }
        }
    }

    public String toString() {
        String ret = "\n********* MTAXI INFO **********\n\n" + getInfo();
        ret += "\n\t- Battery level: " + getBattery() + "%";
        ret += "\n\t- Total km: " + getTotKm();
        ret += "\n\t- Total deliveries: " + getTotDeliveries();

        return ret + "\n****************************\n";
    }

    public MTaxi getSuccessor() {
        return successor;
    }

    public static void main(String[] args) {

        Random rd = new Random();
        MTaxi t = new MTaxi(rd.nextInt(1000), "localhost", 10000 + rd.nextInt(30000));
        t.run();
    }
}
