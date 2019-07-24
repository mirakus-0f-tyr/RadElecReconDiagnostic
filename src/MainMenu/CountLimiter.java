/*
 * To prevent any unforeseen pollution of the data by stray EM/RF waves
 * and fields, this override will place a hard limit on the number of
 * counts that can increase throughout a single record.
 */
package MainMenu;

import static java.lang.Math.abs;

/**
 *
 * @author Rad Elec Inc.
 */
public class CountLimiter {
    
    public static String[] main(String[] DeviceResponse_parsed) {
        
        //If flag is not W, S, I, or E -- let's get the hell out here.
        if(!DeviceResponse_parsed[2].equals("W") && !DeviceResponse_parsed[2].equals("S") && !DeviceResponse_parsed[2].equals("I") && !DeviceResponse_parsed[2].equals("E")) {
            return DeviceResponse_parsed;
        }
        
        String override_Ch1 = "false"; //Let's keep track of whether or not we performed an override for chamber 1
        String override_Ch2 = "false"; //...and do the same for chamber 2
        
        long lastCount_1 = MainMenuUI.LastCount_Ch1; //Assign current last counts (Ch1) to local variable
        long lastCount_2 = MainMenuUI.LastCount_Ch2; //Assign current last counts (Ch2) to local variable
        
        int length_DeviceResponse = 30;
        
        String[] DeviceResponse = new String[length_DeviceResponse];
        System.arraycopy(DeviceResponse_parsed, 0, DeviceResponse, 0, 26);
        
        long counts_Ch1 = Long.parseLong(DeviceResponse[10]); //Assign Ch1 counts to local variable
        long counts_Ch2 = Long.parseLong(DeviceResponse[11]); //Assign Ch2 counts to local variable
        
        //RPD Section
        //Let's first determine the RPD -- if the two chambers are markedly divergent, let's throttle the high one.
        double RPD = 0;
        if((counts_Ch1 > 0 || counts_Ch2 > 0)) {
            RPD = abs((counts_Ch1 - counts_Ch2) / (((double)counts_Ch1 + (double)counts_Ch2)/2)) * 100;
        }
        
        if((counts_Ch1 > 20) && (RPD > 50) && ((counts_Ch2 < counts_Ch1))) {
            counts_Ch1 = (long) Math.ceil(counts_Ch2 * 1.1)+1;
            override_Ch1 = "true(" + Long.parseLong(DeviceResponse[10]) + ")"; //We performed an override for chamber 1 due to unacceptable RPD. Let's mark it.
        } else if((counts_Ch2 > 20) && (RPD > 50) && (counts_Ch1 < counts_Ch2)) {
            counts_Ch2 = (long) Math.ceil(counts_Ch1 * 1.1)+1;
            override_Ch2 = "true(" + Long.parseLong(DeviceResponse[11]) + ")"; //We performed an override for chamber 2 due to unacceptable RPD. Let's mark it.
        }
        
        //Last Count Logical Check Section
        //This is independent from the RPD section, as it is possible that both chambers experience interference simultaneously.
        //Chamber 1
        if(counts_Ch1 > 20 && counts_Ch1 > (2 * lastCount_1)) { //Only limit counts if >50 AND greater than twice the last counts
            if(lastCount_1 <= 50) {
                counts_Ch1 = lastCount_1 * 2 + 1;
            } else {
                counts_Ch1 = (long) Math.ceil(lastCount_1 * 1.1)+1;
            }
            override_Ch1 = "true(" + Long.parseLong(DeviceResponse[10]) + ")"; //We performed an override for chamber 1 due to abnormally high counts. Let's mark it.
        }  
        //Chamber 2
        if(counts_Ch2 > 20 && counts_Ch2 > (2 * lastCount_2)) { //Only consider limiting counts if >50 AND greater than twice the last counts
            if(lastCount_2 <= 50) {
                counts_Ch2 = lastCount_2 * 2+1;
            } else {
                counts_Ch2 = (long) Math.ceil(lastCount_2 * 1.1)+1;
            }
            override_Ch2 = "true(" + Long.parseLong(DeviceResponse[11]) + ")"; //We performed an override for chamber 1 due to abnormally high counts. Let's mark it.
        }
        
        MainMenuUI.LastCount_Ch1 = counts_Ch1; //Assign Ch1 Last Count to global -- being sure to use the modified value of counts_Ch1
        MainMenuUI.LastCount_Ch2 = counts_Ch2; //Assign Ch2 Last Count to global -- being sure to use the modified value of counts_Ch2
        
        DeviceResponse[10] = Long.toString(counts_Ch1);
        DeviceResponse[11] = Long.toString(counts_Ch2);
        
        //The following is for debugging (determining whether count limiter was enacted, and the "unlimited" counts.
        //This paves the way to (eventually) allow XLS creation in the end-user mode, now that it's the only mode available to users.
        DeviceResponse[26] = override_Ch1;
        DeviceResponse[27] = override_Ch2;
        
        return DeviceResponse;
    }
    
}
