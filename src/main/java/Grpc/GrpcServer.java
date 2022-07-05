package Grpc;

import MQTT.MTaxi;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer extends Thread{
    private MTaxi mTaxi;
    private Server server;

    public GrpcServer(MTaxi mTaxi) { this.mTaxi = mTaxi; }

    public void run(){
        server = ServerBuilder.forPort(mTaxi.getPort())
                .addService(new InfoGetterImpl(mTaxi))
                .addService(new InfoSenderImpl(mTaxi))
                .addService(new RideAssignmentImpl(mTaxi))
                .addService(new PingImpl(mTaxi))
                .addService(new ElectionImpl(mTaxi))
                .build();
        try {
            server.start();
            System.out.println("GRPC server started");
        } catch (IOException e) {
            System.out.println("ERROR WHILE STARTING GRPC SERVER");
            e.printStackTrace();
        }

        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            server.shutdown();
            System.out.println("GRPC server stopped ");
        }
    }

}
