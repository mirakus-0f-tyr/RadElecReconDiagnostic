package MainMenu;

// ReconCommandSet class
// Class which holds strings for the Recon commands, as well
// as other methods which will simplify getting the data from the returned strings
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.io.File;

import Config.FlagForm;

class ReconCommand {

    public static String ReconConfirm = ":RV\r\n";
    public static String ReadProtocol = ":RP\r\n";
    public static String CheckNewRecord = ":RB\r\n";
    public static String ReadNextRecord = ":RN\r\n";
    public static String ReadFirstRecord = ":RN1\r\n";
    public static String ClearMemoryCommand = ":CM\r\n";
    public static String ClearSessionCommand = ":CD\r\n";
    public static String ReadCalibrationFactors = ":RL\r\n";
    public static String ReadTime = ":RT\r\n";

    public static String DeviceResponse;
    public static String[] DeviceResponse_parsed;

    public static LinkedList<String[]> reconSession; // container to hold Recon samples
    public static String filenameTXT;
    public static String filenameXLS;

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

    // set the computer system time to the Recon
    public static void SetReconTimeFromPC() {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy,MM,dd,HH,mm,ss");
	String strCurrentTime;

	// get computer system time
	LocalDateTime currentTime = LocalDateTime.now();
	strCurrentTime = currentTime.format(formatter);

	// issue :WT
	System.out.println("Issuing :WT instruction to write time...");
	WriteComm.main(ScanComm.scannedPort, ":WT," + strCurrentTime + "\r\n");
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
	reconSession.add(CountLimiter.main(DeviceResponse_parsed));

	// run :RN and parse until Z record complete
	while (!(DeviceResponse_parsed[2].equals("Z"))) {
	    LoadNextRecord();
	    reconSession.add(CountLimiter.main(DeviceResponse_parsed));
	}

	// check that record numbers are sequential - that one was not skipped in the case of interrupted download
	for (int c = 0; c < reconSession.size() - 1; c++) { // stop before Z
	    if (Integer.parseInt(reconSession.get(c)[1]) + 1 != Integer.parseInt(reconSession.get(c + 1)[1])) {
		System.out.println("Sequential record check failed. Restarting download.");
		DownloadReconSessionToRAM(); // restart the download because we missed a record
	    }
	}
    }

    // Loads filenames into strings here so that it does not need to be done
    // in CreateTXT and CreateXLS individually
    public static void DetermineFileName() {

	String TXT_name = null;
        File TXT_file = null;
        long fileIteration = 1;
        boolean DoesReconFileExist = true;
        boolean TXT_exists = false;
	String XLS_name = null;
        File XLS_file = null;
	boolean XLS_exists = false;

	// get serial number and populate strings we will be searching for values
	String ConfirmSN = ReconCommand.GetSerialNumber();
	LoadNewRecord();
	LoadNextRecord();

	//This while loop will determine the file iteration in the naming process, so that we're not overwriting previously
        //created files. This will basically append -x to the end of a file name, where x is the long file iteration counter.
        while(DoesReconFileExist==true) {
            TXT_name = "data/Recon_" + ConfirmSN + "_" + DeviceResponse_parsed[4] + DeviceResponse_parsed[5] + DeviceResponse_parsed[3] + "-" + fileIteration + ".txt";
            XLS_name = "data/Recon_" + ConfirmSN + "_" + DeviceResponse_parsed[4] + DeviceResponse_parsed[5] + DeviceResponse_parsed[3] + "-" + fileIteration + ".xls";
            TXT_file = new File(TXT_name);
            XLS_file = new File(XLS_name);
            TXT_exists = TXT_file.exists();
            XLS_exists = XLS_file.exists();
            if(!((TXT_exists==true)||(XLS_exists==true))) {
                DoesReconFileExist = false;
            }
            if(DoesReconFileExist==true) {
                fileIteration++;
            }
        }

	filenameTXT = new String(TXT_name);
	filenameXLS = new String(XLS_name);
    }

    public static boolean SetOptionFlag() {
	// OPTIONS TO BE SET WITH FLAG VARIABLE
	// Pressure----------------------
	// InHG		0000 0000
	// mBar		0000 0001
	// Temperature-------------------
	// F		0000 0000
	// C		0000 0100
	// Blind Flag--------------------
	// Show all	0000 0000
	// Show none	0000 1000
	// Dual Chamber------------------
	// Combine	0000 0000
	// Show both	0001 0000
	// Exposure Units----------------
	// pCi/L	0000 0000
	// Bq/m3	0010 0000
	// CPH		0100 0000
	// CPHs		0110 0000
	// ------------------------------
	// Process: add all of the options the user wants, convert to hex and write that value to the unit.

        //boolean opSuccess = false;

	//String flagResponse = null; // value read from unit to verify success
	short flag = 0; // binary number we will be writing to the unit

	if (FlagForm.displayPreferencePres == "mBar")
	    flag += 0b00000001;
	if (FlagForm.displayPreferenceTemp == "C")
	    flag += 0b00000100;
	if (FlagForm.displayPreferenceDual == "yes")
	    flag += 0b00010000;
	if (FlagForm.displayPreferenceUnits == "Bq/m3")
	    flag += 0b00100000;
	if (FlagForm.displayPreferenceUnits == "CPH")
	    flag += 0b01000000;

	System.out.println("Attempting to write flag: " + Integer.toHexString(flag));
	WriteComm.main(ScanComm.scannedPort, ":WF" + Integer.toHexString(flag) + "\r\n");

	// THIS CHECK NEEDS TO BE FIXED - WE DON'T WANT A MISTAKE IN SETTING THIS VALUE TO THE UNIT
	WriteComm.main(ScanComm.scannedPort, ":RF\r\n");
	DeviceResponse = ReadComm.main(ScanComm.scannedPort, 19);
	/*DeviceResponse = DeviceResponse.replaceAll("[\\n\\r+]", ""); // strip line feeds

	flagResponse = DeviceResponse.substring(6);
	flagResponse = StringUtils.stripStart(flagResponse, "0");
	System.out.println(Integer.toHexString(flag));
	System.out.println(Integer.toHexString(Integer.parseInt(flagResponse)));
	if (Integer.toHexString(flag).equals(Integer.toHexString(Integer.parseInt(flagResponse))))
	    opSuccess = true;
	else {
	    opSuccess = false;
	    System.out.println("Value returned by :RF does not match what was written. Flag write failed.");
	}*/

	//return opSuccess;
	return true;
    }

    // this is not necessary yet, but here's the method anyway
    public static void ClearResponse() {
        DeviceResponse = "";
        DeviceResponse_parsed = null;
    }
}
