package Grpc;

import MQTT.MTaxi;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.mtaxi.grpc.MTaxisService;
import com.mtaxi.grpc.PingGrpc;
import com.mtaxi.grpc.PingGrpc.PingStub;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class AliveClient extends Thread{
    private final MTaxi senderMTaxi;
    private final MTaxi receiverMTaxi;

    public AliveClient(MTaxi senderMTaxi, MTaxi receiverMTaxi) {
        this.senderMTaxi = senderMTaxi;
        this.receiverMTaxi = receiverMTaxi;
    }
    /*
        Send a ping to the receiver mtaxi,
        if an error occurs the receiver mtaxi
        is remove from the list, also, if he was the
        master an election is started
         */
    public void start() {
        //System.out.println("Ping service started to mtaxi: " + receiverMTaxi.getId());
        final ManagedChannel channel =
                ManagedChannelBuilder.forTarget(receiverMTaxi.getIp() + ":" + receiverMTaxi.getPort())
                        .usePlaintext().build();

        PingStub stub = PingGrpc.newStub(channel);

        MTaxisService.PingRequest req = MTaxisService.PingRequest.newBuilder().build();

        stub.alive(req, new StreamObserver<MTaxisService.PingResponse>() {
            @Override
            public void onNext(MTaxisService.PingResponse value) {
                //System.out.println("Ping Response received from mtaxi " + receiverMTaxi.getId());
            }

            @Override
            public void onError(Throwable t) {
                channel.shutdown();
                //System.out.println("PING ERROR: mtaxi " + receiverMtaxi.getId() + " is offline");
                senderMTaxi.getMTAxisList().remove(receiverMTaxi);

                if (receiverMTaxi.isMaster()) {
                    //System.out.println("MASTER DOWN: starting election");
                    senderMTaxi.startElection();
                }
            }

            @Override
            public void onCompleted() {
                channel.shutdown();
            }
        });

        try {
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("WAS NOT ABLE TO AWAIT TERMINATION");
            //e.printStackTrace();
        }
    }
}
