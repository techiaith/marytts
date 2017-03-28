package marytts.language.cy;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.*;

public class Welsh_Time {
	
	public static String TIMEREGEX = "([0]?[1]?[0-9]|2[0-3]):([0-5][0-9])(.[0-5][0-9])?";

	public static String tokenise(String timeString){

		StringBuilder sb = new StringBuilder();
	
		Pattern pattern = Pattern.compile(TIMEREGEX);
		Matcher matcher = pattern.matcher(timeString);

		if (matcher.matches()){
			int hours = Integer.parseInt(matcher.group(1));
			int minutes = Integer.parseInt(matcher.group(2));
			int seconds = 0;
			String secondsString = matcher.group(3);
			if (secondsString != null){
				secondsString = secondsString.replace(".","");
				seconds = Integer.parseInt(secondsString);
			}
    		
			if (minutes == 0){
				sb.append(welsh_hours(hours, false));				
			} else {
				
				sb.append(welsh_number_of_minutes(minutes));

				if (minutes <= 30){					
					sb.append(" wedi ");
					sb.append(welsh_hours(hours, false));
				} else {
					sb.append(" i ");
					sb.append(welsh_hours(hours + 1, true));
				}

			} 
			
			sb.append(", o'r gloch");

			if (hours > 17)
				sb.append(", yn nos");
			else if (hours > 12)
				sb.append(", yn y prynhawn"); 
			else
				sb.append(", yn y bore");
		}		
				
		return sb.toString();

	}

	private static String welsh_hours(int hours, boolean tm)
	{
		String hoursString = "";

		if (hours <= 12) {
			switch (hours){
				case 2: hoursString = tm ? "ddau":"dau"; break;
				case 3: hoursString = tm ? "dri":"tri"; break;
				case 4: hoursString = tm ? "bedwar":"pedwar"; break;
				case 5:	hoursString = tm ? "bump":"pump"; break;
				case 10: hoursString = tm ? "ddeg":"deg"; break;
				case 11: hoursString = "un ar ddeg"; break;
				case 12: hoursString = tm ? "ddeuddeg":"deuddeg"; break;
				default:
					hoursString = Welsh_Base_10.tokenise(hours, false);
					break;
			}
		} else {
			hoursString = welsh_hours(hours - 12, tm);
		}

		return hoursString;

	}

	private static String welsh_number_of_minutes(int minutes)
	{
		String minutesString = "";

		if (minutes > 30) {
			return welsh_number_of_minutes(60 - minutes);
		} else {

			switch (minutes)
			{
				case 1: minutesString = "un funud"; break;
				case 2: minutesString = "dwy funud"; break;
				case 3: minutesString = "tair munud"; break;
				case 4: minutesString = "pedair munud"; break;
				case 5: minutesString = "pum munud"; break;
				case 6: minutesString = "chwe munud"; break;
				case 7: minutesString = "saith munud"; break;
				case 8: minutesString = "wyth munud"; break;
				case 9: minutesString = "naw munud"; break;				
				case 10: minutesString = "deng munud"; break;
				case 11: minutesString = "un funud ar ddeg"; break;
				case 12: minutesString = "deuddeg munud"; break;
				case 13: minutesString = "tair munud ar ddeg"; break;
				case 14: minutesString = "pedair munud ar ddeg"; break;
				case 15: minutesString = "chwarter"; break;
				case 16: minutesString = "un funud ar bymtheg"; break;
				case 17: minutesString = "dwy funud ar bymtheg"; break;
				case 18: minutesString = "deunaw munud"; break;
				case 19: minutesString = "pedair munud ar bymtheg"; break;
				case 20: minutesString = "ugain munud";
				case 30: minutesString = "hanner awr"; break;
			}

			if (minutes > 20 && minutes < 30 ){
				minutesString = welsh_number_of_minutes(minutes - 20) + " ar hugain";
			}

			return minutesString;

		}
		
	}

}
