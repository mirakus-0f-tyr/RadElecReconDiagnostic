// This is a basic class meant to provide conversion methods
// for US and SI units.  JMD

package MainMenu;
import java.text.DecimalFormat;

public class ConvMath {
	// expected formatting of various results
	public static DecimalFormat RadonUS = new DecimalFormat("0.0");
	public static DecimalFormat RadonSI = new DecimalFormat("0");
	public static DecimalFormat PressUS = new DecimalFormat("0");
	public static DecimalFormat PressSI = new DecimalFormat("0");

	// radon units
	public static String UStoSIRadon(double pCiL) {
		return RadonSI.format(pCiL * 37);
	}

	public static String SItoUSRadon(int BQM3) {
		return RadonUS.format((double)BQM3 / 37);
	}

	public static String AverageRadon(double val1, double val2, String unit) {
		if (unit == "US") 
			return RadonUS.format((val1 + val2) / 2);
		else if (unit == "SI") {
			val1 = Double.parseDouble(UStoSIRadon(val1));
			val2 = Double.parseDouble(UStoSIRadon(val2));
			return RadonSI.format((val1 + val2) / 2);
		}
		else
			return "Incorrect unit type specified.";
	}

	// temperature units
	public static String SItoUSTemp(int tempC) {
		return Integer.toString(tempC * 9 / 5 + 32);
	}

	public static String UStoSITemp(int tempF) {
		return Integer.toString((tempF - 32) * 5 / 9);
	}

	// pressure units
	public static String UStoSIPressure(double inHG) {
		return PressSI.format(inHG * 33.8639);
	}

	public static String SItoUSPressure(double mBar) {
		return PressUS.format(mBar * 0.02953);
	}
}
