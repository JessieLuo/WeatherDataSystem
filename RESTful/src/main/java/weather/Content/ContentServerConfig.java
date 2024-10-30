package weather.Content;

public record ContentServerConfig(String serverHostname, int serverPort, String filePath) {
    public String getServerHostname() {
        return serverHostname;
    }

    public int getServerPort() {
        return serverPort;
    }

    public String getFilePath() {
        return filePath;
    }
}
