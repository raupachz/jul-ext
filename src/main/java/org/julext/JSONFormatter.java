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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * <code>JSONFormatter</code> formats LogRecords into a standard XML format.
 *
 * @author Björn Raupach (raupach@me.com)
 */
public class JSONFormatter extends Formatter {

    public static final String encoding = "utf8";
    public static final String newline = "\r\n";
    public static final int initialCapacity = 256;

    private static final char openBrace = '{';
    private static final char closeBrace = '}';
    private static final char doubleQuote = '"';
    private static final char colon = ':';
    private static final char comma = ',';

    private static final String date = "date";
    private static final String millis = "millis";
    private static final String sequence = "sequence";
    private static final String logger = "logger";
    private static final String level = "level";
    private static final String clazz = "class";
    private static final String method = "method";
    private static final String thread = "thread";
    private static final String message = "message";
    private static final String params = "params";

    private static final DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    /* Append to the StringBuilder an escaped version of the String */
    void escape(StringBuilder sb, String s) {
        if (s == null || s.length() == 0) {
            sb.append(doubleQuote).append(doubleQuote);
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
    }

    void append(StringBuilder sb, String s) {
        sb.append(doubleQuote);
        escape(sb, s);
        sb.append(doubleQuote);
    }

    void append(StringBuilder sb, long l) {
        sb.append(l);
    }

    void append(StringBuilder sb, String name, String value) {
        append(sb, name);
        sb.append(colon);
        append(sb, value);
    }

    /* DateFormat is not threadsafe */
    synchronized void append(StringBuilder sb, String name, Date value) {
        append(sb, name);
        sb.append(colon);
        append(sb, iso8601.format(value));
    }

    void append(StringBuilder sb, String name, long value) {
        append(sb, name);
        sb.append(colon);
        append(sb, String.valueOf(value));
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder(initialCapacity);

        sb.append(openBrace);
        append(sb, date, new Date(record.getMillis()));
        sb.append(comma);
        append(sb, millis, record.getMillis());
        sb.append(comma);
        append(sb, sequence, record.getSequenceNumber());
        sb.append(comma);
        append(sb, logger, record.getLoggerName());
        sb.append(comma);
        append(sb, level, record.getLevel().getName());
        sb.append(comma);
        append(sb, clazz, record.getSourceClassName());
        sb.append(comma);
        append(sb, method, record.getSourceMethodName());
        sb.append(comma);
        append(sb, thread, record.getThreadID());
        sb.append(comma);
        append(sb, message, record.getMessage());
        sb.append(closeBrace);
        sb.append(newline);
        return sb.toString();
    }

}
