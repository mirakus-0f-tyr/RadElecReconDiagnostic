/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import Config.FlagForm;
import java.io.StringWriter;

import static MainMenu.InitDirs.*;

public class ScanComm {
   
    // Are these used?
    public static String ReconWaitTime = "Unknown";
    public static String ReconDurationSetting = "Unknown";
    public static String ReconCalDate = "Unknown";

    public static String ReconFirmwareVersion;

    public static boolean connectedDeviceIsRecon = false;

    // connected port
    public static SerialPort scannedPort;

    // string to be used in OS determination
    public static String userOS = System.getProperty("os.name").toLowerCase();

    public static void main(String[] args) {
        try {
            ScanComm obj = new ScanComm();
            obj.run(1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //OptArgs are as follows:
    //1 = Scan For Recon CRM (do this first!)
    //2 = Create Text File from current record
    //3 = Move to Next Record
    //4 = Clear All Memory (Remove all pointers)
    //5 = Clear Memory and All Data Dump
    //6 = Download end-user text file - do not generate spreadsheet
    //7 = synchronize Recon time to PC
    //8 = write flag to Recon (display options)
    //9 = clear tamper flag
    //10 = read Recon options flag (display options)
    //11 = read Recon to get filename data
    //12 = read Recon to get pointer table find start addresses
    public static String[] run(Integer OptArgs) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException, ParseException, IOException, WriteException, BiffException {
        boolean foundRecon = false;
        Logging.main("Beginning to scan Comm ports...");

	String[] portNames;

	// search for com port name depending on OS
	if (userOS.contains("win"))
	    portNames = SerialPortList.getPortNames();
	else if (userOS.contains("linux"))
            portNames = SerialPortList.getPortNames("/dev/", Pattern.compile("tty(ACM[0-9]{1,2})"));
	else if (userOS.contains("mac"))
	    portNames = SerialPortList.getPortNames("/dev/", Pattern.compile("tty.usbmode*"));
	else {
	    portNames = SerialPortList.getPortNames();	// initialize anyway if no matches
	    Logging.main("OS not Windows, Linux, or Mac...");
	}

        for(int i = 0; i < portNames.length; i++){
            Logging.main(portNames[i]);
            scannedPort = new SerialPort(portNames[i]);

            try {
                MainMenuUI.displayProgressLabel("Searching for Rad Elec Recon...");
                scannedPort.openPort();//Open serial port
                scannedPort.setParams(scannedPort.BAUDRATE_9600,
			             scannedPort.DATABITS_8,
			             scannedPort.STOPBITS_1,
			             scannedPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
                Thread.sleep(125);
                WriteComm.main(scannedPort, ReconCommand.ReconConfirm); //Test command to see if the device responds appropriately
                Thread.sleep(125);
                String DeviceResponse = ReadComm.main(scannedPort, 19);
                String DeviceResponse_targeted = StringUtils.left(DeviceResponse,8);

                if (DeviceResponse == null) { // whatever is attached to the COM port doesn't respond
		    Logging.main("Non-communicative device at " + portNames[i]);
		    scannedPort.closePort();
		    continue;
                }

                if(StringUtils.equals(DeviceResponse_targeted,"=DV,CRM,")) {
                    foundRecon = true;
                    connectedDeviceIsRecon = true;
                    
		    switch (OptArgs) {

		    	case 1:
			    Logging.main("Rad Elec Recon CRM found!");
			    MainMenuUI.displayProgressLabel("Recon CRM found on " + portNames[i] + "!");
			    String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
			    
			    //A bit of error-handling, just in case the serial number doesn't exist.
			    if(DeviceResponse_parsed.length > 3 && DeviceResponse_parsed[3] != null)
			    {
			        Logging.main("Recon CRM Serial #" + DeviceResponse_parsed[3]);
			     MainMenuUI.displaySerialNumber(DeviceResponse_parsed[3]);
			    }
			    else
			        Logging.main("Unable to determine Recon CRM Serial#. Rogue instrument detected.");
			    
			    if(DeviceResponse_parsed.length > 2 && DeviceResponse_parsed[2] != null)
			    {
			        Logging.main("Firmware v" + DeviceResponse_parsed[2]);
			        ReconFirmwareVersion = DeviceResponse_parsed[2];
			     MainMenuUI.displayFirmwareVersion(ReconFirmwareVersion);
			    }
			    else
			        Logging.main("Unknown Firmware Version! Probably not good...");
			    
			    //Let's get the number of data sessions while we're connecting.
			    CheckReconProtocol(scannedPort);
			    break;

                    	case 2:
			    Logging.main("Checking for records...");
			    MainMenuUI.displayProgressLabel("Checking for records...");

			    if(CheckForNewRecords(scannedPort)==true) {
			        Logging.main("Beginning TXT/XLS file dump...");
			        DownloadNewRecord(scannedPort);
			    }
			    break;

                    	case 3:
			    Logging.main("Clearing Current Session via :CD command...");
			    ClearSessionMemory(scannedPort);
			    CheckReconProtocol(scannedPort);
			    break;

                    	case 4:
			    ClearReconMemory(scannedPort);
			    CheckReconProtocol(scannedPort);
			    break;

                    	case 5:
			    Logging.main("Clearing memory and dumping all data...");
			    DumpAllData(scannedPort);
			    CheckReconProtocol(scannedPort);
			    break;

                    	case 6:
		            Logging.main("Checking for records...");
			    MainMenuUI.displayProgressLabel("Checking for records...");
			    
			    if(CheckForNewRecords(scannedPort)==true) {
			    	Logging.main("Begin downloading session...");
			    	DownloadNewRecord(scannedPort);
			    }
			    break;

		    	case 7:
		            Logging.main("Setting Recon time...");
			    ReconCommand.SetReconTimeFromPC();
			    MainMenuUI.displayProgressLabel("Time synchronization complete.");
			    break;

		    	case 8:
			    Logging.main("ScanComm: Connecting to set options bitmask.");
			    ReconCommand.SetOptionFlag();
			    Logging.main("Display options saved to unit.");
			    MainMenuUI.displayProgressLabel("Display options saved to unit.");
			    FlagForm.displayOptionsWriteSuccess = true;
			    break;

		    	case 9:
		    	    Logging.main("Clearing tamper flag.");
			    ReconCommand.ClearTamperFlag();
			    MainMenuUI.displayProgressLabel("Tamper flag cleared.");
			    break;

		    	case 10:
			    Logging.main("ScanComm: Connecting to parse options bitmask.");
			    ReconCommand.ParseOptionFlag();
			    break;

		    	case 11:
			    Logging.main("ScanComm: Connecting to determine default file name.");
			    ReconCommand.SetDefaultFilename(ReconCommand.sessionAddresses.get(ReconCommand.currentSession));
			    break;

			case 12:
			    Logging.main("ScanComm: Connecting to read pointer table.");
			    ReconCommand.GetPointerTable();

			    for (int z = 0; z < MainMenuUI.getDataSessions(); z++) {
				MainMenuUI.sessionStrings.add(ReconCommand.GetSessionDescription(ReconCommand.sessionAddresses.get(z)));
			    }
			    break;

			default:
			    break;
		    } // end switch

		    // we've found our Recon -- do not scan any further ports (break the loop)
		    connectedDeviceIsRecon = false;
		    scannedPort.closePort();
		    break;
                } // end if Recon found

                else { // device responds but doesn't give expected response for a Recon
		    Logging.main("Device at " + portNames[i] + " responded but is not a Recon.");
		    scannedPort.closePort();
		    continue;
                }
            } // end try

            catch (SerialPortException ex) {
                StringWriter swEx = new StringWriter();
                ex.printStackTrace(new PrintWriter(swEx));
                String strEx = swEx.toString();
                Logging.main(strEx);
            }

        } // end for each port

        if(foundRecon == true) {
            //DecimalFormat TwoDecimalFormat = new DecimalFormat("#.00");
            //return new String[] { ScoutSN, refinedCF, totalError, TwoDecimalFormat.format(ScoutNewCF), totalDays, calStartDate, calEndDate };
            return new String[] { "true" };
        }
	else {
            MainMenuUI.displayProgressLabel("No Recon CRM found...");
            return new String[] { "false" };
        }

    }
    
    //CheckForNewRecords will return true if there is a new record on the Recon.
    //It issues the command :RB. If the response begins with =DB, then we can assume
    //that there is a new record on the device.
    static Boolean CheckForNewRecords(SerialPort scannedPort) throws InterruptedException {
        try {
            Thread.sleep(125);
            WriteComm.main(scannedPort, ReconCommand.CheckNewRecord); //Check the Recon to see if a new record exists.
            Thread.sleep(125);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String DeviceResponse_targeted = StringUtils.left(DeviceResponse,4);
            
            Logging.main(DeviceResponse);
            if(StringUtils.equals(DeviceResponse_targeted,"=DB,")) {
                Logging.main("Recon has a new record waiting to be dumped to a TXT/XLS file.");
                Thread.sleep(125);
                return true;
            } else {
                Logging.main("Recon has no new records.");
                MainMenuUI.displayProgressLabel("Recon has no new records.");
                return false;
            }
        }
        catch (InterruptedException ex) {
                StringWriter swEx = new StringWriter();
                ex.printStackTrace(new PrintWriter(swEx));
                String strEx = swEx.toString();
                Logging.main(strEx);
        }
        return false;
    }
    
    static void CheckReconProtocol(SerialPort scannedPort) throws InterruptedException {
        try {
            Logging.main("Issuing :RP command to determine data session count...");
            Thread.sleep(10);
            WriteComm.main(scannedPort, ReconCommand.ReadProtocol);
            Thread.sleep(10);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            if(DeviceResponse_parsed.length > 3 && DeviceResponse_parsed[3] != null)
            {
                Logging.main("Data sessions in memory: " + DeviceResponse_parsed[3]);
                MainMenuUI.displayDataSessions(DeviceResponse_parsed[3]);
            } else {
                Logging.main("Unable to read number of data sessions in memory.");
            }            
        }
        catch (InterruptedException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        }
    }
    
    static void DownloadNewRecord(SerialPort scannedPort) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException, ParseException, IOException, WriteException, BiffException {
        try {
	    ReconCommand.DetermineFileName();

	    if (ReconCommand.DownloadReconSessionToRAM()) {
		// create the files
		if ((MainMenuUI.diagnosticMode) || (MainMenuUI.createXLS)) {
		    CreateTXT.main();
		    CreateXLS.main();
		}
		else
		    CreateTXT.main();

		MainMenuUI.checkFilesWrittenSuccessfully();
		MainMenuUI.checkAutoLoadFile();
	    }
	    else {
		Logging.main("Error downloading Recon data. Data not saved.");
		return;
	    }
	}

        catch (InterruptedException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        }
}

    static void DumpAllData(SerialPort scannedPort) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException, ParseException, IOException, WriteException {
        try {

            String ConfirmSN = "Unknown";
	    int numDataRecords;
	    int diagBufferLength = 100;
	    boolean diagCircularBuffer = (Float.parseFloat(ReconFirmwareVersion) >= 1.34);
            
            Thread.sleep(10);
            WriteComm.main(scannedPort, ReconCommand.ReconConfirm); //Check the Recon to see if a new record exists.
            Thread.sleep(10);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
	    String dataLine; // This is the final, modified line that will be written to the file.
            String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            ConfirmSN = DeviceResponse_parsed[3];
            //This is needed to remove the carriage-return at the end of the serial number, as it's the last element in the array.
            ConfirmSN = ConfirmSN.replaceAll("[\n\r]", "");
            Logging.main("Confirming Recon S/N #" + ConfirmSN + " for total data dump.");
            
            Thread.sleep(10);
            DeviceResponse = ReadComm.main(scannedPort, 19);
            DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            String DeviceResponse_targeted = "Let's begin!";
            PrintWriter writer = new PrintWriter(dataDir + File.separator + "Recon_" + ConfirmSN + "_AllData.txt", "UTF-8");

	    numDataRecords = diagCircularBuffer ? 6044 : 6144;

            //Initialize i.
            int i = 0;
            DeviceResponse_targeted = "=DB";

	     // Dump data buffer
	    while (i < numDataRecords) {
                if(i==0){
                    WriteComm.main(scannedPort, ReconCommand.ReadFirstRecord);
                } else {
                    WriteComm.main(scannedPort, ReconCommand.ReadNextRecord);
                }
                DeviceResponse = ReadComm.main(scannedPort, 19);
                DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
                if(DeviceResponse_targeted.equals("=DB")) { 
                    DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
                }
		dataLine = DeviceResponse.replaceAll("[\n\r]", "");
                writer.println(dataLine);
                MainMenuUI.displayProgressLabel("Reading Record #" + Integer.toString(i) + "...");
                i++;
            }

	    if (diagCircularBuffer) {
		Logging.main("Finished primary data dump. Dumping diagnostic records buffer...");
		writer.println("-------------------------------------------------------------------------------------------");
		writer.println("DIAGNOSTIC BUFFER");
		writer.println("-------------------------------------------------------------------------------------------");

		i = 0;

		// Dump diagnostic buffer
		while (i < diagBufferLength) {
		    if (i==0)
			WriteComm.main(scannedPort, ReconCommand.ReadFirstDiagnosticRecord);
		    else
			WriteComm.main(scannedPort, ReconCommand.ReadNextDiagnosticRecord);

		    DeviceResponse = ReadComm.main(scannedPort, 19);
                    DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);

		    if(DeviceResponse_targeted.equals("=DB")) {
			DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
                    }
		    dataLine = DeviceResponse.replaceAll("[\n\r]", "");
		    writer.println(dataLine);
                   MainMenuUI.displayProgressLabel("Reading Diagnostic Record #" + Integer.toString(i) + "...");
                   i++;
		}
	    }

            writer.close();
            MainMenuUI.displayProgressLabel("Data dump successful.");
            Logging.main("Data dump should be successful. If you're reading this, we didn't crash or get locked in a never-ending loop.");
        }
        catch (InterruptedException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        }
    }    
    
    //This sub will issue the :CD command, then check to make sure that no record pointers exist.
    static void ClearSessionMemory(SerialPort scannedPort) throws InterruptedException {
        try {
            long RecordHeader = 0;
            WriteComm.main(scannedPort, ReconCommand.CheckNewRecord);
            Thread.sleep(50);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=BD")) {
                Logging.main("There were no new records on this CRM; no session to delete.");
                MainMenuUI.displayProgressLabel("No data sessions found.");
                return;
            }
            Thread.sleep(50);
            WriteComm.main(scannedPort, ReconCommand.ClearSessionCommand);
            Logging.main("Clearing Memory via :CD command...");
            Logging.main(ReconCommand.ClearSessionCommand);
            Thread.sleep(50);
            WriteComm.main(scannedPort, ReconCommand.CheckNewRecord);
            DeviceResponse = ReadComm.main(scannedPort, 19);
            DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=BD")) {
                Logging.main("Ambiguous =BD response from :CD command. Assume the session has been cleared?");
                MainMenuUI.displayProgressLabel("Session probably cleared.");
            } else if(DeviceResponse_targeted.equals("=OK")) {
                Logging.main("Data session cleared.");
                MainMenuUI.displayProgressLabel("Data session cleared.");
            } else {
                Logging.main("Unexpected response from CRM.");
                MainMenuUI.displayProgressLabel("Unable to clear session pointer.");
            }
        }
        catch (InterruptedException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        }
    }
    
    //This sub will issue the :CM command, then check to make sure that no record pointers exist.
    static void ClearReconMemory(SerialPort scannedPort) throws InterruptedException {
        try {
            Thread.sleep(125);
            Logging.main("Issuing :CM command to clear all pointers...");
            WriteComm.main(scannedPort, ReconCommand.ClearMemoryCommand);
            Logging.main(ReconCommand.ClearMemoryCommand);
            Thread.sleep(125);
            WriteComm.main(scannedPort, ReconCommand.CheckNewRecord);
            Thread.sleep(125);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=BD")) {
                Logging.main("Ambiguous =BD response from :CM command. Assume all memory pointers have been cleared?");
                Logging.main("Why are we getting =BD here? It should be =OK...");
                MainMenuUI.displayProgressLabel("All pointers probably cleared.");
            } else if(DeviceResponse_targeted.equals("=OK")) {
                Logging.main("All memory pointers have been cleared!");
                MainMenuUI.displayProgressLabel("All pointers cleared!");
            } else {
                Logging.main("Unexpected response from CRM.");
                MainMenuUI.displayProgressLabel("Unable to clear memory.");
            }
        }
        catch (InterruptedException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        }
    }
    
    //This sub will attempt to pull the calibration factors for each chamber.
    static String[] CheckCalibrationFactors(SerialPort scannedPort) throws InterruptedException {
        try {
            Thread.sleep(125);
            Logging.main("Issuing :RL command to determine CF1 and CF2.");
            WriteComm.main(scannedPort, ReconCommand.ReadCalibrationFactors);
            Thread.sleep(125);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=RL")) {
                String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
                Logging.main("CF1 = " + DeviceResponse_parsed[1]);
                Logging.main("CF2 = " + DeviceResponse_parsed[2]);
                return new String[] { DeviceResponse_parsed[1],DeviceResponse_parsed[2] };
            } else {
                Logging.main("Unexpected response when trying to read calibration factors!");
            }
        }
        catch (InterruptedException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        }
        return new String[] {"0","0"};
    }
    
    public static String GetCalibrationDate(SerialPort scannedPort) throws InterruptedException {
        try {
            Thread.sleep(125);
            Logging.main("Issuing :RL command to determine calibration date.");
            WriteComm.main(scannedPort, ReconCommand.ReadCalibrationFactors);
            Thread.sleep(125);
            String CalDate;
            Integer TempYear;
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=RL")) {
                String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
                CalDate = DeviceResponse_parsed[4] + "/" + DeviceResponse_parsed[5] + "/20" + DeviceResponse_parsed[3]; //default US format works for now
                Logging.main("Calibration Date = " + CalDate);
                return CalDate;
            } else {
                Logging.main("Unexpected response when trying to read calibration date!");
            }
        } catch (InterruptedException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        }
        Logging.main("Unable to determine calibration date!");
        return "Unknown";
    }
}
