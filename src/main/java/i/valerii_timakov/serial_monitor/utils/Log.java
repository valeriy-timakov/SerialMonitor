package i.valerii_timakov.serial_monitor.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Log {

    private static final Path LOG_FILE_PATH = Path.of("C:/Users/valti/Projects/app.log");
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss SSS");

    enum Level {
        DEBUG, INFO, WARNING, ERROR
    }

    private static void log(String message, boolean error) {
        if (error) {
            System.err.print(message);
        } else {
            System.out.print(message);
        }
        LOG_FILE_PATH.toFile().getParentFile().mkdirs();
        try {
            Files.writeString(LOG_FILE_PATH, message, Charset.defaultCharset(), StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error writing message to log file! " + e.getMessage());
        }
    }

    public static void log(String message, Level level) {
        log(LocalDateTime.now().format(timeFormat) + "[" + level.name() + "] " + message + "\n", level == Level.ERROR);
    }

    public static void debug(String message, Object ... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        log(message, Level.DEBUG);
    }

    public static void error(String message, Throwable error) {
        log(message, Level.ERROR);
        logException(error);
    }

    public static void error(Throwable error) {
        log(error.getMessage(), Level.ERROR);
        logException(error);
    }

    private static void logException(Throwable error) {
        log(error.getMessage() + "\n", true);
        for (StackTraceElement stackTraceElement : error.getStackTrace()) {
            log("\t" + stackTraceElement.toString() + "\n", true);
        }
        if (error.getCause() != null) {
            log("Caused by: ", true);
            logException(error.getCause());
        }
    }

    public static void error(String message) {
        log(message, Level.ERROR);
    }
}
