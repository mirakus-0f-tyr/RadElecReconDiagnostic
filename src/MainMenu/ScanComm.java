/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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

public class ScanComm {
   
    public static String ReconWaitTime = "Unknown";
    public static String ReconDurationSetting = "Unknown";
    public static String ReconCalDate = "Unknown";

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
    public static String[] run(Integer OptArgs) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException, ParseException, IOException, WriteException, BiffException {
        boolean foundRecon = false;
        System.out.println("Beginning to scan Comm ports...");

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
	    System.out.println("OS not Windows, Linux, or Mac...");
	}

        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
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
                if(StringUtils.equals(DeviceResponse_targeted,"=DV,CRM,")) {
                    foundRecon = true;
                    if(OptArgs == 1){
                        System.out.println("Rad Elec Recon CRM found!");
                        MainMenuUI.displayProgressLabel("Recon CRM found on " + portNames[i] + "!");
                        String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
                        //A bit of error-handling, just in case the serial number doesn't exist.
                        if(DeviceResponse_parsed.length > 3 && DeviceResponse_parsed[3] != null)
                        {
                            System.out.println("Recon CRM Serial #" + DeviceResponse_parsed[3]);
                            MainMenuUI.displaySerialNumber(DeviceResponse_parsed[3]);
                        } else {
                            System.out.println("Unable to determine Recon CRM Serial#. Rogue instrument detected.");
                        }
                        if(DeviceResponse_parsed.length > 2 && DeviceResponse_parsed[2] != null)
                        {
                            System.out.println("Firmware v" + DeviceResponse_parsed[2]);
                            MainMenuUI.displayFirmwareVersion(DeviceResponse_parsed[2]);
                        } else {
                            System.out.println("Unknown Firmware Version! Probably not good...");
                        }
                        //Let's get the number of data sessions while we're connecting.
                        CheckReconProtocol(scannedPort);
                    } else if(OptArgs == 2) {
                        System.out.println("Checking for records...");
                        MainMenuUI.displayProgressLabel("Checking for records...");
                        if(CheckForNewRecords(scannedPort)==true) {
                            System.out.println("Beginning TXT/XLS file dump...");
                            DownloadNewRecord(scannedPort);
                        }
                    } else if(OptArgs == 3) {
                        System.out.println("Clearing Current Session via :CD command...");
                        ClearSessionMemory(scannedPort);
                        CheckReconProtocol(scannedPort);
                    } else if(OptArgs == 4) {
                        ClearReconMemory(scannedPort);
                        CheckReconProtocol(scannedPort);
                    } else if(OptArgs == 5) {
                        System.out.println("Clearing memory and dumping all data...");
                        DumpAllData(scannedPort);
                        CheckReconProtocol(scannedPort);
                    } else if(OptArgs == 6) {
		        System.out.println("Checking for records...");
			MainMenuUI.displayProgressLabel("Checking for records...");
			if(CheckForNewRecords(scannedPort)==true) {
			    System.out.println("Begin downloading session...");
			    DownloadNewRecord(scannedPort);
			}
		    } else if (OptArgs == 7) {
		        System.out.println("Setting Recon time...");
			ReconCommand.SetReconTimeFromPC();
			MainMenuUI.displayProgressLabel("Time synchronization complete.");
		    } else if (OptArgs == 8) {
			if (ReconCommand.SetOptionFlag()) {
			    System.out.println("Display options saved to unit.");
			    MainMenuUI.displayProgressLabel("Display options saved to unit.");
			    FlagForm.displayOptionsWriteSuccess = true;
			}
			else
			    System.out.println("ERROR: Display options flag NOT written successfully.");
		    }
                }
            scannedPort.closePort();
            }   
            catch (SerialPortException ex) {
                System.out.println(ex);
            }
        }
        if(foundRecon == true) {
            //DecimalFormat TwoDecimalFormat = new DecimalFormat("#.00");
            //return new String[] { ScoutSN, refinedCF, totalError, TwoDecimalFormat.format(ScoutNewCF), totalDays, calStartDate, calEndDate };
            return new String[] { "true" };
        } else {
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
            
            System.out.println(DeviceResponse);
            if(StringUtils.equals(DeviceResponse_targeted,"=DB,")) {
                System.out.println("Recon has a new record waiting to be dumped to a TXT/XLS file.");
                Thread.sleep(125);
                return true;
            } else {
                System.out.println("Recon has no new records.");
                MainMenuUI.displayProgressLabel("Recon has no new records.");
                return false;
            }
        }
        catch (InterruptedException ex) {
                System.out.println(ex);
        }
        return false;
    }
    
    static void CheckReconProtocol(SerialPort scannedPort) throws InterruptedException {
        try {
            System.out.println("Issuing :RP command to determine data session count...");
            Thread.sleep(10);
            WriteComm.main(scannedPort, ReconCommand.ReadProtocol);
            Thread.sleep(10);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            if(DeviceResponse_parsed.length > 3 && DeviceResponse_parsed[3] != null)
            {
                System.out.println("Data sessions in memory: " + DeviceResponse_parsed[3]);
                MainMenuUI.displayDataSessions(DeviceResponse_parsed[3]);
            } else {
                System.out.println("Unable to read number of data sessions in memory.");
            }            
        }
        catch (InterruptedException ex) {
            System.out.println(ex);
        }
    }
    
    static void DownloadNewRecord(SerialPort scannedPort) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException, ParseException, IOException, WriteException, BiffException {
        try {
	    ReconCommand.DetermineFileName();
	    ReconCommand.DownloadReconSessionToRAM();

	    // create the files
	    if (MainMenuUI.diagnosticMode) {
	        CreateTXT.main();
		CreateXLS.main();
	    }
	    else {
	        CreateTXT.main();
	    }

	    MainMenuUI.checkFilesWrittenSuccessfully();
	    MainMenuUI.HandleSessionClear();
            MainMenuUI.checkAutoLoadFile();
        }

        catch (InterruptedException ex) {
            System.out.println(ex);
        }
}

    static void DumpAllData(SerialPort scannedPort) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException, ParseException, IOException, WriteException {
        try {

            String ConfirmSN = "Unknown";
            
            Thread.sleep(10);
            WriteComm.main(scannedPort, ReconCommand.ReconConfirm); //Check the Recon to see if a new record exists.
            Thread.sleep(10);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            ConfirmSN = DeviceResponse_parsed[3];
            //This is needed to remove the carriage-return at the end of the serial number, as it's the last element in the array.
            ConfirmSN = ConfirmSN.replaceAll("[\n\r]", "");
            System.out.println("Confirming Recon S/N #" + ConfirmSN + " for total data dump.");
            
            Thread.sleep(10);
            WriteComm.main(scannedPort, ReconCommand.ClearMemoryCommand); //Check the Recon to see if a new record exists.
            Thread.sleep(10);
            DeviceResponse = ReadComm.main(scannedPort, 19);
            DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            String DeviceResponse_targeted = "Let's begin!";
            PrintWriter writer = new PrintWriter("data/Recon_" + ConfirmSN + "_AllData.txt", "UTF-8");
            //writer.println(DeviceResponse);

            //Initialize i.
            int i = 0;
            DeviceResponse_targeted = "=DB";
            while((DeviceResponse_targeted.equals("=DB"))){
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
                writer.println(DeviceResponse);
                MainMenuUI.displayProgressLabel("Reading Record #" + DeviceResponse_parsed[1] + "...");
                i++;
            }
            writer.close();
            MainMenuUI.displayProgressLabel("Data dump successful.");
            System.out.println("Data dump should be successful. If you're reading this, we didn't crash or get locked in a never-ending loop.");
        }
        catch (InterruptedException ex) {
            System.out.println(ex);
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
                System.out.println("There were no new records on this CRM; no session to delete.");
                MainMenuUI.displayProgressLabel("No data sessions found.");
                return;
            }
            Thread.sleep(50);
            WriteComm.main(scannedPort, ReconCommand.ClearSessionCommand);
            System.out.println("Clearing Memory via :CD command...");
            System.out.println(ReconCommand.ClearSessionCommand);
            Thread.sleep(50);
            WriteComm.main(scannedPort, ReconCommand.CheckNewRecord);
            DeviceResponse = ReadComm.main(scannedPort, 19);
            DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=BD")) {
                System.out.println("Ambiguous =BD response from :CD command. Assume the session has been cleared?");
                MainMenuUI.displayProgressLabel("Session probably cleared.");
            } else if(DeviceResponse_targeted.equals("=OK")) {
                System.out.println("Data session cleared.");
                MainMenuUI.displayProgressLabel("Data session cleared.");
            } else {
                System.out.println("Unexpected response from CRM.");
                MainMenuUI.displayProgressLabel("Unable to clear session pointer.");
            }
        }
        catch (InterruptedException ex) {
            System.out.println(ex);
        }
    }
    
    //This sub will issue the :CM command, then check to make sure that no record pointers exist.
    static void ClearReconMemory(SerialPort scannedPort) throws InterruptedException {
        try {
            Thread.sleep(125);
            System.out.println("Issuing :CM command to clear all pointers...");
            WriteComm.main(scannedPort, ReconCommand.ClearMemoryCommand);
            System.out.println(ReconCommand.ClearMemoryCommand);
            Thread.sleep(125);
            WriteComm.main(scannedPort, ReconCommand.CheckNewRecord);
            Thread.sleep(125);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=BD")) {
                System.out.println("Ambiguous =BD response from :CM command. Assume all memory pointers have been cleared?");
                System.out.println("Why are we getting =BD here? It should be =OK...");
                MainMenuUI.displayProgressLabel("All pointers probably cleared.");
            } else if(DeviceResponse_targeted.equals("=OK")) {
                System.out.println("All memory pointers have been cleared!");
                MainMenuUI.displayProgressLabel("All pointers cleared!");
            } else {
                System.out.println("Unexpected response from CRM.");
                MainMenuUI.displayProgressLabel("Unable to clear memory.");
            }
        }
        catch (InterruptedException ex) {
            System.out.println(ex);
        }
    }
    
    //This sub will attempt to pull the calibration factors for each chamber.
    static String[] CheckCalibrationFactors(SerialPort scannedPort) throws InterruptedException {
        try {
            Thread.sleep(125);
            System.out.println("Issuing :RL command to determine CF1 and CF2.");
            WriteComm.main(scannedPort, ReconCommand.ReadCalibrationFactors);
            Thread.sleep(125);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=RL")) {
                String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
                System.out.println("CF1 = " + DeviceResponse_parsed[1]);
                System.out.println("CF2 = " + DeviceResponse_parsed[2]);
                return new String[] { DeviceResponse_parsed[1],DeviceResponse_parsed[2] };
            } else {
                System.out.println("Unexpected response when trying to read calibration factors!");
            }
        }
        catch (InterruptedException ex) {
            System.out.println(ex);
        }
        return new String[] {"0","0"};
    }
    
    public static String GetCalibrationDate(SerialPort scannedPort) throws InterruptedException {
        try {
            Thread.sleep(125);
            System.out.println("Issuing :RL command to determine calibration date.");
            WriteComm.main(scannedPort, ReconCommand.ReadCalibrationFactors);
            Thread.sleep(125);
            String CalDate;
            Integer TempYear;
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=RL")) {
                String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
                CalDate = DeviceResponse_parsed[4] + "/" + DeviceResponse_parsed[5] + "/20" + DeviceResponse_parsed[3]; //default US format works for now
                System.out.println("Calibration Date = " + CalDate);
                return CalDate;
            } else {
                System.out.println("Unexpected response when trying to read calibration date!");
            }
        } catch (InterruptedException ex) {
            System.out.println(ex);
        }
        System.out.println("Unable to determine calibration date!");
        return "Unknown";
    }
}
