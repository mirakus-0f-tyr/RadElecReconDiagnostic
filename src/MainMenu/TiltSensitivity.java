/*
 * This class will allow the user to customize the sensitivity of the tilts.
 * Values range from 0 (very low sensitivity) to 10 (very high sensitivity), and
 * behave in an exponential (rather than linear) manner. A sensitivity of 10 is
 * approximately 1024 times more sensitive than 0. A sensitivity of 9 is around
 * 512 times more sensitive than 0, etc. Default sensitivity of 5 seems to work
 * fairly well.
 */
package MainMenu;

/**
 *
 * @author Rad Elec Inc.
 */
public class TiltSensitivity {
    
    public static long main(long lngRawTilts) {
        long lngTilts = lngRawTilts;
        if(lngTilts>=1000-(MainMenuUI.tiltSensitivity*100)) {
            lngTilts = Math.round(lngTilts/(1025-(Math.pow(2,MainMenuUI.tiltSensitivity))));
        } else {
            lngTilts = 0; //If the tilts do not exceed the sensitivity threshold, then we reduce them to zero.
        }
        return lngTilts;
    }
    
}
