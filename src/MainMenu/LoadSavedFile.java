/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Rad Elec Inc.
 */

public class LoadSavedFile {
    
    public static ArrayList<ArrayList<String>> LoadedReconTXTFile = new ArrayList<ArrayList<String>>(); //This ArrayList will be used to build the chart, and is populated when loading the file.
    
    public static void main(String ReconTXTFile) {
        //Variable declarations
        
        ArrayList<String> arrLine = new ArrayList<>();
        String[] strLine_parsed;
        int i = 0;
        
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ReconTXTFile)));
            for (String strLine = br.readLine(); strLine != null; strLine = br.readLine()) {
                if(strLine.length()>0) { //If the line is blank, then we definitely don't want to try to split the string.
                    strLine_parsed = StringUtils.split(strLine, ","); //splits strLine into the strLine_parsed[] string array.
                    if(strLine_parsed[0].equals("=DB")) { //make sure that we only add valid data files into our two-dimensional string array (LoadedReconTXTFile)...
                        for(int arrayCounter = 0; arrayCounter <= strLine_parsed.length -1; arrayCounter++) {
                            arrLine.add(strLine_parsed[arrayCounter]); //This will add each element in strLine_parsed to the temporary arrLine ArrayList.
                        }
                        LoadedReconTXTFile.add(arrLine); //This will add the temporary arrLine into the primary LoadedReconTXTFile ArrayList.
                    }
                }
            }
            br.close();
            
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR: Unable to find the requested Recon TXT file in LoadSavedFile.java!");
            Logger.getLogger(LoadSavedFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("ERROR: Fundamental IO Error encountered when parsing Recon TXT file.");
            Logger.getLogger(LoadSavedFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
