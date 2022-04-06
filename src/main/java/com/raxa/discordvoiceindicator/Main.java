/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raxa.discordvoiceindicator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.mihosoft.scaledfx.ScalableContentPane;
import java.io.File;
import java.net.URI;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Raxa
 */
public class Main extends Application {

    static String TOKEN;
    static String USERNAME;
    static config c = new config("config.json");

    //fmxl stuff
    public static Stage stage = new Stage();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //set our config
        c.setConfig();
        //start up our discord listener bot
        startBot();
        //startfxml
        startFXML();
    }

    static DiscordWebsocket dW;

    public static void startBot() {
        Thread botT;
        try {
            botT = new Thread() {
                @Override
                public void run() {
                    //open websocket connection
                    try {
                        TOKEN = c.token;
                        dW = new DiscordWebsocket(
                                new URI("wss://gateway.discord.gg/?v=6&encoding=json"),
                                TOKEN);

                        if (!dW.connectWSS(false)) {
                            throw new Exception("Error when connecting to Twitch.");
                        } else {
                            //todo as reconnect options
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            botT.start();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void startFXML() {
        //create and show GUI
        ScreensController container = new ScreensController();
        container.loadScreen("indicator", "Indicator.fxml");
        container.setScreen("indicator");
        // TODO resizing scale options
        Group root = new Group();
        root.getChildren().addAll(container);
        ScalableContentPane scale = new ScalableContentPane();
        scale.setContent(root);
        Scene testScale = new Scene(scale, 300, 300);
        stage.setScene(testScale);
        stage.setTitle("Fer0cia Mute Indicator");
        stage.show();
        stage.setOnCloseRequest(e -> System.exit(0));
    }

    public static class config {

        String token;
        String username;
        String liveColor;
        String liveText;
        String muteColor;
        String muteText;
        String file;

        public config(String filename) {
            file = filename;
        }

        public void setConfig() {
            try {
                JsonNode config = new ObjectMapper().readTree(new File(file)).get("config");
                System.out.println(config.toPrettyString());
                username = config.get("username").asText();
                token = config.get("token").asText();
                liveColor = config.get("liveColor").asText();
                liveText = config.get("liveText").asText();
                muteColor = config.get("muteColor").asText();
                muteText = config.get("muteText").asText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
