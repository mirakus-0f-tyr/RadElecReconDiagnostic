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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Rad Elec Inc.
 */

public class LoadSavedFile {
    
    public static ArrayList<ArrayList<String>> LoadedReconTXTFile = new ArrayList<>(); //This ArrayList will be used to build the chart, and is populated when loading the file.
    
    public static void main(String ReconTXTFile) {
        //Variable declarations
        
        ArrayList<String> arrLine = new ArrayList<>();
        ArrayList<String> arrLine_temp = new ArrayList<>();
        String[] strLine_parsed;
        int i = 0;
        
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ReconTXTFile)));
            for (String strLine = br.readLine(); strLine != null; strLine = br.readLine()) {
                if(strLine.length()>0) { //If the line is blank, then we definitely don't want to try to split the string.
                    strLine_parsed = StringUtils.split(strLine, ","); //splits strLine into the strLine_parsed[] string array.
                    if(strLine_parsed[0].equals("=DB")) { //make sure that we only add valid data files into our two-dimensional string array (LoadedReconTXTFile)...
                        for(int arrayCounter = 0; arrayCounter <= strLine_parsed.length -1; arrayCounter++) {
                            arrLine.add(arrayCounter, strLine_parsed[arrayCounter]); //This will add each element in strLine_parsed to the temporary arrLine ArrayList.
                        }
                        arrLine_temp = (ArrayList<String>) arrLine.clone(); //This seems really stupid, but if you don't clone the ArrayList to a temporary holder, it'll be lost after arrLine.clear() below.
                        LoadedReconTXTFile.add(arrLine_temp); //This will add the temporary arrLine into the primary LoadedReconTXTFile ArrayList.
                        System.out.println(Arrays.toString(LoadedReconTXTFile.get(i).toArray()));
                        System.out.println("Adding record #"+LoadedReconTXTFile.get(i).get(1)+" to ArrayList, whose new size is now "+LoadedReconTXTFile.size()+".");
                        arrLine.clear(); //If we don't clear arrLine, it will turn into one massive, single-dimensional string array...
                        System.out.println("Checking Status: "+ Arrays.toString(LoadedReconTXTFile.get(i).toArray()));
                        i++;
                    }
                }
            }
            //System.out.println("TEST QUERY: "+Arrays.toString(LoadedReconTXTFile.toArray()));
            //System.out.println("ArrayList has a total size of "+LoadedReconTXTFile.size());
            br.close();
            
            //Creates graph
            String test_args[] = {"Radon Concentration", MainMenu.MainMenuUI.lblLoadedFileName.getText()};
            CreateGraph.main(test_args);
            
        } catch (FileNotFoundException ex) {
            System.out.println("ERROR: Unable to find the requested Recon TXT file in LoadSavedFile.java!");
            Logger.getLogger(LoadSavedFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("ERROR: Fundamental IO Error encountered when parsing Recon TXT file.");
            Logger.getLogger(LoadSavedFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
