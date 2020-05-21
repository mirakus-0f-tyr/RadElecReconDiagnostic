// Simple class to handle updating of text file if the user wants to edit in-app
package MainMenu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FileUpdater {

    public static void UpdateTXTFile(File oldFile) throws IOException {
	BufferedReader br = null;
	String currentLine = null;
	BufferedWriter bw = null;
	boolean custInfoFound = false, testSiteInfoFound = false;
	boolean custLinesCounted = false, testSiteLinesCounted = false;
	int customerLineCounter = 0;
	int testSiteLineCounter = 0;
	int intTrimSuffix;
	String oldFileName = oldFile.getCanonicalPath();
	String updatedFileName = oldFile.getName();
	String newline = System.getProperty("line.separator");
	ArrayList<String> workingFile = new ArrayList<String>();

	try {
	    // DETERMINE FILE NAME
	    // Trim off extension part of the filename so we can append _updated.txt
	    if (updatedFileName.contains("_updated"))
		intTrimSuffix = updatedFileName.lastIndexOf("_updated");
	    else
		intTrimSuffix = updatedFileName.lastIndexOf(".");

            if (intTrimSuffix > 0) {
		updatedFileName = updatedFileName.substring(0, intTrimSuffix);
            }

            updatedFileName = InitDirs.dataDir + File.separator + updatedFileName + "_updated.txt";

	    // LOAD ORIGINAL INTO RAM FOR EVALUATION
	    br = new BufferedReader(new InputStreamReader(new FileInputStream(oldFileName)));

	    while ((currentLine = br.readLine()) != null)
		workingFile.add(currentLine);

	    br.close();

	    // TRIM OUT ALL EXISTING DATA UNDER CUSTOMER AND TEST SITE INFO!
	    // Count how many lines to remove...
	    for (int i = 0; i < workingFile.size(); i++) {
		if (custInfoFound && !custLinesCounted) {
		    if (workingFile.get(i).contains("Test site"))
			custLinesCounted = true;
		    else
			customerLineCounter++;
		    }

		if (testSiteInfoFound && !testSiteLinesCounted) {
		    if (workingFile.get(i).contains("SUMMARY") || workingFile.get(i).contains("Instrument"))
			testSiteLinesCounted = true;
		    else
			testSiteLineCounter++;
		}

		if (workingFile.get(i).contains("Customer info"))
		    custInfoFound = true;

		if (workingFile.get(i).contains("Test site info"))
		    testSiteInfoFound = true;
	    }

	    // Let's go through one final time to remove the customer and test site lines...
	    // Let's also add whatever deployment and technician values are presently set in Config
	    for (int i = 0; i < workingFile.size(); i++) {
		if (i > 0) {
		    if (workingFile.get(i - 1).contains("Customer info")) {
			for (int j = 0; j < customerLineCounter; j++)
			    workingFile.remove(i);
		    }

		    if (workingFile.get(i - 1).contains("Test site")) {
			for (int k = 0; k < testSiteLineCounter; k++)
			    workingFile.remove(i);
		    }

		    if (workingFile.get(i).contains("Location:") || workingFile.get(i).contains("Room:") && MainMenuUI.txtLocation.getText().length() > 0) {
			    workingFile.remove(i);
			    workingFile.add(i, "Location: " + MainMenuUI.txtLocation.getText());
		    }

		    if (workingFile.get(i).contains("Protocol:")) {
			workingFile.remove(i);
			workingFile.add(i, "Protocol: " + MainMenuUI.strProtocol);
		    }

		    if (workingFile.get(i).contains("Tampering:")) {
			workingFile.remove(i);
			workingFile.add(i, "Tampering: " + MainMenuUI.strTampering);
		    }

		    if (workingFile.get(i).contains("Weather:")) {
			workingFile.remove(i);
			workingFile.add(i, "Weather: " + MainMenuUI.strWeather);
		    }

		    if (workingFile.get(i).contains("Mitigation:")) {
			workingFile.remove(i);
			workingFile.add(i, "Mitigation: " + MainMenuUI.strMitigation);
		    }

		    if (workingFile.get(i).contains("Comment:")) {
			workingFile.remove(i);
			workingFile.add(i, "Comment: " + MainMenuUI.strComment);
		    }

		    if (workingFile.get(i).contains("Analyzed By")) {
			workingFile.remove(i);
			workingFile.add(i, "Analyzed By: " + MainMenuUI.strAnalyzedBy);
		    }

		    if (workingFile.get(i).contains("Deployed By")) {
			workingFile.remove(i);
			workingFile.add(i, "Deployed By: " + MainMenuUI.strDeployedBy);
		    }

		    if (workingFile.get(i).contains("Retrieved By")) {
			workingFile.remove(i);
			workingFile.add(i, "Retrieved By: " + MainMenuUI.strRetrievedBy);
		    }
		}
	    }

	    bw = new BufferedWriter(new FileWriter(new File(updatedFileName)));

	    // Write out to new file.
	    for (int i = 0; i < workingFile.size(); i++) {
		currentLine = workingFile.get(i);
		bw.write(currentLine + newline);

		if (currentLine.contains("Customer information:")) {
		    if (MainMenuUI.txtCustomerInfo.getText().length() > 0)
			bw.write(MainMenuUI.txtCustomerInfo.getText() + newline + newline + newline);
		    else
			bw.write(newline + newline + newline);
		}

		if (currentLine.contains("Test site information:")) {
		    if (MainMenuUI.txtTestSiteInfo.getText().length() > 0)
			bw.write(MainMenuUI.txtTestSiteInfo.getText() + newline + newline + newline);
		    else
			bw.write(newline + newline + newline);
		}
	    }

	    bw.close();
	    Logging.main("Updated file has been written.");
        }

	catch (Exception anyEx) {
	    Logging.main(anyEx.toString());
	}
    }
}
