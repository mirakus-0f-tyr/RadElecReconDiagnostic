/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.read.biff.BiffException;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

//import jssc.SerialPortTimeoutException;
/**
 *
 * @author Rad Elec Inc.
 */
public class ScanComm {

    public static String ReconConfirm = ":RV\r\n";
    public static String ReadProtocol = ":RP\r\n";
    public static String CheckNewRecord = ":RB\r\n";
    public static String ReadNextRecord = ":RN\r\n";
    public static String ReadFirstRecord = ":RN1\r\n";
    public static String ClearMemoryCommand = ":CM\r\n";
    public static String ClearSessionCommand = ":CD\r\n";
    public static String ReadCalibrationFactors = ":RL\r\n";
    
    //Old variable block
    public static String ScoutSN = "Unknown";
    public static String refinedCF = "Unknown";
    public static String totalError = "Unknown";
    public static String totalDays = "Unknown";
    public static long totalCounts = 0;
    public static long totalRecords = 0;
    public static double averageRadonConc = 0;
    public static double ScoutNewCF = 0;
    public static double targetRadonConc = 0;
    public static String calStartDate = "Unknown";
    public static String calEndDate = "Unknown";
    public static int fullYear = 2000;
    public static String ReconWaitTime = "Unknown";
    public static String ReconDurationSetting = "Unknown";
    
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
    public static String[] run(Integer OptArgs) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException, ParseException, IOException, WriteException, BiffException {
        boolean foundRecon = false;
        System.out.println("Beginning to scan Comm ports...");
        String[] portNames = SerialPortList.getPortNames();
        for(int i = 0; i < portNames.length; i++){
            System.out.println(portNames[i]);
            SerialPort scannedPort = new SerialPort(portNames[i]);

            try {
                MainMenuUI.displayProgressLabel("Searching for Rad Elec Recon...");
                scannedPort.openPort();//Open serial port
                scannedPort.setParams(scannedPort.BAUDRATE_9600,
                                 scannedPort.DATABITS_8,
                                 scannedPort.STOPBITS_1,
                                 scannedPort.PARITY_NONE);//Set params. Also you can set params by this string: serialPort.setParams(9600, 8, 1, 0);
                Thread.sleep(125);
                WriteComm.main(scannedPort, ReconConfirm); //Test command to see if the device responds appropriately
                Thread.sleep(125);
                String DeviceResponse = ReadComm.main(scannedPort, 19);
                String DeviceResponse_targeted = StringUtils.left(DeviceResponse,8);
                if(StringUtils.equals(DeviceResponse_targeted,"=DV,CRM,")) {
                    foundRecon = true;
                    if(OptArgs == 1){
                        System.out.println("Rad Elec Recon CRM found!");
                        MainMenuUI.displayProgressLabel("Recon CRM found on " + portNames[i] + "!"); // Comment from John: TODO - name Linux device nodes such as ttyACM0
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
            WriteComm.main(scannedPort, CheckNewRecord); //Check the Recon to see if a new record exists.
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
            WriteComm.main(scannedPort, ReadProtocol);
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
            
            //Variable declarations
            double AvgHumidity = 0;
            double AvgTemperature = 0;
            double AvgPressure = 0;
            long TotalMovements = 0;
            long TotalChamber1Counts = 0;
            long TotalChamber2Counts = 0;
            double CF1 = 0;
            double CF2 = 0;
            boolean BeginAveraging = false;
            long ActiveRecordCounts = 0;
            int TempYear = 0;
            DateTimeFormatter DateTimeDisplay = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            LocalDateTime StartDate = null;
            LocalDateTime EndDate = null;
            DecimalFormat RoundAvg = new DecimalFormat("####0.00");
            long totalSeconds = 0;
            long testHours = 0;
            long testMinutes = 0;
            long testSeconds = 0;
            long i = 0;
            //These following variables are for the dynamic file-naming process.
            String ConfirmSN = "Unknown";
            String TXT_name = "Unknown";
            String XLS_name = "Unknown";
            File TXT_file;
            File XLS_file;
            long fileIteration = 1;
            boolean DoesReconFileExist = true;
            boolean TXT_exists = false;
            boolean XLS_exists = false;

            // declaration and null assignment for pointers used in spreadsheet creation
	    // App will not compile when these are declared/assigned in blocks
	    // controlled by a conditional (diagnosticMode) below.
            WritableWorkbook XLfile = null;
            WritableSheet sheet = null;
	    WritableCellFormat XL_Decimal10_Format = null;
            WritableCellFormat XL_Decimal100_Format = null;
	    Number Recon_RecordNumber = null;
	    Label Recon_Flag = null;
	    Number Recon_Year = null;
	    Number Recon_Month = null;
	    Number Recon_Day = null;
	    Number Recon_Hour = null;
	    Number Recon_Minute = null;
	    Number Recon_Second = null;
	    Number Recon_Chamber1Count = null;
	    Number Recon_Chamber2Count = null;
	    Formula Recon_Chamber1CountPerHour = null;
	    Formula Recon_Chamber2CountPerHour = null;
	    Formula Recon_Chamber1RadonConc = null;
	    Formula Recon_Chamber2RadonConc = null;
	    Number Recon_MainInputVoltage = null;
	    Number Recon_BattVoltage = null;
	    Number Recon_HumidityMin = null;
	    Number Recon_HumidityAvg = null;
	    Number Recon_HumidityMax = null;
	    Number Recon_PressureMin = null;
	    Number Recon_PressureAvg = null;
	    Number Recon_PressureMax = null;
	    Number Recon_TemperatureMin = null;
	    Number Recon_TemperatureAvg = null;
	    Number Recon_TemperatureMax = null;
	    Number Recon_Movements = null;
	    Number Recon_CurrentAvg = null;
	    Number Recon_HVSupply = null;
	    Number Recon_RecordsInSample = null;
	    Number Recon_CF1 = null;
	    Number Recon_CF2 = null;

	    //End declarations
            
            Thread.sleep(10);
            WriteComm.main(scannedPort, ReconConfirm); //Check the Recon to see if a new record exists.
            Thread.sleep(10);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            ConfirmSN = DeviceResponse_parsed[3];
            //This is needed to remove the carriage-return at the end of the serial number, as it's the last element in the array.
            ConfirmSN = ConfirmSN.replaceAll("[\n\r]", "");
            System.out.println("Confirming Recon S/N #" + ConfirmSN + ".");
            Thread.sleep(10);
            String[] CF_Array = CheckCalibrationFactors(scannedPort); //Let's pull the calibration factors
            CF1 = Double.parseDouble(CF_Array[0]) / 1000; //We need to add error-handling for this...
            CF2 = Double.parseDouble(CF_Array[1]) / 1000; //We need to add error-handling for this, too...
            Thread.sleep(10);
            WriteComm.main(scannedPort, CheckNewRecord); //We need to go to the :RB first.
            Thread.sleep(10);
            DeviceResponse = ReadComm.main(scannedPort, 19);
            DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            ReconWaitTime = DeviceResponse_parsed[12];
            ReconDurationSetting = DeviceResponse_parsed[13];
            Thread.sleep(10);
            WriteComm.main(scannedPort, ReadNextRecord); //Now, we need to issue :RN in order to pull the first date from the data session.
            Thread.sleep(10);
            DeviceResponse = ReadComm.main(scannedPort, 19);
            DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            System.out.println("Current data session shows start date of " + DeviceResponse_parsed[4] + "/" + DeviceResponse_parsed[5] + "/" + DeviceResponse_parsed[3] + " @ " + DeviceResponse_parsed[6] + ":" + DeviceResponse_parsed[7] + ".");
            
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
            
            PrintWriter writer = new PrintWriter("data/Recon_" + ConfirmSN + "_" + DeviceResponse_parsed[4] + DeviceResponse_parsed[5] + DeviceResponse_parsed[3] + "-" + fileIteration + ".txt", "UTF-8");
            //writer.println(DeviceResponse);
            
            //For Excel spreadsheet. Ye gods, this sub is turning into a bloody hornet's nest...
            if (MainMenuUI.diagnosticMode) {
	    Workbook workbook = Workbook.getWorkbook(new File("ReconTemplate.xls"));
            XLfile = Workbook.createWorkbook(new File("data/Recon_" + ConfirmSN + "_" + DeviceResponse_parsed[4] + DeviceResponse_parsed[5] + DeviceResponse_parsed[3] + "-" + fileIteration + ".xls"), workbook);
            workbook.close();
            sheet = XLfile.getSheet(0);
            //Excel formats, so everything doesn't get passed over as a lousy string.
            //We'll likely need to catch errors in a future version, but I don't have the time to deal with that now.
            XL_Decimal10_Format = new WritableCellFormat(new NumberFormat("0.0"));
            XL_Decimal10_Format.setAlignment(Alignment.CENTRE);
            XL_Decimal100_Format = new WritableCellFormat(new NumberFormat("0.00"));
            XL_Decimal100_Format.setAlignment(Alignment.CENTRE);
	    } // end !diagnosticMode omission
            
            Thread.sleep(10);
            WriteComm.main(scannedPort, CheckNewRecord); //Check the Recon to see if a new record exists.
            Thread.sleep(10);
            DeviceResponse = ReadComm.main(scannedPort, 19);
            DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            writer.println(DeviceResponse);
            
            while(!(DeviceResponse_parsed[2].equals("Z"))){
                if(DeviceResponse_parsed[2].equals("S")){
                    BeginAveraging = true;
                    TempYear = 2000 + Integer.parseInt(DeviceResponse_parsed[3]);
                    StartDate = LocalDateTime.of(TempYear, Integer.parseInt(DeviceResponse_parsed[4]), Integer.parseInt(DeviceResponse_parsed[5]), Integer.parseInt(DeviceResponse_parsed[6]), Integer.parseInt(DeviceResponse_parsed[7]), Integer.parseInt(DeviceResponse_parsed[8]));
                }
                if(DeviceResponse_parsed[2].equals("E")){
                    TempYear = 2000 + Integer.parseInt(DeviceResponse_parsed[3]);
                    EndDate = LocalDateTime.of(TempYear, Integer.parseInt(DeviceResponse_parsed[4]), Integer.parseInt(DeviceResponse_parsed[5]), Integer.parseInt(DeviceResponse_parsed[6]), Integer.parseInt(DeviceResponse_parsed[7]), Integer.parseInt(DeviceResponse_parsed[8]));
                }
                if(BeginAveraging == true) {
                    ActiveRecordCounts++;
                    TotalMovements = TotalMovements + Long.parseLong(DeviceResponse_parsed[9]);
                    TotalChamber1Counts = TotalChamber1Counts + Long.parseLong(DeviceResponse_parsed[10]);
                    TotalChamber2Counts = TotalChamber2Counts + Long.parseLong(DeviceResponse_parsed[11]);
                    AvgHumidity = (AvgHumidity + Double.parseDouble(DeviceResponse_parsed[15]));
                    AvgPressure = (AvgPressure + Double.parseDouble(DeviceResponse_parsed[18]));
                    AvgTemperature = (AvgTemperature + Double.parseDouble(DeviceResponse_parsed[21]));
                    //System.out.println("AvgHum=" + AvgHumidity + ", AvgPress=" + AvgPressure + ", AvgTemp=" + AvgTemperature);
                }
                
                Thread.sleep(10);
                WriteComm.main(scannedPort, ReadNextRecord);
                Thread.sleep(10);
                DeviceResponse = ReadComm.main(scannedPort, 19);
                DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
                writer.println(DeviceResponse);
                MainMenuUI.displayProgressLabel("Reading Record #" + DeviceResponse_parsed[1] + "...");
                
                //Spreadsheet Stuff
                //Move this to another method so we can have a semblance of order...
                if (MainMenuUI.diagnosticMode) {

		if(!(DeviceResponse_parsed[2].equals("Z"))){
                    int rows_total=sheet.getRows();
                    sheet.insertRow(rows_total+1);
                    //This i incrementer is used for determining when to record the counts per hour.
                    if((DeviceResponse_parsed[2].equals("S"))||(DeviceResponse_parsed[2].equals("I"))){
                        i++;
                    }
                    System.out.println(i);
                    Recon_RecordNumber = new Number(0, rows_total, Long.parseLong(DeviceResponse_parsed[1]));
                    sheet.addCell(Recon_RecordNumber);
                    Recon_Flag = new Label(1, rows_total, DeviceResponse_parsed[2]);
                    sheet.addCell(Recon_Flag);
                    Recon_Year = new Number(2, rows_total, Long.parseLong(DeviceResponse_parsed[3]));
                    sheet.addCell(Recon_Year);
                    Recon_Month = new Number(3, rows_total, Long.parseLong(DeviceResponse_parsed[4]));
                    sheet.addCell(Recon_Month);
                    Recon_Day = new Number(4, rows_total, Long.parseLong(DeviceResponse_parsed[5]));
                    sheet.addCell(Recon_Day);
                    Recon_Hour = new Number(5, rows_total, Long.parseLong(DeviceResponse_parsed[6]));
                    sheet.addCell(Recon_Hour);
                    Recon_Minute = new Number(6, rows_total, Long.parseLong(DeviceResponse_parsed[7]));
                    sheet.addCell(Recon_Minute);
                    Recon_Second = new Number(7, rows_total, Long.parseLong(DeviceResponse_parsed[8]));
                    sheet.addCell(Recon_Second);
                    Recon_Movements = new Number(8, rows_total, Long.parseLong(DeviceResponse_parsed[9]));
                    sheet.addCell(Recon_Movements);
                    Recon_Chamber1Count = new Number(9, rows_total, Long.parseLong(DeviceResponse_parsed[10]));
                    sheet.addCell(Recon_Chamber1Count);
                    //Calculate counts per hour, discarding the first two rows in any spreadsheet.
                    //I had originally used the modulus function (i.e. rows_total%6), but this didn't account for
                    //the variable number of "W" flags a given data session may have.
                    if((rows_total>7)&&(i>=6)) {
                        Recon_Chamber1CountPerHour = new Formula(10, rows_total, "SUM(J" + (rows_total-4) + ":J" + (rows_total+1) + ")");
                        sheet.addCell(Recon_Chamber1CountPerHour);
                        Recon_Chamber1RadonConc = new Formula(11, rows_total, "K" + (rows_total+1) + "/$AE$2");
                        sheet.addCell(Recon_Chamber1RadonConc);
                        Recon_Chamber2CountPerHour = new Formula(13, rows_total, "SUM(M" + (rows_total-4) + ":M" + (rows_total+1) + ")");
                        sheet.addCell(Recon_Chamber2CountPerHour);
                        Recon_Chamber2RadonConc = new Formula(14, rows_total, "N" + (rows_total+1) + "/$AF$2");
                        sheet.addCell(Recon_Chamber2RadonConc);
                        i=0;
                    }
                    Recon_Chamber2Count = new Number(12, rows_total, Long.parseLong(DeviceResponse_parsed[11]));
                    sheet.addCell(Recon_Chamber2Count);
                    Recon_MainInputVoltage = new Number(15, rows_total, Double.parseDouble(DeviceResponse_parsed[12]), XL_Decimal100_Format);
                    sheet.addCell(Recon_MainInputVoltage);
                    Recon_BattVoltage = new Number(16, rows_total, Double.parseDouble(DeviceResponse_parsed[13]), XL_Decimal100_Format);
                    sheet.addCell(Recon_BattVoltage);
                    Recon_HumidityMin = new Number(17, rows_total, Double.parseDouble(DeviceResponse_parsed[14]), XL_Decimal10_Format);
                    sheet.addCell(Recon_HumidityMin);
                    Recon_HumidityAvg = new Number(18, rows_total, Double.parseDouble(DeviceResponse_parsed[15]), XL_Decimal10_Format);
                    sheet.addCell(Recon_HumidityAvg);
                    Recon_HumidityMax = new Number(19, rows_total, Double.parseDouble(DeviceResponse_parsed[16]), XL_Decimal10_Format);
                    sheet.addCell(Recon_HumidityMax);
                    Recon_PressureMin = new Number(20, rows_total, Double.parseDouble(DeviceResponse_parsed[17]), XL_Decimal10_Format);
                    sheet.addCell(Recon_PressureMin);
                    Recon_PressureAvg = new Number(21, rows_total, Double.parseDouble(DeviceResponse_parsed[18]), XL_Decimal10_Format);
                    sheet.addCell(Recon_PressureAvg);
                    Recon_PressureMax = new Number(22, rows_total, Double.parseDouble(DeviceResponse_parsed[19]), XL_Decimal10_Format);
                    sheet.addCell(Recon_PressureMax);
                    Recon_TemperatureMin = new Number(23, rows_total, Double.parseDouble(DeviceResponse_parsed[20]), XL_Decimal100_Format);
                    sheet.addCell(Recon_TemperatureMin);
                    Recon_TemperatureAvg = new Number(24, rows_total, Double.parseDouble(DeviceResponse_parsed[21]), XL_Decimal100_Format);
                    sheet.addCell(Recon_TemperatureAvg);
                    Recon_TemperatureMax = new Number(25, rows_total, Double.parseDouble(DeviceResponse_parsed[22]), XL_Decimal100_Format);
                    sheet.addCell(Recon_TemperatureMax);
                    Recon_CurrentAvg = new Number(26, rows_total, Long.parseLong(DeviceResponse_parsed[23]));
                    sheet.addCell(Recon_CurrentAvg);
                    Recon_HVSupply = new Number(27, rows_total, Long.parseLong(DeviceResponse_parsed[24]));
                    sheet.addCell(Recon_HVSupply);
                    //Note: we need to strip the carriage return at the end of the DeviceResponse_parsed array, by using the replaceAll function call shown below.
                    Recon_RecordsInSample = new Number(28, rows_total, Long.parseLong(DeviceResponse_parsed[25].replaceAll("[\n\r]", "")));
                    sheet.addCell(Recon_RecordsInSample);
                    //The following is a bit of a hack, but it should work for adding CF1 and CF2 to the first row only, to serve as a constant.
                    if(rows_total==1) {
                        Recon_CF1 = new Number(30, rows_total, CF1);
                        sheet.addCell(Recon_CF1);
                        Recon_CF2 = new Number(31, rows_total, CF2);
                        sheet.addCell(Recon_CF2);
                    }
                }
                //End Spreadsheet Row Additions
		} // end !diagnosticMode omission
            }

            //Created Named Ranges for spreadsheet graph
	    if (MainMenuUI.diagnosticMode) {
                XLfile.addNameArea("Ch1Counts", sheet, 9, 1, 9, sheet.getRows()-1);
                XLfile.addNameArea("Ch2Counts", sheet, 11, 1, 11, sheet.getRows()-1);
	    }
            
	    // do this if we're in diagnostic mode
	    if(BeginAveraging==true && MainMenuUI.diagnosticMode) {
                writer.println("\r\n");
                writer.println("SUMMARY:");
                writer.println("Start Date/Time: " + StartDate.format(DateTimeDisplay));
                writer.println("End Date/Time: " + EndDate.format(DateTimeDisplay));
                Duration radonDuration = Duration.between(StartDate, EndDate);
                totalSeconds = radonDuration.getSeconds();
                testHours = totalSeconds / 3600;
                testMinutes = ((totalSeconds % 3600) / 60);
                testSeconds = (totalSeconds % 60);
                writer.println("Total Test Duration: " + testHours + " hours, " + testMinutes + " minutes, " + testSeconds + " seconds");
                writer.println("Chamber 1 Total Counts: " + TotalChamber1Counts);
                writer.println("Chamber 2 Total Counts: " + TotalChamber2Counts);
                writer.println("Total Movements: " + TotalMovements);
                writer.println("Avg. Humidity = " + RoundAvg.format(AvgHumidity/ActiveRecordCounts) + "%");
                writer.println("Avg. Pressure = " + RoundAvg.format(AvgPressure/ActiveRecordCounts) + " mmHg");
                writer.println("Avg. Temperature = " + RoundAvg.format(AvgTemperature/ActiveRecordCounts) + "C");
                writer.println("Instrument Wait Setting = " + ReconWaitTime);
                writer.println("Instrument Duration Setting = " + ReconDurationSetting);
            }

	    // or this if we're in regular user mode
	    else if (BeginAveraging == true) {
	        writer.println("\r\n");
		writer.println("Test site:");

		// had to make txtTestSiteInfo public to do this
		// maybe there is a better way?
		writer.println(MainMenuUI.txtTestSiteInfo.getText());
		writer.println("\r\n");
		writer.println("Start Date/Time: " + StartDate.format(DateTimeDisplay));
		writer.println("End Date/Time: " + EndDate.format(DateTimeDisplay));

		// format time data
		Duration radonDuration = Duration.between(StartDate, EndDate);
                totalSeconds = radonDuration.getSeconds();
                testHours = totalSeconds / 3600;
                testMinutes = ((totalSeconds % 3600) / 60);
                testSeconds = (totalSeconds % 60);

                writer.println("Total Test Duration: " + testHours + " hours, " + testMinutes + " minutes, " + testSeconds + " seconds");
		writer.println("Total Movements: " + TotalMovements);
                writer.println("Avg. Humidity = " + RoundAvg.format(AvgHumidity/ActiveRecordCounts) + "%");
                writer.println("Avg. Pressure = " + RoundAvg.format(AvgPressure/ActiveRecordCounts) + " mmHg");
                writer.println("Avg. Temperature = " + RoundAvg.format(AvgTemperature/ActiveRecordCounts) + "C");
		writer.println("Chamber 1 avg pCi/L = ");
		writer.println("Chamber 2 avg pCi/L = ");
		writer.println("Average pCi/L = ");
	    }
            
            writer.close();

	    if (MainMenuUI.diagnosticMode) {
                XLfile.write();
                XLfile.close();
            }

            MainMenuUI.displayProgressLabel("TXT/XLS files created.");
            System.out.println("TXT/XLS files created.");
        }
        catch (InterruptedException ex) {
            System.out.println(ex);
        }
    }

    static void DumpAllData(SerialPort scannedPort) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException, ParseException, IOException, WriteException {
        try {

            String ConfirmSN = "Unknown";
            
            Thread.sleep(10);
            WriteComm.main(scannedPort, ReconConfirm); //Check the Recon to see if a new record exists.
            Thread.sleep(10);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String[] DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            ConfirmSN = DeviceResponse_parsed[3];
            //This is needed to remove the carriage-return at the end of the serial number, as it's the last element in the array.
            ConfirmSN = ConfirmSN.replaceAll("[\n\r]", "");
            System.out.println("Confirming Recon S/N #" + ConfirmSN + " for total data dump.");
            
            Thread.sleep(10);
            WriteComm.main(scannedPort, ClearMemoryCommand); //Check the Recon to see if a new record exists.
            Thread.sleep(10);
            DeviceResponse = ReadComm.main(scannedPort, 19);
            DeviceResponse_parsed = StringUtils.split(DeviceResponse, ",");
            String DeviceResponse_targeted = "Let's begin!";
            PrintWriter writer = new PrintWriter("data/Recon_" + ConfirmSN + "_AllData.txt", "UTF-8");
            //writer.println(DeviceResponse);
            
            //For Excel spreadsheet. Ye gods, this sub is turning into a bloody hornet's nest...
            //WritableWorkbook XLfile = Workbook.createWorkbook(new File("Recon_AllData.xls"));
            //WritableSheet sheet = XLfile.getSheet(0);
            
            //Initialize i.
            int i = 0;
            DeviceResponse_targeted = "=DB";
            while((DeviceResponse_targeted.equals("=DB"))){
                if(i==0){
                    WriteComm.main(scannedPort, ReadFirstRecord);
                } else {
                    WriteComm.main(scannedPort, ReadNextRecord);
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
            WriteComm.main(scannedPort, CheckNewRecord);
            Thread.sleep(50);
            String DeviceResponse = ReadComm.main(scannedPort, 19);
            String DeviceResponse_targeted = StringUtils.left(DeviceResponse,3);
            if(DeviceResponse_targeted.equals("=BD")) {
                System.out.println("There were no new records on this CRM; no session to delete.");
                MainMenuUI.displayProgressLabel("No data sessions found.");
                return;
            }
            Thread.sleep(50);
            WriteComm.main(scannedPort, ClearSessionCommand);
            System.out.println("Clearing Memory via :CD command...");
            System.out.println(ClearSessionCommand);
            Thread.sleep(50);
            WriteComm.main(scannedPort, CheckNewRecord);
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
            WriteComm.main(scannedPort, ClearMemoryCommand);
            System.out.println(ClearMemoryCommand);
            Thread.sleep(125);
            WriteComm.main(scannedPort, CheckNewRecord);
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
            WriteComm.main(scannedPort, ReadCalibrationFactors);
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
}
