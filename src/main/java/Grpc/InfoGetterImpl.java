package Grpc;

import MQTT.MTaxi;
import com.mtaxi.grpc.InfoGetterGrpc;
import com.mtaxi.grpc.MTaxisService.InfoRequest;
import com.mtaxi.grpc.MTaxisService.InfoResponse;
import com.mtaxi.grpc.MTaxisService.Coordinates;
import io.grpc.stub.StreamObserver;

public class InfoGetterImpl extends InfoGetterGrpc.InfoGetterImplBase {
    private final MTaxi mTaxi;

    public InfoGetterImpl(MTaxi mTaxi) { this.mTaxi = mTaxi; }

    @Override
    public void getInfo(InfoRequest request, StreamObserver<InfoResponse> responseObserver) {

        Coordinates cord = Coordinates.newBuilder()
                .setX(mTaxi.getX())
                .setY(mTaxi.getY())
                .build();

        InfoResponse response = InfoResponse.newBuilder()
                .setId(mTaxi.getId())
                .setResidualBattery(mTaxi.getBattery())
                .setPosition(cord)
                .setIsMaster(mTaxi.isMaster())
                .setAvailable(mTaxi.isAvailable())
                .build();

        mTaxi.setParticipant(false);
        mTaxi.getMTAxisList().setNewMaster(request.getId());
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
