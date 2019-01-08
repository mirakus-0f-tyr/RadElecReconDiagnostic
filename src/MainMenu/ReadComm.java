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
            Logging.main(buffer);
            if(buffer == null) {
                buffer = NoResponseHandler(serialPort, byteLength);
            }
            return buffer;
        }
        catch (SerialPortException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static String NoResponseHandler(SerialPort commPort, int byteLength) {
        SerialPort serialPort = commPort;
        try {
            Logging.main("No response from instrument... let's attempt write last command to instrument one more time.");
            Logging.main("Confirm CommPort @ " + commPort + " / Confirm Byte Length = " + byteLength);
            serialPort.purgePort(PURGE_RXCLEAR);
            serialPort.purgePort(PURGE_TXCLEAR);
            serialPort.purgePort(PURGE_RXABORT);
            serialPort.purgePort(PURGE_TXABORT);
            Thread.sleep(100);
            Logging.main("Attempting to issue the last written command: " + MainMenuUI.lastReconCommand);
            serialPort.writeString(MainMenuUI.lastReconCommand); //Write command to Recon
            Thread.sleep(100);
            String buffer = serialPort.readString();
            Logging.main("Retry response = " + buffer);
            
            if(buffer == null) {
                Logging.main("Continued null response!");
                Logging.main("Is port still opened? = " + serialPort.isOpened());
            } else {
                Logging.main("Successful response on retry!");
            }
            
            return buffer;
        }
        catch (SerialPortException ex) {
            StringWriter swEx = new StringWriter();
            ex.printStackTrace(new PrintWriter(swEx));
            String strEx = swEx.toString();
            Logging.main(strEx);
        } catch (InterruptedException ex) {
            Logger.getLogger(ReadComm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
