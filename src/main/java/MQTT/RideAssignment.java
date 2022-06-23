package MQTT;

import SETA.Ride;
import com.mtaxi.grpc.MTaxisService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class RideAssignment extends Thread{
    private final MTaxi mTaxi;
    private final Ride ride;
    private final RideQueue queue;

    public RideAssignment(MTaxi mTaxi, Ride ride, RideQueue queue) {
        this.mTaxi = mTaxi;
        this.ride = ride;
        this.queue = queue;
    }

    /*
    Send order to another Drone
     */
    public void sendOrder(MTaxi receiver){
        System.out.println("\nSENDING ORDER:\n\t- order id: " + ride.id + "\n\t- drone id: " + receiver.getId() + "\n");

        final ManagedChannel channel =
                ManagedChannelBuilder.forTarget(receiver.getIp() + ":" + receiver.getPort())
                        .usePlaintext().build();

        OrderAssignmentGrpc.OrderAssignmentStub stub = OrderAssignmentGrpc.newStub(channel);

        MTaxisService.OrderRequest req = MTaxisService.OrderRequest.newBuilder()
                .setId(ride.id)
                .setStart(
                        MTaxisService.Coordinates.newBuilder()
                                .setX(ride.endCoordinates[0])
                                .setY(ride.endCoordinates[1])
                                .build()
                )
                .setEnd(
                        MTaxisService.Coordinates.newBuilder()
                                .setX(ride.endCoordinates[0])
                                .setY(ride.endCoordinates[1])
                                .build()
                )
                .build();

        stub.assignOrder(req, new StreamObserver<MTaxisService.OrderResponse>() {
            @Override
            public void onNext(MTaxisService.OrderResponse value) {
                System.out.println("\nORDER COMPLETED:\n\t- order id: " + ride.id
                        + "\n\t- drone id: " + receiver.getId() + "\n");
                mTaxi.statisticsMonitor.addStatistic(value);
            }

            @Override
            public void onError(Throwable t) {
                mTaxi.getMTAxisList().remove(receiver);
                System.out.println("ORDER ASSIGNMENT ERROR, removing drone " + receiver.getId());
                queue.retryOrder(ride);
                channel.shutdown();
            }

            @Override
            public void onCompleted() {
                //System.out.println("Order assignment completed by drone " + receiver.id);
                receiver.setAvailable(true);
                channel.shutdown();
            }
        });
        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //System.out.println("WAS NOT ABLE TO AWAIT TERMINATION");
            e.printStackTrace();
        }

    }

    /*
    Try to find an available drone, if not available read
    the order to the queue
     */
    public void run() {
        //System.out.println("Order assignment");
        MTaxi closest = this.mTaxi.getMTAxisList().findClosest(ride);
        if (closest == null) {
            //System.out.println("No drones available");
            queue.retryOrder(ride);
        }else{
            //System.out.println("Closest drone: " + closest.id);
            sendOrder(closest);
        }
        queue.removeThread(this);
    }
}
