/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import Config.Config;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static String strTestSiteInfo = "";
    public static String strCustomerInfo = "";
    public static String strStartDate = "Unknown Start Date";
    public static String strEndDate = "Unknown End Date";
    public static String strUnitSystem = "US";
    public static String strInstrumentSerial = "Unknown";
    public static String strDeployedBy = "Unknown";
    public static String strRetrievedBy = "Unknown";
    public static String strAnalyzedBy = "Unknown";
    public static String strCalDate = "Unknown";
    public static String strReportProtocol = "Unknown";
    public static String strReportTampering = "Unknown";
    public static String strReportWeather = "Unknown";
    public static String strReportMitigation = "Unknown";
    public static String strReportComment = "Unknown";
    public static String strRoomDeployed = "Unknown";
    
    public static void main(String ReconTXTFile) {
        //Variable declarations
        
        ArrayList<String> arrLine = new ArrayList<>();
        ArrayList<String> arrLine_temp = new ArrayList<>();
        String[] strLine_parsed;
        boolean testSiteFlag = false;
        boolean customerInfoFlag = false;
        int i = 0;
        
	// clear customer and test site strings every time a new file is opened
	strTestSiteInfo = "";
	strCustomerInfo = "";
        
        try {
            
            Config getUnits = new Config();
            strUnitSystem = getUnits.findUnitSystem();
            strInstrumentSerial = getReconSerialFromFileName(); //We should still call this, as it will work with older text files.
            
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ReconTXTFile)));
            
            LoadedReconTXTFile.clear(); //We should definitely clear this each time a file is loaded, or else it will continue to grow...
            
            for (String strLine = br.readLine(); strLine != null; strLine = br.readLine()) {
                if(strLine.length()>0) { //If the line is blank, then we definitely don't want to try to split the string.
                    strLine = strLine.replace("[", ""); //Remove left brackets from strLine (introduced with new text file format)
                    strLine = strLine.replace("]", ""); //Remove right brackets from strLine (introduced with new text file format)
                    strLine_parsed = StringUtils.split(strLine, ","); //splits strLine into the strLine_parsed[] string array.
                    if(strLine_parsed[0].equals("=DB")) { //make sure that we only add valid data files into our two-dimensional string array (LoadedReconTXTFile)...
                        for(int arrayCounter = 0; arrayCounter <= strLine_parsed.length -1; arrayCounter++) {
                            arrLine.add(arrayCounter, strLine_parsed[arrayCounter].trim()); //This will add each element in strLine_parsed to the temporary arrLine ArrayList.
                        }
                        arrLine_temp = (ArrayList<String>) arrLine.clone(); //This seems really stupid, but if you don't clone the ArrayList to a temporary holder, it'll be lost after arrLine.clear() below.
                        LoadedReconTXTFile.add(arrLine_temp); //This will add the temporary arrLine into the primary LoadedReconTXTFile ArrayList.
                        System.out.println(Arrays.toString(LoadedReconTXTFile.get(i).toArray()));
                        System.out.println("Adding record #"+LoadedReconTXTFile.get(i).get(1)+" to ArrayList, whose new size is now "+LoadedReconTXTFile.size()+".");
                        arrLine.clear(); //If we don't clear arrLine, it will turn into one massive, single-dimensional string array...
                        System.out.println("Checking Status: "+ Arrays.toString(LoadedReconTXTFile.get(i).toArray()));
                        i++;
                    }
                    if(strLine.contains("Instrument Serial: ")) {
                        strLine_parsed = StringUtils.split(strLine, " "); //Need to parse again to segregate spaces, not commas.
                        strInstrumentSerial = strLine_parsed[2];
                        System.out.println("Serial# found and parsed: " + strInstrumentSerial);
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
                    if(strLine.contains("Start Date/Time:")) {
                        strLine_parsed = StringUtils.split(strLine, " ");
                        strStartDate = strLine_parsed[2] + " " + strLine_parsed[3];
                    }
                    if(strLine.contains("End Date/Time:")) {
                        strLine_parsed = StringUtils.split(strLine, " ");
                        strEndDate = strLine_parsed[2] + " " + strLine_parsed[3];
                    }
                    if(strLine.contains("Deployed By:")) {
                        strDeployedBy = strLine.substring(12);
                    }
                    if(strLine.contains("Retrieved By:")) {
                        strRetrievedBy = strLine.substring(13);
                    }
                    if(strLine.contains("Analyzed By:")) {
                        strAnalyzedBy = strLine.substring(12);
                    }
                    if(strLine.contains("Calibration Date =")) {
                        strLine_parsed = StringUtils.split(strLine, "=");
                        strCalDate = strLine_parsed[1].trim();
                    }
                    if(strLine.length() > 8 && strLine.substring(0,9).contains("Protocol:")) {
                        strReportProtocol = strLine.substring(9).trim(); //Should robustly parse protocol.
                    } else if(strLine.length() > 9 && strLine.substring(0,10).contains("Tampering:")) {
                        strReportTampering = strLine.substring(10).trim(); //Should robustly parse tampering.
                    } else if(strLine.length() > 7 && strLine.substring(0,8).contains("Weather:")) {
                        strReportWeather = strLine.substring(8).trim(); //Should robustly parse weather.
                    } else if(strLine.length() > 10 && strLine.substring(0,11).contains("Mitigation:")) {
                        strReportMitigation = strLine.substring(11).trim(); //Should robustly parse mitigation.
                    } else if(strLine.length() > 7 && strLine.substring(0,8).contains("Comment:")) {
                        strReportComment = strLine.substring(8).trim(); //Should robustly parse comment.
                    } else if(strLine.length() > 4 && strLine.substring(0,5).contains("Room:")) {
		        strRoomDeployed = strLine.substring(5).trim();
		    }
                    //BEGIN: Test Site Parsing Block
                    if(testSiteFlag) {
                        if(strLine.contains("Instrument Serial:") || strLine.contains("SUMMARY:")) {
                            testSiteFlag = false;
                            if (strTestSiteInfo.length() > 1) {
                                strTestSiteInfo = strTestSiteInfo.trim(); //trim any anteceding or succeeding line-feeds...
                                System.out.println("Test Site Info: " + strTestSiteInfo);
                            } else {
                                System.out.println("Unable to find any Test Site Info in " + MainMenu.MainMenuUI.lblLoadedFileName.getText() + "!");
                            }
                        } else {
                            strTestSiteInfo = strTestSiteInfo + "\n" + strLine;
                        }
                    }
                    if(strLine.contains("Test site information:")) { //if we find this, then we know that our test site info will be in the next line.
                        testSiteFlag = true;
                    }
                    //END: Test Site Parsing Block
                    
                    //BEGIN: Customer Info Parsing Block
                    if(customerInfoFlag) {
                        if(strLine.contains("Test site information:")) { //If we find this, we know we're past our customer info block.
                            customerInfoFlag = false;
                            if (strCustomerInfo.length() > 1) {
                                strCustomerInfo = strCustomerInfo.trim(); //trim any anteceding or succeeding line-feeds...
                                System.out.println("Customer Info: " + strCustomerInfo);
                            } else {
                                System.out.println("Unable to find any Customer Info in " + MainMenu.MainMenuUI.lblLoadedFileName.getText() + "!");
                            }
                        } else {
                            strCustomerInfo = strCustomerInfo + "\n" + strLine;
                        }
                    }
                    if(strLine.contains("Customer information:")) { //if we find this, then we know that our customer info will be in the next line.
                        customerInfoFlag = true;
                    }
                    //END: Customer Info Parsing Block

		    // reload customer and test site info in MainMenuUI in case user wants to re-edit
		    MainMenu.MainMenuUI.txtCustomerInfo.setText(strCustomerInfo);
		    MainMenu.MainMenuUI.txtTestSiteInfo.setText(strTestSiteInfo);

                    //Display Main Menu Console label
                    MainMenu.MainMenuUI.lblSystemConsole.setText("File successfully loaded.");
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
    
    public static String getReconSerialFromFileName() {
        //The instrument serial is now stored in the TXT file, as of v0.8.2.
        //This method only exists to maintain backwards compatibility with
        //older text files and to serve as a fallback if no serial# is found.
        if(MainMenu.MainMenuUI.lblLoadedFileName.getText().length() > 0) {
            String[] str_Parsed = MainMenu.MainMenuUI.lblLoadedFileName.getText().split("_");
            if(str_Parsed.length > 2) {
                return str_Parsed[1];
            } else {
                return "Unknown Serial";
            }
        }
        return "Unknown Serial";
    }
    
}
