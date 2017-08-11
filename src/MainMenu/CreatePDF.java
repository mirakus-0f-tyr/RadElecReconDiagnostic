/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import Config.Config;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import static MainMenu.LoadSavedFile.strTestSiteInfo;
import static MainMenu.LoadSavedFile.strCustomerInfo;
import static MainMenu.LoadSavedFile.strUnitSystem;
import static MainMenu.LoadSavedFile.strStartDate;
import static MainMenu.LoadSavedFile.strEndDate;
import static MainMenu.LoadSavedFile.strInstrumentSerial;
import static MainMenu.LoadSavedFile.strDeployedBy;
import static MainMenu.LoadSavedFile.strRetrievedBy;
import static MainMenu.LoadSavedFile.strAnalyzedBy;
import static MainMenu.CreateGraph.OverallAvgRnC;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 *
 * @author Rad Elec Inc.
 */

public class CreatePDF {
    
    public static String strCompany_Name;
    public static String strCompany_Address1;
    public static String strCompany_Address2;
    public static String strCompany_Address3;
    public static String strInstrumentType = "Recon CRM";
    public static String strLocation = "Basement";
    public static String strCustomReportText;
    float PDF_Y = 0;
    
    public void main() throws IOException {
        
        //strips .txt from the filename and replaces it with .pdf
        String PDF_Name = StringUtils.left(MainMenu.MainMenuUI.lblLoadedFileName.getText(),MainMenu.MainMenuUI.lblLoadedFileName.getText().length()-4) + ".pdf";
        
        PDDocument doc = new PDDocument();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        Date currentDate = new Date();
        int marginTop = 10;
        int marginBottom = 30;
        int marginSide = 30;
        int fontSize = 14;
        String textLine;
        float textWidth;
        float textHeight;
        
        try {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            
            //Declare the TTF path and filename
            PDFont fontDefault = PDType0Font.load(doc, new File("fonts/calibri.ttf")); //Truetype fonts are easier to utilize when it comes to margins, etc.
            PDFont fontBold = PDType0Font.load(doc, new File("fonts/calibri_bold.ttf")); //Truetype fonts are easier to utilize when it comes to margins, etc.
            
            PDPageContentStream contents = new PDPageContentStream(doc, page);
            
            //********************
            //Begin PDF Generation
            //This code-block is going to be a tangled mess...
            
            //contents.beginText(); //define beginning of text.
            contents.setFont(fontDefault, fontSize); //sets our font using the TTF loaded above.
            contents.setLeading(14.5f);
            
            //These are critical variables. Let's assign their initial values here.
            textHeight = fontDefault.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize; //important for determining to portion of page to begin text
            PDF_Y = page.getMediaBox().getHeight() - marginTop - textHeight; //assigning first Y coordinate value to PDF_Y. This will be important for making future-proof PDF designs.
            
            //Company Info Block
            GetCompanyInfo(); //pull info from the company.txt file, so that we can toss that info onto the PDF.
            DrawCompanyHeader(contents, page, fontDefault, fontSize);
            
            //Title Block
            contents.beginText(); //this is the only way I know how to reset the text position offset...
            fontSize = 18; //puff up our font-size for the banner title
            contents.setFont(fontBold, fontSize); //sets our bold font using the TTF loaded above.
            contents.setLeading(14.5f);
            textLine = "Radon Test Report"; //Our intended line, the "title" of the report
            textWidth = fontDefault.getStringWidth(textLine) / 1000 * fontSize; //textWidth is important for centering...
            textHeight = fontDefault.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize; //Also important for centering, because PDFBox lacks a centering function...
            PDF_Y -= 50;
            contents.moveTextPositionByAmount((page.getMediaBox().getWidth() - textWidth) / 2, PDF_Y); //This will center the text.
            contents.showText(textLine); //This will "draw" our textLine on the PDF.
            contents.endText();
            
            //Date Block (immediately below title)
            contents.beginText();
            fontSize = 12;
            textLine = dateFormat.format(currentDate); //display date beneath the Radon Test Report title
            textWidth = fontDefault.getStringWidth(textLine) / 1000 * fontSize; //still important for centering
            textHeight = fontDefault.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize; //still important for centering
            PDF_Y -= 5;
            contents.moveTextPositionByAmount((page.getMediaBox().getWidth() - textWidth) / 2, PDF_Y); //centers the date
            contents.newLine(); //We should put an extra newLine here, to give us a bit more distance from the title block.
            contents.setFont(fontDefault, fontSize);
            contents.showText(textLine);
            contents.endText(); //end date text block
            
            //Test Site Banner Block
            contents.beginText();
            fontSize = 12;
            textLine = "A Rad Elec Recon Continuous Radon Monitor was deployed at the following test site:"; //display test site banner beneath the Radon Test Report title
            textWidth = fontDefault.getStringWidth(textLine) / 1000 * fontSize; //still important for centering
            textHeight = fontDefault.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize; //still important for centering
            PDF_Y -= 35;
            contents.moveTextPositionByAmount((page.getMediaBox().getWidth() - textWidth) / 2, PDF_Y); //centers the date
            contents.showText(textLine); //Test Site Banner Text
            contents.endText();
            PDF_Y -= 5;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            PDF_Y -= 3;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            //Customer Info Block
            contents.beginText(); //write the customer info
            textLine = "Customer Information:";
            contents.setFont(fontBold, fontSize);
            PDF_Y -= 17;
            contents.moveTextPositionByAmount(marginSide + 5, PDF_Y); //left-justifies the customer info
            contents.showText(textLine);
            contents.newLine();
            contents.setFont(fontDefault, fontSize);
            String[] CustomerInfo_parsed = strCustomerInfo.split("\\n");
            for(int i = 0; i < CustomerInfo_parsed.length; i++) {
                textLine = CustomerInfo_parsed[i];
                if ((fontDefault.getStringWidth(textLine) / 1000 * fontSize) > textWidth) {
                    textWidth = fontDefault.getStringWidth(textLine) / 1000 * fontSize;
                }
                contents.showText(textLine);
                contents.newLine();
            }
            contents.newLine();
            contents.endText();
            //End Customer Info Block. What a mess.
            
            //Test Site Info Block
            contents.beginText(); //write the test site address
            textLine = "Test Site:";
            contents.setFont(fontBold, fontSize);
            contents.moveTextPositionByAmount(((page.getMediaBox().getWidth() - marginSide)/2)+30, PDF_Y); //left-justifies the customer info
            contents.showText(textLine);
            contents.newLine();
            contents.setFont(fontDefault, fontSize);
            String[] TestSiteInfo_parsed = strTestSiteInfo.split("\\n");
            TestSiteInfo_parsed = strTestSiteInfo.split("\\n");
            for(int i = 0; i < TestSiteInfo_parsed.length; i++) {
                textLine = TestSiteInfo_parsed[i];
                if ((fontDefault.getStringWidth(textLine) / 1000 * fontSize) > textWidth) {
                    textWidth = fontDefault.getStringWidth(textLine) / 1000 * fontSize;
                }
                contents.showText(textLine);
                contents.newLine();
            }
            PDF_Y += 1f*fontSize; //this is a hacky offset correction due to the newLine() (which I'll avoid in the future) ... I'll clean it up later.
            contents.endText();
            
            //Drawing Rectangles around Customer Info and Test Site Info Blocks
            //We need to make sure to grab the longest of the two text blocks and use that as our reference for drawing the rectangle.
            int LongestTextBlock = 1;
            if(CustomerInfo_parsed.length > TestSiteInfo_parsed.length) {
                LongestTextBlock = CustomerInfo_parsed.length;
            } else {
                LongestTextBlock = TestSiteInfo_parsed.length;
            }
            //Customer Info Rectangle:
            PDF_Y -= textHeight*(LongestTextBlock+2);
            contents.addRect(marginSide, PDF_Y, ((page.getMediaBox().getWidth() - marginSide)/2)-25, textHeight * (LongestTextBlock+2));
            contents.stroke();
            //Test Site Info Rectangle:
            contents.addRect(((page.getMediaBox().getWidth() - marginSide)/2)+25, PDF_Y, ((page.getMediaBox().getWidth() - marginSide)/2)-25, textHeight * (LongestTextBlock+2));
            contents.stroke();
            //End Rectangle Section
            
            //Radon Screening Text Block (beneath Customer & Test Site Text Blocks)
            PDF_Y -= 3;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            PDF_Y -= 3;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            fontSize = 12;
            textLine = "A Rad Elec Recon Continuous Radon Monitor was used for radon screening measurements that were conducted at the above referenced test site by: " + strCompany_Name;
            
            //WrapMultiLineText is jumbled as all hell, but at least it's continued to a single method.
            PDF_Y -= 11;
            WrapMultiLineText (contents,page,marginSide*2,PDF_Y,textLine,fontDefault,fontSize,marginSide*2);
            
            //Results Header Line
            contents.beginText();
            fontSize = 14;
            contents.setFont(fontBold, fontSize);
            PDF_Y -= 0.5f*fontSize; //We already have a space buffer from the WrapMultiLineText() call above, so let's just give us a tad more...
            textLine = "The results are as follows:";
            contents.newLineAtOffset(marginSide*2,PDF_Y);
            contents.showText(textLine);
            contents.endText();
            
            //Draw Another Line (above data column headers)
            PDF_Y -= 1f*fontSize;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            //Data Column Headers
            contents.beginText();
            fontSize = 12;
            contents.setFont(fontBold, fontSize);
            PDF_Y -= 1f*fontSize;
            String[] strColumnHeaders = {"Serial", "Instrument", "Location", "Test Start Date", "Test End Date", "Results "};
            //textLine = "Serial          Instrument          Location          Test Start Date          Test End Date          Results ";
            if(strUnitSystem.equals("SI")) {
                strColumnHeaders[5] += "(Bq/m³)";
            } else {
                strColumnHeaders[5] += "(pCi/L)";
            }
            contents.newLineAtOffset((((page.getMediaBox().getWidth()-marginSide*2)) / -6)+marginSide, PDF_Y); //a bit hacky, but the logic should be sound...
            for (int i = 0; i < strColumnHeaders.length; i++) {
                contents.moveTextPositionByAmount(((page.getMediaBox().getWidth()-marginSide*2)) / 6, 0);
                switch(i) {
                    case 1: contents.moveTextPositionByAmount(-40,0); //hacky, to fine-tune column spacing
                    case 4: contents.moveTextPositionByAmount(15,0);
                    case 5: contents.moveTextPositionByAmount(10,0);
                }
                contents.showText(strColumnHeaders[i]);
            }
            contents.endText();
            
            //Draw Another Line (below data column headers)
            PDF_Y -= 0.5f*fontSize;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            //Draw Data Summary
            String strOverallAvgRnC;
            if(strUnitSystem.equals("SI")) {
                strOverallAvgRnC = new DecimalFormat("0").format(OverallAvgRnC); //no decimal places for Bq/m3
            } else {
                strOverallAvgRnC = new DecimalFormat("0.0").format(OverallAvgRnC); //tenth decimal place for pCi/L
            }
            fontSize = 12;
            contents.setFont(fontDefault, fontSize);
            PDF_Y -= 1.0f * fontSize;
            contents.beginText();
            contents.newLineAtOffset((((page.getMediaBox().getWidth()-marginSide*2)) / -6)+marginSide, PDF_Y);
            String[] combinedDataArray = {strInstrumentSerial, strInstrumentType, strLocation, strStartDate, strEndDate, strOverallAvgRnC};
            for (int i = 0; i < combinedDataArray.length; i++) {
                contents.moveTextPositionByAmount(((page.getMediaBox().getWidth()-marginSide*2)) / 6, 0);
                switch(i) {
                    case 1: contents.moveTextPositionByAmount(-40,0); //hacky, to fine-tune column spacing
                    case 4: contents.moveTextPositionByAmount(15,0);
                    case 5: contents.moveTextPositionByAmount(10,0);
                }
                if(i==combinedDataArray.length-1) { //Another hack for the results -- why is the switch statement above so twitchy?
                    contents.moveTextPositionByAmount(30-(strOverallAvgRnC.length()/2), 0);
                }
                contents.showText(combinedDataArray[i]);
            }
            contents.endText();
            
            //Draw yet another line (below data summary)
            PDF_Y -= 0.5f*fontSize;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            //Average Results Banner
            contents.beginText();
            fontSize = 18;
            contents.setFont(fontBold, fontSize);
            PDF_Y -= 1f*fontSize;
            textLine = "Average Radon Concentration in:          " + strLocation + "          " + strOverallAvgRnC;
            if(strUnitSystem.equals("SI")) {
                textLine += " Bq/m³";
            } else {
                textLine += " pCi/L";
            }
            contents.newLineAtOffset(marginSide,PDF_Y);
            contents.showText(textLine);
            contents.endText();
            
            //Another Line (below Average Results Banner)
            PDF_Y -= 0.5f*fontSize;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            //Deployed By, Retrieved By, Analyzed By Lines
            contents.beginText();
            fontSize = 12;
            contents.setFont(fontBold, fontSize);
            float PDF_Y_temp = PDF_Y;
            PDF_Y_temp -= 1.5f*fontSize; //Let's get a little extra space between this and the previous line
            textLine = "Deployed By: ";
            textWidth = fontBold.getStringWidth(textLine) / 1000 * fontSize;
            contents.newLineAtOffset(marginSide, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y_temp -= 1.1f*fontSize;
            textLine = "Retrieved By: ";
            if((fontDefault.getStringWidth(textLine) / 1000 * fontSize) > textWidth) {
                textWidth = (fontDefault.getStringWidth(textLine) / 1000 * fontSize); //If this textWidth is longer, let's use it to align the technician names
            }
            contents.newLineAtOffset(marginSide, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y_temp -= 1.1f*fontSize;
            textLine = "Analyzed By: ";
            if((fontDefault.getStringWidth(textLine) / 1000 * fontSize) > textWidth) {
                textWidth = (fontDefault.getStringWidth(textLine) / 1000 * fontSize); //If this textWidth is the longest, let's use it to align the technician names
            }
            contents.newLineAtOffset(marginSide, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            fontSize = 12;
            contents.setFont(fontDefault, fontSize);
            PDF_Y -= 1.5f*fontSize; //Let's get a little extra space between this and the previous line
            textLine = strDeployedBy;
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
            textLine = strRetrievedBy;
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
            textLine = strAnalyzedBy;
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            //End Deployed By, Retrieved By, Analyzed By Block
            
            //Conditions, Tampering, Weather, etc.
            PDF_Y -= 1f*fontSize;
            contents.beginText();
            fontSize = 12;
            contents.setFont(fontBold, fontSize);
            PDF_Y_temp = PDF_Y;
            PDF_Y_temp -= 1.5f*fontSize; //Let's get a little extra space between this and the previous line
            textLine = "Conditions:  ";
            textWidth = fontBold.getStringWidth(textLine) / 1000 * fontSize;
            contents.newLineAtOffset(marginSide, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y_temp -= 1.1f*fontSize;
            textLine = "Tampering:  ";
            if((fontDefault.getStringWidth(textLine) / 1000 * fontSize) > textWidth) {
                textWidth = (fontDefault.getStringWidth(textLine) / 1000 * fontSize); //If this textWidth is longer, let's use it to align the technician names
            }
            contents.newLineAtOffset(marginSide, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y_temp -= 1.1f*fontSize;
            textLine = "Weather:  ";
            if((fontDefault.getStringWidth(textLine) / 1000 * fontSize) > textWidth) {
                textWidth = (fontDefault.getStringWidth(textLine) / 1000 * fontSize); //If this textWidth is the longest, let's use it to align the technician names
            }
            contents.newLineAtOffset(marginSide, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y_temp -= 1.1f*fontSize;
            textLine = "Mitigation:  ";
            if((fontDefault.getStringWidth(textLine) / 1000 * fontSize) > textWidth) {
                textWidth = (fontDefault.getStringWidth(textLine) / 1000 * fontSize); //If this textWidth is the longest, let's use it to align the technician names
            }
            contents.newLineAtOffset(marginSide, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            fontSize = 12;
            contents.setFont(fontDefault, fontSize);
            PDF_Y -= 1.5f*fontSize; //Let's get a little extra space between this and the previous line
            textLine = "Unknown";
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
            textLine = "Unknown";
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
            textLine = "Unknown";
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
            textLine = "Unknown";
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            //End Conditions, Weather, Tampering, Mitigation Block
            
            //Double Line
            PDF_Y -= 1f*fontSize;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            PDF_Y -= 3;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            //Radon Health Risk Information Banner
            contents.beginText();
            fontSize = 18;
            contents.setFont(fontBold, fontSize);
            PDF_Y -= 1f*fontSize;
            textLine = "Radon Health Risk Information";
            contents.newLineAtOffset(marginSide,PDF_Y);
            contents.showText(textLine);
            contents.endText();
            
            //Custom Report Text
            Config customReport = new Config();
            strCustomReportText = customReport.getCustomReportText();
            textLine = strCustomReportText;
            fontSize = 12;
            contents.setFont(fontDefault, fontSize);
            PDF_Y -= 2f*fontSize;
            WrapMultiLineText (contents,page,marginSide,PDF_Y,textLine,fontDefault,fontSize,marginSide);
            
            //Bottom Signature Line
            contents.beginText();
            textLine = "Signature: ";
            textWidth = (fontDefault.getStringWidth(textLine) / 1000 * fontSize);
            fontSize = 12;
            contents.setFont(fontDefault, fontSize);
            contents.newLineAtOffset(marginSide, marginBottom);
            contents.showText(textLine);
            contents.endText();
            //Draw Signature Line
            contents.moveTo(marginSide+textWidth, marginBottom); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth()/2 - marginSide, marginBottom); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            //Bottom Date Line
            contents.beginText();
            textLine = "Date: ";
            textWidth = (fontDefault.getStringWidth(textLine) / 1000 * fontSize);
            fontSize = 12;
            contents.setFont(fontDefault, fontSize);
            contents.newLineAtOffset(page.getMediaBox().getWidth()/2 + 30, marginBottom);
            contents.showText(textLine);
            contents.endText();
            //Draw Date Line
            contents.moveTo(page.getMediaBox().getWidth()/2 + 30 + textWidth, marginBottom); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, marginBottom); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            contents.close();
            //END FIRST PAGE (SUMMARY)
            
            //BEGIN SECOND PAGE (CHART)
            PDPage chart_page = new PDPage(PDRectangle.A4);
            doc.addPage(chart_page);
            contents = new PDPageContentStream(doc, chart_page);
            
            //Draw Company Header (we already called getCompanyInfo() above, so no need to call it again...
            DrawCompanyHeader(contents, chart_page, fontDefault, marginTop);
            
            //This draws the graph image (graph.jpg), which was externalized to the file in the CreateGraph class.
            PDImageXObject graphJPG = PDImageXObject.createFromFile("graph.jpg", doc);
            contents.drawImage(graphJPG, 40, 250);
            
            
            //End PDF Generation (i.e. rat's nest code)
            //******************
            
            contents.close();
            
            doc.save(PDF_Name);
            System.out.println(PDF_Name + " has been created.");
            MainMenu.MainMenuUI.lblSystemConsole.setText("PDF has been created.");
        }
        catch (IOException ex) {
            System.out.println(ex);
        }
        finally {
            doc.close();
        }
    }
    
    private void WrapMultiLineText(PDPageContentStream contents, PDPage page, float startX, float startY, String textLine, PDFont fontUsed, int fontSize, int marginSide) {
        List<String> lines = new ArrayList<>(); //let's create an arraylist of strings, each of which will serve as an individual "auto-wrapped" line.
        int lastSpace = -1;
        try {
            while (textLine.length() > 0) { //loop until no more words remain in the original string.
                int spaceIndex = textLine.indexOf(' ', lastSpace + 1); //only *consider* wrapping at spaces (i.e. between words)!
                if(spaceIndex < 0) { //if textLine only has a single word, then let's just take the length of it and hope it's not a billion-character string of gibberish.
                    spaceIndex = textLine.length();
                }
                String subString = textLine.substring(0, spaceIndex); //subString is our current textLine at a space between words (i.e. available to be wrapped if necessary)
                float textWidth = fontSize * fontUsed.getStringWidth(subString) / 1000; //width of textLine
                if(textWidth > page.getMediaBox().getWidth() - 2*marginSide) { //if width of textLine is greater than the available width of the page, let's write it to the arraylist
                    if (lastSpace < 0) { //if lastSpace is still -1, let's move it to the first space
                        lastSpace = spaceIndex;
                    }
                    subString = textLine.substring(0, lastSpace); //this is the maximum line that will fit on our page, so it goes into the arraylist
                    lines.add(subString);
                    textLine = textLine.substring(lastSpace).trim(); //get rid of hanging whitespace
                    lastSpace = -1; //reset lastSpace so that we can continue the loop for the next "line"
                } else if (spaceIndex == textLine.length()) { //if spaceIndex == length of textLine, then we don't need to wrap anything!
                    lines.add(textLine);
                    textLine = ""; //let's exit out of this loop and write our single line
                } else {
                    lastSpace = spaceIndex; //we're not at a maximum width yet... let's keep going and add another word.
                }
            }
            
            contents.beginText(); //now we're ready to draw the line(s) on the PDF...
            contents.newLineAtOffset(startX, startY);
            for(String line: lines) { //iterate through the arraylist and draw each line onto the PDF.
                contents.showText(line);
                contents.newLineAtOffset(0, -1.1f*fontSize);//Let's just increase the spacing by like 10% (1.1f) of the font height
                PDF_Y -= 1.1f*fontSize;
            }
            contents.endText();
            } catch (IOException ex) {
                System.out.println(ex);
            }
    }
    
    public static void GetCompanyInfo() {
        String company_info = "config/company.txt";
        try {
            //The user may not have opened the Config menu, so we'll need to pull information from the company.txt file explicitly.
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(company_info)));
            strCompany_Name = br.readLine();
            strCompany_Address1 = br.readLine();
            strCompany_Address2 = br.readLine();
            strCompany_Address3 = br.readLine();
            br.close();
            
        } catch (IOException e) {
            System.out.println("ERROR: Unable to parse company.txt. There was a problem loading the settings.");
        }
    }
    
    public static void DrawCompanyHeader(PDPageContentStream contents, PDPage page, PDFont fontDefault, int marginTop) {
        //Note: getCompanyInfo() needs to be called beforehand
        int fontSize = 14;
        float textHeight = fontDefault.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        float PDF_Y = page.getMediaBox().getHeight() - marginTop - textHeight;
        try {
            contents.beginText(); //define beginning of text.
            contents.setFont(fontDefault, fontSize); //sets our font using the TTF loaded above.
            contents.setLeading(14.5f);
            String textLine = strCompany_Name;
            PDF_Y = page.getMediaBox().getHeight() - marginTop - textHeight;
            contents.newLineAtOffset(20,PDF_Y);
            contents.showText(textLine);
            contents.newLine();
            textLine = strCompany_Address1;
            contents.showText(textLine);
            contents.newLine();
            textLine = strCompany_Address2;
            contents.showText(textLine);
            contents.newLine();
            textLine = strCompany_Address3;
            contents.showText(textLine);
            contents.endText();
        } catch (IOException ex) {
            System.out.println(ex);    
        }
    }
    
    
}
