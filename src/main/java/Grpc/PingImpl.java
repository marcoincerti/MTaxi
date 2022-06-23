package Grpc;

import MQTT.MTaxi;
import com.mtaxi.grpc.PingGrpc.PingImplBase;
import com.mtaxi.grpc.MTaxisService.PingRequest;
import com.mtaxi.grpc.MTaxisService.PingResponse;
import io.grpc.stub.StreamObserver;

public class PingImpl extends PingImplBase{
    private final MTaxi mTaxi;

    public PingImpl(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
    }

    @Override
    public void alive(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        //System.out.println("Ping received");
        PingResponse response = PingResponse.newBuilder().setIsMaster(mTaxi.isMaster()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}