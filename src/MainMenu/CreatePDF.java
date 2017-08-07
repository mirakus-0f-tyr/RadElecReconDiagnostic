/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import static MainMenu.LoadSavedFile.strTestSiteInfo;
import static MainMenu.LoadSavedFile.strCustomerInfo;
import static MainMenu.LoadSavedFile.strStartDate;
import static MainMenu.LoadSavedFile.strEndDate;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    
    public static void main() throws IOException {
        
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
            
            contents.beginText(); //define beginning of text.
            contents.setFont(fontDefault, fontSize); //sets our font using the TTF loaded above.
            contents.setLeading(14.5f);
            
            //Company Info Block
            GetCompanyInfo(); //pull info from the company.txt file, so that we can toss that info onto the PDF.
            textHeight = fontDefault.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize; //important for determining to portion of page to begin text
            textLine = strCompany_Name;
            contents.newLineAtOffset(20,page.getMediaBox().getHeight() - marginTop - textHeight);
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
            contents.endText(); //we'll need to reset the text position offset so I can hack a center method into place for the title below...
            
            //Title Block
            contents.beginText(); //this is the only way I know how to reset the text position offset...
            fontSize = 18; //puff up our font-size for the banner title
            contents.setFont(fontBold, fontSize); //sets our bold font using the TTF loaded above.
            contents.setLeading(14.5f);
            textLine = "Radon Test Report"; //Our intended line, the "title" of the report
            textWidth = fontDefault.getStringWidth(textLine) / 1000 * fontSize; //textWidth is important for centering...
            textHeight = fontDefault.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize; //Also important for centering, because PDFBox lacks a centering function...
            contents.moveTextPositionByAmount((page.getMediaBox().getWidth() - textWidth) / 2, page.getMediaBox().getHeight() - marginTop - textHeight - 50); //This will center the text.
            contents.showText(textLine); //This will "draw" our textLine on the PDF.
            contents.endText();
            
            //Date Block (immediately below title)
            contents.beginText();
            fontSize = 12;
            textLine = dateFormat.format(currentDate); //display date beneath the Radon Test Report title
            textWidth = fontDefault.getStringWidth(textLine) / 1000 * fontSize; //still important for centering
            textHeight = fontDefault.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize; //still important for centering
            contents.moveTextPositionByAmount((page.getMediaBox().getWidth() - textWidth) / 2, page.getMediaBox().getHeight() - marginTop - textHeight - 55); //centers the date
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
            contents.moveTextPositionByAmount((page.getMediaBox().getWidth() - textWidth) / 2, page.getMediaBox().getHeight() - marginTop - textHeight - 90); //centers the date
            contents.showText(textLine); //Test Site Banner Text
            contents.endText();
            contents.moveTo(marginSide, page.getMediaBox().getHeight() - marginTop - textHeight - 95); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, page.getMediaBox().getHeight() - marginTop - textHeight - 95); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            contents.moveTo(marginSide, page.getMediaBox().getHeight() - marginTop - textHeight - 98); //getting ready to draw a line (starting coordinates)
            contents.lineTo(page.getMediaBox().getWidth() - marginSide, page.getMediaBox().getHeight() - marginTop - textHeight - 98); //getting ready to draw a line (ending coordinates)
            contents.stroke(); //draw the line, starting at moveTo and ending at lineTo
            
            //Customer Info Block
            contents.beginText(); //write the customer info
            textLine = "Customer Information:";
            contents.setFont(fontBold, fontSize);
            contents.moveTextPositionByAmount(marginSide + 5, page.getMediaBox().getHeight() - marginTop - textHeight - 115); //left-justifies the customer info
            contents.showText(textLine);
            contents.newLine();
            contents.setFont(fontDefault, fontSize);
            String[] CustomerInfo_parsed = strCustomerInfo.split("\\n");
            for(int i = 0; i < CustomerInfo_parsed.length; i++) {
                textLine = CustomerInfo_parsed[i];
                if ((fontDefault.getStringWidth(textLine) / 100 * fontSize) > textWidth) {
                    textWidth = fontDefault.getStringWidth(textLine) / 100 * fontSize;
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
            contents.moveTextPositionByAmount(((page.getMediaBox().getWidth() - marginSide)/2)+30, page.getMediaBox().getHeight() - marginTop - textHeight - 115); //left-justifies the customer info
            contents.showText(textLine);
            contents.newLine();
            contents.setFont(fontDefault, fontSize);
            String[] TestSiteInfo_parsed = strTestSiteInfo.split("\\n");
            TestSiteInfo_parsed = strTestSiteInfo.split("\\n");
            for(int i = 0; i < TestSiteInfo_parsed.length; i++) {
                textLine = TestSiteInfo_parsed[i];
                if ((fontDefault.getStringWidth(textLine) / 100 * fontSize) > textWidth) {
                    textWidth = fontDefault.getStringWidth(textLine) / 100 * fontSize;
                }
                contents.showText(textLine);
                contents.newLine();
            }
            contents.newLine();
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
            contents.addRect(marginSide, page.getMediaBox().getHeight() - marginTop - (textHeight*(LongestTextBlock+2)) - 115, ((page.getMediaBox().getWidth() - marginSide)/2)-25, textHeight * (LongestTextBlock+2));
            contents.stroke();
            //Test Site Info Rectangle:
            contents.addRect(((page.getMediaBox().getWidth() - marginSide)/2)+25, page.getMediaBox().getHeight() - marginTop - (textHeight*(LongestTextBlock+2)) - 115, ((page.getMediaBox().getWidth() - marginSide)/2)-25, textHeight * (LongestTextBlock+2));
            contents.stroke();
            //End Rectangle Section
            
            //Start and End Date Block
            contents.beginText();
            fontSize = 14;
            contents.setFont(fontDefault, fontSize);
            contents.moveTextPositionByAmount(marginSide, page.getMediaBox().getHeight() - marginTop - (textHeight * (LongestTextBlock+2)) - 125);
            contents.newLine();
            textLine = "Test Start Date: " + strStartDate;
            contents.showText(textLine);
            contents.newLine();
            textLine = "Test End Date:   " + strEndDate;
            contents.showText(textLine);
            contents.endText();
            
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
    
    
}
