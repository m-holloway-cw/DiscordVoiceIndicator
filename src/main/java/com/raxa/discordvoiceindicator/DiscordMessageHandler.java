/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.raxa.discordvoiceindicator;

import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author Raxa
 */
public class DiscordMessageHandler {

    IndicatorController ic = new IndicatorController();

    public DiscordMessageHandler() {

    }

    public void handleIncoming(JsonNode data) {
        String update = data.get("t").asText();

        if (update.equals("VOICE_STATE_UPDATE")) {
            String username = data.get("d").get("member").get("user").get("username").asText();
            String disc = data.get("d").get("member").get("user").get("discriminator").asText();
            String fullUser = (username) + ("#") + (disc);
            if ((fullUser).equalsIgnoreCase(Main.c.username)) {
                //check channelid is null (null means we're leaving voice, set mic to not connected)
                String channelID = data.get("d").get("channel_id").asText();
                if (channelID.equals("null")) {
                    ic.leaveLabel();
                } else {
                    //find and use mute value to set ui
                    boolean isMute = data.get("d").get("self_mute").asBoolean();
                    if (isMute) {
                        System.out.println("turning mute");
                        ic.changeLabel(isMute);
                    } else if (!isMute) {
                        System.out.println("turning unmuted");
                        ic.changeLabel(isMute);
                    } else {
                        //ignore
                    }
                }
            }
        }
    }
}
