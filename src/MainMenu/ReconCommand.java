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

    public static String DeviceResponse;
    public static String[] DeviceResponse_parsed;

    public static LinkedList<String[]> reconSession; // container to hold Recon samples
    public static String filenameTXT;
    public static String filenameXLS;
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

    // download Recon session into memory, making data available for multitude of data exporters
    public static boolean DownloadReconSessionToRAM() throws InterruptedException {
	int numDataRecords = (Float.parseFloat(ScanComm.ReconFirmwareVersion) >= 1.34) ? 6043 : 6143;

        // initialize linked list
	if (reconSession != null)
	    reconSession.clear();
	else
	    reconSession = new LinkedList();

	// run :RB and check ST/LT mode
	LoadNewRecord();

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
            TXT_name = InitDirs.dataDir + File.separator + "Recon_" + ConfirmSN + "_" + DeviceResponse_parsed[4] + DeviceResponse_parsed[5] + DeviceResponse_parsed[3] + "-" + fileIteration + ".txt";
            XLS_name = InitDirs.dataDir + File.separator + "Recon_" + ConfirmSN + "_" + DeviceResponse_parsed[4] + DeviceResponse_parsed[5] + DeviceResponse_parsed[3] + "-" + fileIteration + ".xls";
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

    // This method reads the preferences from FlagForm and will write an appropriate bitmask
    // value to the Recon.
    public static boolean SetOptionFlag() {
	String flagResponse = null; // value read from unit to verify success
	short flag = 0; // binary number we will be writing to the unit
	short comp = 9999; // comparison value - 0 may be a valid setting, so we use another number

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

	// do not ask the Recon for a response for one second...
	try {
	    Thread.sleep(1000);
	}
	catch (InterruptedException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        }

	WriteComm.main(ScanComm.scannedPort, ":RF\n"); // load the written value so we can double-check
	DeviceResponse = ReadComm.main(ScanComm.scannedPort, 19);
	DeviceResponse = DeviceResponse.replaceAll("[\\n\\r+]", ""); // strip line feeds

	// search device response until we've found the part we're interested in
	for (int c = 0; c < DeviceResponse.length(); c++) {
	    if (DeviceResponse.charAt(c) == '0' && (DeviceResponse.charAt(c+1) == '0')) {
	        flagResponse = DeviceResponse.substring(c);
		break;
	    }
	}

	// parse number contained in string as hexadecimal value for purposes of comparison
	if (flagResponse != null)
	    comp = Short.parseShort(flagResponse, 16);
	else
	    Logging.main("flagResponse is null! Cannot compare written value.");

	return (flag == comp);
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
