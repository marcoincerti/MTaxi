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
        System.out.println("RIDE ASSIGNMENT RECEIVED: \n\t- ride id: " + request.getId());
        if (mTaxi.getBattery() < 30){
            responseObserver.onError(new Exception());
        } else {
            RideResponse response = mTaxi.deliver(request);
            responseObserver.onNext(response);
            if (response.getResidualBattery() < 30) {
                System.out.println("\nLOW BATTERY WARNING: going to the charge!");
                mTaxi.stop();
            }
        }
        responseObserver.onCompleted();
    }
}
