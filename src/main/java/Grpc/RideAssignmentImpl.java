package Grpc;

import MQTT.MTaxi;
import io.grpc.stub.StreamObserver;
import com.mtaxi.grpc.MTaxisService.RideRequest;
import com.mtaxi.grpc.MTaxisService.RideResponse;
import com.mtaxi.grpc.RideAssignmentGrpc.RideAssignmentImplBase;

public class RideAssignmentImpl extends RideAssignmentImplBase {
    private final MTaxi mTaxi;

    public RideAssignmentImpl(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
    }

    @Override
    public void assignRide(RideRequest request, StreamObserver<RideResponse> responseObserver) {
        System.out.println("ORDER ASSIGNMENT RECEIVED: \n\t- order id: " + request.getId());
        if (mTaxi.getBattery() < 15){
            responseObserver.onError(new Exception());
        } else {
            RideResponse response = mTaxi.deliver(request);
            responseObserver.onNext(response);
            if (response.getResidualBattery() < 15) {
                System.out.println("\nLOW BATTERY WARNING: exiting the network!");
                mTaxi.stop();
            }
        }
        responseObserver.onCompleted();
    }
}
