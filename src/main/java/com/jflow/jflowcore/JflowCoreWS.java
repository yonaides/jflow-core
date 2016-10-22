/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jflow.jflowcore;

import com.jflow.jflowcore.config.Configuration;
import com.jflow.jflowcore.config.JsonConfiguration;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author hectorvent@gmail.com
 * @version 0.1
 * @since 0.1
 * @date 2016-05-22
 */
public class JflowCoreWS implements Runnable {

    private static JflowCoreWS jflowCoreWS;
    private static boolean JflowCoreWSInstaced = false;
    private boolean runProcessQueue = true;
    private final Queue<Runnable> process = new LinkedList();
    private final List<MessageListener> queueMessageListerners = new ArrayList();
    private final List<MessageListener> messageListerners = new ArrayList();
    private static final Logger LOG = LogManager.getLogger(JflowCoreWS.class);
    private static final Configuration CONFIG;
    private WSClient wsc;
    private String serverUrl;
    private String tokenApi;
    private JflowWSType jflowWsType;

    static {
        CONFIG = new JsonConfiguration("./", "jflowCore");
    }

    public static Configuration getConfig() {
        return CONFIG;
    }

    public JflowCoreWS(JflowWSType jflowWsType) {
        if (JflowCoreWSInstaced) {
            throw new IllegalStateException("This class has been instanced previously");
        }

        this.jflowWsType = jflowWsType;
        JflowCoreWSInstaced = true;
    }

    public static JflowCoreWS createWsCore(JflowWSType jflowWsType) {
        if (jflowCoreWS != null) {
            return jflowCoreWS;
        }

        jflowCoreWS = new JflowCoreWS(jflowWsType);
        return jflowCoreWS;
    }

    public static JflowCoreWS getWsCore(JflowWSType jflowWsType) {
        if (jflowCoreWS != null) {
            return jflowCoreWS;
        }

        return null;
    }

    private void initConfig() {

        tokenApi = CONFIG.getAsString("tokenApi");
        serverUrl = CONFIG.getAsString("serverUrl");

        if (tokenApi == null) {
            throw new IllegalArgumentException("Param 'tokenApi' hasn't been found");
        }
        if (serverUrl == null) {
            throw new IllegalArgumentException("Param 'serverUrl' hasn't been found");
        }
    }

    public void start() {

        initConfig();
        createWSClient();

        // Thread that will execute the queue proccess
        Thread processRunner = new Thread(this);
        processRunner.start();

        Integer time = CONFIG.getAsInt("observerStateTime");
        RestStatusCheker.createStatusChecker()
                .setTokenApi(tokenApi)
                .setServerUrl(serverUrl + "?type=" + (jflowWsType == JflowWSType.PRINTER ? "printer" : "kiosco"))
                .setTime(time == null ? 3 : time)
                .addMessageListener((message) -> {
                    if (MessageType.REFRESH.equals(message.getTipoMensaje())) {
                        wsc.reconnect();
                    }
                })
                .start();

    }

    public void close() {
        RestStatusCheker.getRestatusCheker()
                .stop();
        wsc.close();
        runProcessQueue = false;
    }

    private void createWSClient() {

        StringBuilder wsUrl = new StringBuilder(serverUrl.replace("http", "ws"));
        wsUrl.append(jflowWsType.toString())
                .append("?tokenApi=")
                .append(tokenApi)
                .append("&version=")
                .append(Params.VERSION);

        String uuid = UUID.randomUUID().toString();

        wsc = new WSClient(wsUrl.toString());
        wsc.setMessageListener(message -> {

            synchronized (process) {

                // Cola de porcesos que se ejecutan por llegada
                process.add(() -> {
                    queueMessageListerners
                            .stream()
                            .forEach(ml -> {
                                ml.onMessage(message);
                            });
                });

                process.notify();

                // Por cada mensaje recibido inicia un Hilo separado para que no bloquee el sistema
                if (messageListerners.size() > 0) {
                    Thread th = new Thread(() -> {
                        messageListerners
                                .stream()
                                .forEach(ml -> ml.onMessage(message));
                    });
                    th.start();
                }
            }
        });

        try {
            wsc.connet();
        } catch (URISyntaxException ex) {
            LOG.error("Error connecting", "Error: ", ex);
        }
    }

    @Override
    public void run() {

        while (runProcessQueue) {

            synchronized (process) {
                if (process.isEmpty()) {
                    try {
                        process.wait();
                    } catch (InterruptedException ex) {
                        LOG.error("Error waiting synchronized process", "Error: ", ex);
                    }
                }

                Runnable pro = process.poll();

                if (pro != null) {
                    pro.run();
                }

            }
        }
    }

    public void stopQueueProcess() {
        runProcessQueue = false;
    }

    public JflowCoreWS addQueueMessageListener(MessageListener ml) {
        queueMessageListerners.add(ml);
        return this;
    }

    public JflowCoreWS addMessageListener(MessageListener ml) {
        messageListerners.add(ml);
        return this;
    }

}
