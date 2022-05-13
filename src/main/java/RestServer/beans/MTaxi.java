package RestServer.beans;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class MTaxi {

    private int id;
    private String ip;
    private int port;

    public MTaxi() {}

    public MTaxi(int id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "MTaxi:\n" +
                "\n\t-id: " + id +
                "\n\t-ip: " + ip +
                "\n\t-port: " + port;
    }
}
