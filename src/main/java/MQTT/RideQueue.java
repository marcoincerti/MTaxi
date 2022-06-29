package MQTT;

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
        this.threadList = new LinkedList<>();
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
    Re-add an order to the top of the queue
     */
    public synchronized void retryRide(Ride r){
        synchronized (queueLock){
            rideQueue.addLast(r);
            queueLock.notifyAll();
        }
    }

    /*
    Add a new order to the queue, this will notify the consumer
     */
    public void produce(Ride r){
        synchronized (queueLock){
            rideQueue.add(r);
            queueLock.notify();
        }
    }

    /*
    Remove delivery assignment thread from the thread list
     */
    public void removeThread(RideAssignment t){
        synchronized (threadLock) {
            threadList.remove(t);
            t.interrupt();
            threadLock.notify();
        }
    }

    /*
    Add a thread to the thread list and start it
     */
    public void addThread(RideAssignment t){
        synchronized (threadLock){
            threadList.add(t);
            t.start();
        }
    }

    /*
    Getter and setter to manage quit command received at master
     */
    public boolean getExit() {
        boolean ret;
        synchronized (exitLock){
            ret = exit;
        }
        return ret;
    }

    public void setExit(boolean b) {
        synchronized (exitLock){
            exit = b;
        }
    }

    public boolean isEmpty(){
        boolean ret;
        synchronized (queueLock) {
            ret = rideQueue.isEmpty();
        }
        return ret;
    }

    public void run() {
        try {
            // if I set exit, when the queue is empty I quit
            while (!getExit() || !isEmpty()) {
                Ride next = consume();
                addThread(new RideAssignment(mTaxi, next, this));
            }
            /*
            A notify is called when a thread is removed from the list,
            if the thread list is empty I can quit
             */
            synchronized (threadLock){
                while(!threadList.isEmpty()){
                    threadLock.wait();
                }
            }
            /*
            This will wake up the master waiting in the stop method()
            He can then proceed to quit
             */
            synchronized (queueLock) {
                queueLock.notifyAll();
            }
        } catch (InterruptedException e){
            System.out.println("Interrupted received at ride queue");
        }
    }

    public String toString(){
        String ret = "\nRides left to be assigned:";
        synchronized (rideQueue) {
            for (Ride r : rideQueue) {
                ret += "\n\t- " + r.id;
            }
        }
        return ret + "\n\n";
    }
}