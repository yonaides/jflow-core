/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jflow.jflowcore;

import com.jflow.jflowcore.utils.GsonUtils;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

/**
 * @author hectorvent@gmail.com
 * @version 0.1
 * @since 0.1
 * @date 2016-05-22
 */
class WSClient {

    private static final Logger LOG = LogManager.getLogger(WSClient.class);
    private Thread connetionThreat;
    private WebSocketClient sc;
    private String socketUrl;
    private Boolean conectado = false;
    private boolean started = false;
    private boolean reconnect = false;
    private MessageListener messageListener;

    public WSClient(String serverUrl) {
        try {
            this.socketUrl = serverUrl;
        } catch (Exception ex) {
            LOG.error("Error config file", ex.getMessage(), ex);
        }
    }

    private void init() throws URISyntaxException {

        sc = new WebSocketClient(new URI(socketUrl), new Draft_17()) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                conectado = true;
            }

            @Override
            public void onMessage(String message) {

                Message ms;
                try {
                    ms = GsonUtils.from(message, Message.class);
                } catch (Exception ex) {
                    ms = new Message("EXCEPTION")
                            .put("error", ex.getMessage());
                }

                if (messageListener != null) {
                    messageListener.onMessage(ms);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {

                if (!reconnect) {
                    synchronized (conectado) {
                        conectado = false;
                    }
                }

                //NOT_CONSISTENT, VIOLATED_POLICY
                if (1007 == code || 1008 == code) {
                    LOG.error("Message from server : {}", reason);
                    System.exit(1);
                }

            }

            @Override
            public void onError(Exception ex) {
            }

        };

    }

    public void close() {
        try {
            reconnect = false;
            sc.closeBlocking();
        } catch (InterruptedException ex) {

        }
    }

    public void reconnect() {
        try {
            reconnect = true;
            close();
            conectado = false;
            init();
        } catch (URISyntaxException ex) {
            LOG.error(ex);
        }

    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void connet() throws URISyntaxException {

        if (started) {
            return;
        }

        started = true;
        connetionThreat = new Thread(() -> {
            while (true) {
                try {

                    synchronized (conectado) {
                        if (!conectado) {
                            LOG.info("Connecting...");
                            init();
                            sc.connectBlocking();
                        }
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException | URISyntaxException ex) {

                }
            }
        });

        connetionThreat.start();
    }

}
