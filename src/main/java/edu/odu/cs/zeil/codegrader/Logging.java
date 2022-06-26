package edu.odu.cs.zeil.codegrader;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
//import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Logging {
	static private FileHandler logFileHandler;
    static private SimpleFormatter formatter;

    static public void setup() throws IOException {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        logger.setLevel(Level.INFO);
        
        // Do log to console
        Handler[] handlers = logger.getHandlers();
        if (!(handlers[0] instanceof ConsoleHandler)) {
            logger.addHandler(new ConsoleHandler());
        }

        // And to a file
        logFileHandler = new FileHandler("codegrader.log.txt");
        
        formatter = new SimpleFormatter();
        logFileHandler.setFormatter(formatter);
        logger.addHandler(logFileHandler);

    }
}
