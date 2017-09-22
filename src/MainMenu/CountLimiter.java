/*
 * To prevent any unforeseen pollution of the data by stray EM/RF waves
 * and fields, this override will place a hard limit on the number of
 * counts that can increase throughout a single record.
 */
package MainMenu;

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
        
        String[] DeviceResponse = new String[28];
        System.arraycopy(DeviceResponse_parsed, 0, DeviceResponse, 0, 26);
        
        long counts_Ch1 = Long.parseLong(DeviceResponse[10]); //Assign Ch1 counts to local variable
        long counts_Ch2 = Long.parseLong(DeviceResponse[11]); //Assign Ch2 counts to local variable
        
        //Chamber 1
        if(counts_Ch1 > 50 && counts_Ch1 > (2 * lastCount_1)) { //Only consider limiting counts if >50 AND greater than twice the last counts
            if(lastCount_1 <= 3) {
                counts_Ch1 = 6;
            } else if(lastCount_1 <= 6) {
                counts_Ch1 = 12;
            } else if(lastCount_1 <= 50) {
                counts_Ch1 = lastCount_1 * 2;
            } else {
                counts_Ch1 = (long) (lastCount_1 * 1.1);
            }
            override_Ch1 = "true(" + Long.parseLong(DeviceResponse[10]) + ")"; //We performed an override for chamber 1. Let's mark it.
        }
        
        //Chamber 2
        if(counts_Ch2 > 50 && counts_Ch2 > (2 * lastCount_2)) { //Only consider limiting counts if >50 AND greater than twice the last counts
            if(lastCount_2 <= 3) {
                counts_Ch2 = 6;
            } else if(lastCount_2 <= 6) {
                counts_Ch2 = 12;
            } else if(lastCount_2 <= 50) {
                counts_Ch2 = lastCount_2 * 2;
            } else {
                counts_Ch2 = (long) (lastCount_2 * 1.1);
            }
            override_Ch2 = "true(" + Long.parseLong(DeviceResponse[11]) + ")";
        }
        
        MainMenuUI.LastCount_Ch1 = counts_Ch1; //Assign Ch1 Last Count to global -- being sure to use the modified value of counts_Ch1
        MainMenuUI.LastCount_Ch2 = counts_Ch2; //Assign Ch2 Last Count to global -- being sure to use the modified value of counts_Ch2
        
        DeviceResponse[10] = Long.toString(counts_Ch1);
        DeviceResponse[11] = Long.toString(counts_Ch2);
        
        DeviceResponse[26] = override_Ch1;
        DeviceResponse[27] = override_Ch2;
        
        return DeviceResponse;
    }
    
}
