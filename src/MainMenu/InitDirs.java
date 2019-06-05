package MainMenu;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author Rad Elec Inc.
 */
public class InitDirs {
    
    //Operating System Variables
    public static boolean boolMacOS = false;
    
    //Directory Variables
    public static File baseDir = new File("ReconDownloadTool");
    public static File configDir = new File("config");
    public static File dataDir = new File("data");
    public static File fontsDir = new File("fonts");
    public static File logsDir = new File("logs");
    public static File reportsDir = new File("reports");
    
    public static void main() {
        
        //Handle logging first...
        logsDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool"+File.separator+"logs") : new File("logs");
        logsDir = boolMacOS==false ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool"+File.separator+"logs") : new File("logs");
	if (!logsDir.exists()) {
            System.out.println("Logs directory does not exist. Creating...");
            logsDir.mkdirs();
            Logging.createLogFile(); //If no log directory exists, let's create the log file too.
        }
        
        //Determine OS before checking for config/data/reports folders...
        findOperatingSystem();
        
	// check existence of critical directories before proceeding
        
	configDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool"+File.separator+"config") : new File("config");
        configDir = boolMacOS==false ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool"+File.separator+"config") : new File("config");
	if (!configDir.exists()) {
            Logging.main(configDir.toString());
            Logging.main("Config directory does not exist.  Creating...");
            configDir.mkdirs();
        }
	dataDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool"+File.separator+"data") : new File("data");
        dataDir = boolMacOS==false ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool"+File.separator+"data") : new File("data");
	if (!dataDir.exists()) {
            Logging.main("Data directory does not exist.  Creating...");
            dataDir.mkdirs();
        }
        fontsDir = boolMacOS==true ? new File(new File("ReconDownloadTool.app").getAbsolutePath() + File.separator + "Contents" + File.separator + "Java" + File.separator + "fonts") : new File("fonts");
	if (!fontsDir.exists()) {
            Logging.main("WARNING: Fonts directory is not found or does not exist.");
            Logging.main("WARNING: Attempted Fonts directory = " + fontsDir);
        }
	reportsDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool"+File.separator+"reports") : new File("reports");
        reportsDir = boolMacOS==false ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool"+File.separator+"reports") : new File("reports");
	if (!reportsDir.exists()) {
            Logging.main("Reports directory does not exist.  Creating...");
            reportsDir.mkdirs();
        }
        
        //baseDir is only used in macOS, to place graph.png
        baseDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool") : new File("ReconDownloadTool");
        baseDir = boolMacOS==false ? new File(new JFileChooser().getFileSystemView().getDefaultDirectory().toString()+File.separator+"ReconDownloadTool") : new File("ReconDownloadTool");
    }
    
    public static void findOperatingSystem() {
        String strOperatingSystem = System.getProperty("os.name");
        Logging.main("Operating System: " + strOperatingSystem);
        boolMacOS = strOperatingSystem.startsWith("Mac");
    }
}
