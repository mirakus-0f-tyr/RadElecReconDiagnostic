package MainMenu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.JFileChooser;


/**
 *
 * @author Rad Elec Inc.
 */
public class InitDirs {
    
    //Operating System Variables
    public static boolean boolMacOS = false;
    public static boolean boolWindows = false;
    
    //Directory Variables
    public static File baseDir = new File("ReconDownloadTool");
    public static File configDir = new File("config");
    public static File dataDir = new File("data");
    public static File fontsDir = new File("fonts");
    public static File logsDir = new File("logs");
    public static File reportsDir = new File("reports");
    public static File appDir = new File("");
    
    public static void main() {
        
        //Handle logging first...
	if (!logsDir.exists()) {
            System.out.println("Logs directory does not exist. Creating...");
            logsDir.mkdirs();
            Logging.createLogFile(); //If no log directory exists, let's create the log file too.
        }
        
        //Determine OS before checking for config/data/reports folders...
        findOperatingSystem();
        
        //Determine application (JAR file) directory.
        showAppDirectory();
        
	// check existence of critical directories before proceeding, and copy exiting config files to user directories (if applicable).
        assignWorkingDirectories();
        
	if (!configDir.exists()) {
            Logging.main("Config directory does not exist.  Creating...");
            configDir.mkdirs();
            copySourceFileToUserDirectory("config",configDir,"company.txt");
            copySourceFileToUserDirectory("config",configDir,"config.txt");
            copySourceFileToUserDirectory("config",configDir,"deployment.txt");
            copySourceFileToUserDirectory("config",configDir,"report.txt");
            copySourceFileToUserDirectory("config",configDir,"signature.png");
            copySourceFileToUserDirectory("config",configDir,"signature.jpg"); //...just in case somebody had added a JPEG.
        }
	if (!dataDir.exists()) {
            Logging.main("Data directory does not exist.  Creating...");
            dataDir.mkdirs();
        }
	if (!fontsDir.exists()) {
            Logging.main("WARNING: Fonts directory is not found or does not exist.");
            Logging.main("WARNING: Attempted Fonts directory = " + fontsDir);
        }
	if (!reportsDir.exists()) {
            Logging.main("Reports directory does not exist.  Creating...");
            reportsDir.mkdirs();
        }
    }
    
    public static void findOperatingSystem() {
        String strOperatingSystem = System.getProperty("os.name");
        Logging.main("Operating System: " + strOperatingSystem);
        boolMacOS = strOperatingSystem.startsWith("Mac");
        boolWindows = strOperatingSystem.contains("Win");
    }
    
    public static void showAppDirectory() {
        try {
            System.out.println("Application Directory (JAR file location) = " + appDir.getAbsolutePath());
        } catch (Exception ex) {
            Logging.main("ERROR: Unable to find application directory!");
        }
    }
    
    public static void copySourceFileToUserDirectory(String strSource, File fileDestination, String strFileName) {
        try {
            File sourceDir = new File(strSource + File.separator + strFileName);
            if (sourceDir.exists()) {
                File targetDir = new File(fileDestination.toString() + File.separator + strFileName);
                Files.copy(sourceDir.toPath(), targetDir.toPath());
            } else {
                Logging.main("WARNING: Unable to resolve application source directory in copySourceFileToUserDir (" + strSource + File.separator + strFileName + ")!");
            }
        } catch (IOException ex) {
            Logging.main("ERROR: copySourceFileToUserDir!");
        }
    }
    
    public static void assignWorkingDirectories() {
        //Make sure that findOperatingSystem() has been called at least once before running this.
        //Assigns proper paths for configDir, dataDir, reportsDir, baseDir, and fontsDir
        //baseDir is used to place graph.png (and provide an easy way to find the user's created RDT files).
        try {
            if(boolMacOS==true) {
                baseDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool") : new File("ReconDownloadTool");
                configDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool"+File.separator+"config") : new File("config");
                dataDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool"+File.separator+"data") : new File("data");
                fontsDir = boolMacOS==true ? new File(new File("ReconDownloadTool.app").getAbsolutePath() + File.separator + "Contents" + File.separator + "Java" + File.separator + "fonts") : new File("fonts");
                logsDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool"+File.separator+"logs") : new File("logs");
                reportsDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool"+File.separator+"reports") : new File("reports");
            }else if(boolWindows==true) {
                baseDir = boolWindows==true ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool") : new File("ReconDownloadTool");
                configDir = boolWindows==true ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool"+File.separator+"config") : new File("config");
                dataDir = boolWindows==true ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool"+File.separator+"data") : new File("data");
                fontsDir = new File("fonts");
                logsDir = boolWindows==true ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool"+File.separator+"logs") : new File("logs");
                reportsDir = boolWindows==true ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool"+File.separator+"reports") : new File("reports");
            }else {
                baseDir = new File("ReconDownloadTool");
                configDir = new File("config");
                dataDir = new File("data");
                fontsDir = new File("fonts");
                logsDir = new File("logs");
                reportsDir = new File("reports");
            }
        } catch (Exception ex) {
            Logging.main("ERROR: assignWorkingDirectories!");
        }
    }
}
