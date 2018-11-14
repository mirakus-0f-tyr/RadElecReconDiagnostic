// Simple class to handle updating of text file if the user wants to edit in-app
package MainMenu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class FileUpdater {
// This method works by writing a temporary file with the original customer and test site info cleared,
// then writing a final "updated" file with the new information.  This gets the feature in place, but
// there is likely a much better way to do this.  This method is a minefield.  There is a fair amount of
// work to be done here, making sure files exist, that everything was done successfully, etc.
    public static void UpdateTXTFile(File oldFile) throws IOException {
	BufferedReader br = null;
	String currentLine = null;
	BufferedWriter bw = null;
	int customerLineCounter = 0;
	int testSiteLineCounter = 0;
	String oldFileName = oldFile.getCanonicalPath();
	String updatedFileName = oldFile.getName();
        int intTrimSuffix = updatedFileName.lastIndexOf(".");
        if (intTrimSuffix > 0) {
            updatedFileName = updatedFileName.substring(0,intTrimSuffix);
        }
        updatedFileName = "data/" + updatedFileName + "_updated.txt";
	String tempFileName = "data/temp.txt";
	File temporaryFile = new File(tempFileName);

	try {
	    // first pass - count how many lines we need to clean
            br = new BufferedReader(new InputStreamReader(new FileInputStream(oldFileName)));

	    for (currentLine = br.readLine(); currentLine != null; currentLine = br.readLine()) {
		if (currentLine.contains("Customer information:")) {
		    while (!(currentLine.contains("Test site information:"))) {
			currentLine = br.readLine();
			customerLineCounter += 1;
		    }
		}
		if (currentLine.contains("Test site information:")) {
		    while (!(currentLine.contains("Instrument Serial") || currentLine.contains("SUMMARY"))) {
		        currentLine = br.readLine();
			testSiteLineCounter += 1;
		    }
		}
	    }
	    br.close();

	    // second pass - write temporary file with select lines now blank
	    bw = new BufferedWriter(new FileWriter(temporaryFile));
	    br = new BufferedReader(new InputStreamReader(new FileInputStream(oldFileName)));

	    for (currentLine = br.readLine(); currentLine != null; currentLine = br.readLine()) {
	        bw.write(currentLine);
		bw.write("\r\n");

	        if (currentLine.contains("Customer information:")) {
		    for (int i = 0; i < customerLineCounter - 1; i++) {
		        currentLine = br.readLine();
		        bw.write("\r\n");
		    }
		}
		if (currentLine.contains("Test site information:")) {
		    for (int i = 0; i < testSiteLineCounter - 1; i++) {
		        currentLine = br.readLine();
		        bw.write("\r\n");
		    }
		}
	    }
	    br.close();
	    bw.close();

	    // third pass - write the file again, updating with the new information
	    bw = new BufferedWriter(new FileWriter(new File(updatedFileName)));
	    br = new BufferedReader(new InputStreamReader(new FileInputStream(temporaryFile)));

	    for (currentLine = br.readLine(); currentLine != null; currentLine = br.readLine()) {
	        bw.write(currentLine);
		bw.write("\r\n");

		if (currentLine.contains("Customer information:")) {
		    currentLine = br.readLine();
		    bw.write(MainMenuUI.txtCustomerInfo.getText());
		}

		if (currentLine.contains("Test site information:")) {
		    currentLine = br.readLine();
		    bw.write(MainMenuUI.txtTestSiteInfo.getText());
		}
	    }
	    br.close();
	    bw.close();

	    temporaryFile.delete();
	    System.out.println("Updated file has been written.");
	}

	catch(IOException ex) {
	    ex.printStackTrace();
	}
    }
}
