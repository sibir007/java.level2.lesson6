package ru.geekbrains.java.level2.chat.server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Loader;

public class LoggerExample {
    //Trace < Debug < Info < Warn < Error < Fatal
    private static final Logger logger = LogManager.getLogger(LoggerExample.class);

    public static void main(String[] args) {
        System.out.println(logger.getName());

        logger.debug("Debug");
        logger.info("Info");
        logger.warn("Warn");
        logger.error("Error");
        logger.fatal("Fatal");
        logger.info("String: {}.","Hello World");
        a();

    }
    public static void a() {
        try {
            throw new RuntimeException("123");
        }catch (RuntimeException e) {
            logger.throwing(Level.ERROR, e);
        }
    }
}
