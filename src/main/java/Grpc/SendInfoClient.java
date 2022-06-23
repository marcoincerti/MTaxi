package Grpc;

import MQTT.MTaxi;
import com.mtaxi.grpc.MTaxisService.SenderInfoRequest;
import com.mtaxi.grpc.MTaxisService.Coordinates;
import com.mtaxi.grpc.MTaxisService.SenderInfoResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.mtaxi.grpc.InfoSenderGrpc;
import com.mtaxi.grpc.InfoSenderGrpc.InfoSenderStub;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.TimeUnit;

public class SendInfoClient extends Thread {
    private final MTaxi senderMTaxi;
    private final MTaxi receiverMTaxi;

    public SendInfoClient(MTaxi senderMTaxi, MTaxi receiverMTaxi) {
        this.senderMTaxi = senderMTaxi;
        this.receiverMTaxi = receiverMTaxi;
    }

    public void start(){
        // build channel pointing receiver drone
        //System.out.println("Creating stub " + receiverDrone.getIp() + ":" + receiverDrone.getPort());
        final ManagedChannel channel =
                ManagedChannelBuilder.forTarget(receiverMTaxi.getIp() + ":" + receiverMTaxi.getPort())
                        .usePlaintext().build();

        // create a non blocking stub
        InfoSenderStub stub = InfoSenderGrpc.newStub(channel);

        SenderInfoRequest req = SenderInfoRequest.newBuilder()
                .setId(senderMTaxi.getId())
                .setIp(senderMTaxi.getIp())
                .setPort(senderMTaxi.getPort())
                .setResidualBattery(senderMTaxi.getBattery())
                .setIsMaster(senderMTaxi.isMaster())
                .setPosition(
                        Coordinates.newBuilder()
                                .setX(senderMTaxi.getX())
                                .setY(senderMTaxi.getY())
                                .build()
                )
                .setAvailable(senderMTaxi.isAvailable())
                .build();

        stub.sendInfo(req, new StreamObserver<SenderInfoResponse>() {
            @Override
            public void onNext(SenderInfoResponse value) {
                /*

                System.out.println("DRONE RESPONSE");
                System.out.println(value.getId());
                System.out.println(value.getPosition().getX() + ", " + value.getPosition().getY());
                System.out.println(value.getResidualBattery());
                System.out.println(value.getIsMaster());
                 */
               // senderMTaxi.getMTAxisList().updateMasterDrone(value);
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("SEND INFO RESPONSE ERROR");
                channel.shutdown();
            }

            @Override
            public void onCompleted() {
                System.out.println("SEND INFO COMPLETED");
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

