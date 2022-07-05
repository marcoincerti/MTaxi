package Grpc;

import MQTT.MTaxi;
import com.mtaxi.grpc.InfoGetterGrpc;
import com.mtaxi.grpc.InfoGetterGrpc.InfoGetterStub;
import com.mtaxi.grpc.MTaxisService.InfoRequest;
import com.mtaxi.grpc.MTaxisService.InfoResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class GetInfoClient extends Thread {
    private final MTaxi senderMTaxi;
    private final MTaxi receiverMTaxi;
    private final int listIndex;

    public GetInfoClient(MTaxi senderMTaxi, MTaxi receiverMTaxi, int listIndex) {
        this.senderMTaxi = senderMTaxi;
        this.receiverMTaxi = receiverMTaxi;
        this.listIndex = listIndex;
    }

    public void start() {
        // build channel pointing receiver mtaxi
        //System.out.println("Creating stub " + receiverMTaxi.getIp() + ":" + receiverMTaxi.getPort());
        final ManagedChannel channel =
                ManagedChannelBuilder.forTarget(receiverMTaxi.getIp() + ":" + receiverMTaxi.getPort())
                        .usePlaintext().build();

        // create a non blocking stub
        InfoGetterStub stub = InfoGetterGrpc.newStub(channel);

        InfoRequest req = InfoRequest.newBuilder()
                .setId(senderMTaxi.getId()).build();

        stub.getInfo(req, new StreamObserver<InfoResponse>() {
            //man mano che gli arrivano le risposte aggiorna i taxi nella sua lista
            @Override
            public void onNext(InfoResponse value) {
                senderMTaxi.getMTAxisList().updateMTaxi(value, listIndex);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("GET INFO ERROR, removing mtaxi " + receiverMTaxi.getId());
                senderMTaxi.getMTAxisList().remove(receiverMTaxi);
                channel.shutdown();
            }

            @Override
            public void onCompleted() {
                //System.out.println("GET INFO COMPLETED");
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

}
