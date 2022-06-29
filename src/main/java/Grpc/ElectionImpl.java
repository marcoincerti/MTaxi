package Grpc;


import MQTT.MTaxi;
import com.mtaxi.grpc.MTaxisService.ElectionRequest;
import com.mtaxi.grpc.MTaxisService.ElectionResponse;
import com.mtaxi.grpc.ElectionGrpc.ElectionImplBase;
import io.grpc.stub.StreamObserver;

public class ElectionImpl extends ElectionImplBase {
    private final MTaxi mTaxi;

    public ElectionImpl(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
    }

    @Override
    public void elect(ElectionRequest request, StreamObserver<ElectionResponse> responseObserver) {

        System.out.println("\nElection message received:");
        System.out.println("\t- id: " + request.getId());
        System.out.println("\t- battery: " + request.getBattery());
        System.out.println("\t- elected: " + request.getElected());

        boolean master = false;
        // if this drone is being elected
        if (request.getElected() && request.getId() == mTaxi.getId()){
            System.out.println("\nELECTION FINISHED: I'm the new master");
            master  = true;
        } else {
            // if the requesting drone has less battery and I am participant I ignore the message
            if (mTaxi.isParticipant() && request.getBattery() < mTaxi.getBattery()){
                System.out.println("MULTIPLE ELECTIONS: ignoring the message " +
                        "by " + request.getId());
            } else {
                mTaxi.enterRing();
                mTaxi.forwardElection(buildResponse(request));
                // forward election
            }
        }

        if (master) {
            mTaxi.becomeMaster();
        }

        //System.out.println("ELECTION: sending response");
        responseObserver.onNext(ElectionResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    private ElectionRequest buildResponse(ElectionRequest request) {
        // if I receive the elected message simply forward it
        if (request.getElected()) {
            mTaxi.getMTAxisList().setNewMaster(request.getId());
            mTaxi.setParticipant(false);
            return request;
        }

        int chosenId, battery;
        boolean elected = false;

        if( request.getId() == mTaxi.getId() || mTaxi.isMaster()){
            chosenId = mTaxi.getId();
            battery = mTaxi.getBattery();
            elected = true;
        } else {
            // if the requester has higher battery or equal battery and higher id,
            // he wins against this drone
            if ((request.getBattery() > mTaxi.getBattery()) ||
                    (request.getBattery() == mTaxi.getBattery()
                            && request.getId() > mTaxi.getId())){
                chosenId = request.getId();
                battery = request.getBattery();
            } else {
                chosenId = mTaxi.getId();
                battery = mTaxi.getBattery();
            }
            mTaxi.setParticipant(true);
        }

        System.out.println("\nElection message to send: ");
        System.out.println("\t- id: " + chosenId);
        System.out.println("\t- battery: " + battery);
        System.out.println("\t- elected: " + elected);

        return ElectionRequest.newBuilder()
                .setId(chosenId)
                .setBattery(battery)
                .setElected(elected)
                .build();
    }
}

