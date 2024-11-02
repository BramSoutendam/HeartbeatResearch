package dev.jaronline;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Host {
    //the pong
    private static final List<InetSocketAddress> addresses = new ArrayList<>();
    private static final Map<InetSocketAddress, Timer> pingTimers = new HashMap<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/join", new HostJoinHandler(addresses));
        server.setExecutor(null);
        server.start();
        System.out.println("Server has started on: " + server.getAddress());


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Pinging all addresses");
                for (InetSocketAddress address : addresses
                ) {
                    try {
                        sendPing(address);
                        checkPong(address);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 10000);

    }

    private static void checkPong(InetSocketAddress address) {
        // start new timer for address
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // if no pong is sent: remove the address from addresses
                addresses.remove(address);
                System.out.println("address" + address + "is removed");
            }
        }, 4000);
        pingTimers.put(address, timer);
    }

    public static void sendPing(InetSocketAddress address) throws IOException, InterruptedException {
        int port = 8081;
        String path = "ping";
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http:/" + address.getAddress() + ":" + port + "/" + path))
                    .build();
            HttpResponse<String> response = httpClient.send(request, new HostPingHandler(pingTimers));
            String message = response.body();
            System.out.println(message);
        }
    }

    static class HostPingHandler implements HttpResponse.BodyHandler<String> {
        private final Map<InetSocketAddress, Timer> pingTimers;

        public HostPingHandler(Map<InetSocketAddress, Timer> pingTimers) {
            this.pingTimers = pingTimers;
        }

        @Override
        public HttpResponse.BodySubscriber<String> apply(HttpResponse.ResponseInfo responseInfo) {
            HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();

            if (responseInfo.statusCode() != 200) {
                return HttpResponse.BodySubscribers.mapping( //start pinging every few seconds
                    upstream,
                    (_) -> "Unexpected response"
                );
            }

            // if pong is sent: remove timer
//            Timer timer = pingTimers.remove()

            return HttpResponse.BodySubscribers.mapping( //start pinging every few seconds
                    upstream,
                    (_) -> "Received a pong!"
            );
        }
    }

    static class HostJoinHandler implements HttpHandler {
        private final List<InetSocketAddress> addresses;

        public HostJoinHandler(List<InetSocketAddress> addresses) {
            this.addresses = addresses;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addresses.add(exchange.getLocalAddress());
            System.out.println(addresses);

            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
