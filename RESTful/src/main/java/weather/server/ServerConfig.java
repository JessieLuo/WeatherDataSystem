package weather.server;

public record ServerConfig(String host, int port, int backlog) {
}
