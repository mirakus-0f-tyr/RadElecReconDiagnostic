/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;

public class WriteComm {

    public static void main(SerialPort commPort, String ReconCommand) {
        SerialPort serialPort = commPort;
        try {
            Thread.sleep(10);
            serialPort.writeString(ReconCommand); //Write command to Recon
            MainMenuUI.lastReconCommand = ReconCommand; //Store the last written command (for non-responsive instrument)
        }
        catch (SerialPortException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        } catch (InterruptedException ex) {
            Logger.getLogger(WriteComm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
