package MQTT;

import RestServer.beans.Statistic;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RestMethods {
    MTaxi mTaxi;
    // rest api base
    public static String restBaseAddressMTaxis = "http://localhost:1337/mtaxis/";
    public static String restBaseAddressStatistics = "http://localhost:1337/statistics/";

    public RestMethods(MTaxi mTaxi) {
        this.mTaxi = mTaxi;
    }

    /*
    Make initial API request, initialize all the mtaxi fields
     */
    public boolean initialize() {
        System.out.println("REST API INITIALIZATION: ");
        try {
            Client client = Client.create();
            WebResource webResource = client
                    .resource(restBaseAddressMTaxis + "add");

            String payload = this.getInitializePostPayload();

            ClientResponse response = webResource.type("application/json")
                    .post(ClientResponse.class, payload);

            // if the id is not present in the system
            int status = response.getStatus();

            if (status == 200) {
                // no conflict, unpack the response and go on
                if (unpackInitializeResponse(response.getEntity(String.class))) {
                    System.out.println("\t- MTaxi " + mTaxi.id + " initialization completed");
                    return true;
                }
            } else if (status == 409) {
                // if rest api gives a conflict response
                System.out.println("\t- The given ID " + mTaxi.id + " is already in the system, retry.");
            } else {
                // unhandled
                System.out.println("\t- Unhandled case: response.getStatus() = " + status);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
    Get the request payload to initialize
     */
    private String getInitializePostPayload() throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("id", mTaxi.id);
        payload.put("ip", mTaxi.ip);
        payload.put("port", mTaxi.port);
        return payload.toString();
    }

    /*
    Unpack the initialize respose,
    update the mtaxi list
     */
    private boolean unpackInitializeResponse(String response) {

        JSONObject input = null;
        try {
            input = new JSONObject(response);
            // unpack coordinates
            JSONArray coordinates = input.getJSONArray("coordinates");
            for (int i = 0; i < 2; i++)
                mTaxi.coordinates[i] = coordinates.getInt(i);

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        /*
        unpack mtaxi list
        first request gives out a json object and not a json array,
        as only one mtaxi is in the system, i.e. the mtaxi becomes the master
         */
        try {
            JSONArray list = input.getJSONArray("mtaxisList");
            for (int i = 0; i < list.length(); i++) {
                JSONObject current = list.getJSONObject(i);
                int id = current.getInt("id");
                String ip = current.getString("ip");
                int port = current.getInt("port");
                if (id != mTaxi.id) {
                    MTaxi d = new MTaxi(id, ip, port);
                    mTaxi.mTaxisList.mTaxisList.add(d);
                }
            }
            mTaxi.isMaster = false;
        } catch (JSONException e) {
            mTaxi.isMaster = true;
        }

        return true;
    }

    private String getStatisticPayload(Statistic s) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("avgKm", s.getAvgKm());
        payload.put("avgDelivery", s.getAvgDelivery());
        payload.put("avgBattery", s.getAvgBattery());
        payload.put("timestamp", s.getTimestamp());
        payload.put("avgPollution", s.getAvgPollution());
        return payload.toString();
    }

    public void sendStatistic(Statistic s){
        try {
            Client client = Client.create();
            WebResource webResource = client
                    .resource(restBaseAddressStatistics + "add");

            String payload = this.getStatisticPayload(s);

            ClientResponse response = webResource.type("application/json")
                    .post(ClientResponse.class, payload);

            /*
            // if the id is not present in the system
            int status = response.getStatus();


            if (status == 200) {
                System.out.println("STATISTIC SENT TO THE REST API");
                System.out.println(payload);
            } else {
                System.out.println("ERROR SENDING STATISTIC: status code " + status);
            }
             */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Send quit request to the API
     */
    public void quit() {
        System.out.println("Quitting mTaxi " + mTaxi.id);
        try {
            Client client = Client.create();
            // calling a DELETE host/remove/id removes the mtaxi with the given id
            WebResource webResource = client
                    .resource(restBaseAddressMTaxis + "remove/" + mTaxi.id);

            ClientResponse response = webResource.type("application/json")
                    .delete(ClientResponse.class);

            // if the id is not present in the system
            int status = response.getStatus();

            if (status == 200) {
                // id found
                System.out.println("MTaxi " + mTaxi.id + " removed from REST api");
            } else if (status == 404) {
                // if rest api gives a conflict response
                System.out.println("MTaxi " + mTaxi.id + " was not found on rest api");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // stop threads and quit
    }
}
