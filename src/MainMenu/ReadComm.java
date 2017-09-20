/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MainMenu;

import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPort;
import static jssc.SerialPort.PURGE_RXABORT;
import static jssc.SerialPort.PURGE_RXCLEAR;
import static jssc.SerialPort.PURGE_TXABORT;
import static jssc.SerialPort.PURGE_TXCLEAR;
import jssc.SerialPortException;

/**
 *
 * @author Rad Elec Inc.
 */
public class ReadComm {

    public static String main(SerialPort commPort, int byteLength) {
        SerialPort serialPort = commPort;
        try {
            Thread.sleep(10);
            String buffer = serialPort.readString();

            if(buffer == null) {
                buffer = NoResponseHandler(serialPort, byteLength);
            }
            return buffer;
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static String NoResponseHandler(SerialPort commPort, int byteLength) {
        SerialPort serialPort = commPort;
        try {
            System.out.println("No response from instrument... let's attempt write last command to instrument one more time.");
            System.out.println("Confirm CommPort @ " + commPort + " / Confirm Byte Length = " + byteLength);
            serialPort.purgePort(PURGE_RXCLEAR);
            serialPort.purgePort(PURGE_TXCLEAR);
            serialPort.purgePort(PURGE_RXABORT);
            serialPort.purgePort(PURGE_TXABORT);
            Thread.sleep(100);
            System.out.println("Attempting to issue the last written command: " + MainMenuUI.lastReconCommand);
            serialPort.writeString(MainMenuUI.lastReconCommand); //Write command to Recon
            Thread.sleep(100);
            String buffer = serialPort.readString();
            System.out.println("Retry response = " + buffer);
            
            if(buffer == null) {
                System.out.println("Continued null response!");
                System.out.println("Is port still opened? = " + serialPort.isOpened());
            } else {
                System.out.println("Successful response on retry!");
            }
            
            return buffer;
        }
        catch (SerialPortException ex) {
            System.out.println(ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
