package RestServer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Scanner;


public class AdministratorClient {

    public static String restBaseAddressMTaxis = "http://localhost:1337/mtaxis/";
    public static String restBaseAddressStatistics = "http://localhost:1337/statistics/";
    public static Client client = Client.create();
    public static Scanner sc=new Scanner(System.in);

    static String commandList = "\nAvailable methods:\n\n" +
            "\t(1) Get mTaxis in the smartCity\n" +
            "\t(2) Get last N stats from the smartCity\n" +
            "\t(3) Get the average number of deliveries between two timestamps\n" +
            "\t(4) Get the average km between two timestamps\n" +
            "\t(5) Quit\n\n" +
            "Insert a command between 0 and 5: ";

    private static void getmTaxis(){
        WebResource webResource = client
                .resource(restBaseAddressMTaxis + "get");
        ClientResponse response = webResource.type("application/json")
                .get(ClientResponse.class);
        try {
            JSONArray r = new JSONArray(response.getEntity(String.class));
            System.out.println( (r.length() > 0)? "MTaxis in the smart city: \n" : "No mTaxis found\n");
            for (int i = 0; i < r.length(); i++) {
                JSONObject d = r.getJSONObject(i);
                System.out.println((i+1)+". mTaxi: " + "\n\t- id: "+ d.getInt("id")
                        + "\n\t- ip: " + d.getString("ip") + "\n\t- port: " + d.getInt("port") + '\n');
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

   private static void getNStats(){
        System.out.print("\nEnter the number of statistics you want: ");
        int n = sc.nextInt();
        WebResource webResource = client
                .resource(restBaseAddressStatistics + "get/" + n);
        ClientResponse response = webResource.type("application/json")
                .get(ClientResponse.class);
        try {
            JSONArray r = new JSONArray(response.getEntity(String.class));
            System.out.println( (r.length() > 0)? "LAST STATISTICS: \n" : "No statistics found\n");
            for (int i = 0; i < r.length(); i++) {
                JSONObject d = r.getJSONObject(i);
                System.out.println((i+1)+". statistic: "
                        + "\n\t- avgDelivery: "+ d.getDouble("avgDelivery")
                        + "\n\t- avgKm: " + d.getDouble("avgKm")
                        + "\n\t- avgPollution: " + d.getDouble("avgPollution")
                        + "\n\t- avgBattery: " + d.getDouble("avgBattery")
                        + "\n\t- timestamp: " + d.getLong("timestamp")
                        + '\n');
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void getAvgDeliveries(){
        System.out.print("\nEnter the start timestamp: ");
        long t1 = sc.nextLong();
        System.out.print("Enter the end timestamp: ");
        long t2 = sc.nextLong();
        WebResource webResource = client
                .resource(restBaseAddressStatistics + "get/delivery/" + t1 + "-" + t2);
        ClientResponse response = webResource.type("application/json")
                .get(ClientResponse.class);

        System.out.println("Average number of deliveries " +
                "between " + t1 + " and " + t2 + ": " +
                response.getEntity(String.class));
    }

    private static void getAvgkm(){
        System.out.print("\nEnter the start timestamp: ");
        long t1 = sc.nextLong();
        System.out.print("Enter the end timestamp: ");
        long t2 = sc.nextLong();
        WebResource webResource = client
                .resource(restBaseAddressStatistics + "get/km/" + t1 + "-" + t2);
        ClientResponse response = webResource.type("application/json")
                .get(ClientResponse.class);
        System.out.println("Average number of km " +
                "between " + t1 + " and " + t2 + ": " +
                response.getEntity(String.class));
    }


    public static void main(String[] args){
        System.out.println("==== Smart-city ADMIN CLIENT ====\n");
        int command = 0;
        boolean exit = false;
        while (!exit) {
            System.out.print(commandList);
            try{
                command = sc.nextInt();
                switch (command) {
                    case 1: getmTaxis(); break;
                    //case 2: getNStats(); break;
                    //case 3: getAvgDeliveries(); break;
                    //case 4: getAvgkm(); break;
                    case 5: exit = true; break;
                    default:
                        System.out.println("Please enter a valid command.");
                }
            } catch (Exception e){
                command = 0;
                System.out.println("Please enter a valid command.");
            }
        }
        sc.close();
        System.exit(0);
    }

}
