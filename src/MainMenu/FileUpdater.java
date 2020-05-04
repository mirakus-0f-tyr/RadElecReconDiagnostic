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

	    // Let's go through one final time to trim the lines...
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

		    if (workingFile.get(i).contains("Location:") || workingFile.get(i).contains("Room:"))
			    workingFile.remove(i);
		}
	    }

	    bw = new BufferedWriter(new FileWriter(new File(updatedFileName)));

	    for (int i = 0; i < workingFile.size(); i++) {
		currentLine = workingFile.get(i);
		bw.write(currentLine + newline);

		if (currentLine.contains("Customer information:"))
		    bw.write(MainMenuUI.txtCustomerInfo.getText() + newline + newline);

		if (currentLine.contains("Test site information:"))
		    bw.write(MainMenuUI.txtTestSiteInfo.getText() + newline + newline);

		if (workingFile.get(i).contains("Comment:"))
		    bw.write("Location: " + MainMenuUI.txtLocation.getText() + newline);
	    }

	    bw.close();
	    Logging.main("Updated file has been written.");
        }

	catch (Exception anyEx) {
	    Logging.main(anyEx.toString());
	}
    }
}
