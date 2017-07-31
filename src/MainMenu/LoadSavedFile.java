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
    public static double LoadedReconCF1 = 6;
    public static double LoadedReconCF2 = 6;
    public static ArrayList<Double> LoadedReconTXTFile_Ch1RnC; //This array will store Ch1RnC
    public static ArrayList<Double> LoadedReconTXTFile_Ch2RnC; //This array will store Ch2RnC
    
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
                    if(strLine.contains("Chamber 1 CF: ")) {
                        strLine_parsed = StringUtils.split(strLine, " "); //Need to parse again to segregate spaces, not commas.
                        LoadedReconCF1 = Double.parseDouble(strLine_parsed[3]);
                        System.out.println("CF1 found and parsed: " + LoadedReconCF1);
                    }
                    if(strLine.contains("Chamber 2 CF: ")) {
                        strLine_parsed = StringUtils.split(strLine, " "); //Need to parse again to segregate spaces, not commas.
                        LoadedReconCF2 = Double.parseDouble(strLine_parsed[3]);
                        System.out.println("CF2 found and parsed: " + LoadedReconCF2);
                    }
                    if(strLine.contains("Ch1: ")) {
                        //strLine_parsed = StringUtils.split(strLine, "\\t"); //Parses both spaces and tabs for this line.
                        //System.out.println("CH1 Array: " + strLine_parsed[0]);
                        //LoadedReconTXTFile_Ch1RnC.add(Double.parseDouble(strLine_parsed[1]));
                    }
                    if(strLine.contains("Ch2: ")) {
                        //strLine_parsed = StringUtils.split(strLine.trim(), "\\s+"); //Parses both spaces and tabs for this line.
                        //LoadedReconTXTFile_Ch2RnC.add(Double.parseDouble(strLine_parsed[3]));
                    }
                }
            }

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
