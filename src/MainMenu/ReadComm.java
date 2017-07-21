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

/**
 *
 * @author Rad Elec Inc.
 */
public class ReadComm {

    public static String main(SerialPort commPort, int byteLength) {
        SerialPort serialPort = commPort;
        try {
            //serialPort.openPort();//Open serial port
            //serialPort.setParams(9600, 8, 1, 0);//Set params.
            Thread.sleep(10);
            //String buffer = serialPort.readHexString(byteLength," ");//Read bytes from serial port
            String buffer = serialPort.readString();
            System.out.println(buffer);
            //serialPort.closePort();//Close serial port
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
