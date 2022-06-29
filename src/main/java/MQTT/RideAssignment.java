package MQTT;

import SETA.Ride;
import com.mtaxi.grpc.MTaxisService;
import com.mtaxi.grpc.RideAssignmentGrpc;
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
    public void sendRide(MTaxi receiver){
        System.out.println("\nSENDING RIDE:\n\t- ride id: " + ride.id + "\n\t- mTaxi id: " + receiver.getId() + "\n");

        final ManagedChannel channel =
                ManagedChannelBuilder.forTarget(receiver.getIp() + ":" + receiver.getPort())
                        .usePlaintext().build();

        RideAssignmentGrpc.RideAssignmentStub stub = RideAssignmentGrpc.newStub(channel);

        MTaxisService.RideRequest req = MTaxisService.RideRequest.newBuilder()
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

        stub.assignRide(req, new StreamObserver<MTaxisService.RideResponse>() {
            @Override
            public void onNext(MTaxisService.RideResponse value) {
                System.out.println("\nRIDE COMPLETED:\n\t- ride id: " + ride.id
                        + "\n\t- mTaxi id: " + receiver.getId() + "\n");
                mTaxi.statisticsMonitor.addStatistic(value);
            }

            @Override
            public void onError(Throwable t) {
                mTaxi.getMTAxisList().remove(receiver);
                System.out.println("RIDE ASSIGNMENT ERROR, removing Mtaxi " + receiver.getId());
                queue.retryRide(ride);
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
            //System.out.println("No mtaxis available in the district");
            queue.retryRide(ride);
        }else{
            //System.out.println("Closest drone: " + closest.id);
            sendRide(closest);
        }
        queue.removeThread(this);
    }
}
