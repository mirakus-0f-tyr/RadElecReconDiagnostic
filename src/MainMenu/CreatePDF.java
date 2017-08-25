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
import static MainMenu.LoadSavedFile.strCalDate;
import static MainMenu.LoadSavedFile.strReportProtocol;
import static MainMenu.LoadSavedFile.strReportTampering;
import static MainMenu.LoadSavedFile.strReportWeather;
import static MainMenu.LoadSavedFile.strReportMitigation;
import static MainMenu.LoadSavedFile.strReportComment;
import static MainMenu.MainMenuUI.displaySig;

import static MainMenu.CreateGraph.OverallAvgRnC;
import static MainMenu.CreateGraph.HourlyReconData;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    static String validDate = "MM/dd/yyyy";
    SimpleDateFormat dateFormatCalibration = new SimpleDateFormat("MM/dd/yyyy");
    Date currentDate = new Date();
    
    //Margin Stuff
    static int marginTop = 10;
    static int marginBottom = 30;
    static int marginSide = 30;
    static int fontSize = 14;
        
    public void main() throws IOException {
        
        //strips .txt from the filename and replaces it with .pdf
        String PDF_Name = StringUtils.left(MainMenu.MainMenuUI.lblLoadedFileName.getText(),MainMenu.MainMenuUI.lblLoadedFileName.getText().length()-4) + ".pdf";
        
        PDDocument doc = new PDDocument();
        
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
            DrawTitleHeader(contents, page, "Radon Test Report", fontBold, fontDefault);
            
            //Customer / Test Site Info Block
            DrawCustomerTestSiteBlock(contents, page, fontBold, fontDefault);
            
            //Test Summary Block
            drawTestSummaryBlock(contents, page, fontDefault, fontBold);
            
            //Average Radon Concentration Banner
            DrawAverageRadonBanner(contents, page, fontBold, true);
            
            //Calibration Line (same PDF_Y as Analyzed By)
            fontSize = 12;
            textLine = "Cal. Date: " + strCalDate + "   Cal. Due: ";
            String strDateCalibrationDue = "Unknown";
            if(isValidDate(strCalDate)) {
                try {
                    Calendar dateInstance = Calendar.getInstance();
                    dateInstance.setTime((Date)dateFormatCalibration.parse(strCalDate));
                    dateInstance.add(Calendar.YEAR,1);
                    strDateCalibrationDue = dateFormatCalibration.format(dateInstance.getTime());
                } catch (ParseException ex) {
                    System.out.println("Unable to parse calibration date... this shouldn't have happened!");
                    strDateCalibrationDue = "Unknown";
                }
            } else {
                strDateCalibrationDue = "Unknown";
            }
            textLine += strDateCalibrationDue;
            textWidth = fontDefault.getStringWidth(textLine) / 1000 * fontSize;
            contents.beginText();
            contents.setFont(fontDefault, fontSize);
            float PDF_Y_temp = PDF_Y;
            PDF_Y_temp -= 1.5f*fontSize; //Let's get a little extra space between this and the previous line
            contents.newLineAtOffset(page.getMediaBox().getWidth()-marginSide-textWidth, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            
            //Analyzed By, Deployed By, Retrieved By Lines
            fontSize = 12;
            contents.beginText();
            textLine = "Analyzed By: ";
            contents.setFont(fontBold, fontSize);
            textWidth = fontBold.getStringWidth(textLine) / 1000 * fontSize;
            contents.newLineAtOffset(marginSide, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y_temp -= 1.1f*fontSize;
            textLine = "Deployed By: ";
            if((fontDefault.getStringWidth(textLine) / 1000 * fontSize) > textWidth) {
                textWidth = (fontDefault.getStringWidth(textLine) / 1000 * fontSize); //If this textWidth is longer, let's use it to align the technician names
            }
            contents.newLineAtOffset(marginSide, PDF_Y_temp);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y_temp -= 1.1f*fontSize;
            textLine = "Retrieved By: ";
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
            textLine = strAnalyzedBy;
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
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
            //End Analyzed By, Deployed By, Retrieved By Block
            
            //Conditions, Tampering, Weather, etc.
            PDF_Y -= 1f*fontSize;
            contents.beginText();
            fontSize = 12;
            contents.setFont(fontBold, fontSize);
            PDF_Y_temp = PDF_Y;
            PDF_Y_temp -= 1.5f*fontSize; //Let's get a little extra space between this and the previous line
            textLine = "Protocol:  ";
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
            PDF_Y_temp -= 1.1f*fontSize;
            textLine = "Comment:  ";
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
            textLine = strReportProtocol; //Protocols Details
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
            textLine = strReportTampering; //Tampering Details
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
            textLine = strReportWeather; //Weather Details
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
            textLine = strReportMitigation; //Mitigation Details
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            contents.beginText();
            PDF_Y -= 1.1f*fontSize;
            textLine = strReportComment; //Comment Details
            contents.newLineAtOffset(marginSide+textWidth, PDF_Y);
            contents.showText(textLine);
            contents.endText();
            //End Conditions, Weather, Tampering, Mitigation, Comment Block
            
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
            /*contents.beginText();
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
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo*/
            
            if(displaySig==1) {
                drawSignatureLine(contents, page, fontDefault); //Draw Signature Line; for now, only if DisplaySig = 1 in options.
            }
            
            contents.close();
            //END FIRST PAGE (SUMMARY)
            
            //BEGIN SECOND PAGE (CHART)
            PDPage page_chart = new PDPage(PDRectangle.A4);
            doc.addPage(page_chart);
            contents = new PDPageContentStream(doc, page_chart);
            PDF_Y = page_chart.getMediaBox().getHeight() - marginTop - textHeight; //Reset PDF_Y
            
            //Draw Company Header (we already called getCompanyInfo() above, so no need to call it again...
            DrawCompanyHeader(contents, page_chart, fontDefault, marginTop);
            
            //Draw Title Block again on this second page
            DrawTitleHeader(contents, page_chart, "Graphical Radon Report", fontBold, fontDefault);
            
            //Draw Customer / Test Site Info Block on this second page, too...
            DrawCustomerTestSiteBlock(contents, page_chart, fontBold, fontDefault);
            
            //Test Summary Block
            drawTestSummaryBlock(contents, page, fontDefault, fontBold);
            
            //Draw Average Radon Concentration Banner
            DrawAverageRadonBanner(contents, page_chart, fontBold, true);
            
	    //This draws the graph image (graph.png), which was externalized to the file in the CreateGraph class.
	    PDImageXObject graphPNG = PDImageXObject.createFromFile("graph.png", doc);
	    PDF_Y -= 400;
	    contents.drawImage(graphPNG, marginSide*2, PDF_Y);
	    contents.close();
            //END SECOND PAGE (CHART)
            
            //BEGIN THIRD PAGE (DETAILED)
            PDPage page_detailed = new PDPage(PDRectangle.A4);
            doc.addPage(page_detailed);
            contents = new PDPageContentStream(doc, page_detailed);
            PDF_Y = page_detailed.getMediaBox().getHeight() - marginTop - textHeight; //Reset PDF_Y
            
            //Draw Company Header for the 3rd page (we already called getCompanyInfo() above, so no need to call it again...
            DrawCompanyHeader(contents, page_detailed, fontDefault, marginTop);
            
            //Draw Title Block again on this third page
            DrawTitleHeader(contents, page_detailed, "Hourly Radon Report", fontBold, fontDefault);
            
            //Draw Customer / Test Site Info Block on this third page, too...
            DrawCustomerTestSiteBlock(contents, page_detailed, fontBold, fontDefault);
            
            //Test Summary Block
            drawTestSummaryBlock(contents, page, fontDefault, fontBold);
            
            //Draw Average Radon Concentration Banner
            DrawAverageRadonBanner(contents, page_detailed, fontBold, true);
            
            //Draw Column Headers
            DrawDetailedColumnHeaders(contents, fontBold);
            
            //Let's start drawing rows of detailed summary data
            for (int arrayCounter = 0; arrayCounter < HourlyReconData.size(); arrayCounter++) {
                //contents.moveTextPositionByAmount(0,-1.0f*fontSize);
                PDF_Y -= 1.1f*fontSize;
                contents.beginText();
                contents.setFont(fontDefault, fontSize);
                contents.newLineAtOffset(marginSide, PDF_Y);
                //Record #
                textLine = HourlyReconData.get(arrayCounter).get(0);
                contents.showText(textLine);
                //Date-Time
                contents.moveTextPositionByAmount(45, 0);
                textLine = HourlyReconData.get(arrayCounter).get(1);
                contents.showText(textLine);
                //Radon
                contents.moveTextPositionByAmount(115, 0);
                textLine = HourlyReconData.get(arrayCounter).get(2);
                contents.showText(textLine);
                //Temperature
                contents.moveTextPositionByAmount(90, 0);
                textLine = HourlyReconData.get(arrayCounter).get(3);
                contents.showText(textLine);
                //Pressure
                contents.moveTextPositionByAmount(100, 0);
                textLine = HourlyReconData.get(arrayCounter).get(4);
                contents.showText(textLine);
                //Humidity
                contents.moveTextPositionByAmount(95, 0);
                textLine = HourlyReconData.get(arrayCounter).get(5);
                contents.showText(textLine);
                //Tilts
                contents.moveTextPositionByAmount(70, 0);
                textLine = HourlyReconData.get(arrayCounter).get(6);
                contents.showText(textLine);
                
                contents.endText();
                
                
                if((PDF_Y-1.0f*fontSize <= marginBottom) && (arrayCounter < HourlyReconData.size()-1)) { //We need to be able to add a new page for long exposures.
                    //Don't add a new page if we've already drawn our final record! (if arrayCounter < HourlyReconData.size()-1)
                    contents.close();
                    page_detailed = new PDPage(PDRectangle.A4);
                    doc.addPage(page_detailed);
                    contents = new PDPageContentStream(doc, page_detailed);
                    PDF_Y = page_detailed.getMediaBox().getHeight() - marginTop - textHeight; //Reset PDF_Y
                    DrawCompanyHeader(contents, page_detailed, fontDefault, marginTop);
                    DrawTitleHeader(contents, page_detailed, "Radon Detailed Report", fontBold, fontDefault);
                    DrawCustomerTestSiteBlock(contents, page_detailed, fontBold, fontDefault);
                    drawTestSummaryBlock(contents, page, fontDefault, fontBold);
                    DrawAverageRadonBanner(contents, page_chart, fontBold, true);
                    PDF_Y -= 15;
                    DrawDetailedColumnHeaders(contents, fontBold);
                }
                
            }
            
            //End PDF Generation (i.e. rat's nest code)
            //******************
            
            contents.close();
            
	    // check if reports directory exists and write file - create dir if necessary
	    File reportsDir = new File("reports");

	    if (reportsDir.exists() && reportsDir.isDirectory())
		doc.save(reportsDir + "/" + PDF_Name);
	    else {
		System.out.println("Reports directory does not exist.  Creating...");
		reportsDir.mkdir();
		doc.save(reportsDir + "/" + PDF_Name);
	    }
            
            //Draw the footer info (page #, version, etc.)
            //It's a bit shoddy, but because we're appending, we need to have already saved it
            //and then re-open the file.
            File ReconPDF = new File(reportsDir + "/" + PDF_Name);
            drawFooterInfo(ReconPDF, reportsDir + "/" + PDF_Name);

	    if (ReconPDF.exists()) {
		System.out.println(PDF_Name + " has been created.");
		MainMenu.MainMenuUI.lblSystemConsole.setText("PDF has been created.");
	    }
	    else {
		System.out.println("Problem creating PDF.");
		MainMenu.MainMenuUI.lblSystemConsole.setText("Problem creating PDF.");
	    }

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
    
    public void DrawCompanyHeader(PDPageContentStream contents, PDPage page, PDFont fontDefault, int marginTop) {
        //Note: getCompanyInfo() needs to be called beforehand
        int fontSize = 14;
        float textHeight = fontDefault.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        //float PDF_Y = page.getMediaBox().getHeight() - marginTop - textHeight;
        try {
            contents.beginText(); //define beginning of text.
            contents.setFont(fontDefault, fontSize); //sets our font using the TTF loaded above.
            contents.setLeading(14.5f);
            String textLine = strCompany_Name;
            contents.newLineAtOffset(20,PDF_Y);
            contents.showText(textLine);
            textLine = strCompany_Address1;
            PDF_Y -= 1.0f*fontSize;
            contents.newLineAtOffset(0, -1.0f*fontSize);
            contents.showText(textLine);
            textLine = strCompany_Address2;
            PDF_Y -= 1.0f*fontSize;
            contents.newLineAtOffset(0, -1.0f*fontSize);
            contents.showText(textLine);
            textLine = strCompany_Address3;
            PDF_Y -= 1.0f*fontSize;
            contents.newLineAtOffset(0, -1.0f*fontSize);
            contents.showText(textLine);
            contents.endText();
        } catch (IOException ex) {
            System.out.println(ex);    
        }
    }
    
    public void DrawTitleHeader(PDPageContentStream contents, PDPage page, String strTitle, PDFont fontTitle, PDFont fontDate) {
        
        try {
            //Title Block
            contents.beginText(); //this is the only way I know how to reset the text position offset...
            int fontSize = 18; //puff up our font-size for the banner title
            contents.setFont(fontTitle, fontSize); //sets our bold font using the TTF loaded above.
            contents.setLeading(14.5f);
            String textLine = strTitle; //Our intended line, the "title" of the report
            float textWidth = fontTitle.getStringWidth(textLine) / 1000 * fontSize;
            float textHeight = fontTitle.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
            PDF_Y -= 50;
            contents.moveTextPositionByAmount((page.getMediaBox().getWidth() - textWidth) / 2, PDF_Y); //This will center the text.
            contents.showText(textLine); //This will "draw" our textLine on the PDF.
            contents.endText();
            
            //Date Block (immediately below title)
            contents.beginText();
            fontSize = 12;
            textLine = dateFormat.format(currentDate); //display date beneath the Radon Test Report title
            textWidth = fontDate.getStringWidth(textLine) / 1000 * fontSize; //still important for centering
            textHeight = fontDate.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize; //still important for centering
            PDF_Y -= 5;
            contents.moveTextPositionByAmount((page.getMediaBox().getWidth() - textWidth) / 2, PDF_Y); //centers the date
            contents.newLine(); //We should put an extra newLine here, to give us a bit more distance from the title block.
            contents.setFont(fontDate, fontSize);
            contents.showText(textLine);
            contents.endText(); //end date text block    
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    public void DrawCustomerTestSiteBlock(PDPageContentStream contents, PDPage page, PDFont fontBold, PDFont fontDefault) {
        try {
            //Test Site Banner Block
            PDF_Y -= 35;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at l8ineTo
            PDF_Y -= 3;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            //Customer Info Block
            contents.beginText(); //write the customer info
            String textLine = "Customer Information:";
            fontSize = 12;
            contents.setFont(fontBold, fontSize);
            float textWidth = fontBold.getStringWidth(textLine) / 1000 * fontSize; //textWidth is important for centering...
            float textHeight = fontBold.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize; //Also important for centering, because PDFBox lacks a centering function...
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
            
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    private void DrawAverageRadonBanner(PDPageContentStream contents, PDPage page, PDFont font, boolean drawTopDoubleLines) {
        try {
            
            String strOverallAvgRnC;
            if(strUnitSystem.equals("SI")) {
                strOverallAvgRnC = new DecimalFormat("0").format(OverallAvgRnC); //no decimal places for Bq/m3
            } else {
                strOverallAvgRnC = new DecimalFormat("0.0").format(OverallAvgRnC); //tenth decimal place for pCi/L
            }
            
            //Draw yet another pair of double-lines (above Average Results Banner) -- if drawTopDoubleLines is true!
            if(drawTopDoubleLines) {
                PDF_Y -= 0.5f*fontSize;
                contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
                contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
                contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
                PDF_Y -= 3;
                contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
                contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
                contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            }
            
            //Average Results Banner
            contents.beginText();
            fontSize = 18;
            contents.setFont(font, fontSize);
            PDF_Y -= 1f*fontSize;
            String textLine = "Average Radon Concentration in:          " + strLocation + "          " + strOverallAvgRnC;
            if(strUnitSystem.equals("SI")) {
                textLine += " Bq/m³";
            } else {
                textLine += " pCi/L";
            }
            contents.newLineAtOffset(marginSide,PDF_Y);
            contents.showText(textLine);
            contents.endText();
            
            //Another Double-Line (below Average Results Banner)
            PDF_Y -= 0.5f*fontSize;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            PDF_Y -= 3;
            contents.moveTo(marginSide, PDF_Y); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, PDF_Y); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    private void DrawDetailedColumnHeaders(PDPageContentStream contents, PDFont font) {
        try {
            
            //For our column headers, let's generate our units
            String strTempUnits;
            String strPressUnits;
            String strRadonUnits;
            
            if(strUnitSystem.equals("SI")) {
                strTempUnits = " (°C)";
                strPressUnits = " (mbar)";
                strRadonUnits = " (Bq/m³)";
            } else {
                strTempUnits = " (°F)";
                strPressUnits = " (inHg)";
                strRadonUnits = " (pCi/L)";
            }
            
            String textLine;
            float textWidth;
            fontSize = 12;
            PDF_Y -= 15;
            contents.beginText();
            contents.setFont(font, fontSize);
            contents.newLineAtOffset(marginSide, PDF_Y);
            if(MainMenu.MainMenuUI.diagnosticMode) {
                textLine = "Record#";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(15+textWidth, 0);
                textLine = "Date/Time";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(15+textWidth, 0);
                textLine = "Ch1 RnC";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(15+textWidth, 0);
                textLine = "Ch2 RnC";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(15+textWidth, 0);
                textLine = "Avg Radon" + strRadonUnits;
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(15+textWidth, 0);
                textLine = "Temperature" + strTempUnits;
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(15+textWidth, 0);
                textLine = "Pressure" + strPressUnits;
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(15+textWidth, 0);
                textLine = "Humidity (%)";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(15+textWidth, 0);
                textLine = "Tilts";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(15+textWidth, 0);
            } else {
                textLine = "Record#";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(20+textWidth, 0);
                textLine = "Date/Time";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(20+textWidth, 0);
                textLine = "Radon" + strRadonUnits;
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(20+textWidth, 0);
                textLine = "Temperature" + strTempUnits;
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(20+textWidth, 0);
                textLine = "Pressure" + strPressUnits;
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(20+textWidth, 0);
                textLine = "Humidity (%)";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(20+textWidth, 0);
                textLine = "Tilts";
                contents.showText(textLine);
                textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
                contents.moveTextPositionByAmount(20+textWidth, 0);
            }
            
            contents.endText();
            
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    public void drawTestSummaryBlock(PDPageContentStream contents, PDPage page, PDFont fontDefault, PDFont fontBold) {
        try {
            fontSize = 12;
            String textLine = "A Rad Elec Recon® CRM (NRPP Device Code #8304) was used for radon screening measurements that were conducted at the above referenced test site by: " + strCompany_Name;
            
            //WrapMultiLineText is jumbled as all hell, but at least it's continued to a single method.
            PDF_Y -= 20;
            WrapMultiLineText (contents,page,marginSide,PDF_Y,textLine,fontDefault,fontSize,marginSide);
            
            //Results Header Line
            contents.beginText();
            fontSize = 14;
            contents.setFont(fontBold, fontSize);
            PDF_Y -= 0.5f*fontSize; //We already have a space buffer from the WrapMultiLineText() call above, so let's just give us a tad more...
            textLine = "The results are as follows:";
            contents.newLineAtOffset(marginSide,PDF_Y);
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
            String[] strColumnHeaders = {"Serial#", "Instrument", "Location", "Start Date/Time", "End Date/Time", "Results "};
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
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
    
    private void drawSignatureLine(PDPageContentStream contents, PDPage page, PDFont font) {
        try {
            //Bottom Signature Line
            contents.beginText();
            String textLine = "Signature: ";
            float textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
            fontSize = 12;
            contents.setFont(font, fontSize);
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
            textWidth = (font.getStringWidth(textLine) / 1000 * fontSize);
            fontSize = 12;
            contents.setFont(font, fontSize);
            contents.newLineAtOffset(page.getMediaBox().getWidth()/2 + 30, marginBottom);
            contents.showText(textLine);
            contents.endText();
            //Draw Date Line
            contents.moveTo(page.getMediaBox().getWidth()/2 + 30 + textWidth, marginBottom); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, marginBottom); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo  
        } catch (IOException ex) {
            System.out.println("ERROR: Unable to draw signature line!");
        }
    }
    
    //Write page numbers and version numbers in lower right margin
    private void drawFooterInfo(File ReconPDF, String FileName) {
        try {
            PDDocument doc = PDDocument.load(ReconPDF);
            PDFont fontDefault = PDType0Font.load(doc, new File("fonts/calibri.ttf")); //Truetype fonts are easier to utilize when it comes to margins, etc.
            if(doc.getNumberOfPages() >= 1) {
                fontSize = 8;
                for (int numPages = 0; numPages < doc.getNumberOfPages(); numPages++) {
                    PDPage page = doc.getPage(numPages);
                    PDPageContentStream contents = new PDPageContentStream(doc, page, true, false);
                    String textLine = "Page " + (numPages+1) + " / " + doc.getNumberOfPages() + " (" + MainMenu.MainMenuUI.version + ")";
                    float textWidth = (fontDefault.getStringWidth(textLine) / 1000 * fontSize);
                    contents.beginText();
                    contents.setFont(fontDefault, fontSize);
                    contents.newLineAtOffset(page.getMediaBox().getWidth() - marginSide - textWidth, (float) (marginBottom*0.5));
                    contents.showText(textLine);
                    contents.endText();
                    contents.close();
                }
                doc.save(FileName); //Only save if we've actually made changes
            }
        } catch (IOException ex) {
            System.out.println("Could not write page footer lines!");
        }
    }
    
    public static boolean isValidDate(String date) {
        try {
            DateFormat df = new SimpleDateFormat(validDate);
            df.setLenient(false);
            df.parse(date);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }
    
    
}
