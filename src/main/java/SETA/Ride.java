package SETA;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Ride {
    public int id;
    public int[] startCoordinates;
    public int[] endCoordinates;

    public Ride(int id, int[] startCoordinates, int[] endCoordinates) {
        this.id = id;
        this.startCoordinates = startCoordinates;
        this.endCoordinates = endCoordinates;
    }

    public String getJson() throws JSONException {
        JSONObject ride = new JSONObject();
        ride.put("id", this.id);

        JSONObject startCoordinates = new JSONObject();
        startCoordinates.put("x", this.startCoordinates[0]);
        startCoordinates.put("y", this.startCoordinates[1]);
        ride.put("startCoordinates", startCoordinates);

        JSONObject endCoordinates = new JSONObject();
        endCoordinates.put("x", this.endCoordinates[0]);
        endCoordinates.put("y", this.endCoordinates[1]);
        ride.put("endCoordinates", endCoordinates);

        return ride.toString();
    }

    public static Ride unpackJson(String json) {

        JSONObject order = null;
        try {
            order = new JSONObject(json);

            JSONObject startCoordinates = order.getJSONObject("startCoordinates");
            JSONObject endCoordinates = order.getJSONObject("endCoordinates");

            return new Ride(
                    order.getInt("id"),
                    new int[]{
                            startCoordinates.getInt("x"),
                            startCoordinates.getInt("y")
                    },
                    new int[]{
                            endCoordinates.getInt("x"),
                            endCoordinates.getInt("y")
                    });

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new Ride(-1, new int[]{}, new int[]{});
    }
}
