package Simulators;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MeasurementsBuffer implements Buffer{

    static int bufferLenght = 8;
    static double overlap = 0.5;

    private LinkedList<Measurement> buffer;
    public final Object bufferLock;

    public MeasurementsBuffer() {
        buffer = new LinkedList<>();
        bufferLock = new Object();
    }

    @Override
    public void addMeasurement(Measurement m) {
        synchronized (bufferLock){
            buffer.add(m);
            //System.out.println("ADDED MEASUREMENT TO SLIDING WINDOW: " + m);
            if(buffer.size() == 8)
                bufferLock.notify();
        }
    }

    @Override
    public List<Measurement> readAllAndClean() {
        ArrayList<Measurement> l;
        synchronized (bufferLock){
            //System.out.println("CALLED READ ALL AND CLEAN");
            l = new ArrayList<>(buffer);
            for (int i = 0; i < bufferLenght * overlap; i++) {
                buffer.removeFirst();
            }
        }
        return l;
    }
}
