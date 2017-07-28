/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import static MainMenu.LoadSavedFile.LoadedReconTXTFile;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;


/**
 *
 * @author Rad Elec Inc.
 */
public class CreateGraph extends ApplicationFrame {
    
    public CreateGraph(String applicationTitle, String chartTitle) {
        super(applicationTitle);
        JFreeChart lineChart = ChartFactory.createLineChart(
            chartTitle,
            "Time","Radon Concentration",
            createRnCGraph(),
            PlotOrientation.VERTICAL,
            true,true,false);
         
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(560,367));
        setContentPane(chartPanel);
    }

    private DefaultCategoryDataset createRnCGraph() {
        //Variable declarations
        DateTimeFormatter DateTimeDisplay = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
        LocalDateTime ReconDate = null;
        double Ch1Counts = 0;
        double Ch2Counts = 0;
        int TempYear = 0;
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
        System.out.println("Attempting to construct graph from ArrayList of size "+LoadedReconTXTFile.size()+"...");
        System.out.println("Query validity of array: "+ Arrays.toString(LoadedReconTXTFile.toArray()));
        for(int arrayCounter = 0; arrayCounter < LoadedReconTXTFile.size(); arrayCounter++) {
            if(LoadedReconTXTFile.get(arrayCounter).get(2).equals("S")||(LoadedReconTXTFile.get(arrayCounter).get(2).equals("I"))||(LoadedReconTXTFile.get(arrayCounter).get(2).equals("E"))) { //Only build data from S, I, and E flags.
                Ch1Counts = Double.parseDouble(LoadedReconTXTFile.get(arrayCounter).get(10));
                Ch2Counts = Double.parseDouble(LoadedReconTXTFile.get(arrayCounter).get(11));
                TempYear = 2000 + Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(3));
                ReconDate = LocalDateTime.of(TempYear,
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(4)),
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(5)),
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(6)),
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(7)),
                    Integer.parseInt(LoadedReconTXTFile.get(arrayCounter).get(8)));
                dataset.addValue(Ch1Counts, "Ch1_RnC" , ReconDate.format(DateTimeDisplay));
                dataset.addValue(Ch2Counts, "Ch2_RnC" , ReconDate.format(DateTimeDisplay));
            }
        }
        return dataset;
    }
   
    public static void main(String[] args) {
        CreateGraph chart = new CreateGraph(
            args[0] ,
            args[1]);
            chart.pack( );
            RefineryUtilities.centerFrameOnScreen(chart);
            chart.setVisible(true);
   }
}

