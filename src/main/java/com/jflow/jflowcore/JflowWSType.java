/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jflow.jflowcore;

/**
 * @author hectorvent@gmail.com
 * @version 0.1
 * @since 0.1
 * @date 2016-05-22
 */
public enum JflowWSType {

    PRINTER(Params.PRINTER_WSOCKET),
    KIOSCOINF(Params.KIOSCO_INF_WSOCKET);

    private final String type;

    private JflowWSType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

}
