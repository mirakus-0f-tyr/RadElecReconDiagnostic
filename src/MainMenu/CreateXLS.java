package MainMenu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.time.Duration;

import org.apache.commons.lang3.StringUtils;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.read.biff.BiffException;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class CreateXLS {

    public static WritableWorkbook XLfile = null;
    public static WritableSheet sheet = null;
    public static WritableCellFormat XL_Decimal10_Format = null;
    public static WritableCellFormat XL_Decimal100_Format = null;
    public static Number Recon_RecordNumber = null;
    public static Label Recon_Flag = null;
    public static Number Recon_Year = null;
    public static Number Recon_Month = null;
    public static Number Recon_Day = null;
    public static Number Recon_Hour = null;
    public static Number Recon_Minute = null;
    public static Number Recon_Second = null;
    public static Number Recon_Chamber1Count = null;
    public static Number Recon_Chamber2Count = null;
    public static Formula Recon_Chamber1CountPerHour = null;
    public static Formula Recon_Chamber2CountPerHour = null;
    public static Formula Recon_Chamber1RadonConc = null;
    public static Formula Recon_Chamber2RadonConc = null;
    public static Number Recon_MainInputVoltage = null;
    public static Number Recon_BattVoltage = null;
    public static Number Recon_HumidityMin = null;
    public static Number Recon_HumidityAvg = null;
    public static Number Recon_HumidityMax = null;
    public static Number Recon_PressureMin = null;
    public static Number Recon_PressureAvg = null;
    public static Number Recon_PressureMax = null;
    public static Number Recon_TemperatureMin = null;
    public static Number Recon_TemperatureAvg = null;
    public static Number Recon_TemperatureMax = null;
    public static Number Recon_Movements = null;
    public static Number Recon_CurrentAvg = null;
    public static Number Recon_HVSupply = null;
    public static Number Recon_RecordsInSample = null;
    public static Number Recon_CF1 = null;
    public static Number Recon_CF2 = null;

    //Variable declarations
    public static double AvgHumidity = 0;
    public static double AvgTemperature = 0;
    public static double AvgPressure = 0;
    public static long TotalMovements = 0;
    public static long TotalChamber1Counts = 0;
    public static long TotalChamber2Counts = 0;
    public static double CF1 = 0;
    public static double CF2 = 0;
    public static boolean BeginAveraging = false;
    public static long ActiveRecordCounts = 0;
    public static int TempYear = 0;
    public static DateTimeFormatter DateTimeDisplay = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
    public static LocalDateTime StartDate = null;
    public static LocalDateTime EndDate = null;
    public static DecimalFormat RoundAvg = new DecimalFormat("####0.00");
    public static long totalSeconds = 0;
    public static long testHours = 0;
    public static long testMinutes = 0;
    public static long testSeconds = 0;
    public static long i = 0;

    public static void main() throws BiffException, WriteException, InterruptedException, IOException {

	// used in traversing reconSession list
	int sessionCounter = 0;

        ReconCommand.LoadNewRecord();
        ReconCommand.LoadNextRecord();

        // pull CF's
        String[] CF_Array = ScanComm.CheckCalibrationFactors(ScanComm.scannedPort); //Let's pull the calibration factors
        CF1 = Double.parseDouble(CF_Array[0]) / 1000; //We need to add error-handling for this...
        CF2 = Double.parseDouble(CF_Array[1]) / 1000; //We need to add error-handling for this, too...

        try {
            //For Excel spreadsheet. Ye gods, this sub is turning into a bloody hornet's nest...
            Workbook workbook = Workbook.getWorkbook(new File("ReconTemplate.xls"));
            XLfile = Workbook.createWorkbook(new File(ReconCommand.filenameXLS), workbook);
            workbook.close();
            sheet = XLfile.getSheet(0);
            //Excel formats, so everything doesn't get passed over as a lousy string.
            //We'll likely need to catch errors in a future version, but I don't have the time to deal with that now.
            XL_Decimal10_Format = new WritableCellFormat(new NumberFormat("0.0"));
            XL_Decimal10_Format.setAlignment(Alignment.CENTRE);
            XL_Decimal100_Format = new WritableCellFormat(new NumberFormat("0.00"));
            XL_Decimal100_Format.setAlignment(Alignment.CENTRE);

	    // move to next record
	    sessionCounter++;

            while (sessionCounter < ReconCommand.reconSession.size()) {

                MainMenuUI.displayProgressLabel("Reading Record #" + ReconCommand.reconSession.get(sessionCounter)[1] + "...");

                //Spreadsheet Stuff
                //Move this to another method so we can have a semblance of order...
                if (!(ReconCommand.reconSession.get(sessionCounter)[2].equals("Z"))) {
                    int rows_total = sheet.getRows();
                    sheet.insertRow(rows_total + 1);
                    //This i incrementer is used for determining when to record the counts per hour.
                    if ((ReconCommand.reconSession.get(sessionCounter)[2].equals("S")) || (ReconCommand.reconSession.get(sessionCounter)[2].equals("I"))) {
                        i++;
                    }
                    Recon_RecordNumber = new Number(0, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[1]));
                    sheet.addCell(Recon_RecordNumber);
                    Recon_Flag = new Label(1, rows_total, ReconCommand.reconSession.get(sessionCounter)[2]);
                    sheet.addCell(Recon_Flag);
                    Recon_Year = new Number(2, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[3]));
                    sheet.addCell(Recon_Year);
                    Recon_Month = new Number(3, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[4]));
                    sheet.addCell(Recon_Month);
                    Recon_Day = new Number(4, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[5]));
                    sheet.addCell(Recon_Day);
                    Recon_Hour = new Number(5, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[6]));
                    sheet.addCell(Recon_Hour);
                    Recon_Minute = new Number(6, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[7]));
                    sheet.addCell(Recon_Minute);
                    Recon_Second = new Number(7, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[8]));
                    sheet.addCell(Recon_Second);
                    Recon_Movements = new Number(8, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[9]));
                    sheet.addCell(Recon_Movements);
                    Recon_Chamber1Count = new Number(9, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[10]));
                    sheet.addCell(Recon_Chamber1Count);
                    //Calculate counts per hour, discarding the first two rows in any spreadsheet.
                    //I had originally used the modulus function (i.e. rows_total%6), but this didn't account for
                    //the variable number of "W" flags a given data session may have.
		    if (ReconCommand.longTermMode) {
			if ((rows_total > 4) && (i >= 2)) {
			    Recon_Chamber1CountPerHour = new Formula(10, (rows_total - 1), "SUM(J" + (rows_total - 1) + ":J" + rows_total + ")");
			    sheet.addCell(Recon_Chamber1CountPerHour);
			    Recon_Chamber1RadonConc = new Formula(11, (rows_total - 1), "K" + rows_total + "/$AE$2");
			    sheet.addCell(Recon_Chamber1RadonConc);
			    Recon_Chamber2CountPerHour = new Formula(13, (rows_total - 1), "SUM(M" + (rows_total - 1) + ":M" + rows_total + ")");
			    sheet.addCell(Recon_Chamber2CountPerHour);
			    Recon_Chamber2RadonConc = new Formula(14, (rows_total - 1), "N" + rows_total + "/$AF$2");
			    sheet.addCell(Recon_Chamber2RadonConc);
			    i = 0;
                        }
		    }
		    else {
			if ((rows_total > 7) && (i >= 6)) {
			    Recon_Chamber1CountPerHour = new Formula(10, rows_total, "SUM(J" + (rows_total - 4) + ":J" + (rows_total + 1) + ")");
			    sheet.addCell(Recon_Chamber1CountPerHour);
			    Recon_Chamber1RadonConc = new Formula(11, rows_total, "K" + (rows_total + 1) + "/$AE$2");
			    sheet.addCell(Recon_Chamber1RadonConc);
			    Recon_Chamber2CountPerHour = new Formula(13, rows_total, "SUM(M" + (rows_total - 4) + ":M" + (rows_total + 1) + ")");
			    sheet.addCell(Recon_Chamber2CountPerHour);
			    Recon_Chamber2RadonConc = new Formula(14, rows_total, "N" + (rows_total + 1) + "/$AF$2");
			    sheet.addCell(Recon_Chamber2RadonConc);
			    i = 0;
                        }
		    }
                    Recon_Chamber2Count = new Number(12, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[11]));
                    sheet.addCell(Recon_Chamber2Count);
                    Recon_MainInputVoltage = new Number(15, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[12]), XL_Decimal100_Format);
                    sheet.addCell(Recon_MainInputVoltage);
                    Recon_BattVoltage = new Number(16, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[13]), XL_Decimal100_Format);
                    sheet.addCell(Recon_BattVoltage);
                    Recon_HumidityMin = new Number(17, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[14]), XL_Decimal10_Format);
                    sheet.addCell(Recon_HumidityMin);
                    Recon_HumidityAvg = new Number(18, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[15]), XL_Decimal10_Format);
                    sheet.addCell(Recon_HumidityAvg);
                    Recon_HumidityMax = new Number(19, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[16]), XL_Decimal10_Format);
                    sheet.addCell(Recon_HumidityMax);
                    Recon_PressureMin = new Number(20, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[17]), XL_Decimal10_Format);
                    sheet.addCell(Recon_PressureMin);
                    Recon_PressureAvg = new Number(21, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[18]), XL_Decimal10_Format);
                    sheet.addCell(Recon_PressureAvg);
                    Recon_PressureMax = new Number(22, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[19]), XL_Decimal10_Format);
                    sheet.addCell(Recon_PressureMax);
                    Recon_TemperatureMin = new Number(23, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[20]), XL_Decimal100_Format);
                    sheet.addCell(Recon_TemperatureMin);
                    Recon_TemperatureAvg = new Number(24, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[21]), XL_Decimal100_Format);
                    sheet.addCell(Recon_TemperatureAvg);
                    Recon_TemperatureMax = new Number(25, rows_total, Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[22]), XL_Decimal100_Format);
                    sheet.addCell(Recon_TemperatureMax);
                    Recon_CurrentAvg = new Number(26, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[23]));
                    sheet.addCell(Recon_CurrentAvg);
                    Recon_HVSupply = new Number(27, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[24]));
                    sheet.addCell(Recon_HVSupply);
                    //Note: we need to strip the carriage return at the end of the DeviceResponse_parsed array, by using the replaceAll function call shown below.
                    Recon_RecordsInSample = new Number(28, rows_total, Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[25].replaceAll("[\n\r]", "")));
                    sheet.addCell(Recon_RecordsInSample);
                    //The following is a bit of a hack, but it should work for adding CF1 and CF2 to the first row only, to serve as a constant.
                    if (rows_total == 1) {
                        Recon_CF1 = new Number(30, rows_total, CF1);
                        sheet.addCell(Recon_CF1);
                        Recon_CF2 = new Number(31, rows_total, CF2);
                        sheet.addCell(Recon_CF2);
                    }
                    
                    //At least for now, let's temporarily highlight any chamber counts that have had their count values limited by CountLimiter.
                    //This should be backwards compatible with previous files, as it will only proc if the array length is 28.
                    if(ReconCommand.reconSession.get(sessionCounter).length == 28) {
                        if(ReconCommand.reconSession.get(sessionCounter)[26].contains("true")) {
                            WritableCell Ch1Cell = sheet.getWritableCell(9, rows_total);
                            WritableCellFormat Ch1CellFormat = new WritableCellFormat(Ch1Cell.getCellFormat());
                            Ch1CellFormat.setBackground(Colour.YELLOW);
                            Ch1CellFormat.setAlignment(Alignment.CENTRE);
                            Ch1Cell.setCellFormat(Ch1CellFormat);
                        }
                        if(ReconCommand.reconSession.get(sessionCounter)[27].contains("true")) {
                            WritableCell Ch2Cell = sheet.getWritableCell(12, rows_total);
                            WritableCellFormat Ch2CellFormat = new WritableCellFormat(Ch2Cell.getCellFormat());
                            Ch2CellFormat.setBackground(Colour.YELLOW);
                            Ch2CellFormat.setAlignment(Alignment.CENTRE);
                            Ch2Cell.setCellFormat(Ch2CellFormat);
                        }
                    }
                    
                } // end if

		sessionCounter++;
            } // end while loop

            //Created Named Ranges for spreadsheet graph
            XLfile.addNameArea("Ch1Counts", sheet, 9, 1, 9, sheet.getRows() - 1);
            XLfile.addNameArea("Ch2Counts", sheet, 11, 1, 11, sheet.getRows() - 1);

	} catch (IOException ex) {
        }

	finally {
	    XLfile.write();
	    XLfile.close();
	}
    }
}
