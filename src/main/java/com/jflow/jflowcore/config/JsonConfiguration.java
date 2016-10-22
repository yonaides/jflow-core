/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jflow.jflowcore.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author hectorvent@gmail.com
 * @version 0.1
 * @since 0.1
 * @date 2016-05-22
 */
public final class JsonConfiguration implements Configuration {

    private final Gson gson;
    private String directory;
    private String configName;
    private JsonObject data;
    private boolean saveOnChanged = false;

    private List<OnSaveEvent> onSaveEvents;

    public JsonConfiguration(String directory, String configName) {
        this.directory = directory;
        this.configName = configName;
        this.gson = new GsonBuilder()
                //                .setPrettyPrinting()
                .create();
        load();
    }

    public JsonConfiguration(String directory) {
        this(directory, "default");
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public boolean isSaveOnChanged() {
        return saveOnChanged;
    }

    @Override
    public void addOnSaveEvent(OnSaveEvent event) {
        if (onSaveEvents == null) {
            onSaveEvents = new ArrayList();
        }

        onSaveEvents.add(event);
    }

    private File getFile() {
        return new File(directory, configName + ".json");
    }

    @Override
    public boolean load() {

        try {
            File file = getFile();

            if (file.exists()) {
                data = gson.fromJson(new FileReader(file), JsonObject.class);
                if (data != null) {
                    return true;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(JsonConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }

        data = new JsonObject();
        return false;
    }

    @Override
    public boolean save() {

        try {
            File file = getFile();
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(gson.toJson(data));
            }

            notifiedOnSaveEvents();
            return true;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JsonConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JsonConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
    public Configuration put(String key, Object value) {

        if (data == null) {
            load();
        }

        if (value != null) {
            data.add(key, gson.toJsonTree(value));

            if (saveOnChanged) {
                save();
            }
        }

        return this;
    }

    private <T> T toClass(JsonElement el, Class<T> classOfT) {
        return gson.fromJson(el, classOfT);
    }

    @Override
    public <T> T get(String key, Class<T> classOf) {

        JsonElement obj = null;
        try {
            obj = data.get(key);
        } catch (NullPointerException ex) {
        }

        if (obj == null) {
            return null;
        }
        return toClass(obj, classOf);
    }

    @Override
    public boolean getAsBoolean(String key) {
        try {
            return data.get(key).getAsBoolean();
        } catch (NullPointerException ex) {
            return false;
        }
    }

    @Override
    public String getAsString(String key) {
        try {
            return data.get(key).getAsString();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    @Override
    public Double getAsDouble(String key) {
        try {
            return data.get(key).getAsDouble();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    @Override
    public Integer getAsInt(String key) {
        try {
            return data.get(key).getAsInt();
        } catch (NullPointerException ex) {
            return null;
        }
    }

    @Override
    public void setSaveOnChanged(boolean saveOnChanged) {
        this.saveOnChanged = saveOnChanged;
    }

    @Override
    public boolean remove(String key) {
        try {
            JsonElement je = data.remove(key);
            return je != null;
        } catch (NullPointerException ex) {
        }

        return false;

    }

    private void notifiedOnSaveEvents() {
        if (onSaveEvents == null) {
            return;
        }

        onSaveEvents.stream().forEach((event) -> {
            event.saveEvent(this);
        });

    }

    @Override
    public <T> T get(String key, Class<T> classOf, T defaultValue) {
        T value = get(key, classOf);
        return value == null ? defaultValue : value;

    }

}
