package weather.client;

import java.net.Socket;

public record ClientConfig(String serverHostname, int serverPort, Socket mockSocket) {
}
