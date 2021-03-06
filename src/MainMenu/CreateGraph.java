/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import static MainMenu.LoadSavedFile.LoadedReconTXTFile;
import static MainMenu.LoadSavedFile.LoadedReconCF1;
import static MainMenu.LoadSavedFile.LoadedReconCF2;
import static MainMenu.LoadSavedFile.strUnitSystem;
import static MainMenu.MainMenuUI.excludeFirst4Hours;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.DecimalFormat;
import java.time.temporal.ChronoUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCrosshairLabelGenerator;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;


/**
 *
 * @author Rad Elec Inc.
 */

public class CreateGraph extends JFrame {
    
    public static double OverallAvgRnC = 0;
    public static boolean photodiodeFailure_Ch1 = false; //If CreateGraph.consecutiveZeroTally_Ch1 >= MainMenuUI.ConsecutiveZeroLimit (default=5), this becomes true.
    public static boolean photodiodeFailure_Ch2 = false; //If CreateGraph.consecutiveZeroTally_Ch2 >= MainMenuUI.ConsecutiveZeroLimit (default=5), this becomes true.
    
    public static ArrayList<ArrayList<String>> HourlyReconData = new ArrayList<>();
    
    public static SimpleDateFormat dateFormat_Intl = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
    
    static class MyPanel extends JPanel implements ChartMouseListener {
        
        private static int SERIES_COUNT = 1; //default to end-user mode, where only average radon conc. is displayed
    
        private ChartPanel chartPanel;
    
        private Crosshair xCrosshair;
    
        private Crosshair[] yCrosshairs;

        public MyPanel() {
            super(new BorderLayout());
            SERIES_COUNT = 1; //always assign static value here, or else it'll get screwy when we switch between Diagnostic and End-User mode
            if(MainMenu.MainMenuUI.diagnosticMode) {
                SERIES_COUNT = SERIES_COUNT + 2; //if we're in diagnostic mode, then let's make sure to increase our SERIES_COUNT to account for both chambers and average, barring photodiode failure.
            }
            JFreeChart chart = createChart(createDataset());
            SaveGraph(chart); //saves the graph to a JPEG image, for use in the Create PDF class.
            this.chartPanel = new ChartPanel(chart);
            this.chartPanel.addChartMouseListener(this);
            CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
            this.xCrosshair = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
            this.xCrosshair.setLabelVisible(true);
            crosshairOverlay.addDomainCrosshair(xCrosshair);
            this.yCrosshairs = new Crosshair[SERIES_COUNT+1]; //we need to be sure to add all series from *all* datasets into the yCrosshairs array!
            for (int i = 0; i < SERIES_COUNT; i++) {
                this.yCrosshairs[i] = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
                this.yCrosshairs[i].setLabelVisible(true);
                
                //the following should distribute the labels on both sides, so we're not all cluttered...
                if (i % 2 != 0) {
                    this.yCrosshairs[i].setLabelAnchor(
                        RectangleAnchor.TOP_RIGHT);
                }
                
                crosshairOverlay.addRangeCrosshair(yCrosshairs[i]);
                
            }
            
            //Humidity yCrossHair initialization. The index should always equal SERIES_COUNT.
            this.yCrosshairs[SERIES_COUNT] = new Crosshair(Double.NaN, Color.GRAY, new BasicStroke(0f));
            this.yCrosshairs[SERIES_COUNT].setLabelVisible(true);
           
            
            if (SERIES_COUNT % 2 != 0) { //...so our labels don't get cluttered all on the same side of the graph
                this.yCrosshairs[SERIES_COUNT].setLabelAnchor(RectangleAnchor.TOP_RIGHT);
            }
            crosshairOverlay.addRangeCrosshair(yCrosshairs[SERIES_COUNT]);

            Logging.main("RANGE CROSSHAIRS: " + Arrays.toString(crosshairOverlay.getRangeCrosshairs().toArray()));
            chartPanel.addOverlay(crosshairOverlay);
            add(chartPanel);
        }
        
        private JFreeChart createChart(XYDataset[] dataset) {

            XYPlot plot = new XYPlot();
            plot.setDataset(0, dataset[0]); //Radon Concentration Series
            plot.setDataset(1, dataset[1]); //Humidity Series
            plot.setDataset(2, dataset[2]); //Temperature Series
            plot.setDataset(3, dataset[3]); //Pressure Series
            plot.setDataset(4, dataset[4]); //Movement Series
            
            XYSplineRenderer spline_radon = new XYSplineRenderer();
            XYSplineRenderer spline_humidity = new XYSplineRenderer();
            XYSplineRenderer spline_temp = new XYSplineRenderer();
            XYSplineRenderer spline_press = new XYSplineRenderer();
            XYBarRenderer bar_movement = new XYBarRenderer();
            
            //Sets the plot for each dataset
            plot.setRenderer(0, spline_radon);
            plot.setRenderer(1, spline_humidity);
            plot.setRenderer(2, spline_temp);
            plot.setRenderer(3, spline_press);
            plot.setRenderer(4, bar_movement);
            
            //Hides the hourly data points from the graph
            spline_radon.setShapesVisible(false);
            spline_humidity.setShapesVisible(false);
            spline_temp.setShapesVisible(false);
            spline_press.setShapesVisible(false);
            bar_movement.setShadowVisible(false); //no shadows for the bar; they look tacky.
            bar_movement.setDrawBarOutline(false);
            bar_movement.setBarPainter(new StandardXYBarPainter());   
            
            //Draw thicker AvRnC line if we're not in diagnostic mode...
            if(!(MainMenu.MainMenuUI.diagnosticMode)) {
                spline_radon.setStroke(new BasicStroke(3.0f));
            }
            
            if(MainMenu.MainMenuUI.diagnosticMode) {
                plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(0, Color.GREEN); //Ch. 1 Concenration Dataset = green
                plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(1, Color.MAGENTA); //Ch. 2 Concenration Dataset = magenta
                if(photodiodeFailure_Ch1==false && photodiodeFailure_Ch2==false) { //We currently do not display average in diagnostic mode if photodiode failure is detected...
                    plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(2, Color.DARK_GRAY); //Avg. Radon Concenration Dataset = dark grey
                }
            } else {
                plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(0, Color.DARK_GRAY); //Avg. Radon Concenration Dataset = dark grey
            }
            plot.getRendererForDataset(plot.getDataset(1)).setSeriesPaint(0, Color.BLUE); //Humidity Dataset = blue
            plot.getRendererForDataset(plot.getDataset(2)).setSeriesPaint(0, Color.RED); //Temperature Dataset = red
            plot.getRendererForDataset(plot.getDataset(3)).setSeriesPaint(0, Color.ORANGE); //Pressure Dataset = green
            plot.getRendererForDataset(plot.getDataset(4)).setSeriesPaint(0, Color.LIGHT_GRAY); //Movement Dataset = light grey
            
            //Define Domain (x) axis and Range (y) axes.
            if(strUnitSystem.equals("SI")) {
                plot.setRangeAxis(0, new NumberAxis("Bq/m³"));
                plot.setRangeAxis(2, new NumberAxis("Temperature (°C)"));
                plot.setRangeAxis(3, new NumberAxis("Pressure (mbar)"));
            } else {
                plot.setRangeAxis(0, new NumberAxis("pCi/L"));
                plot.setRangeAxis(2, new NumberAxis("Temperature (°F)"));
                plot.setRangeAxis(3, new NumberAxis("Pressure (inHg)"));
            }
            plot.setRangeAxis(1, new NumberAxis("%Humidity"));
            plot.setRangeAxis(4, new NumberAxis("Tilt"));
            plot.setDomainAxis(new NumberAxis("Elapsed Time (Hours)"));
            
            //don't display decimal places on the Tilts/Movement y-axis
            NumberAxis rangeMovement = (NumberAxis)plot.getRangeAxis(4);
            NumberFormat formatterMovement = DecimalFormat.getInstance();
            formatterMovement.setMinimumFractionDigits(0);
            formatterMovement.setMaximumFractionDigits(0);
            rangeMovement.setNumberFormatOverride(formatterMovement);
            rangeMovement.setTickUnit(new NumberTickUnit(1));
            rangeMovement.setRange(0,10); //for right now, this should be sufficient.
            
            //Map each dataset to its unique y-axis / range.
            plot.mapDatasetToRangeAxis(0, 0);
            plot.mapDatasetToRangeAxis(1, 1);
            plot.mapDatasetToRangeAxis(2, 2);
            plot.mapDatasetToRangeAxis(3, 3);
            plot.mapDatasetToRangeAxis(4, 4);
            
            //Set Y-Axis (range) label colors, after the ranges have been mapped.
            plot.getRangeAxis(0).setLabelPaint(Color.DARK_GRAY); //Radon
            plot.getRangeAxis(1).setLabelPaint(Color.BLUE); //Humidity
            plot.getRangeAxis(2).setLabelPaint(Color.RED); //Temperature
            plot.getRangeAxis(3).setLabelPaint(Color.ORANGE); //Pressure
            plot.getRangeAxis(4).setLabelPaint(Color.LIGHT_GRAY); //Movement
            
            //Set Y-Axis (range) axis colors, after the ranges have been mapped.
            plot.getRangeAxis(0).setAxisLinePaint(Color.DARK_GRAY); //Radon
            plot.getRangeAxis(1).setAxisLinePaint(Color.BLUE); //Humidity
            plot.getRangeAxis(2).setAxisLinePaint(Color.RED); //Temperature
            plot.getRangeAxis(3).setAxisLinePaint(Color.ORANGE); //Pressure
            plot.getRangeAxis(4).setAxisLinePaint(Color.LIGHT_GRAY); //Movement
            
            //Set Y-Axis (range) tick label colors, after the ranges have been mapped.
            plot.getRangeAxis(0).setTickLabelPaint(Color.DARK_GRAY); //Radon
            plot.getRangeAxis(1).setTickLabelPaint(Color.BLUE); //Humidity
            plot.getRangeAxis(2).setTickLabelPaint(Color.RED); //Temperature
            plot.getRangeAxis(3).setTickLabelPaint(Color.ORANGE); //Pressure
            plot.getRangeAxis(4).setTickLabelPaint(Color.LIGHT_GRAY); //Movement
            
            
            JFreeChart chart = new JFreeChart("Radon Concentration", getFont(), plot, true);
            chart.setBackgroundPaint(Color.white);
            
            //This will shade the first four hours of the graph
            if (excludeFirst4Hours) {
                final IntervalMarker ExcludedGraphShade = new IntervalMarker(0,4);
                ExcludedGraphShade.setAlpha(0.3f);
                plot.addDomainMarker(ExcludedGraphShade, Layer.FOREGROUND);
            }
            
            return chart;
        }

        private XYDataset[] createDataset() {
            
            //Variable Declarations
            XYSeriesCollection datasetRadon = new XYSeriesCollection();
            XYSeriesCollection datasetHumidity = new XYSeriesCollection();
            XYSeriesCollection datasetTemp = new XYSeriesCollection();
            XYSeriesCollection datasetPress = new XYSeriesCollection();
            XYSeriesCollection datasetMovement = new XYSeriesCollection();
            XYSeriesCollection[] datasetArray = {new XYSeriesCollection(), new XYSeriesCollection(), new XYSeriesCollection(), new XYSeriesCollection(), new XYSeriesCollection()};
            
            DateTimeFormatter DateTimeDisplay = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            LocalDateTime ReconDate = null;
            LocalDateTime HourCounter = null;
            
            //Number Format Stuff
            NumberFormat formatUS_RnC = new DecimalFormat("#0.0");
            NumberFormat formatSI_RnC = new DecimalFormat("#0");
            NumberFormat formatZero = new DecimalFormat("#0"); //redundant, but easier to read
            NumberFormat formatTenth = new DecimalFormat("#0.0");
            
            long Ch1Counts = 0;
            long Ch2Counts = 0;
            double hourlyAvgHumidity = 0;
            double hourlyAvgTemp = 0;
            double hourlyAvgPress = 0;
            double TotalAvgRnC = 0;
            double TotalAvgRnC_Ch1 =0;
            double TotalAvgRnC_Ch2 = 0;
            double TotalAvgRnC_Ch1_Raw = 0;
            double TotalAvgRnC_Ch2_Raw = 0;
            long TotalHourCounter = 0;
            long hourlyMovement = 0;
            int TempYear = 0;
            long diffMinutes = 0;
            long tempCounts_Ch1 = 0;
            long tempCounts_Ch2 = 0;
            long hourCounter = 0;
            int avgCounter = 0; //this will allow us to correctly calculate average temps, humidities, pressures, etc.
            
            //Used to determine if a photodiode failure has occurred!
            int consecutiveZeroTally_Ch1 = 0; //This will tally the consecutive number of hourly zero counts on chamber 1
            int consecutiveZeroTally_Ch2 = 0; //This will tally the consecutive number of hourly zero counts on chamber 2
            long rawCh1Counts = 0; //Raw, unlimited chamber 1 counts. Introduced in v1.0.0.
            long rawCh2Counts = 0; //Raw, unlimited chamber 2 counts. Introduced in v1.0.0.
            long rawTempCounts_Ch1 = 0;
            long rawTempCounts_Ch2 = 0;
            boolean rawCountsExist = false;
            
            //Let's make sure that our troublesome ArrayLists are still valid...
            Logging.main("Attempting to construct graph from ArrayList of size "+LoadedReconTXTFile.size()+"...");
            //Logging.main("Query validity of array: "+ Arrays.toString(LoadedReconTXTFile.toArray()));
            
            //Create RnC series for each chamber...
            XYSeries Ch1_Series = new XYSeries("Ch1_RnC");
            XYSeries Ch2_Series = new XYSeries("Ch2_RnC");
            XYSeries AvgRnC_Series = new XYSeries("Avg. Radon Concentration");
            XYSeries AvgHumidity_Series = new XYSeries("%Humidity");
            XYSeries AvgTemp_Series = new XYSeries("Temperature");
            XYSeries AvgPress_Series = new XYSeries("Pressure");
            XYSeries Movement_Series = new XYSeries("Tilt");
            
            //Create RnC series for raw/unlimited counts
            XYSeries Ch1_Raw = new XYSeries("Ch1_RnC_Raw");
            XYSeries Ch2_Raw = new XYSeries("Ch2_RnC_Raw");
            
            //Confirm whether we're in SI/US Units
            if(strUnitSystem.equals("SI")) {
                Logging.main("SI Units detected in Config for graph.");
            } else if(strUnitSystem.equals("US")) {
                Logging.main("US Units detected in Config for graph.");
            } else {
                Logging.main("No units detected in Config for graph... defaulting to US.");
            }
            
            HourlyReconData.clear(); //Let's clear our summary array, which will be used for the detailed summary in the PDF.
            
            //Needed for building HourlyReconData arraylist...
            ArrayList<String> arrLine = new ArrayList<>();
            ArrayList<String> arrLine_temp = new ArrayList<>();
        
            //Iterate through the ArrayList, to build each series.
            for(int arrayCounter = 0; arrayCounter < LoadedReconTXTFile.size(); arrayCounter++) {
                if(LoadedReconTXTFile.get(arrayCounter).get(2).equals("S")||(LoadedReconTXTFile.get(arrayCounter).get(2).equals("I"))||(LoadedReconTXTFile.get(arrayCounter).get(2).equals("E"))) { //Only build data from S, I, and E flags.
                    if(LoadedReconTXTFile.get(arrayCounter).get(2).equals("S")) { //Make sure we assign the hour counter to the frist record.
                        TempYear = 2000 + Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(3));
                        HourCounter = LocalDateTime.of(TempYear,
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(4)),
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(5)),
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(6)),
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(7)),
                        Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(8)));
                    }
                    
                    Ch1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(10)); //pull Chamber #1 counts from LoadedReconTXTFile ArrayList
                    Ch2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(11)); //pull Chamber #2 counts from LoadedReconTXTFile ArrayList
                    
                    hourlyAvgHumidity = hourlyAvgHumidity + Double.parseDouble(LoadedReconTXTFile.get(arrayCounter).get(15)); //pull average humidity...
                    hourlyAvgTemp = hourlyAvgTemp + Double.parseDouble(LoadedReconTXTFile.get(arrayCounter).get(21)); //pull average temperature (Celsius units)...
                    hourlyAvgPress = hourlyAvgPress + Double.parseDouble(LoadedReconTXTFile.get(arrayCounter).get(18)); //pull average barometric pressure (mbar units)...
                    hourlyMovement = hourlyMovement + Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(9)); //pull movements from ArrayList...
                    
                    avgCounter++; //we need this in order to calculate hourly averages for humidity, temperature, pressure, etc.
                    
                    //Add to our temporary chamber counters, which will be reset hourly.
                    tempCounts_Ch1 = tempCounts_Ch1 + Ch1Counts;
                    tempCounts_Ch2 = tempCounts_Ch2 + Ch2Counts;

                    //Let's handle the raw counts here... and maintain backwards compatibility with TXT files created before v1.0.0.
                    if(LoadedReconTXTFile.get(arrayCounter).size()>=28) {
                        if(rawCountsExist==false) rawCountsExist=true;
                        if((LoadedReconTXTFile.get(arrayCounter).get(26)).contains("false")) { //Check to see if count limiter proc'ed for Ch1...
                            rawCh1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(10)); //If it didn't, then we pull the Ch1 counts from their normal place.
                        } else if((LoadedReconTXTFile.get(arrayCounter).get(26)).contains("true(") && (LoadedReconTXTFile.get(arrayCounter).get(26)).contains(")")) { //If it did, then we need to pull the raw value...
                            rawCh1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(26).replaceAll("[^0-9]", ""));
                        } else {
                            rawCh1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(10));
                        }
                        if((LoadedReconTXTFile.get(arrayCounter).get(27)).contains("false")) { //Check to see if count limiter proc'ed for Ch2...
                            rawCh2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(11)); //If it didn't, then we pull the Ch2 counts from their normal place.
                        } else if((LoadedReconTXTFile.get(arrayCounter).get(27)).contains("true(") && (LoadedReconTXTFile.get(arrayCounter).get(27)).contains(")")) { //If it did, then we need to pull the raw value...
                            rawCh2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(27).replaceAll("[^0-9]", ""));
                        } else {
                            rawCh2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(11));
                        }
                    } else {
                        rawCh1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(10));
                        rawCh2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(11));
                    }
                    rawTempCounts_Ch1 += rawCh1Counts;
                    rawTempCounts_Ch2 += rawCh2Counts;
                    
                    //Keep an eye out for potential photodiode failure...
                    if(Ch1Counts==0) {
                        if(Ch2Counts<5) { //If the other chamber is also measuring low counts, then it's possible we're in an extremely low radon environment...
                            consecutiveZeroTally_Ch1 += .1; //Therefore we should "weight" this encounter less.
                        } else {
                            consecutiveZeroTally_Ch1++; //If the other chamber counts are 5 or greater, then let's count this as a solid clue for potential photodiode failure...
                        }
                        if(consecutiveZeroTally_Ch1>=MainMenuUI.ConsecutiveZeroLimit && photodiodeFailure_Ch1==false) {
                            if(MainMenuUI.photodiodeFailureRecovery==true) {
                                JOptionPane.showMessageDialog(null, "Potential photodiode failure has been detected in chamber 1. The software will attempt to construct the graph and report using chamber 2.", "Potential Failure in Chamber #1", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(null, "Potential photodiode failure has been detected in chamber 1.", "Potential Failure in Chamber #1", JOptionPane.INFORMATION_MESSAGE);
                            }
                            Logging.main("WARNING: Possible photodiode failure detected in chamber 1 when creating graph.");
                            photodiodeFailure_Ch1 = true;
                        }
                    } else {
                        consecutiveZeroTally_Ch1 = 0;
                    }
                    if(Ch2Counts==0) {
                        if(Ch1Counts<5) { //If the other chamber is also measuring low counts, then it's possible we're in an extremely low radon environment...
                            consecutiveZeroTally_Ch2 += .1; //Therefore we should "weight" this encounter less.
                        } else {
                            consecutiveZeroTally_Ch2++; //If the other chamber counts are 5 or greater, then let's count this as a solid clue for potential photodiode failure...
                        }
                        if(consecutiveZeroTally_Ch2>=MainMenuUI.ConsecutiveZeroLimit && photodiodeFailure_Ch2==false) {
                            if(MainMenuUI.photodiodeFailureRecovery==true) {
                                JOptionPane.showMessageDialog(null, "Potential photodiode failure has been detected in chamber 2. The software will attempt to construct the graph and report using chamber 1.", "Potential Failure in Chamber #2", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(null, "Potential photodiode failure has been detected in chamber 2.", "Potential Failure in Chamber #2", JOptionPane.INFORMATION_MESSAGE);
                            }
                            Logging.main("WARNING: Possible photodiode failure detected in chamber 2 when creating graph.");
                            photodiodeFailure_Ch2 = true;
                        }
                    } else {
                        consecutiveZeroTally_Ch2 = 0;
                    }
                    
                    TempYear = 2000 + Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(3));
                    ReconDate = LocalDateTime.of(TempYear,
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(4)),
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(5)),
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(6)),
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(7)),
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(8)));
                    
                    //logic to be implemented at later time...
                    if(HourCounter != null && ReconDate != null) {
                        diffMinutes = ChronoUnit.MINUTES.between(HourCounter,ReconDate);
                        if(diffMinutes>=60) { //Every time we have more than 60 minutes, let's calculate our hourly radon concentration
                            //Reset Hour Counter and display hourly average
                            TempYear = 2000 + Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(3));
                            HourCounter = LocalDateTime.of(TempYear,
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(4)),
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(5)),
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(6)),
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(7)),
                            Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(8)));
                            
                            //Increase our hour counter (temporary until we figure out x-axis dates)
                            hourCounter++;
                            
                            //Add values to series independent of unitType (i.e. humidity and movement)
                            AvgHumidity_Series.add(hourCounter, hourlyAvgHumidity / avgCounter); //This will calculate hourly average humidity
                            
                            //Movement / "Tilt" Logic Handling -- now located in TiltSensitivity.java class!
                            hourlyMovement = TiltSensitivity.main(hourlyMovement);
                            Movement_Series.add(hourCounter, hourlyMovement);
                            
                            //Add values to series that are dependent upon unitType
                            if(strUnitSystem.equals("SI")) {
                                Ch1_Series.add(hourCounter, tempCounts_Ch1/LoadedReconCF1*37);
                                Ch2_Series.add(hourCounter, tempCounts_Ch2/LoadedReconCF2*37);
                                AvgRnC_Series.add(hourCounter, ((tempCounts_Ch1/LoadedReconCF1+tempCounts_Ch2/LoadedReconCF2)/2)*37); //This will calculate hourly average of both chambers (in Bq/m3)
                                AvgTemp_Series.add(hourCounter, (hourlyAvgTemp / avgCounter)); //This will calculate hourly average temperature (in Celsius)
                                AvgPress_Series.add(hourCounter, (hourlyAvgPress / avgCounter)); //This will calculate hourly average temperature (in mbar)
                                Ch1_Raw.add(hourCounter, rawTempCounts_Ch1/LoadedReconCF1*37);
                                Ch2_Raw.add(hourCounter, rawTempCounts_Ch2/LoadedReconCF2*37);

                                //If we are excluding first four hours, let's not add them to TotalAvgRnC
                                if(((TotalHourCounter>3) && excludeFirst4Hours) || (!excludeFirst4Hours)) {
                                    TotalAvgRnC = TotalAvgRnC + (((tempCounts_Ch1/LoadedReconCF1+tempCounts_Ch2/LoadedReconCF2)/2)*37); //Overall AvgRnC (in Bq/m3)
                                    TotalAvgRnC_Ch1 = TotalAvgRnC_Ch1 + ((tempCounts_Ch1/LoadedReconCF1)*37); //Overall AvgRnC for Chamber 1 (in Bq/m3)
                                    TotalAvgRnC_Ch2 = TotalAvgRnC_Ch2 + ((tempCounts_Ch2/LoadedReconCF2)*37); //Overall AvgRnC for Chamber 2 (in Bq/m3)
                                    TotalAvgRnC_Ch1_Raw += ((rawTempCounts_Ch1/LoadedReconCF1)*37);
                                    TotalAvgRnC_Ch2_Raw += ((rawTempCounts_Ch2/LoadedReconCF2)*37);
                                }
                                
                                TotalHourCounter = TotalHourCounter + 1; //Overall Hour Counter
                                
                                //Add to HourlyReconData array, to be used in our PDF (only SI-specific elements to be added)
                                arrLine.add(0, Long.toString(TotalHourCounter)); //Total Hour Counter Index = 0
                                arrLine.add(1, (ReconDate.toString())); //Datetime Index = 1;
                                arrLine.add(2, formatSI_RnC.format(((tempCounts_Ch1/LoadedReconCF1+tempCounts_Ch2/LoadedReconCF2)/2)*37)); //Hourly Avg Radon Index = 2
                                arrLine.add(3, formatZero.format(hourlyAvgTemp/avgCounter)); //Hourly Avg Temperature (in Celsius) Index = 3
                                arrLine.add(4, formatTenth.format(hourlyAvgPress / avgCounter)); //Hourly Avg Pressure (in mbar) Index = 4
                                arrLine.add(5, formatZero.format(hourlyAvgHumidity / avgCounter)); //Humidity Index = 5
                                arrLine.add(6, formatZero.format(Math.round(hourlyMovement))); //Movement/Tilt Index = 6
                                arrLine.add(7, (formatUS_RnC.format((tempCounts_Ch1/LoadedReconCF1*37)))); //Hourly Chamber 1 radon concentration Index = 7
                                arrLine.add(8, (formatUS_RnC.format((tempCounts_Ch2/LoadedReconCF2*37)))); //Hourly Chamber 2 radon concentration Index = 8
                                arrLine.add(9, (rawCountsExist ? formatSI_RnC.format((rawTempCounts_Ch1/LoadedReconCF1)*37) : formatSI_RnC.format((tempCounts_Ch1/LoadedReconCF1)*37))); //Raw Hourly Chamber 1 radon concentration Index = 7
                                arrLine.add(10, (rawCountsExist ? formatSI_RnC.format((rawTempCounts_Ch2/LoadedReconCF2)*37) : formatSI_RnC.format((tempCounts_Ch2/LoadedReconCF2)*37))); //Raw Hourly Chamber 2 radon concentration Index = 8
                                
                            } else {
                                Ch1_Series.add(hourCounter, tempCounts_Ch1/LoadedReconCF1);
                                Ch2_Series.add(hourCounter, tempCounts_Ch2/LoadedReconCF2);
                                AvgRnC_Series.add(hourCounter, ((tempCounts_Ch1/LoadedReconCF1+tempCounts_Ch2/LoadedReconCF2)/2)); //This will calculate hourly average of both chambers
                                AvgTemp_Series.add(hourCounter, (hourlyAvgTemp/avgCounter)*9/5+32); //This will calculate hourly average temperature (in Fahrenheit)
                                AvgPress_Series.add(hourCounter, (hourlyAvgPress / avgCounter)*0.02952998751); //This will calculate hourly average temperature (in inHg)
                                Ch1_Raw.add(hourCounter, rawTempCounts_Ch1/LoadedReconCF1);
                                Ch2_Raw.add(hourCounter, rawTempCounts_Ch2/LoadedReconCF2);
                                
                                //If we are excluding first four hours, let's not add them to TotalAvgRnC and TotalHourCounter
                                if(((TotalHourCounter>3) && excludeFirst4Hours) || (!excludeFirst4Hours)) {
                                    TotalAvgRnC = TotalAvgRnC + (((tempCounts_Ch1/LoadedReconCF1)+(tempCounts_Ch2/LoadedReconCF2))/2); //Overall AvgRnC (in pCi/L)
                                    TotalAvgRnC_Ch1 = TotalAvgRnC_Ch1 + ((tempCounts_Ch1/LoadedReconCF1)); //Overall AvgRnC for Chamber 1 (in pCi/L)
                                    TotalAvgRnC_Ch2 = TotalAvgRnC_Ch2 + ((tempCounts_Ch2/LoadedReconCF2)); //Overall AvgRnC for Chamber 2 (in pCi/L)
                                    TotalAvgRnC_Ch1_Raw += ((rawTempCounts_Ch1/LoadedReconCF1));
                                    TotalAvgRnC_Ch2_Raw += ((rawTempCounts_Ch2/LoadedReconCF2));
                                }
                                
                                TotalHourCounter += 1; //Overall Hour Counter
                                
                                //Add to HourlyReconData array, to be used in our PDF (only US-specific elements to be added)
                                arrLine.add(0, Long.toString(TotalHourCounter)); //Total Hour Counter Index = 0
                                arrLine.add(1, (ReconDate.toString())); //Datetime Index = 1;
                                arrLine.add(2, formatUS_RnC.format((tempCounts_Ch1/LoadedReconCF1+tempCounts_Ch2/LoadedReconCF2)/2)); //Hourly Avg Radon Index = 2
                                arrLine.add(3, formatZero.format((hourlyAvgTemp/avgCounter)*9/5+32)); //Hourly Avg Temperature (in Fahrenheit) Index = 3
                                arrLine.add(4, formatTenth.format((hourlyAvgPress / avgCounter)*0.02952998751)); //Hourly Avg Pressure (in inHg) Index = 4
                                arrLine.add(5, formatZero.format(hourlyAvgHumidity / avgCounter)); //Humidity Index = 5
                                arrLine.add(6, formatZero.format(Math.round(hourlyMovement))); //Movement/Tilt Index = 6
                                arrLine.add(7, (formatUS_RnC.format((tempCounts_Ch1/LoadedReconCF1)))); //Hourly Chamber 1 radon concentration Index = 7
                                arrLine.add(8, (formatUS_RnC.format((tempCounts_Ch2/LoadedReconCF2)))); //Hourly Chamber 2 radon concentration Index = 8
                                arrLine.add(9, (rawCountsExist ? formatUS_RnC.format((rawTempCounts_Ch1/LoadedReconCF1)) : formatUS_RnC.format((tempCounts_Ch1/LoadedReconCF1)))); //Raw hourly Chamber 1 radon concentration Index = 9
                                arrLine.add(10, (rawCountsExist ? formatUS_RnC.format((rawTempCounts_Ch2/LoadedReconCF2)) : formatUS_RnC.format((tempCounts_Ch2/LoadedReconCF2)))); //Raw Hourly Chamber 2 radon concentration Index = 10
                                System.out.println(arrLine);
                            }
                            
                            //Finalize HourlyReconData line, and add it to the ArrayList
                            arrLine_temp = (ArrayList<String>) arrLine.clone(); //This seems really stupid, but if you don't clone the ArrayList to a temporary holder, it'll be lost after arrLine.clear() below.
                            HourlyReconData.add(arrLine_temp); //This will add the temporary arrLine into the primary HourlyReconData ArrayList.
                            arrLine.clear();
                            
                            //Reset the temporary chamber counts
                            tempCounts_Ch1 = 0;
                            tempCounts_Ch2 = 0;
                            rawTempCounts_Ch1 = 0;
                            rawTempCounts_Ch2 = 0;
                            hourlyAvgHumidity = 0;
                            hourlyAvgTemp = 0;
                            hourlyAvgPress = 0;
                            hourlyMovement = 0;
                            avgCounter = 0; //also reset avgCounter, as we just calculated average humidity, temperature, pressure, etc.
                            
                        }                   
                    }                   
                }
            }
            
            //Assign Overall Average Radon Concentration
            if(MainMenuUI.photodiodeFailureRecovery==true && photodiodeFailure_Ch1==true && photodiodeFailure_Ch2==false) {
                OverallAvgRnC = (rawCountsExist ? TotalAvgRnC_Ch2_Raw : TotalAvgRnC_Ch2) / (TotalHourCounter-(excludeFirst4Hours ? 4 : 0));
            } else if(MainMenuUI.photodiodeFailureRecovery==true && photodiodeFailure_Ch2==true && photodiodeFailure_Ch1==false) {
                OverallAvgRnC = (rawCountsExist ? TotalAvgRnC_Ch1_Raw : TotalAvgRnC_Ch1) / (TotalHourCounter-(excludeFirst4Hours ? 4 : 0));
            } else {
                OverallAvgRnC = TotalAvgRnC / (TotalHourCounter-(excludeFirst4Hours ? 4 : 0)); //You know what's funny? If the dividend is zero, we'll show infinity pCi/L on the PDF... :)
            }
            
            //Display the Avg Radon Concentration Label
            MainMenuUI.DisplayAvgRadonLabel(OverallAvgRnC);
            
            //We need to add each completed series to the dataset, or we won't have any data to display.
            //Only display AvRnC series for End-User Mode, whereas display both chambers for diagnostic mode.
            if(MainMenu.MainMenuUI.diagnosticMode) {
                if(MainMenuUI.photodiodeFailureRecovery==true && photodiodeFailure_Ch1==true && photodiodeFailure_Ch2==false) {
                    datasetRadon.addSeries(Ch1_Series);
                    datasetRadon.addSeries((rawCountsExist ? Ch2_Raw : Ch2_Series));
                    SERIES_COUNT -= 1; //We are not including averages for diagnostic mode on photodiode failure, so we must remove the expected average series from SERIES_COUNT.
                } else if (MainMenuUI.photodiodeFailureRecovery==true && photodiodeFailure_Ch2==true && photodiodeFailure_Ch1==false) {
                    datasetRadon.addSeries((rawCountsExist ? Ch1_Raw : Ch1_Series));
                    datasetRadon.addSeries(Ch2_Series);
                    SERIES_COUNT -= 1; //We are not including averages for diagnostic mode on photodiode failure, so we must remove the expected average series from SERIES_COUNT.
                } else if (MainMenuUI.photodiodeFailureRecovery==true && photodiodeFailure_Ch1==true && photodiodeFailure_Ch2==true) {
                    datasetRadon.addSeries((rawCountsExist ? Ch1_Raw : Ch1_Series));
                    datasetRadon.addSeries((rawCountsExist ? Ch2_Raw : Ch2_Series));
                    datasetRadon.addSeries(AvgRnC_Series); //On the presumably super-rare occasion when both photodiodes fail, display the original average?
                } else {
                    datasetRadon.addSeries(Ch1_Series);
                    datasetRadon.addSeries(Ch2_Series);
                    datasetRadon.addSeries(AvgRnC_Series);
                }
            } else {
                if(MainMenuUI.photodiodeFailureRecovery==true && photodiodeFailure_Ch1==true && photodiodeFailure_Ch2==false) {
                    datasetRadon.addSeries((rawCountsExist ? Ch2_Raw : Ch2_Series));
                } else if (MainMenuUI.photodiodeFailureRecovery==true && photodiodeFailure_Ch2==true && photodiodeFailure_Ch1==false) {
                    datasetRadon.addSeries((rawCountsExist ? Ch1_Raw : Ch1_Series));
                } else {
                    datasetRadon.addSeries(AvgRnC_Series);
                }
            }
            datasetHumidity.addSeries(AvgHumidity_Series); //always add humidity
            datasetTemp.addSeries(AvgTemp_Series); //always add temperature
            datasetPress.addSeries(AvgPress_Series); //always add pressure
            datasetMovement.addSeries(Movement_Series); //always add movement
        
        datasetArray[0] = datasetRadon;
        datasetArray[1] = datasetHumidity;
        datasetArray[2] = datasetTemp;
        datasetArray[3] = datasetPress;
        datasetArray[4] = datasetMovement;
        
        return datasetArray;
        }
    
        @Override
        public void chartMouseClicked(ChartMouseEvent event) {
            // ignore (for now...)
        }

        //This updates the crosshairs
        @Override
        public void chartMouseMoved(ChartMouseEvent event) {
            Rectangle2D dataArea = this.chartPanel.getScreenDataArea();
            JFreeChart chart = event.getChart();
            XYPlot plot = (XYPlot) chart.getPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            //Logging.main("Crosshair plot domain_axis count = " + plot.getDomainAxisCount());
            //Logging.main("Crosshair plot range axis count = " + plot.getRangeAxisCount());
            //Logging.main("Total dataset count = " + plot.getDatasetCount());
            //Logging.main("Total series count = " + plot.getSeriesCount());
            //Logging.main("Renderer Count = " + plot.getRendererCount());
            
            //This nonsense makes sure that we're only displaying to 1 decimal point for the radon concentration
            NumberFormat yCrossHairNumberFormat = NumberFormat.getInstance();
            yCrossHairNumberFormat.setMinimumIntegerDigits(1);
            StandardCrosshairLabelGenerator yCrosshairLabel;
            if(strUnitSystem.equals("SI")) {
                yCrossHairNumberFormat.setMinimumFractionDigits(0);
                yCrossHairNumberFormat.setMaximumFractionDigits(0);
                yCrosshairLabel = new StandardCrosshairLabelGenerator("{0} Bq/m³", yCrossHairNumberFormat);
            } else {
                yCrossHairNumberFormat.setMinimumFractionDigits(1);
                yCrossHairNumberFormat.setMaximumFractionDigits(1);
                yCrosshairLabel = new StandardCrosshairLabelGenerator("{0} pCi/L", yCrossHairNumberFormat);
            }
            //Humidity Crosshair Number Format
            NumberFormat humidityCrossHairNumberFormat = NumberFormat.getInstance();
            humidityCrossHairNumberFormat.setMinimumFractionDigits(0);
            humidityCrossHairNumberFormat.setMaximumFractionDigits(0);
            humidityCrossHairNumberFormat.setMinimumIntegerDigits(1);
            StandardCrosshairLabelGenerator humidityCrosshairLabel = new StandardCrosshairLabelGenerator("{0}%RH", humidityCrossHairNumberFormat);
            
            double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea, RectangleEdge.BOTTOM);
            this.xCrosshair.setValue(x);
            
            //This is only for the radon concentration (which could have more than one series)
            for (int i = 0; i < SERIES_COUNT; i++) {
                double y = DatasetUtilities.findYValue(plot.getDataset(0), i, x);
                this.yCrosshairs[i].setLabelGenerator(yCrosshairLabel);
                this.yCrosshairs[i].setValue(y);
            }
            
            //This is for humidity series
            //double y2 = DatasetUtilities.findYValue(plot.getDataset(1), 0, x); //this *SHOULD* work, but it looks like a bug in jFreeChart (see Github)
            //this.yCrosshairs[SERIES_COUNT].setLabelGenerator(humidityCrosshairLabel);
            //this.yCrosshairs[SERIES_COUNT].setValue(y2); //Humidity index should always equate to SERIES_COUNT
            //Logging.main("Avg. Humidity = " + this.yCrosshairs[SERIES_COUNT].getValue());

        }
    }
    
    
    public CreateGraph(String title) {
        super(title);
        setContentPane(createPanel());
    }

    public static JPanel createPanel() {
        return new MyPanel();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                CreateGraph app = new CreateGraph(
                        MainMenu.MainMenuUI.lblLoadedFileName.getText());
                app.pack();
                app.setVisible(true);
            }
        });
    }
    
    public static void SaveGraph(JFreeChart chart) {
        try {
	    ChartUtilities.saveChartAsPNG(InitDirs.boolMacOS==true ? new File(InitDirs.baseDir + File.separator + new File("graph.png")) : new File("graph.png"), chart, 500, 300);
        }
        catch (Exception ex) {
            Logging.main("ERROR: Cannot externalize graph to PNG image.");
        }
    }

}


