package Grpc;

import MQTT.MTaxi;
import com.mtaxi.grpc.ElectionGrpc;
import com.mtaxi.grpc.MTaxisService;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.mtaxi.grpc.ElectionGrpc.ElectionStub;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class ElectClient extends Thread{
    private final MTaxi senderMTaxi;
    private MTaxi receiverMTaxi;
    private final MTaxisService.ElectionRequest request;

    public ElectClient (MTaxi senderMTaxi, MTaxisService.ElectionRequest request) {
        this.senderMTaxi = senderMTaxi;
        this.request = request;
    }

    /*
    Send the election request to the next, if he
    is dead retry to forward to the next one
     */

    @Override
    public synchronized void start() {
        senderMTaxi.enterRing();
        receiverMTaxi = senderMTaxi.getSuccessor();

        final ManagedChannel channel =
                ManagedChannelBuilder.forTarget(receiverMTaxi.getIp() + ":" + receiverMTaxi.getPort())
                        .usePlaintext().build();

        ElectionStub stub = ElectionGrpc.newStub(channel);


        Context newContext = Context.current().fork();
        Context origContext = newContext.attach();
        try{
            stub.elect(request, new StreamObserver<MTaxisService.ElectionResponse>() {
                @Override
                public void onNext(MTaxisService.ElectionResponse value) {
                }

                @Override
                public void onError(Throwable t) {
                    System.out.println("\nSuccessor is dead, forwarding to the next");
                    senderMTaxi.getMTAxisList().remove(receiverMTaxi);
                    senderMTaxi.forwardElection(request);
                    channel.shutdown();
                }

                @Override
                public void onCompleted() {
                    channel.shutdown();
                }
            });

            try {
                channel.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.out.println("Was not able to await termination in election");
                e.printStackTrace();
            }
        } finally {
            newContext.detach(origContext);
        }

    }
}
