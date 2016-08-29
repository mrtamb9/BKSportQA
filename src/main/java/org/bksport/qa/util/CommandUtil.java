package org.bksport.qa.util;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommandUtil {

	private static String[] commandPrefixs = { "tell me", "show me", "give me",
			"tell us", "show us", "give us", "could you tell me",
			"could you show me", "could you give me", "could you tell us",
			"could you show us", "could you give us", "can you tell me",
			"can you show me", "can you give me", "can you tell us",
			"can you show us", "can you give us", "i want to know",
			"i wanna know", "i'd like to know" };
	private static String[] timeLabels = { "yesterday", "the last day",
			"tomorrow", "the next day" };

	private static String dd, mm, yyyy;
	private static String ddBegin, mmBegin, ddEnd, mmEnd;

	/**
	 * cut command prefix in question, and normalize the question
	 * 
	 * @param command
	 * @return
	 */
	public CommandUtil() {
		dd = new String();
		mm = new String();
		yyyy = new String();
		ddBegin = new String("01");
		mmBegin = new String("01");
		ddEnd = new String("31");
		mmEnd = new String("12");
	}

	public static String truncate(String command) {
		if (command != null) {
			String lcCommand = command.toLowerCase();
			for (int i = 0; i < commandPrefixs.length; i++) {
				if (lcCommand.startsWith(commandPrefixs[i])
						&& lcCommand.length() > commandPrefixs[i].length()) {
					return command.substring(commandPrefixs[i].length()).trim();
				}
			}
		} else {
			return null;
		}
		return command.trim();
	}

	public static String preProcessTimeLable(String command) {

		Calendar calYesterday = Calendar.getInstance();
		calYesterday.add(Calendar.DATE, -1);

		Calendar calTomorrow = Calendar.getInstance();
		calTomorrow.add(Calendar.DATE, +1);

		Calendar calToday = Calendar.getInstance();
		calToday.add(Calendar.DATE, 0);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		System.out.println(sdf.format(calYesterday.getTime()));

		if (command != null) {
			if (command.contains("today")) {
				command = command.replaceAll("today",
						"in " + sdf.format(calToday.getTime()).toString());
			}
			if (command.contains("yesterday")) {
				command = command.replaceAll("yesterday",
						"in " + sdf.format(calYesterday.getTime()).toString());
			}
			if (command.contains("the last day")) {
				command = command.replaceAll("the last day", "in "
						+ sdf.format(calYesterday.getTime()).toString());
			}
			if (command.contains("tomorrow")) {
				command = command.replaceAll("tomorrow",
						"in " + sdf.format(calTomorrow.getTime()).toString());
			}
			if (command.contains("the next day")) {
				command = command.replaceAll("the next day", "in "
						+ sdf.format(calTomorrow.getTime()).toString());
			}
			if (command.contains("this year")) {
				command = command.replaceAll("this year", "in "
						+ Calendar.getInstance().get(Calendar.YEAR));
			}
			if (command.contains("this season")) {
				command = command.replaceAll("this season", "in "
						+ Calendar.getInstance().get(Calendar.YEAR));
			}
			if (command.contains("last year")) {
				command = command.replaceAll("last year", "in "
						+ (Calendar.getInstance().get(Calendar.YEAR) - 1));
			}
			if (command.contains("last season")) {
				command = command.replaceAll("last season", "in "
						+ (Calendar.getInstance().get(Calendar.YEAR) - 1));
			}
		}
		return command;
	}

	public int checkDateFormat(String dateString) {
		int index;

		String datePattern1 = "\\d{1,2}\\\\/\\d{1,2}\\\\/\\d{4}";
		String datePattern2 = "\\d{1,2}\\\\/\\d{4}";
		String datePattern3 = "\\d{4}";
		System.out.println(dateString);

		if (dateString.matches(datePattern1)) {
			// do nothing
			index = 1;
		} else if (dateString.matches(datePattern2)) {
			index = 2;
			dateString = "00\\\\/" + dateString;
		} else if (dateString.matches(datePattern3)) {
			index = 3;
			dateString = "00\\\\/00\\\\/" + dateString;
		} else {
			return 0; // tra ve gia tri =0
		}

		String[] dateSplit = dateString.split("\\\\/");
		dd = dateSplit[0];
		if (dd.length() == 1) {
			dd = "0" + dd;
		}
		mm = dateSplit[1];
		if (mm.length() == 1) {
			mm = "0" + mm;
		}
		yyyy = dateSplit[2];

		if (index == 1) {
			ddBegin = dd.toString();
			ddEnd = dd.toString();
			mmBegin = mm.toString();
			mmEnd = mm.toString();
		} else if (index == 2) {
			mmBegin = mm.toString();
			mmEnd = mm.toString();
		}

		return index;
	}

	public static String getDD() {
		return dd;
	}

	public static String getMM() {
		return mm;
	}

	public String getYYYY() {
		return yyyy;
	}

	public String getDDBegin() {
		return ddBegin;
	}

	public String getMMBegin() {
		return mmBegin;
	}

	public String getDDEnd() {
		return ddEnd;
	}

	public String getMMEnd() {
		return mmEnd;
	}

}
