package MainMenu;

import static MainMenu.InitDirs.logsDir;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Rad Elec Inc
 */
public class Logging {
    private static final Logger logger = Logger.getLogger("RDT");
    private static FileHandler logHandler;
    
    public static void main(String strLog) {
        if(MainMenuUI.initializedLogging == false) {
            prepareLogging();
            MainMenuUI.initializedLogging = true;
        }
        log(strLog);
    }
    
    public static void log(String strLog) {
        logger.setUseParentHandlers(false);
        logger.info(strLog);
        System.out.println(strLog);
    }
    
    public static void createLogFile() {
        File logFile = new File(logsDir + File.separator + "RDT.log");
        try {
            PrintWriter pw = null;
            if (!(logFile.exists())) {
                pw = new PrintWriter(logFile);
                pw.close();
            }
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR: Unable to create logging file!"); //No need to write to a log if we can't even create the damn thing...
        }
    }
    
    public static void prepareLogging() {
        try {
            System.out.println("Initiating logging system...");
            createLogFile();
            logHandler = new FileHandler(InitDirs.logsDir + File.separator + "RDT.log");
            logger.addHandler(logHandler);
            SimpleFormatter formatter = new SimpleFormatter();
            logHandler.setFormatter(formatter);
        } catch (IOException | SecurityException ex) {
            System.out.println("ERROR: Unhandled exception in Logging::prepareLogging()...");
            System.out.println(ex);
        }
    }
}
