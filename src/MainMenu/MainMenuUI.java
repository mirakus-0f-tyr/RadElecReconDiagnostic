/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import Config.Config;
import Config.FlagForm;

import MainMenu.InitDirs;

import java.io.BufferedReader;
import java.io.File;
import java.awt.Desktop;
import java.awt.Window;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;

import static MainMenu.InitDirs.*;
import java.text.DecimalFormat;

/**
 *
 * @author Rad Elec Inc.
 */
public class MainMenuUI extends javax.swing.JFrame {
    
    //Operating System Variables
    //public static boolean boolMacOS = false;

    // OS specific newline string
    public static String newline = System.getProperty("line.separator");
    
    //Logging Variables
    public static boolean initializedLogging = false;
    
    //Directory Variables
    //public static File baseDir = new File("ReconDownloadTool");
    //public static File configDir = new File("config");
    //public static File dataDir = new File("data");
    //public static File fontsDir = new File("fonts");
    //public static File logsDir = new File("logs");
    //public static File reportsDir = new File("reports");
    
    //Rad Elec Recon Variables
    String[] CRM_Parameters;
    public static String version = "v0.9.9.32";
    public static String lastReconCommand = "";
    public static long LastCount_Ch1 = 0;
    public static long LastCount_Ch2 = 0;
    
    //Old variables
    String[] SNandCF;
    String stickerCalDate = "Unknown";
    String stickerNextCalDate = "Unknown";
    public static String strCompanyName = "New Company";
    public static String strAddress1 = "Address Line #1";
    public static String strAddress2 = "Address Line #2";
    public static String strAddress3 = "Address Line #3";
    public static double targetRnCAvg;
    public static double numFirmwareRevision = 0;

    // variables for config.txt file values
    public static boolean diagnosticMode = false;
    public static boolean countLimiter = true;
    public static boolean excludeFirst4Hours = true;
    public static String unitType = "US";
    public static int waitTime = 0;
    public static int testDuration = 48;
    public static boolean displayStatus = false;
    public static int displaySig = 1;
    public static int openPDFWind = 1;
    public static int tiltSensitivity = 5; //Tilt Sensitivity (only applicable when drawing graphs and generating PDFs)
    public static boolean autoLoadFile = true;
    public static boolean dataPathOverride = false;
    public static String specifiedDataDir;
    public static boolean photodiodeFailureRecovery=true; //Attempts to reconstruct graph/PDF using the other chamber when photodiode failure is detected.
    public static boolean createXLS = false; //Generate an end-user XLS spreadsheet alongside the TXT file
    
    //Deployment Variables
    public static String strProtocol = "Closed Building Conditions Met";
    public static String strTampering = "No Tampering Detected";
    public static String strWeather = "No Abnormal Weather Conditions";
    public static String strMitigation = "No Mitigation System Installed";
    public static String strComment = "Thanks for the business!";
    
    //Technician Variables
    public static String strDeployedBy = "Unknown";
    public static String strRetrievedBy = "Unknown";
    public static String strAnalyzedBy = "Unknown";
    
    //Loaded File Variables
    public static String strLoadedFilePath = "Unknown";

    //"Download any session" variables
    public static LinkedList<String> sessionStrings = new LinkedList();
    
    //Troubleshooting Variables
    public static double ConsecutiveZeroLimit = 5; //If this number of consecutive zeros is met (or exceeded) by a chamber when creating a TXT or loading a file, we will alert the user to a potential photodiode failure.

    /**
     * Creates new form MainMenuUI
     */
    public MainMenuUI() {
        //Auto-generated GUI builder

        InitDirs.main();
        
        //baseDir is only used in macOS, to place graph.png
        baseDir = boolMacOS==true ? new File(System.getProperty("user.home")+File.separator+"Documents"+File.separator+"ReconDownloadTool") : new File("ReconDownloadTool");
        
	// check existence of ReconTemplate.xls and inform user if it's not there
	File xlsTemplate = new File("ReconTemplate.xls");
	if (!xlsTemplate.exists())
	    Logging.main("XLS template not present. You will not be able to create XLS files.");

        parseCompanyTXT();
	parseConfigTXT();
        parseDeploymentTXT();
        parseReportTXT();
        initComponents();

	// only call this again if overriding dataDir
	if (dataPathOverride)
	    InitDirs.main();
        
        //Invis certain labels on load
        lblReconSN.setVisible(false);
        lblFirmwareVersion.setVisible(false);
        lblDataSessions.setVisible(false);
        btnCreateTXT.setVisible(false);
        btnClearSession.setVisible(false);
        btnClearMemory.setVisible(false);
        btnAllDataDump.setVisible(false);
        btnDownloadSession.setVisible(false);
        btnOpenSavedFile.setVisible(true);
        btnGeneratePDF.setVisible(false);
        btnEraseReconData.setVisible(false);
        lblLoadedFile.setVisible(false);
        lblLoadedFileName.setVisible(false);
        btnUpdateTXTFile.setVisible(false);
	btnSyncTime.setVisible(false);
        btnClearTamperFlag.setVisible(false);
	if(diagnosticMode) {
            lblVersion.setText(version+" [DIAGNOSTIC]");
        } else {
            lblVersion.setText(version);
        }

	if (countLimiter)
	    limiterLabel.setVisible(false);

	// disable file naming options until we have verified valid data is ready to be downloaded
	EnableFileNaming(false);
	cboSessionSelect.setVisible(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnCloseProgram = new javax.swing.JButton();
        lblRadonScoutQuickCal = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lblSystemConsole = new javax.swing.JLabel();
        btnConnect = new javax.swing.JButton();
        lblReconSN = new javax.swing.JLabel();
        btnCreateTXT = new javax.swing.JButton();
        btnClearMemory = new javax.swing.JButton();
        btnClearSession = new javax.swing.JButton();
        btnAllDataDump = new javax.swing.JButton();
        lblVersion = new javax.swing.JLabel();
        lblFirmwareVersion = new javax.swing.JLabel();
        lblDataSessions = new javax.swing.JLabel();
        btnConfig = new javax.swing.JButton();
        btnDownloadSession = new javax.swing.JButton();
        btnOpenSavedFile = new javax.swing.JButton();
        btnGeneratePDF = new javax.swing.JButton();
        btnEraseReconData = new javax.swing.JButton();
        lblTestSiteInfo = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtTestSiteInfo = new javax.swing.JTextArea();
        lblLoadedFile = new javax.swing.JLabel();
        lblLoadedFileName = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtCustomerInfo = new javax.swing.JTextArea();
        lblTestSiteInfo1 = new javax.swing.JLabel();
        btnUpdateTXTFile = new javax.swing.JButton();
        btnSyncTime = new javax.swing.JButton();
        btnOpenPDF = new javax.swing.JButton();
        limiterLabel = new javax.swing.JLabel();
        btnClearTamperFlag = new javax.swing.JButton();
        lblFinalAvg = new javax.swing.JLabel();
        lblLocation = new javax.swing.JLabel();
        txtLocation = new javax.swing.JTextField();
        txtNewFileName = new javax.swing.JTextField();
        chkUseStreetAddressForFilename = new javax.swing.JCheckBox();
        cboSessionSelect = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Rad Elec Recon Download Tool");
        setIconImages(null);
        setResizable(false);

        btnCloseProgram.setText("Close");
        btnCloseProgram.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        btnCloseProgram.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCloseProgramMouseClicked(evt);
            }
        });
        btnCloseProgram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseProgramActionPerformed(evt);
            }
        });

        lblRadonScoutQuickCal.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblRadonScoutQuickCal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/MainMenu/RadElecReconDownloadTool.png"))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N

        lblSystemConsole.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        lblSystemConsole.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSystemConsole.setText("System Console");

        btnConnect.setFont(new java.awt.Font("Calibri", 1, 24)); // NOI18N
        btnConnect.setText("Connect");
        btnConnect.setToolTipText("Click here to search for the Recon CRM.");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        lblReconSN.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        lblReconSN.setText("Recon S/N:");

        btnCreateTXT.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnCreateTXT.setText("Create TXT/XLS");
        btnCreateTXT.setMaximumSize(new java.awt.Dimension(125, 25));
        btnCreateTXT.setMinimumSize(new java.awt.Dimension(125, 25));
        btnCreateTXT.setPreferredSize(new java.awt.Dimension(125, 25));
        btnCreateTXT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCreateTXTActionPerformed(evt);
            }
        });

        btnClearMemory.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnClearMemory.setText("Clear All Memory (:CM)");
        btnClearMemory.setMaximumSize(new java.awt.Dimension(125, 25));
        btnClearMemory.setMinimumSize(new java.awt.Dimension(125, 25));
        btnClearMemory.setPreferredSize(new java.awt.Dimension(125, 25));
        btnClearMemory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearMemoryActionPerformed(evt);
            }
        });

        btnClearSession.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnClearSession.setText("Clear Current Session (:CD)");
        btnClearSession.setMaximumSize(new java.awt.Dimension(125, 25));
        btnClearSession.setMinimumSize(new java.awt.Dimension(125, 25));
        btnClearSession.setPreferredSize(new java.awt.Dimension(125, 25));
        btnClearSession.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearSessionActionPerformed(evt);
            }
        });

        btnAllDataDump.setFont(new java.awt.Font("Calibri", 1, 12)); // NOI18N
        btnAllDataDump.setLabel("All Data Dump");
        btnAllDataDump.setMaximumSize(new java.awt.Dimension(125, 25));
        btnAllDataDump.setMinimumSize(new java.awt.Dimension(125, 25));
        btnAllDataDump.setPreferredSize(new java.awt.Dimension(125, 25));
        btnAllDataDump.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAllDataDumpActionPerformed(evt);
            }
        });

        lblVersion.setFont(new java.awt.Font("Calibri", 0, 11)); // NOI18N
        lblVersion.setText("v?");
        lblVersion.setToolTipText("");

        lblFirmwareVersion.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        lblFirmwareVersion.setText("Firmware v");

        lblDataSessions.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        lblDataSessions.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDataSessions.setText("Data Sessions:");
        lblDataSessions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblDataSessions.setMaximumSize(new java.awt.Dimension(181, 16));
        lblDataSessions.setMinimumSize(new java.awt.Dimension(181, 16));
        lblDataSessions.setName(""); // NOI18N
        lblDataSessions.setPreferredSize(new java.awt.Dimension(181, 16));

        btnConfig.setText("Config");
        btnConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigActionPerformed(evt);
            }
        });

        btnDownloadSession.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnDownloadSession.setText("Download Session");
        btnDownloadSession.setToolTipText("Download current session from Recon.");
        btnDownloadSession.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownloadSessionActionPerformed(evt);
            }
        });

        btnOpenSavedFile.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnOpenSavedFile.setText("Open Saved File");
        btnOpenSavedFile.setToolTipText("Open previously downloaded data file.");
        btnOpenSavedFile.setName(""); // NOI18N
        btnOpenSavedFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenSavedFileActionPerformed(evt);
            }
        });

        btnGeneratePDF.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnGeneratePDF.setToolTipText("Generate PDF from current results.");
        btnGeneratePDF.setLabel("Generate PDF");
        btnGeneratePDF.setMaximumSize(new java.awt.Dimension(136, 26));
        btnGeneratePDF.setMinimumSize(new java.awt.Dimension(136, 26));
        btnGeneratePDF.setName(""); // NOI18N
        btnGeneratePDF.setPreferredSize(new java.awt.Dimension(136, 26));
        btnGeneratePDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGeneratePDFActionPerformed(evt);
            }
        });

        btnEraseReconData.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnEraseReconData.setText("Clear Session");
        btnEraseReconData.setToolTipText("Clears current data session from Recon.");
        btnEraseReconData.setMaximumSize(new java.awt.Dimension(136, 26));
        btnEraseReconData.setMinimumSize(new java.awt.Dimension(136, 26));
        btnEraseReconData.setPreferredSize(new java.awt.Dimension(136, 26));
        btnEraseReconData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEraseReconDataActionPerformed(evt);
            }
        });

        lblTestSiteInfo.setFont(new java.awt.Font("Calibri", 1, 14)); // NOI18N
        lblTestSiteInfo.setText("Test Site Information");

        txtTestSiteInfo.setColumns(20);
        txtTestSiteInfo.setRows(5);
        txtTestSiteInfo.setTabSize(4);
        txtTestSiteInfo.setBorder(null);
        jScrollPane1.setViewportView(txtTestSiteInfo);

        lblLoadedFile.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        lblLoadedFile.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLoadedFile.setText("Loaded File");
        lblLoadedFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblLoadedFile.setMaximumSize(new java.awt.Dimension(181, 16));
        lblLoadedFile.setMinimumSize(new java.awt.Dimension(181, 16));
        lblLoadedFile.setName(""); // NOI18N
        lblLoadedFile.setPreferredSize(new java.awt.Dimension(181, 16));

        lblLoadedFileName.setFont(new java.awt.Font("Calibri", 1, 12)); // NOI18N
        lblLoadedFileName.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLoadedFileName.setText("No File Loaded");
        lblLoadedFileName.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblLoadedFileName.setMaximumSize(new java.awt.Dimension(181, 16));
        lblLoadedFileName.setMinimumSize(new java.awt.Dimension(181, 16));
        lblLoadedFileName.setName(""); // NOI18N
        lblLoadedFileName.setPreferredSize(new java.awt.Dimension(181, 16));

        txtCustomerInfo.setColumns(20);
        txtCustomerInfo.setRows(5);
        txtCustomerInfo.setTabSize(4);
        txtCustomerInfo.setBorder(null);
        jScrollPane2.setViewportView(txtCustomerInfo);
        txtCustomerInfo.getAccessibleContext().setAccessibleName("");

        lblTestSiteInfo1.setFont(new java.awt.Font("Calibri", 1, 14)); // NOI18N
        lblTestSiteInfo1.setText("Customer Information");

        btnUpdateTXTFile.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnUpdateTXTFile.setText("Update TXT");
        btnUpdateTXTFile.setToolTipText("Update currently opened file with updated customer and test site info.");
        btnUpdateTXTFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateTXTFileActionPerformed(evt);
            }
        });

        btnSyncTime.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnSyncTime.setToolTipText("Sync Recon time to PC time. Must have Recon connected.");
        btnSyncTime.setLabel("Sync Time");
        btnSyncTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSyncTimeActionPerformed(evt);
            }
        });

        btnOpenPDF.setFont(new java.awt.Font("Calibri", 0, 12)); // NOI18N
        btnOpenPDF.setText("Open PDF Folder");
        btnOpenPDF.setToolTipText("Opens the PDF directory, where your generated reports are stored.");
        btnOpenPDF.setName(""); // NOI18N
        btnOpenPDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenPDFActionPerformed(evt);
            }
        });

        limiterLabel.setForeground(new java.awt.Color(204, 0, 0));
        limiterLabel.setText("COUNT LIMITER = OFF");

        btnClearTamperFlag.setFont(new java.awt.Font("Calibri", 1, 12)); // NOI18N
        btnClearTamperFlag.setText("Clear Tamper Flag");
        btnClearTamperFlag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearTamperFlagActionPerformed(evt);
            }
        });

        lblFinalAvg.setFont(new java.awt.Font("Calibri", 1, 24)); // NOI18N

        lblLocation.setFont(new java.awt.Font("Calibri", 1, 14)); // NOI18N
        lblLocation.setText("Test Location");

        txtLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtLocationActionPerformed(evt);
            }
        });

        txtNewFileName.setToolTipText("Enter preferred filename for the downloaded test. Do not include an extension.");

        chkUseStreetAddressForFilename.setFont(new java.awt.Font("Calibri", 1, 14)); // NOI18N
        chkUseStreetAddressForFilename.setText("Use street address?");
        chkUseStreetAddressForFilename.setToolTipText("Check to use the first line of Test Site Information.");

        cboSessionSelect.setFont(new java.awt.Font("Calibri", 1, 14)); // NOI18N
        cboSessionSelect.setToolTipText("Select a data session to download.");
        cboSessionSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboSessionSelectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lblRadonScoutQuickCal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblVersion)
                            .addComponent(limiterLabel)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblTestSiteInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblLocation))
                                .addGap(91, 91, 91)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(lblLoadedFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnCloseProgram, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(lblLoadedFile, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addComponent(chkUseStreetAddressForFilename)
                                                .addComponent(txtNewFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                                                .addComponent(cboSessionSelect, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(lblSystemConsole, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(btnConnect, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lblDataSessions, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(lblReconSN, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                                            .addComponent(lblFirmwareVersion, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblFinalAvg)
                                        .addGap(68, 68, 68))))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(229, 229, 229)
                                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblTestSiteInfo1, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnGeneratePDF, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(1, 1, 1)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(btnEraseReconData, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(btnUpdateTXTFile, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(btnSyncTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(btnAllDataDump, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                                    .addComponent(btnClearTamperFlag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnOpenSavedFile, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnClearSession, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnDownloadSession, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnCreateTXT, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(btnOpenPDF, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(btnClearMemory, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(8, 8, 8))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblRadonScoutQuickCal)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblVersion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(limiterLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCreateTXT, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDownloadSession, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFirmwareVersion))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClearSession, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOpenSavedFile, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnClearMemory, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnOpenPDF, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnGeneratePDF, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnEraseReconData, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnAllDataDump, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClearTamperFlag, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnUpdateTXTFile, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSyncTime, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblReconSN)
                            .addComponent(btnConnect, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblSystemConsole)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblDataSessions, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblFinalAvg))
                        .addGap(18, 18, 18)
                        .addComponent(lblTestSiteInfo1, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblTestSiteInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cboSessionSelect, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNewFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(chkUseStreetAddressForFilename)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblLoadedFile, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblLoadedFileName, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnCloseProgram, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        limiterLabel.getAccessibleContext().setAccessibleName("limiterLabel");

        setSize(new java.awt.Dimension(795, 653));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseProgramMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCloseProgramMouseClicked
        System.exit(0);
    }//GEN-LAST:event_btnCloseProgramMouseClicked

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        MySwingWorker worker = new MySwingWorker();
        worker.execute();
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnCreateTXTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateTXTActionPerformed
        GenerateTXTDump worker = new GenerateTXTDump();
        worker.execute();
    }//GEN-LAST:event_btnCreateTXTActionPerformed

    private void btnClearMemoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearMemoryActionPerformed
        ClearReconMemory worker = new ClearReconMemory();
        worker.execute();
    }//GEN-LAST:event_btnClearMemoryActionPerformed

    private void btnClearSessionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearSessionActionPerformed
        ClearCurrentSession worker = new ClearCurrentSession();
        worker.execute();
    }//GEN-LAST:event_btnClearSessionActionPerformed

    private void btnAllDataDumpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAllDataDumpActionPerformed
        AllDataDump worker = new AllDataDump();
        worker.execute();
    }//GEN-LAST:event_btnAllDataDumpActionPerformed

    private void btnConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigActionPerformed
        JFrame frameOptions = new Config();
        frameOptions.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameOptions.setLocationRelativeTo(this);
        frameOptions.setVisible(true);
    }//GEN-LAST:event_btnConfigActionPerformed

    private void btnDownloadSessionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownloadSessionActionPerformed
        DownloadSession worker = new DownloadSession();
	worker.execute();
    }//GEN-LAST:event_btnDownloadSessionActionPerformed

    private void btnOpenSavedFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenSavedFileActionPerformed
        JFileChooser SavedReconTXT_Dialog = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt"); //This will set the filters we'll allow.
        SavedReconTXT_Dialog.setFileFilter(filter); //Applies the filter to SavedReconTXTDialog
        //File workingDirectory = new File(System.getProperty("user.dir") + File.separator + "data"); //Default location of file dialog will be the data directory.
        File workingDirectory = dataDir;
        SavedReconTXT_Dialog.setCurrentDirectory(workingDirectory); //Sets default directory
        int returnVal = SavedReconTXT_Dialog.showOpenDialog(null); //This instantiates the file dialog window.
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            Logging.main("File loaded: " + SavedReconTXT_Dialog.getSelectedFile().getName()); //Lets us know if user selected a valid file.
            lblLoadedFileName.setText(SavedReconTXT_Dialog.getSelectedFile().getName());
            lblLoadedFile.setVisible(true);
            lblLoadedFileName.setVisible(true);

	    // go through windows and close previous graph(s) that were open
	    Window[] progWindows = Window.getWindows();
	    for (Window window : progWindows) {
		if (window.getClass().toString().contains("MainMenu.CreateGraph"))
		window.dispose();
	    }

            try {
                LoadSavedFile.main(SavedReconTXT_Dialog.getSelectedFile().getCanonicalPath());
                strLoadedFilePath = SavedReconTXT_Dialog.getSelectedFile().getCanonicalPath();
            } catch (IOException ex) {
                Logging.main("ERROR: Unable to determine file path for the loaded file!");
            }

	    // The below should only be done if file was opened successfully - come back to this
	    btnGeneratePDF.setVisible(true); // draw the GeneratePDF button
	    btnUpdateTXTFile.setVisible(true); // draw the UpdateTXT button
        } else {
            lblLoadedFile.setVisible(false);
            lblLoadedFileName.setVisible(false);
        }
    }//GEN-LAST:event_btnOpenSavedFileActionPerformed

    private void btnGeneratePDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGeneratePDFActionPerformed
        GeneratePDF worker = new GeneratePDF();
        worker.execute();
    }//GEN-LAST:event_btnGeneratePDFActionPerformed

    private void btnEraseReconDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEraseReconDataActionPerformed
        ClearCurrentSession worker = new ClearCurrentSession();
	worker.execute();
    }//GEN-LAST:event_btnEraseReconDataActionPerformed

    private void btnUpdateTXTFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateTXTFileActionPerformed
	// Write the new file.
	UpdateTXTFile worker = new UpdateTXTFile();
	worker.execute();

	// Determine new filename and reopen
	String newFileName = lblLoadedFileName.getText().substring(0, lblLoadedFileName.getText().lastIndexOf('.')) + "_updated.txt";
	File myFile;

	// Clean up the name if we're updating an already updated file.
	if (newFileName.contains("_updated_updated")) {
	    String updNewFileName = newFileName.replace("_updated_updated", "_updated");
	    myFile = new File(dataDir + File.separator + updNewFileName);
	}
	else
	    myFile = new File(dataDir + File.separator + newFileName);

	// go through windows and close previous graph(s) that were open
	Window[] progWindows = Window.getWindows();
	for (Window window : progWindows) {
	    if (window.getClass().toString().contains("MainMenu.CreateGraph"))
		window.dispose();
	}

	try {
	    LoadSavedFile.main(myFile.getCanonicalPath());
	} catch (IOException ex) {
	    Logging.main("ERROR: Unable to open updated text file!");
	    Logging.main(ex.toString());
	}
	lblLoadedFileName.setText(myFile.getName());
    }//GEN-LAST:event_btnUpdateTXTFileActionPerformed

    private void btnSyncTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSyncTimeActionPerformed
        SyncReconTime worker = new SyncReconTime();
	worker.execute();
    }//GEN-LAST:event_btnSyncTimeActionPerformed

    private void btnOpenPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenPDFActionPerformed
        Desktop desktop = null;
        try {
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
                desktop.open(InitDirs.reportsDir);
            }
            else {
                Logging.main("ERROR: Opening PDF Folder!");
            }
        } catch (IOException ex) { }
    }//GEN-LAST:event_btnOpenPDFActionPerformed

    private void btnClearTamperFlagActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearTamperFlagActionPerformed
	ClearTamperStatus worker = new ClearTamperStatus();
	worker.execute();
    }//GEN-LAST:event_btnClearTamperFlagActionPerformed

    private void txtLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtLocationActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtLocationActionPerformed

    private void btnCloseProgramActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseProgramActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCloseProgramActionPerformed

    private void cboSessionSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboSessionSelectActionPerformed
       MainMenu.ReconCommand.currentSession = cboSessionSelect.getSelectedIndex();
       RefreshDefaultFileName();
    }//GEN-LAST:event_cboSessionSelectActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainMenuUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainMenuUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainMenuUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainMenuUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame myFrame = new MainMenuUI();
                myFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                myFrame.setVisible(true);
                //Eventually need to learn how to implement custom window icons...
                //myFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("assets/radelec.ico")));
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAllDataDump;
    private javax.swing.JButton btnClearMemory;
    private javax.swing.JButton btnClearSession;
    private javax.swing.JButton btnClearTamperFlag;
    private javax.swing.JButton btnCloseProgram;
    public static javax.swing.JButton btnConfig;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnCreateTXT;
    private javax.swing.JButton btnDownloadSession;
    private javax.swing.JButton btnEraseReconData;
    public static javax.swing.JButton btnGeneratePDF;
    private javax.swing.JButton btnOpenPDF;
    private javax.swing.JButton btnOpenSavedFile;
    private javax.swing.JButton btnSyncTime;
    public static javax.swing.JButton btnUpdateTXTFile;
    public static javax.swing.JComboBox<String> cboSessionSelect;
    public static javax.swing.JCheckBox chkUseStreetAddressForFilename;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    public static javax.swing.JLabel lblDataSessions;
    public static javax.swing.JLabel lblFinalAvg;
    public static javax.swing.JLabel lblFirmwareVersion;
    public static javax.swing.JLabel lblLoadedFile;
    public static javax.swing.JLabel lblLoadedFileName;
    private javax.swing.JLabel lblLocation;
    private javax.swing.JLabel lblRadonScoutQuickCal;
    public static javax.swing.JLabel lblReconSN;
    public static javax.swing.JLabel lblSystemConsole;
    private javax.swing.JLabel lblTestSiteInfo;
    private javax.swing.JLabel lblTestSiteInfo1;
    public static javax.swing.JLabel lblVersion;
    private javax.swing.JLabel limiterLabel;
    public static javax.swing.JTextArea txtCustomerInfo;
    public static javax.swing.JTextField txtLocation;
    public static javax.swing.JTextField txtNewFileName;
    public static javax.swing.JTextArea txtTestSiteInfo;
    // End of variables declaration//GEN-END:variables

//Returns today's date as the default calibration date.
public String DefaultCalDate() {
    LocalDate caldate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/dd/yyyy");
    DateTimeFormatter sticker_format = DateTimeFormatter.ofPattern("MMM d, yyyy");
    stickerCalDate = caldate.format(sticker_format);
    return caldate.format(formatter);
}

//Returns the next calibration date (one year from current calibration date)
public String NextCalDate() {
    LocalDate nextcaldate = LocalDate.now().plusYears(1);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/dd/yyyy");
    DateTimeFormatter sticker_format = DateTimeFormatter.ofPattern("MMM d, yyyy");
    stickerNextCalDate = nextcaldate.format(sticker_format);
    return nextcaldate.format(formatter);
}

public static void displayProgressLabel(String update_progress) {
    lblSystemConsole.setText(update_progress);
}

public static void displaySerialNumber(String ReconSerial) {
    lblReconSN.setVisible(true);
    lblReconSN.setText("Recon S/N: #" + ReconSerial);
}

public static void displayFirmwareVersion(String FirmwareVersion) {
    lblFirmwareVersion.setVisible(true);
    lblFirmwareVersion.setText("Firmware v" + FirmwareVersion);
}

public static void displayDataSessions(String NumSessions) {
    lblDataSessions.setVisible(true);
    lblDataSessions.setText("Data Sessions: " + NumSessions);
}

// Get the data sessions as was recorded in the lblDataSessions variable.
public static int getDataSessions() {
	// This will FAIL if we ever change the Data Sessions: wording!
	String sessionString = lblDataSessions.getText().substring(15);
	sessionString = sessionString.trim();
	return Integer.parseInt(sessionString);
}

public static double convertFirmwareVersionToNumber() {
    try {
        String strFirmwareRevision = lblFirmwareVersion.getText();
        if(strFirmwareRevision.length() > 10) {
            numFirmwareRevision = Double.parseDouble(strFirmwareRevision.substring(11));
            Logging.main("MainMenuUI.convertFirmwareVersionToNumber() = " + numFirmwareRevision);
            return numFirmwareRevision;
        }
        return 0;
    } catch (Exception e) {
        //If we can't correctly parse the firmware revision, then let's default to zero.
        return 0;
    }
}

public void parseCompanyTXT() {
    String company_info = configDir + File.separator + "company.txt";
    try {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(company_info)));
        strCompanyName = br.readLine();
        strAddress1 = br.readLine();
        strAddress2 = br.readLine();
        strAddress3 = br.readLine();
        br.close();
    } catch (IOException e) {
        Logging.main("WARNING: Unable to parse company.txt file... attempting to create file.");
        createCompanyTXT();
    }
}

public void createCompanyTXT() {
    String companyTXT = configDir + File.separator + "company.txt";
        try {
            PrintWriter pw = new PrintWriter(companyTXT);
	    pw.println(newline);
	    pw.println(newline);
            pw.close();
        } catch (FileNotFoundException ex) {
            Logging.main("ERROR: Unable to create company.txt file!");
        }
}

public static void createConfigTXT() {
    String configTXT = configDir + File.separator + "config.txt";
        try {
            PrintWriter pw = new PrintWriter(configTXT);
            pw.print("UnitType=US" + newline);
            pw.print("DisplaySig=1" + newline);
	    pw.print("OpenPDFWindow=1" + newline);
            pw.print("TiltSensitivity=5" + newline);
            pw.print("AutoLoadFile=1" + newline);
            pw.close();
        } catch (FileNotFoundException ex) {
            Logging.main("ERROR: Unable to create config.txt file!");
        }
}

public static void parseConfigTXT() {
    // set name of config text file
    String configTextFile = configDir + File.separator + "config.txt";
    
    // try to parse the config file
    try {
        
        Logging.main("Loading current settings from config.txt...");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configTextFile)));
   
        for (String strLine = br.readLine(); strLine != null; strLine = br.readLine()) {

	    // first, check for commented lines
	    if (strLine.charAt(0) == '#')
	        continue;

            if(strLine.equals("DiagMode=0011")) {
                diagnosticMode = true; //Defaults to End-User mode, only switching to Diagnostic mode if properly applied in config.          
            } else if (strLine.contains("COUNT_LIMITER=OFF")) {
		countLimiter = false;
            } else if (strLine.contains("PhotodiodeFailureRecovery=")) {
                photodiodeFailureRecovery = strLine.endsWith("1");
	    } else if(strLine.contains("UnitType=")) { //Defaults to US units if anything unexpected appears.
                if(strLine.contains("SI")) {
                    unitType = "SI";
                } else {
                    unitType = "US";
                }
            } else if(strLine.contains("WaitTime=")) { //This should robustly parse WaitTime, and default to zero if there's gibberish.
                String[] strSplitWaitTime = strLine.split("=");
                if(strLine.length() < 10) { //If the length for WaitTime= is less than 10, then we know that no value follows the parameter in the config file. Default to zero.
                    waitTime = 0;
                } else {
                    try {
                        waitTime = Integer.parseInt(strSplitWaitTime[1]);
                    } catch (NumberFormatException e) {
                        waitTime = 0; //If we cannot make an integer out of what follows WaitTime=, then this should make sure it stays zero.
                    }
                }
            } else if(strLine.contains("TestDur=")) {
                String[] strSplitTestDur = strLine.split("=");
                if(strLine.length() < 9) { //If the length for TestDur= is less than 9, then we know that no value follows the parameter in the config file. Default to zero.
                    testDuration = 0;
                } else {
                    try {
                        testDuration = Integer.parseInt(strSplitTestDur[1]);
                    } catch (NumberFormatException e) {
                        testDuration = 0; //If we cannot make an integer out of what follows TestDur=, default to zero.
                    }
                }
            } else if(strLine.contains("DispRes=")) {
                displayStatus = strLine.endsWith("1");
            } else if(strLine.contains("DisplaySig=")) {
                displaySig = Integer.parseInt(strLine.substring(strLine.length()-1)); //This should parse the DisplaySig
            }
	      else if(strLine.contains("OpenPDFWindow=")) {
	        openPDFWind = Integer.parseInt(strLine.substring(strLine.length()-1)); // parse opening reports folder preference
	    } else if(strLine.contains("TiltSensitivity=")) {
                String[] strSplitTiltSensitivity = strLine.split("=");
                if(strLine.length() < 17) {
                    tiltSensitivity = 5; //If there isn't any value after TiltSensitivity=, then let's just default to 5.
                } else {
                    try {
                        tiltSensitivity = Integer.parseInt(strSplitTiltSensitivity[1]);
                    } catch (NumberFormatException e) {
                        tiltSensitivity = 5;
                    }
                    if(tiltSensitivity>10) {
                        tiltSensitivity = 10;
                    } else if (tiltSensitivity < 0) {
                        tiltSensitivity = 0;
                    }
                }
            } else if(strLine.contains("AutoLoadFile=")) {
                autoLoadFile = !strLine.contains("0");
            } else if(strLine.contains("CreateXLS=")) {
                createXLS = !strLine.contains("0");    
            }
	      else if(strLine.contains("DataDir=")) {
		dataPathOverride = true;
		specifiedDataDir = strLine.substring(8);
		Logging.main("Data directory overridden. New location is " + specifiedDataDir);
	    }
        }

	// cleanup buffered reader
	br.close();
    }

    // if error, print error and show stack trace
    catch (IOException e){
	Logging.main("WARNING: Unable to parse config.txt file... attempting to create file.");
        createConfigTXT();
    }
    
}

public static void createDeploymentTXT() {
    String configTXT = configDir + File.separator + "deployment.txt";
        try {
            PrintWriter pw = new PrintWriter(configTXT);
            pw.print("Protocol: Closed Building Conditions Met" + newline);
            pw.print("Tampering: No Tampering Detected" + newline);
            pw.print("Weather: No Abnormal Weather Conditions" + newline);
            pw.print("Mitigation: No Mitigation System Installed" + newline);
            pw.print("Comment: Thanks for the business!" + newline);
            pw.close();
        } catch (FileNotFoundException ex) {
            Logging.main("ERROR: Unable to create deployment.txt file!");
        }
}

public static void parseDeploymentTXT() {
    // set name of deployment text file
    String configTextFile = configDir + File.separator + "deployment.txt";
    
    // try to parse the deployment file
    try {  
        Logging.main("Loading current settings from deployment.txt...");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configTextFile)));
   
        for (String strLine = br.readLine(); strLine != null; strLine = br.readLine()) {
            if(strLine.length() > 8 && strLine.substring(0,9).contains("Protocol:")) {
                strProtocol = strLine.substring(9).trim(); //Should robustly parse protocol.
            } else if(strLine.length() > 9 && strLine.substring(0,10).contains("Tampering:")) {
                strTampering = strLine.substring(10).trim(); //Should robustly parse tampering.
            } else if(strLine.length() > 7 && strLine.substring(0,8).contains("Weather:")) {
                strWeather = strLine.substring(8).trim(); //Should robustly parse weather.
            } else if(strLine.length() > 10 && strLine.substring(0,11).contains("Mitigation:")) {
                strMitigation = strLine.substring(11).trim(); //Should robustly parse mitigation.
            } else if(strLine.length() > 7 && strLine.substring(0,8).contains("Comment:")) {
                strComment = strLine.substring(8).trim(); //Should robustly parse comment.
            }
	}
	// cleanup buffered reader
	br.close();
    } catch (IOException e){
	Logging.main("WARNING: Unable to parse deployment.txt file... attempting to create file.");
        //Assign default values to the variables...
        strProtocol = "Closed Building Conditions Met";
        strTampering = "No Tampering Detected";
        strWeather = "No Abnormal Weather Conditions";
        strMitigation = "No Mitigation System Installed";
        strComment = "Thanks for the business!";
        createDeploymentTXT();
    }
    
}

public static void createReportTXT() {
    String companyTXT = configDir + File.separator + "report.txt";
        try {
            PrintWriter pw = new PrintWriter(companyTXT);
            pw.print("DeployedBy=" + newline);
            pw.print("RetrievedBy=" + newline);
            pw.print("AnalyzedBy=" + newline);
            pw.print("Radon is the second leading cause of lung cancer after smoking. The U.S. Environmental Protection Agency (US EPA)");
            pw.print(" and the Surgeon General strongly recommend that further action be taken when a home’s radon test results are 4.0 pCi/L or greater.");
            pw.print(" The national average indoor radon level is about 1.3 pCi/L. The higher the home’s radon level, the greater the health risk to you");
            pw.print(" and your family. Reducing your radon levels can be done easily, effectively and fairly inexpensively. Even homes with very high");
            pw.print(" radon levels can be reduced below 4.0 pCi/L. Please refer to the EPA website at www.epa.gov/radon for further information to assist");
            pw.print(" you in evaluating your test results or deciding if further action is needed." + newline);
            pw.close();
        } catch (FileNotFoundException ex) {
            Logging.main("ERROR: Unable to create report.txt file!");
        }
}

public static void parseReportTXT() {
    // set name of report text file
    String configTextFile = configDir + File.separator + "report.txt";
    
    // try to parse the report.txt, but only for technicians at the moment...
    try {  
        Logging.main("Loading current settings from report.txt...");
        
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configTextFile)));
   
        for (String strLine = br.readLine(); strLine != null; strLine = br.readLine()) {
            if(strLine.length() > 10 && strLine.substring(0,11).contains("DeployedBy=")) {
                strDeployedBy = strLine.substring(11).trim(); //Should robustly parse deployment tech
            } else if(strLine.length() > 11 && strLine.substring(0,12).contains("RetrievedBy=")) {
                strRetrievedBy = strLine.substring(12).trim(); //Should robustly parse retrieval tech
            } else if(strLine.length() > 10 && strLine.substring(0,11).contains("AnalyzedBy=")) {
                strAnalyzedBy = strLine.substring(11).trim(); //Should robustly parse analyst
            }
        }
	// cleanup buffered reader
	br.close();
    } catch (IOException e){
	Logging.main("WARNING: Unable to parse report.txt file... attempting to create file.");
        //Assign default values to the variables...
        strDeployedBy = "Unknown";
        strRetrievedBy = "Unknown";
        strAnalyzedBy = "Unknown";
        createReportTXT();
    }
    
}

public static void findOperatingSystem() {
    String strOperatingSystem = System.getProperty("os.name");
    Logging.main("Operating System: " + strOperatingSystem);
    boolMacOS = strOperatingSystem.startsWith("Mac");
    if(boolMacOS) {
        Logging.main("WARNING: Walled garden detected. Redirecting config/data/reports folders to documents...");
    }
}

public static void DisplayAvgRadonLabel(double AvgRnC) {
    String strAvgRnC;
    if(unitType.equals("SI")) {
        strAvgRnC = new DecimalFormat("0").format(AvgRnC); //No decimal places for Bq/m^3...
    } else {
        strAvgRnC = new DecimalFormat("0.0").format(AvgRnC);
    }
    lblFinalAvg.setText("Average Radon: " + strAvgRnC + (unitType.equals("SI") ? " Bq/m³" : " pCi/L"));
}

public static void checkFilesWrittenSuccessfully() {
// For now the method only checks if the files exist. There are occasions when
// the file will exist but still might not have been written correctly.
// This should be expanded as we encounter any such files.

    File txt_file = new File(ReconCommand.filenameTXT);
    File xls_file = new File(ReconCommand.filenameXLS);

    if (diagnosticMode) {
        if (txt_file.exists() && xls_file.exists()) {
	    displayProgressLabel("TXT and XLS written successfully.");
	    Logging.main("TXT and XLS written successfully.");
	}
	else {
	    displayProgressLabel("Error: Problem saving TXT and XLS files.");
	    Logging.main("Error: Problem saving TXT and XLS files.");
	}
    }
    else {
        if (txt_file.exists()) {
	    displayProgressLabel("TXT file written successfully.");
	    Logging.main("TXT file written successfully.");
	}
	else {
	    displayProgressLabel("Error: Problem saving TXT file.");
	    Logging.main("Error: Problem saving TXT file.");
	}
    }
}

public static void checkAutoLoadFile() {
    
    File txt_file = new File(ReconCommand.filenameTXT);
    String[] strSplitFileName;
    String strSimpleFileName = "unknown";
    
    int splitIterator;

    if (txt_file.exists() && autoLoadFile) {
	try {
            Logging.main("Attempting to automagically load the downloaded session...");
            strSplitFileName = ReconCommand.filenameTXT.split(Pattern.quote(File.separator));

	     for (splitIterator = 0; splitIterator < strSplitFileName.length; splitIterator++) {
		 if (strSplitFileName[splitIterator].endsWith(".txt"))
		     break;
	     }
	     strSimpleFileName = strSplitFileName[splitIterator];

            lblLoadedFileName.setText(strSimpleFileName);
            LoadSavedFile.main(ReconCommand.filenameTXT);
            Logging.main("File loaded: " + strSimpleFileName);
            lblLoadedFile.setVisible(true);
            lblLoadedFileName.setVisible(true);
            btnGeneratePDF.setVisible(true);
            btnUpdateTXTFile.setVisible(true);
            btnGeneratePDF.setEnabled(true);
            btnUpdateTXTFile.setEnabled(true);
        } catch (Exception ex) {
            Logging.main("ERROR when auto-loading the file!");
            lblLoadedFile.setVisible(false);
            lblLoadedFileName.setVisible(false);
            btnGeneratePDF.setVisible(false);
            btnUpdateTXTFile.setVisible(false);
            btnGeneratePDF.setEnabled(false);
            btnUpdateTXTFile.setEnabled(false);
        }
    }
}

private class MySwingWorker extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
        EnableAllButtons(false);
        lblReconSN.setVisible(false);
        lblFirmwareVersion.setVisible(false);
        lblDataSessions.setVisible(false);
        btnCreateTXT.setVisible(false);
        btnClearMemory.setVisible(false);
        btnClearSession.setVisible(false);
        btnAllDataDump.setVisible(false);
        btnDownloadSession.setVisible(false);
        btnClearTamperFlag.setVisible(false);
        btnOpenSavedFile.setVisible(true);
        btnOpenPDF.setVisible(true);
        btnGeneratePDF.setVisible(false);
        btnEraseReconData.setVisible(false);
        lblLoadedFile.setVisible(false);
        lblLoadedFileName.setVisible(false);
	btnUpdateTXTFile.setVisible(false);
        Logging.main("Connect button pressed.");
        CRM_Parameters = ScanComm.run(1);
        if(CRM_Parameters[0].equals("true")) {
            parseConfigTXT(); //Just in case these options have changed, let's recheck the config.txt and company.txt files.
            parseCompanyTXT();
            parseDeploymentTXT();
            parseReportTXT();
            if (diagnosticMode) {
                btnDownloadSession.setVisible(false);
                btnOpenSavedFile.setVisible(true);
                btnGeneratePDF.setVisible(false);
                btnEraseReconData.setVisible(false);
                btnCreateTXT.setVisible(true);
                btnClearMemory.setVisible(true);
                btnClearSession.setVisible(true);
                btnAllDataDump.setVisible(true);
                btnOpenPDF.setVisible(true);
		btnClearTamperFlag.setVisible(true);
            }
            else {
                btnCreateTXT.setVisible(false);
                btnClearMemory.setVisible(false);
                btnClearSession.setVisible(false);
                btnAllDataDump.setVisible(false);            
                btnDownloadSession.setVisible(true);
                btnOpenSavedFile.setVisible(true);
                btnGeneratePDF.setVisible(false);
                btnEraseReconData.setVisible(true);
                btnOpenPDF.setVisible(true);
                btnClearTamperFlag.setVisible(false);
            }

	    btnSyncTime.setVisible(true);
	    RefreshSessionList();
	    RefreshDefaultFileName();
        }
	else {
	    btnSyncTime.setVisible(false); // disable the sync time button if no Recon is connected
	    EnableFileNaming(false);
	}

        EnableAllButtons(true);
	lblFinalAvg.setText("");
        return null;
    }
}

private class GenerateTXTDump extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
      EnableAllButtons(false);
      Logging.main("CreateTXT/XLS button pressed.");
      CRM_Parameters = ScanComm.run(2);
      EnableAllButtons(true);
      
      return null;
    }
}

private class AllDataDump extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
        EnableAllButtons(false);
        Logging.main("AllDataDump button pressed.");
        CRM_Parameters = ScanComm.run(5);
        EnableAllButtons(true);
        
        return null;
    }
}

private class ClearCurrentSession extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
      EnableAllButtons(false);
      Logging.main("Clear Current Session button pressed.");
      CRM_Parameters = ScanComm.run(3);
      EnableAllButtons(true);
      RefreshSessionList();
      RefreshDefaultFileName();

      return null;
    }
}

private class ClearReconMemory extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
      EnableAllButtons(false);
      Logging.main("Clear Session button pressed.");
      CRM_Parameters = ScanComm.run(4);
      EnableAllButtons(true);
      EnableFileNaming(false);
      
      return null;
    }
}

private class DownloadSession extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
      EnableAllButtons(false);
      Logging.main("Download Session button pressed.");
      CRM_Parameters = ScanComm.run(6);
      EnableAllButtons(true);
      RefreshSessionList();
      RefreshDefaultFileName();

      return null;
    }
}

private class GeneratePDF extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
      // some buttons may be missed - check later

      EnableAllButtons(false);
      Logging.main("Generate PDF button pressed.");
      CreatePDF generate_pdf = new CreatePDF();
      generate_pdf.main();
      // once CreatePDF.main returns, open an "explorer" window
      Desktop desktop = null;
      //File reportsdir = new File("reports");
      if (openPDFWind == 1) {
          try {
	      if (Desktop.isDesktopSupported()) {
	          desktop = Desktop.getDesktop();
	          desktop.open(reportsDir);
	      }
	      else
	          Logging.main("Opening PDF folder window - unsupported desktop.");
          }
          catch (IOException ex) { }
      }
      EnableAllButtons(true);
      
      return null;
    }
}

private class UpdateTXTFile extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
    EnableAllButtons(false);
    Logging.main("Update TXT file button pressed.");
    String oldFileName;
    // the file SHOULD already be loaded before this function is called, but it might
    // be a good idea to add checks
    oldFileName = strLoadedFilePath;
    MainMenu.FileUpdater.UpdateTXTFile(new File(oldFileName));
    EnableAllButtons(true);
    
    return null;
    }
}

private class SyncReconTime extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
    EnableAllButtons(false);
    Logging.main("SyncTime button pressed.");
    CRM_Parameters = ScanComm.run(7);
    EnableAllButtons(true);
    
    return null;
    }
}

private class ClearTamperStatus extends SwingWorker<Void, Void>{
    @Override
    protected Void doInBackground() throws Exception {
        EnableAllButtons(false);
        Logging.main("ClearTamperFlag button pressed.");
        //We need to check firmware revision. The :WX command was not available on pre-1.16 firmware revisions.
        CRM_Parameters = ScanComm.run(1);
            if(CRM_Parameters[0].equals("true")) {
                if (MainMenuUI.convertFirmwareVersionToNumber() >= 1.16)
                CRM_Parameters = ScanComm.run(9);
            }
        EnableAllButtons(true);
        
        return null;
    }
}

public void EnableAllButtons(boolean boolEnableButtons) {
    btnAllDataDump.setEnabled(boolEnableButtons);
    btnClearMemory.setEnabled(boolEnableButtons);
    btnClearSession.setEnabled(boolEnableButtons);
    btnClearTamperFlag.setEnabled(boolEnableButtons);
    btnConfig.setEnabled(boolEnableButtons);
    btnConnect.setEnabled(boolEnableButtons);
    btnCreateTXT.setEnabled(boolEnableButtons);
    btnDownloadSession.setEnabled(boolEnableButtons);
    btnEraseReconData.setEnabled(boolEnableButtons);
    btnGeneratePDF.setEnabled(boolEnableButtons);
    btnOpenPDF.setEnabled(boolEnableButtons);
    btnOpenSavedFile.setEnabled(boolEnableButtons);
    btnSyncTime.setEnabled(boolEnableButtons);
    btnUpdateTXTFile.setEnabled(boolEnableButtons);
    //Check if Config.FlagForm window is open, to disable the Apply button.
    Window[] progWindows = Window.getWindows();
    for (Window window : progWindows) {
	if (window.getClass().toString().contains("Config.FlagForm")) {
            FlagForm.EnableAllButtons(boolEnableButtons);
        }
    }
}

public static void EnableFileNaming(boolean boolWantToEnable) {
    txtNewFileName.setEnabled(boolWantToEnable);
    txtNewFileName.setVisible(boolWantToEnable);
    chkUseStreetAddressForFilename.setEnabled(boolWantToEnable);
    chkUseStreetAddressForFilename.setVisible(boolWantToEnable);
    cboSessionSelect.setVisible(boolWantToEnable);
    return;
}

// Queries the Recon for information for the next session
// and provides the "default filename" to MainMenuUI.
private static void RefreshDefaultFileName() {
    try {
	if (getDataSessions() > 0) {

	    if (ReconCommand.currentSession < 0) // we just deleted a session, reset to 0
		ReconCommand.currentSession = 0;

	    EnableFileNaming(true);
	    ScanComm.run(11);
	    txtNewFileName.setText(ReconCommand.defaultFilename);
	    txtNewFileName.requestFocus();
	    txtNewFileName.selectAll();
	}
	else
	    EnableFileNaming(false);
    }

    catch (Exception anyEx) {
	Logging.main("ERROR: Exception thrown in RefreshDefaultFileName()!");
	Logging.main(anyEx.toString());
    }
}

private static void RefreshSessionList() {
    // clear the descriptive strings
    sessionStrings.clear();
    cboSessionSelect.removeAllItems();

    // We shouldn't be here if there is nothing to download!
    if (getDataSessions() < 1)
	return;

    try {
	// pass control to ReconCommand for pointer table handling
	ScanComm.run(12);

	// Log the string descriptions of the sessions
	for (int k = 0; k < sessionStrings.size(); k++) {
	    Logging.main("Session detected: " + sessionStrings.get(k));
	    cboSessionSelect.addItem(sessionStrings.get(k));
	}

	// Set the currentSession variable just in case the user doesn't do anything
	MainMenu.ReconCommand.currentSession = cboSessionSelect.getSelectedIndex();
    }

    catch (Exception anyEx) {
	Logging.main(anyEx.toString());
    }

    return;
}

}

