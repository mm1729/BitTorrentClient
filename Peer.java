public class Peer {
    private String id, ip;
    private int port;

    public Peer(String id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public int  getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public String getId() {
        return id;
    }

    public String toString() {
        return String.format("\nPeer:\tID:%s\tIP:%s\tPORT:%d\n", id, ip, port);
    }

}
