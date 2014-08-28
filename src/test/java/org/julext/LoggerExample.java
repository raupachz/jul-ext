package org.julext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerExample {
    
    private static final Logger logger = Logger.getLogger(LoggerExample.class.getName());
    
    public static void main(String[] args) throws IOException {
        File in = new File("src/test/resources/logging.properties");
        LogManager.getLogManager().readConfiguration(new FileInputStream(in));
        
        for (int i = 0; i < 10; i++) {
            logger.info(i + "# message");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignore) {}
        }
    }
    
    public static void printLoggers() {
        Enumeration<String> enumerator = LogManager.getLogManager().getLoggerNames();
        while (enumerator.hasMoreElements()) {
            String loggerName = enumerator.nextElement();
            System.out.println(loggerName);
        }
    }
    
}
