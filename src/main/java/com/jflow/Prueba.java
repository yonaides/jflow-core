/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jflow;

import com.jflow.jflowcore.JflowCoreWS;
import com.jflow.jflowcore.JflowWSType;

/**
 *
 * @author hventurar@edenorte.com.do
 */
public class Prueba {

    public static void main(String[] args) {

        JflowCoreWS.createWsCore(JflowWSType.PRINTER)
                .addMessageListener(message -> {
                    System.out.println("Message : " + message);
                })
                .start();

    }
}
