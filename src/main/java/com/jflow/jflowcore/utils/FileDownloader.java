/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jflow.jflowcore.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hventurar@edenorte.com.do
 */
public class FileDownloader {

    private String url;
    private File file;
    private Integer timeOut;
    private Consumer<File> consumer;

    public static FileDownloader newDownloader() {
        return new FileDownloader();
    }

    public static void download(String url, File file) throws Exception {
        newDownloader()
                .setFile(file)
                .setUrl(url)
                .download();
    }

    public static void asyncDownload(String url, File file, Consumer<File> consumer) {
        newDownloader()
                .setFile(file)
                .setUrl(url)
                .setConsumer(consumer)
                .asyncDownload();
    }

    public Consumer<File> getConsumer() {
        return consumer;
    }

    public FileDownloader setConsumer(Consumer<File> consumer) {
        this.consumer = consumer;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public File getFile() {
        return file;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public FileDownloader setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public FileDownloader setFile(File file) {
        this.file = file;
        return this;
    }

    public FileDownloader setUrl(String url) {
        this.url = url;
        return this;
    }

    public FileDownloader toFile(File file) {
        this.file = file;
        return this;
    }

    public void asyncDownload() {
        Thread download = new Thread(() -> {
            try {
                download();
                if (consumer != null) {
                    consumer.accept(file);
                }
            } catch (IOException ex) {
                Logger.getLogger(FileDownloader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(FileDownloader.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        download.start();
    }

    public void download() throws Exception {

        Objects.requireNonNull(this.url, "URL can't be null");
        Objects.requireNonNull(this.file, "file can't be null");

        URL urlFile;

        try {
            urlFile = new URL(this.url);
        } catch (MalformedURLException ex) {
            throw ex;
        }

        URLConnection con = urlFile.openConnection();
        if (timeOut != null) {
            con.setConnectTimeout(timeOut);
        }

        Files.copy(con.getInputStream(), file.toPath());
    }

//    public void download() throws IOException {
//
//        Objects.requireNonNull(this.url);
//        Objects.requireNonNull(this.file);
//
//        try {
//            URL urlFile = new URL(this.url);
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//
//            try (InputStream in = new BufferedInputStream(urlFile.openStream())) {
//                byte[] buf = new byte[1024];
//                int n;
//                while ((n = in.read(buf)) != -1) {
//                    out.write(buf, 0, n);
//                }
//                out.close();
//            }
//
//            byte[] response = out.toByteArray();
//
//            try (FileOutputStream fos = new FileOutputStream(file)) {
//                fos.write(response);
//            }
//        } catch (MalformedURLException ex) {
//        }
//    }
}
