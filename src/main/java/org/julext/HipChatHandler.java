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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * <code>HipChatHandler</code>: A handler for writing formatted records to a
 * HipChat room. This handler uses HipChat API Version 2.
 *
 * @author Björn Raupach (raupach@me.com)
 */
public class HipChatHandler extends Handler {
    
    private int roomId;
    private String accessToken;
    
    public HipChatHandler() {
        setFormatter(new JSONFormatter());
    }
    
    @Override
    public synchronized void publish(LogRecord record) {
        if (isLoggable(record)) {
            String message;
            try {
                message = getFormatter().format(record);
            } catch (Exception e) {
                reportError(null, e, ErrorManager.FORMAT_FAILURE);
                return;
            }
            String request = "https://api.hipchat.com/v2/room/" + roomId + "/notification";
            try {
                URL url = new URL(request);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", "Bearer " + "o5qMX9ZuPdhGdBzsLiWK7HTZyvcfCGbS0YykWaAk");
                con.setRequestProperty("Host", "api.hipchat.com");
                con.setDoOutput(true);
                String json = "{ \"notify\": true, \"message\": \"" + escape(message) + "\", \"message_format\": \"text\" }";
                con.getOutputStream().write(json.getBytes(Charset.forName("utf-8")));
                int responseCode = con.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    ByteBuffer buffer = new ByteBuffer(con.getErrorStream());
                    String response = buffer.getAsString("utf-8");
                    throw new IOException("Expected HTTP status code 204 not " + responseCode + "\n" + response);
                }
            } catch (IOException e) {
                reportError(null, e, ErrorManager.WRITE_FAILURE);
            }
        }
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    String escape(String s) {
        StringBuilder sb = new StringBuilder(64);
        if (s == null || s.length() == 0) {
            sb.append("\"\"");
        } else {
            char c;
            int i;
            int len = s.length();

            for (i = 0; i < len; i += 1) {
                c = s.charAt(i);
                switch (c) {
                    case '\\':
                    case '"':
                        sb.append('\\');
                        sb.append(c);
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    default:
                        if (c < ' ') {
                            String tmp = "000" + Integer.toHexString(c);
                            sb.append("\\u").append(tmp.substring(tmp.length() - 4));
                        } else {
                            sb.append(c);
                        }
                }
            }
        }
        return sb.toString();
    }
    
    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}

}
