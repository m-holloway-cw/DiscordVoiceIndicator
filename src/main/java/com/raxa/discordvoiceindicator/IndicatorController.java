/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raxa.discordvoiceindicator;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class IndicatorController implements Initializable {

    @FXML
    Pane pane;
    public static Label indicator = new Label("DISCONNECTED");

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //set our label properties on load
        indicator.setStyle("-fx-background-color: blue; -fx-font-weight: bold; -fx-font-size: 30;");
        indicator.setTextFill(Color.web("white"));
        indicator.setAlignment(Pos.CENTER);
        indicator.setPrefSize(250, 250);
        pane.getChildren().add(indicator);
    }

    public void changeLabel(boolean mute) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (mute) {
                    String text = Main.c.muteText;
                    indicator.setText(text);
                    String color = Main.c.muteColor;
                    indicator.setStyle("-fx-background-color: " + color + ";");
                } else {
                    String text = Main.c.liveText;
                    indicator.setText(text);
                    String color = Main.c.liveColor;
                    indicator.setStyle("-fx-background-color: " + color + ";");
                }
            }
        });
    }

    public void leaveLabel() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                indicator.setText("DISCONNECTED");
                indicator.setStyle("-fx-background-color: blue;");
            }
        });
    }
}
