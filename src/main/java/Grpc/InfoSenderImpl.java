package Grpc;

import MQTT.MTaxi;
import com.mtaxi.grpc.InfoSenderGrpc.InfoSenderImplBase;
import com.mtaxi.grpc.MTaxisService.SenderInfoRequest;
import com.mtaxi.grpc.MTaxisService.SenderInfoResponse;

import io.grpc.stub.StreamObserver;

public class InfoSenderImpl extends InfoSenderImplBase {
    private final MTaxi mTaxi;

    public InfoSenderImpl(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
    }

    @Override
    public void sendInfo(SenderInfoRequest request, StreamObserver<SenderInfoResponse> responseObserver) {
        System.out.println("GRPC Send info received from " + request.getId() );
        mTaxi.getMTAxisList().addNewMTaxi(request);

        SenderInfoResponse response = SenderInfoResponse.newBuilder()
                .setId(mTaxi.getId())
                .setIsMaster(mTaxi.isMaster())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
