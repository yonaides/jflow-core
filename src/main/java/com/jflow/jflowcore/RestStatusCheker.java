/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jflow.jflowcore;

import com.jflow.jflowcore.utils.GsonUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author hectorvent@gmail.com
 * @version 0.1
 * @since 0.1
 * @date 2016-05-22
 */
public class RestStatusCheker {

    private Integer time = 3;
    private String tokenApi;
    private boolean runThread = true;
    private String serverUrl;
    private final List<MessageListener> messageListeners = new ArrayList();
    private final static Logger LOG = LogManager.getLogger(RestStatusCheker.class);
    private static RestStatusCheker restStatusCheker;

    public static RestStatusCheker createStatusChecker() {
        if (restStatusCheker == null) {
            restStatusCheker = new RestStatusCheker();
        }

        return restStatusCheker;
    }

    public static RestStatusCheker getRestatusCheker() {
        return restStatusCheker;
    }

    public Integer getTime() {
        return time;
    }

    public RestStatusCheker setTime(Integer time) {
        this.time = time;
        return this;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public RestStatusCheker setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public String getTokenApi() {
        return tokenApi;
    }

    public RestStatusCheker setTokenApi(String tokenApi) {
        this.tokenApi = tokenApi;
        return this;
    }

    public void stop() {
        runThread = false;
    }

    public void start() {

        final int timeMS = (60000) * time;
        serverUrl = serverUrl + "/api/v1/kiosco/status/" + tokenApi;

        Thread statusChequer = new Thread(() -> {

            while (runThread) {

                try {
                    Thread.sleep(timeMS);

                    String response = httpRequest();
                    if (response != null) {
                        Message ms = GsonUtils.from(response, Message.class);
                        fireMessageListerners(ms);
                    }
                } catch (InterruptedException ex) {
                    LOG.error(ex);
                }
            }
        });

        statusChequer.start();
    }

    private String httpRequest() {

        try {

            URL url1 = new URL(serverUrl);
            URLConnection urlConnection = url1.openConnection();
            StringBuilder respose = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    urlConnection.getInputStream()))) {

                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    respose.append(inputLine);
                }
            }

            return respose.toString();

        } catch (MalformedURLException ex) {
            LOG.error(ex);
        } catch (IOException ex) {
            LOG.error(ex);
        }

        return null;
    }

    public RestStatusCheker addMessageListener(MessageListener ml) {
        messageListeners.add(ml);
        return this;
    }

    private void fireMessageListerners(Message ms) {
        messageListeners.stream().forEach((ml) -> {
            ml.onMessage(ms);
        });
    }

}
