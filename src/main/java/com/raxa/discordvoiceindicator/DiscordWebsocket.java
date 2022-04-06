/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raxa.discordvoiceindicator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

/**
 *
 * @author Raxa
 */
public class DiscordWebsocket extends WebSocketClient {

    static String token;
    DiscordMessageHandler dMH = new DiscordMessageHandler();

    public DiscordWebsocket(URI uri, String tokenS) {
        super(uri, new Draft_6455());
        this.uri = uri;
        token = tokenS;
    }

    /*
     * Method that sets sockets and connects to Twitch.
     *
     * @param {boolean} reconnect
     */
    public boolean connectWSS(boolean reconnect) {
        try {

            // Get our context.
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // Init the context.
            sslContext.init(null, null, null);
            // Get a socket factory.
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            // Create the socket.
            Socket socket = sslSocketFactory.createSocket();
            // Set TCP no delay.
            socket.setTcpNoDelay(true);
            // Set the socket.
            this.setSocket(socket);
            // Connect.
            this.connect();
            return true;
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException ex) {
            System.err.println(ex.toString());
        }
        return false;
    }

    /**
     * Callback that is called when we open a connect to Twitch.
     *
     * @param {ServerHandshake} handshakedata
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        //System.out.println("opening websocket connection to discord");
        String data = "{\"token\":\"" + token + "\","
                + "\"properties\": {"
                + "\"$os\": \"linux\","
                + "\"$browser\": \"ferocia\","
                + "\"$device\": \"fer0ciabot\""
                + "}"
                + "}";
        this.send("{\"op\":2,\"d\":" + data + "}");
    }

    /**
     * Callback that is called when the connection with Twitch is lost.
     *
     * @param code
     * @param reason
     * @param remote
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("On Close: code: " + code + " reason: " + reason + " remote: " + remote);
        //ignore for now reconnect logic could cause recursion
        //notify server/client of issue and handle via ui restart button instead
    }

    /**
     * Callback that is called when we get an error from the socket.
     *
     * @param ex
     */
    @Override
    public void onError(Exception ex) {
        //System.out.println("Error: " + ex.getMessage());
        ex.printStackTrace();
    }

    /**
     * Callback that is called when we get a message from Twitch.
     *
     * @param message
     */
    @Override
    public void onMessage(String message) {
        try {
            //System.out.println("incoming message: " + message);
            JsonNode data = new ObjectMapper().readTree(message);
            parse(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parse(JsonNode data) {
        //parse the incoming json and switch depending on 'op' code
        //op codes we should receive:
        //0 - general event dispatc
        //7 - reconnect
        //9 - invalid session
        //10 - contains heartbeat interval on initial connection
        //11 - acknowledges our heartbeat was successful
        switch (data.get("op").asInt()) {
            case 0:
                //System.out.println("found an event");
                dMH.handleIncoming(data);
                return;
            case 7:
                //System.out.println("server sent reconnect notification");
                reconnect();
                return;
            case 9:
                //System.out.println("invalid session");
                return;
            case 10:
                startPings(data.get("d").get("heartbeat_interval").asInt());
                return;
            case 11:
                //DiscordMessageHandler.cacheMessage("d", "user", "Pong message", "", "");
            //System.out.println("server acknowledged successful ping");
        }
    }

    /*
     * Method that handles reconnecting with Twitch.
     */
    @Override
    public void reconnect() {
        //should call connect in DiscordBot file to initiate the reconnect
        super.connect();
    }

    public static HttpURLConnection createCon(URL webhook) {
        try {
            HttpURLConnection post = (HttpURLConnection) webhook.openConnection();
            post.setRequestMethod("POST");
            post.setDoOutput(true);
            post.setRequestProperty("Content-Type", "application/json");
            post.setRequestProperty("Authorization", "Bot " + token);
            post.setRequestProperty("User-Agent", "Mozilla/5.0");
            return post;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void startPings(int millis) {
        ThreadFactory tF = Executors.defaultThreadFactory();
        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor(tF);
        s.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (sendPing("ping")) {
                    // System.out.println("pinged");
                };
            }
        }, millis, millis, TimeUnit.MILLISECONDS);
    }

    public boolean sendPing(String message) {
        try {
            this.send("{\"op\":1,\"d\":null}");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
