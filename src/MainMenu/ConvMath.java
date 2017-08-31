// This is a basic class meant to provide conversion methods
// for US and SI units.  JMD

package MainMenu;
import java.text.DecimalFormat;

public class ConvMath {
	// expected formatting of various results
	public static DecimalFormat formatUS_RnC = new DecimalFormat("0.0");
	public static DecimalFormat formatSI_RnC = new DecimalFormat("0");
	public static DecimalFormat formatUS_pres = new DecimalFormat("0");
	public static DecimalFormat formatSI_pres = new DecimalFormat("0");

	// radon units
	public static String UStoSIRadon(double pCiL) {
		return formatSI_RnC.format(pCiL * 37);
	}

	public static String SItoUSRadon(int BQM3) {
		return formatUS_RnC.format((double)BQM3 / 37);
	}

	public static String AverageRadon(double val1, double val2, String unit) {
		if (unit == "US") 
			return formatUS_RnC.format((val1 + val2) / 2);
		else if (unit == "SI") {
			val1 = Double.parseDouble(UStoSIRadon(val1));
			val2 = Double.parseDouble(UStoSIRadon(val2));
			return formatSI_RnC.format((val1 + val2) / 2);
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
		return formatSI_pres.format(inHG * 33.8639);
	}

	public static String SItoUSPressure(double mBar) {
		return formatUS_pres.format(mBar * 0.02953);
	}
}
