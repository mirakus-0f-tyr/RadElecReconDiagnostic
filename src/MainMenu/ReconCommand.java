package MainMenu;

// ReconCommandSet class
// Class which holds strings for the Recon commands, as well
// as other methods which will simplify getting the data from the returned strings
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

class ReconCommand {

    public static String ReconConfirm = ":RV\r\n";
    public static String ReadProtocol = ":RP\r\n";
    public static String CheckNewRecord = ":RB\r\n";
    public static String ReadNextRecord = ":RN\r\n";
    public static String ReadFirstRecord = ":RN1\r\n";
    public static String ClearMemoryCommand = ":CM\r\n";
    public static String ClearSessionCommand = ":CD\r\n";
    public static String ReadCalibrationFactors = ":RL\r\n";
    //public static String WriteCalibrationFactors = ":WL,xyz,xyz";
    public static String ReadTime = ":RT\r\n";
    //public static String WriteTime = ":WT,x/x/x/y/y/y";

    public static String DeviceResponse;
    public static String[] DeviceResponse_parsed;

    public static LinkedList<String[]> reconSession;

    public static String GetSerialNumber() {
        WriteComm.main(ScanComm.scannedPort, ReconConfirm);
        DeviceResponse = ReadComm.main(ScanComm.scannedPort, 19);
	DeviceResponse = DeviceResponse.replaceAll("[\\n\\r+]", ""); // strip line feeds
        DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
        return DeviceResponse_parsed[3];
    }

    public static void LoadNewRecord() { //:RB
        WriteComm.main(ScanComm.scannedPort, CheckNewRecord);
        DeviceResponse = ReadComm.main(ScanComm.scannedPort, 19);
	DeviceResponse = DeviceResponse.replaceAll("[\\n\\r+]", ""); // strip line feeds
        DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
    }

    public static void LoadNextRecord() { //:RN
        WriteComm.main(ScanComm.scannedPort, ReadNextRecord);
        DeviceResponse = ReadComm.main(ScanComm.scannedPort, 19);
	DeviceResponse = DeviceResponse.replaceAll("[\\n\\r+]", ""); // strip line feeds
        DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
    }

    // Beginnings of a method for setting the Recon time from computer system time.
    // Not complete...
    public static void SetReconTimeFromPC() {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YY,MM,DD,hh,mm,ss"); // this format causes an exception - fix
	String strCurrentTime;
	String[] reconFormattedTime;

	// get computer system time
	LocalDateTime currentTime = LocalDateTime.now();
	strCurrentTime = currentTime.format(formatter);

	// rip apart system time into string we can convert to the following

	// issue :WT,YY,MM,DD,hh,mm,ss
    }

    // download Recon session into memory, making data available for multitude of data exporters
    public static void DownloadReconSessionToRAM() throws InterruptedException {
        // initialize linked list
	if (reconSession != null)
	    reconSession.clear();
	else
	    reconSession = new LinkedList();

        // run :RB
        LoadNewRecord();

	// parse and add to list
	reconSession.add(DeviceResponse_parsed);

	// run :RN and parse until Z record complete
	while (!(DeviceResponse_parsed[2].equals("Z"))) {
	    LoadNextRecord();
	    reconSession.add(DeviceResponse_parsed);
	}
    }

    public static void DetermineFileName() {
        // TODO:
        // If we're going to grab values from the units to name the files, we should do it ahead of time
	// and save to memory, that way a file save is NEVER interuppted to do another serialPort read that might
	// possibly fail, and leave a file unsaved/mis-named, etc.
    }

    // this is not necessary yet, but here's the method anyway
    public static void ClearResponse() {
        DeviceResponse = "";
        DeviceResponse_parsed = null;
    }
}
