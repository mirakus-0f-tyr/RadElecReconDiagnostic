// CreateTXT.java
// The purpose of this class is to encapsulate much of the "clutter" which is contained
// in ScanComm.java.  Much of the code will be the same - it has simply been relocated here.
// For the time being, expect excessive comments.
package MainMenu;

import java.io.PrintWriter;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.time.Duration;
import java.util.Arrays;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class CreateTXT {

    public static PrintWriter writer; // declaration - is defined later

    public static void main() throws FileNotFoundException, UnsupportedEncodingException, InterruptedException {

	String ReconWaitTime = "Unknown";
        String ReconDurationSetting = "Unknown";
        String ReconCalDate = "Unknown";

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

        // variables needed for tallying hourly counts and calculating radon
        int tenMinuteCounter = 0; //3 // used to determine when to stow away hourly count values
        int ch1Counter = 0;
        int ch2Counter = 0;
        double avgResult1 = 0;
        double avgResult2 = 0;

	// used when traversing session list
	int sessionCounter = 0;

        // these are used for numerical formats at the final stage of writing to text
        // the DecimalFormat object has the ability to set properties such as rounding up or down
        // investigate later if we need more precision or if something is wrong
        DecimalFormat cfDec = new DecimalFormat("0.000"); // decimal format for calibration factors

        // decimal formats for radon concentration
        DecimalFormat df = new DecimalFormat("0.0");
        DecimalFormat si = new DecimalFormat("0");

        LinkedList<CountContainer> AllHourlyCounts = new LinkedList(); // list which will hold groups of hourly counts

        // pull CF's
        String[] CF_Array = ScanComm.CheckCalibrationFactors(ScanComm.scannedPort); //Let's pull the calibration factors
        CF1 = Double.parseDouble(CF_Array[0]) / 1000; //We need to add error-handling for this...
        CF2 = Double.parseDouble(CF_Array[1]) / 1000; //We need to add error-handling for this, too...
        ReconCalDate = ScanComm.GetCalibrationDate(ScanComm.scannedPort);

        ReconCommand.LoadNewRecord();
        ReconWaitTime = ReconCommand.DeviceResponse_parsed[12];
        ReconDurationSetting = ReconCommand.DeviceResponse_parsed[13];

        // create text file
        try {
            writer = new PrintWriter(ReconCommand.filenameTXT, "UTF-8");

	    // print first line of data (start of test)
            writer.println(Arrays.toString(ReconCommand.reconSession.get(sessionCounter)));

            while (sessionCounter < ReconCommand.reconSession.size()) {
                if (ReconCommand.reconSession.get(sessionCounter)[2].equals("S")) {
                    BeginAveraging = true;
                    TempYear = 2000 + Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[3]);
                    StartDate = LocalDateTime.of(TempYear, Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[4]), Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[5]), Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[6]), Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[7]), Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[8]));
                }

                if (ReconCommand.reconSession.get(sessionCounter)[2].equals("I") && BeginAveraging == true) {
                    tenMinuteCounter++;
                }

                if (ReconCommand.reconSession.get(sessionCounter)[2].equals("E")) {
                    TempYear = 2000 + Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[3]);
                    EndDate = LocalDateTime.of(TempYear, Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[4]), Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[5]), Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[6]), Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[7]), Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[8]));
                }
                if (BeginAveraging == true && !ReconCommand.reconSession.get(sessionCounter)[2].equals("Z")) {
                    ActiveRecordCounts++;
                    TotalMovements = TotalMovements + Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[9]);
                    TotalChamber1Counts = TotalChamber1Counts + Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[10]);
                    TotalChamber2Counts = TotalChamber2Counts + Long.parseLong(ReconCommand.reconSession.get(sessionCounter)[11]);
                    AvgHumidity = (AvgHumidity + Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[15]));
                    AvgPressure = (AvgPressure + Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[18]));
                    AvgTemperature = (AvgTemperature + Double.parseDouble(ReconCommand.reconSession.get(sessionCounter)[21]));

                    // section of code to tally counts and push hourly values into linked list for later analysis
                    // if (!LTMode) - do not forget this won't work for LT mode!
                    ch1Counter += Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[10]);
                    ch2Counter += Integer.parseInt(ReconCommand.reconSession.get(sessionCounter)[11]);

		    if (ReconCommand.longTermMode) {
		        if (tenMinuteCounter == 2) {
		            AllHourlyCounts.addLast(new CountContainer(ch1Counter, ch2Counter));
			    ch1Counter = 0;
			    ch2Counter = 0;
			    tenMinuteCounter = 0;
		        }
		    }
		    else {
                        if (tenMinuteCounter == 6) {
                            AllHourlyCounts.addLast(new CountContainer(ch1Counter, ch2Counter)); // add new grouping of hourly totals to the list

                            // clear chamber counters
                            ch1Counter = 0;
                            ch2Counter = 0;
                            tenMinuteCounter = 0;
                        }
		    }
                }

		if (sessionCounter > 0) // we've already written that one outside the loop
                    writer.println(Arrays.toString(ReconCommand.reconSession.get(sessionCounter)));

		MainMenuUI.displayProgressLabel("Reading Record #" + ReconCommand.reconSession.get(sessionCounter)[1] + "...");
		sessionCounter++;
            }  // end while loop

            // do this if we're in diagnostic mode
            if (BeginAveraging == true && MainMenuUI.diagnosticMode) {
                // write customer info to file
                writer.println("\r\n");
                writer.println("Customer information:");
                writer.println(MainMenuUI.txtCustomerInfo.getText());
                writer.println("\r\n");

                // write test site info to file
                writer.println("Test site information:");
                writer.println(MainMenuUI.txtTestSiteInfo.getText());
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
                writer.println("Avg. Humidity = " + RoundAvg.format(AvgHumidity / ActiveRecordCounts) + "%");
                writer.println("Avg. Pressure = " + RoundAvg.format(AvgPressure / ActiveRecordCounts) + " mmHg");
                writer.println("Avg. Temperature = " + RoundAvg.format(AvgTemperature / ActiveRecordCounts) + "C");
                writer.println("Instrument Wait Setting = " + ReconWaitTime);
                writer.println("Instrument Duration Setting = " + ReconDurationSetting);
                writer.println("Chamber 1 CF: " + cfDec.format(CF1));
                writer.println("Chamber 2 CF: " + cfDec.format(CF2));
                writer.println("Calibration Date = " + ReconCalDate);
                writer.println("Protocol: " + MainMenu.MainMenuUI.strProtocol);
                writer.println("Tampering: " + MainMenu.MainMenuUI.strTampering);
                writer.println("Weather: " + MainMenu.MainMenuUI.strWeather);
                writer.println("Mitigation: " + MainMenu.MainMenuUI.strMitigation);
                writer.println("Comment: " + MainMenu.MainMenuUI.strComment);
		writer.println("Room: " + MainMenu.MainMenuUI.strRoomDeployed);
                writer.println("\n");
                writer.println("Analyzed By: " + MainMenu.MainMenuUI.strAnalyzedBy);
                writer.println("Deployed By: " + MainMenu.MainMenuUI.strDeployedBy);
                writer.println("Retrieved By: " + MainMenu.MainMenuUI.strRetrievedBy);
                writer.println("\n");
            } // or this if we're in regular user mode
            else if (BeginAveraging == true) {
                // write customer info to file
                writer.println("\r\n");
                writer.println("Customer information:");
                writer.println(MainMenuUI.txtCustomerInfo.getText());
                writer.println("\r\n");

                // write test site info to file
                writer.println("Test site information:");
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
                writer.println("Avg. Humidity = " + RoundAvg.format(AvgHumidity / ActiveRecordCounts) + "%");
                writer.println("Avg. Pressure = " + RoundAvg.format(AvgPressure / ActiveRecordCounts) + " mmHg");
                writer.println("Avg. Temperature = " + RoundAvg.format(AvgTemperature / ActiveRecordCounts) + "C");
                writer.println("Chamber 1 CF: " + cfDec.format(CF1));
                writer.println("Chamber 2 CF: " + cfDec.format(CF2));
                writer.println("Calibration Date = " + ReconCalDate);
                writer.println("Protocol: " + MainMenu.MainMenuUI.strProtocol);
                writer.println("Tampering: " + MainMenu.MainMenuUI.strTampering);
                writer.println("Weather: " + MainMenu.MainMenuUI.strWeather);
                writer.println("Mitigation: " + MainMenu.MainMenuUI.strMitigation);
                writer.println("Comment: " + MainMenu.MainMenuUI.strComment);
		writer.println("Room: " + MainMenu.MainMenuUI.strRoomDeployed);
                writer.println("\n");
                writer.println("Analyzed By: " + MainMenu.MainMenuUI.strAnalyzedBy);
                writer.println("Deployed By: " + MainMenu.MainMenuUI.strDeployedBy);
                writer.println("Retrieved By: " + MainMenu.MainMenuUI.strRetrievedBy);
                writer.println("\n");
            }
            // do following regardless of mode
            writer.println("Radon Concentration");

            if (MainMenuUI.unitType == "US") {
                writer.println("Unit: pCi/L");
            } else {
                writer.println("Unit: Bq/m3");
            }

            // write hourly radon values
	    for (int loopCount1 = 0; loopCount1 < AllHourlyCounts.size(); loopCount1++) {
                if (MainMenuUI.unitType == "US") {
                    writer.println("Hour: " + (Integer.toString(loopCount1)));
                    writer.println("Ch1: " + df.format((double) AllHourlyCounts.get(loopCount1).getCh1HourlyCount() / CF1) + "\tCh2: " + df.format((double) AllHourlyCounts.get(loopCount1).getCh2HourlyCount() / CF2));
                } else { // assuming SI
                    writer.println("Hour: " + (Integer.toString(loopCount1)));

                    writer.println("Ch1: " + si.format((double) AllHourlyCounts.get(loopCount1).getCh1HourlyCount() / CF1 * 37) + "\tCh2: " + si.format((double) AllHourlyCounts.get(loopCount1).getCh2HourlyCount() / CF2 * 37));
                }
            }

            // perform averaging of results
	    if (MainMenuUI.excludeFirst4Hours) {
                for (int loopCount2 = 4; loopCount2 < AllHourlyCounts.size(); loopCount2++) {
		    avgResult1 += (AllHourlyCounts.get(loopCount2).getCh1HourlyCount() / CF1);
		    avgResult2 += (AllHourlyCounts.get(loopCount2).getCh2HourlyCount() / CF2);
                }

		avgResult1 = avgResult1 / (double)(AllHourlyCounts.size() - 4);
		avgResult2 = avgResult2 / (double)(AllHourlyCounts.size() - 4);
	    }
	    else {
		for (int loopCount2 = 0; loopCount2 < AllHourlyCounts.size(); loopCount2++) {
		    avgResult1 += (AllHourlyCounts.get(loopCount2).getCh1HourlyCount() / CF1);
		    avgResult2 += (AllHourlyCounts.get(loopCount2).getCh2HourlyCount() / CF2);
                }

		avgResult1 = avgResult1 / (double)AllHourlyCounts.size();
		avgResult2 = avgResult2 / (double)AllHourlyCounts.size();
	    }

            writer.println("\n");

            if (MainMenuUI.unitType == "US") {
                writer.println("Chamber 1 Avg pCi/L = " + df.format((double) avgResult1));
                writer.println("Chamber 2 Avg pCi/L = " + df.format((double) avgResult2));
                writer.println("Average pCi/L = " + df.format((double) (avgResult1 + avgResult2) / 2));
            } else {
                writer.println("Chamber 1 Avg Bq/m3 = " + si.format((double) (avgResult1 * 37)));
                writer.println("Chamber 2 Avg Bq/m3 = " + si.format((double) (avgResult2 * 37)));
                writer.println("Average Bq/m3 = " + si.format((double) (avgResult1 + avgResult2) / 2 * 37));
            }
        } catch (FileNotFoundException ex) {
        }

	finally {
	    writer.close();
	}
    }
}
