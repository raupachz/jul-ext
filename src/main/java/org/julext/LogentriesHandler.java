/*
 * Copyright 2014 Björn Raupach.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.julext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.MessageFormat;
import static java.util.logging.ErrorManager.CLOSE_FAILURE;
import static java.util.logging.ErrorManager.FORMAT_FAILURE;
import static java.util.logging.ErrorManager.GENERIC_FAILURE;
import static java.util.logging.ErrorManager.OPEN_FAILURE;
import static java.util.logging.ErrorManager.WRITE_FAILURE;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * <code>LogentriesHandler</code>: A handler for writing formatted records to a
 * logentries.com. This handler uses Token-based input.
 *
 * @author Björn Raupach (raupach@me.com)
 */
public final class LogentriesHandler extends Handler {
    
    private String host;
    private int port;
    private byte[] token;
    private SocketChannel channel;
    private final ByteBuffer buffer;

    public LogentriesHandler() {
        buffer = ByteBuffer.allocate(1024);
        configure();
        connect();
    }
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
    
    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }

    @Override
    public void publish(LogRecord record) {
        if (!isLoggable(record)) {
            return;
        }
        String msg;
        try {
            msg = getFormatter().format(record);
        } catch (Exception e) {
            reportError("Error while formatting.", e, FORMAT_FAILURE);
            return;
        }
        buffer.clear();
        buffer.put(token);
        buffer.put((byte)0x020);
        buffer.put(msg.getBytes());
        buffer.put((byte)0x0a);
        buffer.flip();
        while (buffer.hasRemaining()) {
            try {
                channel.write(buffer);
            } catch (IOException e) {
                reportError("Error while writing channel.", e, WRITE_FAILURE);
            }
        }
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                reportError("Error while closing channel.", e, CLOSE_FAILURE);
            }
        }
    }
    
    void configure() {
        String cname = getClass().getName();
        setLevel(getLevelProperty(cname +".level", Level.INFO));
        setFormatter(getFormatterProperty(cname +".formatter", new OneLineFormatter()));
        setHost(getStringProperty(cname + ".host", "data.logentries.com"));
        setPort(getIntProperty(cname + ".port", 514));
        setToken(getBytesProperty(cname + ".token", ""));
    }

    void connect() {
        try {
            channel = SocketChannel.open();
            channel.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            reportError(MessageFormat.format("Error connection to host ''{0}:{1}''", host, port) , e, OPEN_FAILURE);
        }
    }
    
    // -- These methods are private in LogManager
    
    Level getLevelProperty(String name, Level defaultValue) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.parse(val.trim());
        return l != null ? l : defaultValue;
    }
    
    Formatter getFormatterProperty(String name, Formatter defaultValue) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty(name);
        try {
            if (val != null) {
                Class<?> clz = ClassLoader.getSystemClassLoader().loadClass(val);
                return (Formatter) clz.newInstance();
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            reportError(MessageFormat.format("Error reading property ''{0}''", name) , e, GENERIC_FAILURE);
        }
        return defaultValue;
    }
    
    String getStringProperty(String name, String defaultValue) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        return val.trim();
    }
    
    byte[] getBytesProperty(String name, String defaultValue) {
        return getStringProperty(name, defaultValue).getBytes();
    }
    
    int getIntProperty(String name, int defaultValue) {
        LogManager manager = LogManager.getLogManager();
        String val = manager.getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            reportError(MessageFormat.format("Error reading property ''{0}''", name) , e, GENERIC_FAILURE);
            return defaultValue;
        }
    }

}