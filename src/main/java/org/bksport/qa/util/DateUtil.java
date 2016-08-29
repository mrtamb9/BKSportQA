package org.bksport.qa.util;

import java.util.Calendar;

import org.bksport.sparql.IRI;
import org.bksport.sparql.RDFLiteral;

/**
 * 
 * @author congnh
 * 
 */
public class DateUtil {

  /**
   * get RDFLiteral datetime from specific <code>calendar</code>
   * 
   * @param calendar
   * @return
   */
  public static final RDFLiteral getDateTime(Calendar calendar) {
    int month = calendar.get(Calendar.MONTH);
    int date = calendar.get(Calendar.DATE);
    int hour = calendar.get(Calendar.HOUR);
    int minute = calendar.get(Calendar.MINUTE);
    int second = calendar.get(Calendar.SECOND);
    String vl = calendar.get(Calendar.YEAR) + "-"
        + (month < 10 ? "0" + month : month) + "-"
        + (date < 10 ? "0" + date : date) + "T"
        + (hour < 10 ? "0" + hour : hour) + ":"
        + (minute < 10 ? "0" + minute : minute) + ":"
        + (second < 10 ? "0" + second : second);
    return new RDFLiteral(vl, IRI.XSD_DATETIME_IRI);
  }

  public static final RDFLiteral getDateTimeToday() {
    return getDateTime(Calendar.getInstance());
  }

  public static final RDFLiteral getDateTimeYesterday() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -1);
    return getDateTime(calendar);
  }

  /**
   * get RDFLiteral date from specific <code>calendar</code>
   * 
   * @param calendar
   * @return
   */
  public static final RDFLiteral getDate(Calendar calendar) {
    int month = calendar.get(Calendar.MONTH);
    int date = calendar.get(Calendar.DATE);
    String vl = calendar.get(Calendar.YEAR) + "-"
        + (month < 10 ? "0" + month : month) + "-"
        + (date < 10 ? "0" + date : date);
    return new RDFLiteral(vl, IRI.XSD_DATE_IRI);
  }

  public static final RDFLiteral getDateToday() {
    return getDate(Calendar.getInstance());
  }

  public static final RDFLiteral getDateYesterday() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -1);
    return getDate(calendar);
  }

  /**
   * get timestamp of specific datetime RDFLiteral
   * 
   * @param datetime
   * @return number of milliseconds represent datetime if success, othewise
   *         number of milliseconds represent today
   */
  public static long getTimestamp(RDFLiteral datetime) {
    if (datetime.getIRI().equals(IRI.XSD_DATETIME_IRI)) {
      String datetimeStr = datetime.getString();
      Calendar calendar = Calendar.getInstance();
      String split[] = datetimeStr.split("T");
      String date[] = split[0].split("-");
      String time[] = split[1].split(":");
      calendar.set(Calendar.YEAR, Integer.parseInt(date[0]));
      calendar.set(Calendar.MONTH, Integer.parseInt(date[1]));
      calendar.set(Calendar.DATE, Integer.parseInt(date[2]));
      calendar.set(Calendar.HOUR, Integer.parseInt(time[0]));
      calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
      calendar.set(Calendar.SECOND, Integer.parseInt(time[2]));
      return calendar.getTimeInMillis();
    } else {
      return Calendar.getInstance().getTimeInMillis();
    }
  }

  public static void main(String args[]) {
    System.out.println(Calendar.getInstance().getTimeInMillis());
    RDFLiteral today = getDateTimeToday();
    System.out.println(getTimestamp(today));
    System.out.println(getDateYesterday());
  }
}
