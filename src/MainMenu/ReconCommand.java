package MainMenu;

// ReconCommand class
// Class which holds strings for the Recon commands, as well
// as other methods which will simplify getting the data from the returned strings
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.io.File;

import Config.FlagForm;
import java.io.PrintWriter;
import java.io.StringWriter;

class ReconCommand {

    public static String ReconConfirm = ":RV\n";
    public static String ReadProtocol = ":RP\n";
    public static String CheckNewRecord = ":RB\n";
    public static String ReadNextRecord = ":RN\n";
    public static String ReadFirstRecord = ":RN0\n";
    public static String ReadNextDiagnosticRecord = ":DN\n";
    public static String ReadFirstDiagnosticRecord = ":DN0\n";
    public static String ClearMemoryCommand = ":CM\n";
    public static String ClearSessionCommand = ":CD\n";
    public static String ReadCalibrationFactors = ":RL\n";
    public static String ReadTime = ":RT\n";
    public static String ResetTamperFlag = ":WX\n";
    public static String WriteOptionsFlag = ":WF";
    public static String ReadOptionsFlag = ":RF\n";
    public static String ReadPointerTable = ":CN\n";

    public static String DeviceResponse;
    public static String[] DeviceResponse_parsed;

    public static LinkedList<String[]> reconSession; // container to hold Recon samples
    public static LinkedList<Integer> sessionAddresses = new LinkedList();
    public static int currentSession; 	// index into sessionAddresses, based on UI combo box selection
    public static String[] pointerTable_raw;
    public static String filenameTXT;
    public static String filenameXLS;
    public static String defaultFilename;
    public static boolean longTermMode;

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

    public static void LoadSpecifiedRecord(String number) {
	WriteComm.main(ScanComm.scannedPort, ":RN" + number + "\n");
	DeviceResponse = ReadComm.main(ScanComm.scannedPort, 19);
	DeviceResponse = DeviceResponse.replaceAll("[\\n\\r+]", ""); // strip line feeds
	DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
    }

    public static void LoadSpecifiedRecord(int number) {
	WriteComm.main(ScanComm.scannedPort, ":RN" + Integer.toString(number) + "\n");
	DeviceResponse = ReadComm.main(ScanComm.scannedPort, 19);
	DeviceResponse = DeviceResponse.replaceAll("[\\n\\r+]", "");
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
	Logging.main("Issuing :WT instruction to write time...");
	WriteComm.main(ScanComm.scannedPort, ":WT," + strCurrentTime + "\n");
    }

    // get the pointer table from the recon as well as start points of valid sessions
    public static void GetPointerTable() {
	// reset pointer table variables
	pointerTable_raw = new String[16];
	sessionAddresses.clear();

	// read pointer table
	WriteComm.main(ScanComm.scannedPort, ReadPointerTable);
	DeviceResponse = ReadComm.main(ScanComm.scannedPort, 19);
	DeviceResponse = DeviceResponse.replaceAll("[\\n\\r+]", "");
	DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");

	// get array index to start of oldest session
	String oldestSessionString = DeviceResponse_parsed[1];
	oldestSessionString = oldestSessionString.trim();
	int oldestSession = Integer.parseInt(oldestSessionString);

	int numSessions = MainMenu.MainMenuUI.getDataSessions();

	// clean the pointer table, eliminating the =OK and the two indices
	int rawAddressIndex = 0;
	for (int i = 0; i < 19; i++) {
	    if (i < 3)
		continue;

	    pointerTable_raw[rawAddressIndex] = DeviceResponse_parsed[i];
	    rawAddressIndex++;
	}

	rawAddressIndex = oldestSession;
	for (int j = 0; j < numSessions; j++) {
	    // We can only use indices 0-15, so check it

	    // access the beginning of the table if we're at the end
	    if (rawAddressIndex == 16)
		rawAddressIndex = 0;

	    String tempString = pointerTable_raw[rawAddressIndex];
	    tempString = tempString.trim();
	    sessionAddresses.add(Integer.parseInt(tempString));
	    rawAddressIndex++;
	}

	return;
    }

    // download Recon session into memory, making data available for multitude of data exporters
    public static boolean DownloadReconSessionToRAM() throws InterruptedException {
	int numDataRecords = (Float.parseFloat(ScanComm.ReconFirmwareVersion) >= 1.34) ? 6043 : 6143;

        // initialize linked list
	if (reconSession != null)
	    reconSession.clear();
	else
	    reconSession = new LinkedList();

	// load first record and check ST/LT mode
	LoadSpecifiedRecord(sessionAddresses.get(currentSession));

	if (DeviceResponse_parsed.length == 1) {
	    Logging.main("Session pointer pointing to null record. Aborting download.");
	    return false;
	}

	longTermMode = false;
	if (DeviceResponse_parsed[14].equals("6"))
	    longTermMode = true;

	// init value to start of current test
	// NOTE FOR FUTURE: We could use this number with LoadSpecifiedRecord(), eliminating the need for a comparison below
	int recordIterator = Integer.parseInt(DeviceResponse_parsed[1]);

	// parse and add to list
	if (MainMenuUI.countLimiter)
	    reconSession.add(CountLimiter.main(DeviceResponse_parsed));
	else
	    reconSession.add(DeviceResponse_parsed);

	// run :RN and parse until Z record complete
	while (!(DeviceResponse_parsed[2].equals("Z"))) {
	    ++recordIterator; // what the current sample number SHOULD be
	    LoadNextRecord();

	    // check here for a null record before doing ANYTHING
	    if (DeviceResponse_parsed.length == 1) {
		Logging.main("Null record found in session. Skipping.");
		continue;
	    }

	    MainMenuUI.displayProgressLabel("Reading Record #" + DeviceResponse_parsed[1] + "...");

	    // if it's a D record, skip
	    if (DeviceResponse_parsed[2].equals("D"))
		continue;

	    if (recordIterator != Integer.parseInt(DeviceResponse_parsed[1])) {
	        Logging.main("Re-reading sample #" + Integer.toString(recordIterator));
		LoadSpecifiedRecord(Integer.toString(recordIterator));
	    }

	if (MainMenuUI.countLimiter)
	    reconSession.add(CountLimiter.main(DeviceResponse_parsed));
	else
	    reconSession.add(DeviceResponse_parsed);

	    // check for rollover of memory
	    if (Integer.parseInt(DeviceResponse_parsed[1]) == numDataRecords)
	        recordIterator = -1;
	}
	return true;
    }

    public static void SetDefaultFilename(int sessionIndex) {
	// Get serial number and start date in case we need to use the default filename:
	String ConfirmSN = GetSerialNumber();
	LoadSpecifiedRecord(sessionIndex + 1);

	defaultFilename = "Recon_" + ConfirmSN + "_" + DeviceResponse_parsed[4] + DeviceResponse_parsed[5] + DeviceResponse_parsed[3];
	return;
    }

    public static String GetSessionDescription(int sessionStartPoint) {
	if (sessionStartPoint < 6043)
	    LoadSpecifiedRecord(sessionStartPoint + 1);
	else
	    LoadSpecifiedRecord(0);

	// add US/international preference at a later time
	String description = DeviceResponse_parsed[4] + "/" + DeviceResponse_parsed[5] + "/" + DeviceResponse_parsed[3]
	+ " @ " + DeviceResponse_parsed[6] + ":" + DeviceResponse_parsed[7] + ":" + DeviceResponse_parsed[8];

	return description;
    }

    // Loads filenames into strings here so that it does not need to be done
    // in CreateTXT and CreateXLS individually
    public static void DetermineFileName() {

	String TXT_name = null;			// filename we want to use for the txt
	String XLS_name = null;			// filename we want to use for the xls
	long fileIteration = 1;			// digit to be added to end of filename if the file exists
	boolean DoesReconFileExist = true;	// used to control the loop below
	boolean TXT_exists = false;		// self explanatory
	boolean XLS_exists = false;		// self explanatory
	File TXT_file = null;			// File object for determining if txt exists
	File XLS_file = null;			// File object for determining if xls exists
	int loopCounter = 0;			// count how many times we've gone through the loop

	// Get the first line of txtTestSiteInfo in case we need it:
	String[] tempArray = MainMenu.MainMenuUI.txtTestSiteInfo.getText().split("\\r?\\n");

	//This while loop will check for user input and determine if we need to tack a number onto the end of the filename to avoid overwrites.
        while(DoesReconFileExist==true) {

	    // See if the checkbox is selected, and if so, use the first line of Test Site Info...
	    if (MainMenu.MainMenuUI.chkUseStreetAddressForFilename.isSelected() && MainMenu.MainMenuUI.txtTestSiteInfo.getText().length() > 0) {
		// Don't tack on the digit if the files don't already exist
		if (loopCounter > 0) {
		    TXT_name = InitDirs.dataDir + File.separator + tempArray[0] + "-" + fileIteration + ".txt";
		    XLS_name = InitDirs.dataDir + File.separator + tempArray[0] + "-" + fileIteration + ".xls";
		}
		else {
		    TXT_name = InitDirs.dataDir + File.separator + tempArray[0] + ".txt";
		    XLS_name = InitDirs.dataDir + File.separator + tempArray[0] + ".xls";
		}
	    }
	    // Otherwise, use the contents of the name text box
	    else {
		// First, make sure the user hasn't clobbered the default text.
		if (MainMenu.MainMenuUI.txtNewFileName.getText().length() > 0) {
		    if (loopCounter > 0) {
			TXT_name = InitDirs.dataDir + File.separator + MainMenu.MainMenuUI.txtNewFileName.getText() + "-" + fileIteration + ".txt";
			XLS_name = InitDirs.dataDir + File.separator + MainMenu.MainMenuUI.txtNewFileName.getText() + "-" + fileIteration + ".xls";
		    }
		    else {
		        TXT_name = InitDirs.dataDir + File.separator + MainMenu.MainMenuUI.txtNewFileName.getText() + ".txt";
			XLS_name = InitDirs.dataDir + File.separator + MainMenu.MainMenuUI.txtNewFileName.getText() + ".xls";
		    }
		} // If the box has inadvertently been cleared, use the default from the variable
		else {
		    if (loopCounter > 0) {
			TXT_name = InitDirs.dataDir + File.separator + defaultFilename + "-" + fileIteration + ".txt";
			XLS_name = InitDirs.dataDir + File.separator + defaultFilename + "-" + fileIteration + ".xls";
		    }
		    else {
			TXT_name = InitDirs.dataDir + File.separator + defaultFilename + ".txt";
			XLS_name = InitDirs.dataDir + File.separator + defaultFilename + ".xls";
		    }
		}
	    }

	    // Determine if the files exist with the names we want to use.
            TXT_file = new File(TXT_name);
            XLS_file = new File(XLS_name);
            TXT_exists = TXT_file.exists();
            XLS_exists = XLS_file.exists();

	    // If nothing exists, we're done. Break the loop.
            if(!((TXT_exists==true)||(XLS_exists==true))) {
                DoesReconFileExist = false;
            }
	    // Or increment our end digit if we have to.
            if(DoesReconFileExist==true) {
		fileIteration++;
		loopCounter++;
            }
        }

	// Finally, set the names that are going to be used.
	filenameTXT = new String(TXT_name);
	filenameXLS = new String(XLS_name);
    }

    // This method reads the preferences from FlagForm and will write an appropriate bitmask
    // value to the Recon.
    public static void SetOptionFlag() {
	short flag = 0; // binary number we will be writing to the unit

	if (FlagForm.displayPreferencePres == "mBar")
	    flag += 0x01;
	if (FlagForm.displayPreferenceTemp == "C")
	    flag += 0x04;
	if (FlagForm.displayPreferenceDual == "yes")
	    flag += 0x10;
	if (FlagForm.displayPreferenceUnits == "Bq/m3")
	    flag += 0x20;
	if (FlagForm.displayPreferenceUnits == "CPH")
	    flag += 0x40;
	if (FlagForm.displayPreferenceNoAvg == "Ten Mins.")
	    flag += 0x80;

	Logging.main("Attempting to write flag: " + Integer.toHexString(flag));
	WriteComm.main(ScanComm.scannedPort, WriteOptionsFlag + Integer.toHexString(flag) + "\n");

	return;
    }

    // This method is responsible for both reading the options bitmask from the Recon
    // as well as setting the preference variables in the flag form based on that bitmask.
    public static void ParseOptionFlag() {
	// - issue :RF to get the flag
	WriteComm.main(ScanComm.scannedPort, ReadOptionsFlag);

	try {
	    Thread.sleep(125);

	    String parsedFlag = ReadComm.main(ScanComm.scannedPort, 19);
	    parsedFlag = parsedFlag.replaceAll("[\\n\\r+]", ""); // strip line feeds
	    parsedFlag = parsedFlag.replaceAll("=RF", "");	     // get :RF out of the value
	    Logging.main("ParseOptionFlag(): Parsed flag value is: " + parsedFlag);

	    // get a number we can work with
	    int flagValue = Integer.parseInt(parsedFlag, 16);

	    // - derive temperature setting
	    if ((flagValue & 0x04) == 0)
		FlagForm.displayPreferenceTemp = "F";
	    else
		FlagForm.displayPreferenceTemp = "C";

	    // - derive pressure setting
	    if ((flagValue & 0x01) == 0)
		FlagForm.displayPreferencePres = "inHG";
	    else
		FlagForm.displayPreferencePres = "mBar";

	    if (((flagValue & 0x20) == 0) && ((flagValue & 0x40) == 0))
		FlagForm.displayPreferenceUnits = "pCi/L";
	    else if ((flagValue & 0x20) != 0)
		FlagForm.displayPreferenceUnits = "Bq/m3";
	    else if ((flagValue & 0x40) != 0)
		FlagForm.displayPreferenceUnits = "CPH";

	    // - derive dual chamber
	    if ((flagValue & 0x10) == 0)
		FlagForm.displayPreferenceDual = "no";
	    else
		FlagForm.displayPreferenceDual = "yes";

	    // - derive reporting interval
	    if ((flagValue & 0x80) == 0)
		FlagForm.displayPreferenceNoAvg = "Hourly";
	    else
		FlagForm.displayPreferenceNoAvg = "Ten Mins.";

	}

	catch (Exception anyEx) {
	    Logging.main(anyEx.toString());
	}

	return;
    }

    public static void ClearTamperFlag() {
	WriteComm.main(ScanComm.scannedPort, ResetTamperFlag);
	return;
    }
}
