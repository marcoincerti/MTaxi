package MQTT;

import RestServer.beans.MTaxi;
import SETA.Ride;

import java.util.LinkedList;

public class RideQueue extends Thread{
    private final MTaxi mTaxi;
    private final LinkedList<Ride> rideQueue;
    private final LinkedList<RideAssignment> threadList;

    protected final Object queueLock;
    private final Object threadLock;

    private boolean exit = false;
    private final Object exitLock;

    public RideQueue(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
        this.rideQueue = new LinkedList<>();
        //this.threadList = new LinkedList<>();
        queueLock = new Object();
        threadLock = new Object();
        exitLock = new Object();
    }

    /*
    Queue consume
    */
    public Ride consume() throws InterruptedException {
        if(!getExit()) {
            boolean empty = true;

            while (empty) {
                synchronized (queueLock) {
                    queueLock.wait();
                    empty = rideQueue.isEmpty();
                }
            }
        }
        Ride r = rideQueue.getFirst();
        rideQueue.removeFirst();
        return r;
    }

    /*
    Re-add an ride to the top of the queue
    */
    public synchronized void retryRide(Ride r){
        synchronized (queueLock){
            rideQueue.addFirst(r);
            queueLock.notifyAll();
        }
    }

    /*
    Remove ride assignment thread from the thread list
     */
    public void removeThread(OrderAssignment t){
        synchronized (threadLock) {
            threadList.remove(t);
            t.interrupt();
            threadLock.notify();
        }
    }
}
