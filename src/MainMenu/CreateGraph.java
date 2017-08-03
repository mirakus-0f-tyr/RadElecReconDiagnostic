/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import static MainMenu.LoadSavedFile.LoadedReconTXTFile;
import static MainMenu.LoadSavedFile.LoadedReconCF1;
import static MainMenu.LoadSavedFile.LoadedReconCF2;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.time.temporal.ChronoUnit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
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
  
    static class MyDemoPanel extends JPanel implements ChartMouseListener {

        private static final int SERIES_COUNT = 2;
    
        private ChartPanel chartPanel;
    
        private Crosshair xCrosshair;
    
        private Crosshair[] yCrosshairs;

        public MyDemoPanel() {
            super(new BorderLayout());
            JFreeChart chart = createChart(createDataset());
            SaveGraph(chart);
            this.chartPanel = new ChartPanel(chart);
            this.chartPanel.addChartMouseListener(this);
            CrosshairOverlay crosshairOverlay = new CrosshairOverlay();
            this.xCrosshair = new Crosshair(Double.NaN, Color.GRAY, 
                    new BasicStroke(0f));
            this.xCrosshair.setLabelVisible(true);
            crosshairOverlay.addDomainCrosshair(xCrosshair);
            this.yCrosshairs = new Crosshair[SERIES_COUNT];
            for (int i = 0; i < SERIES_COUNT; i++) {
                this.yCrosshairs[i] = new Crosshair(Double.NaN, Color.GRAY, 
                        new BasicStroke(0f));
                this.yCrosshairs[i].setLabelVisible(true);
                if (i % 2 != 0) {
                    this.yCrosshairs[i].setLabelAnchor(
                            RectangleAnchor.TOP_RIGHT);
                }
                crosshairOverlay.addRangeCrosshair(yCrosshairs[i]);
            }
            chartPanel.addOverlay(crosshairOverlay);
            add(chartPanel);
        }
        
        private JFreeChart createChart(XYDataset dataset) {
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Radon Concentration", "Elapsed Time (Hours)", "pCi/L", dataset);
            return chart;
        }

        private XYDataset createDataset() {
            
            //Variable Declarations
            XYSeriesCollection dataset = new XYSeriesCollection();
            DateTimeFormatter DateTimeDisplay = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            LocalDateTime ReconDate = null;
            LocalDateTime HourCounter = null;
            long Ch1Counts = 0;
            long Ch2Counts = 0;
            int TempYear = 0;
            long diffMinutes = 0;
            long tempCounts_Ch1 = 0;
            long tempCounts_Ch2 = 0;
            long hourCounter = 0;
            
            //Let's make sure that our troublesome ArrayLists are still valid...
            System.out.println("Attempting to construct graph from ArrayList of size "+LoadedReconTXTFile.size()+"...");
            //System.out.println("Query validity of array: "+ Arrays.toString(LoadedReconTXTFile.toArray()));
            
            //Create RnC series for each chamber...
            XYSeries Ch1_Series = new XYSeries("Ch1_RnC");
            XYSeries Ch2_Series = new XYSeries("Ch2_RnC");
            
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
                    
                    Ch1Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(10));
                    Ch2Counts = Long.parseLong(LoadedReconTXTFile.get(arrayCounter).get(11));
                    
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
                            
                            //Add values to series
                            Ch1_Series.add(hourCounter, tempCounts_Ch1/(LoadedReconCF1));
                            Ch2_Series.add(hourCounter, tempCounts_Ch2/(LoadedReconCF2));
                            
                            //Reset the temporary chamber counts
                            tempCounts_Ch1 = 0;
                            tempCounts_Ch2 = 0;
                            
                        }                   
                    }                   
                }
            }
            
            //We need to add each completed series to the dataset, or we won't have any data to display.
            dataset.addSeries(Ch1_Series);
            dataset.addSeries(Ch2_Series);
            
        return dataset;
        }
    
        @Override
        public void chartMouseClicked(ChartMouseEvent event) {
            // ignore
        }

        //This updates the crosshairs
        @Override
        public void chartMouseMoved(ChartMouseEvent event) {
            Rectangle2D dataArea = this.chartPanel.getScreenDataArea();
            JFreeChart chart = event.getChart();
            XYPlot plot = (XYPlot) chart.getPlot();
            ValueAxis xAxis = plot.getDomainAxis();
            double x = xAxis.java2DToValue(event.getTrigger().getX(), dataArea, 
                    RectangleEdge.BOTTOM);
            this.xCrosshair.setValue(x);
            for (int i = 0; i < SERIES_COUNT; i++) {
                double y = DatasetUtilities.findYValue(plot.getDataset(), i, x);
                this.yCrosshairs[i].setValue(y);
            }
        }
    }
    
    
    public CreateGraph(String title) {
        super(title);
        setContentPane(createDemoPanel());
    }

    public static JPanel createDemoPanel() {
        return new MyDemoPanel();
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
            ChartUtilities.saveChartAsJPEG(new File("graph.jpg"), chart, 500, 300);
        }
        catch (Exception ex) {
            System.out.println("ERROR: Cannot externalize graph to JPG image.");
        }
    }

}


