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
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;


/**
 *
 * @author Rad Elec Inc.
 */

public class CreateGraph extends JFrame {
    
    public static double OverallAvgRnC = 0;
    
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
                SERIES_COUNT = SERIES_COUNT + 2; //if we're in diagnostic mode, then let's make sure to increase our SERIES_COUNT to account for both chambers.
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

            System.out.println("RANGE CROSSHAIRS: " + Arrays.toString(crosshairOverlay.getRangeCrosshairs().toArray()));
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
                plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(2, Color.DARK_GRAY); //Avg. Radon Concenration Dataset = dark grey
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
            long TotalHourCounter = 0;
            long hourlyMovement = 0;
            int TempYear = 0;
            long diffMinutes = 0;
            long tempCounts_Ch1 = 0;
            long tempCounts_Ch2 = 0;
            long hourCounter = 0;
            int avgCounter = 0; //this will allow us to correctly calculate average temps, humidities, pressures, etc.
            
            //Let's make sure that our troublesome ArrayLists are still valid...
            System.out.println("Attempting to construct graph from ArrayList of size "+LoadedReconTXTFile.size()+"...");
            //System.out.println("Query validity of array: "+ Arrays.toString(LoadedReconTXTFile.toArray()));
            
            //Create RnC series for each chamber...
            XYSeries Ch1_Series = new XYSeries("Ch1_RnC");
            XYSeries Ch2_Series = new XYSeries("Ch2_RnC");
            XYSeries AvgRnC_Series = new XYSeries("Avg. Radon Concentration");
            XYSeries AvgHumidity_Series = new XYSeries("%Humidity");
            XYSeries AvgTemp_Series = new XYSeries("Temperature");
            XYSeries AvgPress_Series = new XYSeries("Pressure");
            XYSeries Movement_Series = new XYSeries("Tilt");
            
            //Confirm whether we're in SI/US Units
            if(strUnitSystem.equals("SI")) {
                System.out.println("SI Units detected in Config for graph.");
            } else if(strUnitSystem.equals("US")) {
                System.out.println("US Units detected in Config for graph.");
            } else {
                System.out.println("No units detected in Config for graph... defaulting to US.");
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
                            
                            //Movement Logic Handling
                            //The Recon is too sensitive / high resolution when it comes to movements.
                            //We need to tone them down.
                            //If the hourlyMovements are less than 100, then let's flat-out ignore them.
                            //Also, let's divide our final value by 100, and then truncate it.
                            if(hourlyMovement>=100) {
                                Movement_Series.add(hourCounter, Math.round(hourlyMovement/100));
                            }
                            
                            //Add values to series that are dependent upon unitType
                            if(strUnitSystem.equals("SI")) {
                                Ch1_Series.add(hourCounter, tempCounts_Ch1/(LoadedReconCF1)*37);
                                Ch2_Series.add(hourCounter, tempCounts_Ch2/(LoadedReconCF2)*37);
                                AvgRnC_Series.add(hourCounter, ((tempCounts_Ch1/LoadedReconCF1+tempCounts_Ch2/LoadedReconCF2)/2)*37); //This will calculate hourly average of both chambers (in Bq/m3)
                                AvgTemp_Series.add(hourCounter, (hourlyAvgTemp / avgCounter)); //This will calculate hourly average temperature (in Celsius)
                                AvgPress_Series.add(hourCounter, (hourlyAvgPress / avgCounter)); //This will calculate hourly average temperature (in mbar)
                                
                                TotalAvgRnC = TotalAvgRnC + (((tempCounts_Ch1/LoadedReconCF1)+(tempCounts_Ch2/LoadedReconCF2)/2)*37); //Overall AvgRnC (in Bq/m3)
                                TotalHourCounter = TotalHourCounter + 1; //Overall Hour Counter
                                
                                //Add to HourlyReconData array, to be used in our PDF (only SI-specific elements to be added)
                                arrLine.add(0, Long.toString(TotalHourCounter)); //Total Hour Counter Index = 0
                                arrLine.add(1, (ReconDate.toString())); //Datetime Index = 1;
                                arrLine.add(2, formatSI_RnC.format(((tempCounts_Ch1/LoadedReconCF1+tempCounts_Ch2/LoadedReconCF2)/2)*37)); //Hourly Avg Radon Index = 2
                                arrLine.add(3, formatZero.format(hourlyAvgTemp/avgCounter)); //Hourly Avg Temperature (in Celsius) Index = 3
                                arrLine.add(4, formatTenth.format(hourlyAvgPress / avgCounter)); //Hourly Avg Pressure (in mbar) Index = 4
                                arrLine.add(5, formatZero.format(hourlyAvgHumidity / avgCounter)); //Humidity Index = 5
                                arrLine.add(6, formatZero.format(Math.round(hourlyMovement/100))); //Movement/Tilt Index = 6
                                arrLine.add(7, Double.toString((tempCounts_Ch1/LoadedReconCF1)*37)); //Hourly Chamber 1 radon concentration Index = 7
                                arrLine.add(8, Double.toString((tempCounts_Ch2/LoadedReconCF2)*37)); //Hourly Chamber 2 radon concentration Index = 8
                                
                            } else {
                                Ch1_Series.add(hourCounter, tempCounts_Ch1/(LoadedReconCF1));
                                Ch2_Series.add(hourCounter, tempCounts_Ch2/(LoadedReconCF2));
                                AvgRnC_Series.add(hourCounter, ((tempCounts_Ch1/LoadedReconCF1+tempCounts_Ch2/LoadedReconCF2)/2)); //This will calculate hourly average of both chambers
                                AvgTemp_Series.add(hourCounter, (hourlyAvgTemp / avgCounter) * 9 / 5 + 32); //This will calculate hourly average temperature (in Fahrenheit)
                                AvgPress_Series.add(hourCounter, (hourlyAvgPress / avgCounter)*0.02952998751); //This will calculate hourly average temperature (in inHg)
                                
                                TotalAvgRnC = TotalAvgRnC + (((tempCounts_Ch1/LoadedReconCF1)+(tempCounts_Ch2/LoadedReconCF2))/2); //Overall AvgRnC (in pCi/L)
                                TotalHourCounter = TotalHourCounter + 1; //Overall Hour Counter
                                
                                //Add to HourlyReconData array, to be used in our PDF (only US-specific elements to be added)
                                arrLine.add(0, Long.toString(TotalHourCounter)); //Total Hour Counter Index = 0
                                arrLine.add(1, (ReconDate.toString())); //Datetime Index = 1;
                                arrLine.add(2, formatUS_RnC.format((tempCounts_Ch1/LoadedReconCF1+tempCounts_Ch2/LoadedReconCF2)/2)); //Hourly Avg Radon Index = 2
                                arrLine.add(3, formatZero.format((hourlyAvgTemp / avgCounter) * 9 / 5 + 32)); //Hourly Avg Temperature (in Fahrenheit) Index = 3
                                arrLine.add(4, formatTenth.format((hourlyAvgPress / avgCounter)*0.02952998751)); //Hourly Avg Pressure (in inHg) Index = 4
                                arrLine.add(5, formatZero.format(hourlyAvgHumidity / avgCounter)); //Humidity Index = 5
                                arrLine.add(6, formatZero.format(Math.round(hourlyMovement/100))); //Movement/Tilt Index = 6
                                arrLine.add(7, Double.toString(tempCounts_Ch1/LoadedReconCF1)); //Hourly Chamber 1 radon concentration Index = 7
                                arrLine.add(8, Double.toString(tempCounts_Ch2/LoadedReconCF2)); //Hourly Chamber 2 radon concentration Index = 8
                            }
                            
                            //Finalize HourlyReconData line, and add it to the ArrayList
                            arrLine_temp = (ArrayList<String>) arrLine.clone(); //This seems really stupid, but if you don't clone the ArrayList to a temporary holder, it'll be lost after arrLine.clear() below.
                            HourlyReconData.add(arrLine_temp); //This will add the temporary arrLine into the primary HourlyReconData ArrayList.
                            arrLine.clear();
                            
                            //Reset the temporary chamber counts
                            tempCounts_Ch1 = 0;
                            tempCounts_Ch2 = 0;
                            hourlyAvgHumidity = 0;
                            hourlyAvgTemp = 0;
                            hourlyAvgPress = 0;
                            hourlyMovement = 0;
                            avgCounter = 0; //also reset avgCounter, as we just calculated average humidity, temperature, pressure, etc.
                            
                        }                   
                    }                   
                }
            }
            
            OverallAvgRnC = TotalAvgRnC / TotalHourCounter; //Assign Overall Average Radon Concentration
            
            //We need to add each completed series to the dataset, or we won't have any data to display.
            //Only display AvRnC series for End-User Mode, whereas display both chambers for diagnostic mode.
            if(MainMenu.MainMenuUI.diagnosticMode) {
                datasetRadon.addSeries(Ch1_Series);
                datasetRadon.addSeries(Ch2_Series);
                datasetRadon.addSeries(AvgRnC_Series);
            } else {
                datasetRadon.addSeries(AvgRnC_Series);
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
            //System.out.println("Crosshair plot domain_axis count = " + plot.getDomainAxisCount());
            //System.out.println("Crosshair plot range axis count = " + plot.getRangeAxisCount());
            //System.out.println("Total dataset count = " + plot.getDatasetCount());
            //System.out.println("Total series count = " + plot.getSeriesCount());
            //System.out.println("Renderer Count = " + plot.getRendererCount());
            
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
            //System.out.println("Avg. Humidity = " + this.yCrosshairs[SERIES_COUNT].getValue());

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
	    ChartUtilities.saveChartAsPNG(new File("graph.png"), chart, 500, 300);
        }
        catch (Exception ex) {
            System.out.println("ERROR: Cannot externalize graph to JPG image.");
        }
    }

}


