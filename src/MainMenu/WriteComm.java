/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import jssc.SerialPortException;

public class WriteComm {

    public static void main(SerialPort commPort, String ReconCommand) {
        SerialPort serialPort = commPort;
        try {
            commPort.purgePort(15);
            Thread.sleep(10);
            serialPort.writeString(ReconCommand); //Write command to Recon
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(WriteComm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
