/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jflow.jflowcore.config;

/**
 * @author hectorvent@gmail.com
 * @version 0.1
 * @since 0.1
 * @date 2016-05-22
 */
public interface Configuration {

    void setSaveOnChanged(boolean saveOnChanged);

    boolean load();

    boolean save();

    Configuration put(String key, Object value);

    boolean remove(String key);

    <T> T get(String key, Class<T> classOf);

    <T> T get(String key, Class<T> classOf, T defaultValue);

    boolean getAsBoolean(String key);

    String getAsString(String key);

    Double getAsDouble(String key);

    Integer getAsInt(String key);

    void addOnSaveEvent(OnSaveEvent event);

}
