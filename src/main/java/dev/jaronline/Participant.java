package dev.jaronline;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Participant {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Enter the ip address of the person you want to connect to:");
        Scanner scanner = new Scanner(System.in);
        String ipAddress = scanner.next();
        String port = "8080";
        String path = "join";

        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create("http://" + ipAddress + ":" + port + "/" + path))
                    .build();
            HttpResponse response = httpClient.send(request, new ParticipantHandler());
            System.out.println(response);
        }
    }

    static class ParticipantHandler implements HttpResponse.BodyHandler<String> {
        @Override
        public HttpResponse.BodySubscriber<String> apply(HttpResponse.ResponseInfo responseInfo) {
            HttpResponse.BodySubscriber<InputStream> upstream = HttpResponse.BodySubscribers.ofInputStream();

            return HttpResponse.BodySubscribers.mapping( //start pinging every few seconds
                    upstream,
                    (_) -> "Hey!"
            );
            // ping method
            // send ad
            // await the request
            // if it doesnt reply error
            // else send another ping after some time
        }
    }

    static class Pinger {
        private int nrOfPings = 0;
        boolean expectedPongIsHere;

        public void pingTen(int interval) throws IOException, InterruptedException {
            long start = System.currentTimeMillis();
            expectedPongIsHere = false;
            while (nrOfPings < 10) {
                java.util.concurrent.TimeUnit.SECONDS.sleep(1);
                //sendPing();
                if (expectedPongIsHere) {
                    java.util.concurrent.TimeUnit.SECONDS.sleep(interval);
                    nrOfPings++;
                    start = System.currentTimeMillis();
                } else if (!expectedPongIsHere && start > 10000) {
                    throw new IOException();
                }

                //expectPong
                //not = exception
                //else continue

                //pingTen
            }
        }
//        public boolean sendPing(){
//            try (HttpClient httpClient = HttpClient.newHttpClient()) {
//                // maak request
//                HttpRequest request = HttpRequest.newBuilder().POST();
//
//            }
//    }

    }
}