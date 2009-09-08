package com.opengamma.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {
  public static final double MILLISECONDS_PER_DAY = 86400000;
  public static final double DAYS_PER_YEAR = 365.25;

  public static double subtract(Date d1, Date d2) {
    return (d1.getTime() - d2.getTime()) / MILLISECONDS_PER_DAY;
  }

  public static Date add(Date d, double offset) {
    long x = d.getTime() + (long) (offset * MILLISECONDS_PER_DAY * 365.25);
    return new Date(x);
  }

  public static Date today() {
    Calendar today = Calendar.getInstance();
    int year = today.get(Calendar.YEAR);
    int month = today.get(Calendar.MONTH);
    int day = today.get(Calendar.DAY_OF_MONTH);
    Calendar c = new GregorianCalendar(year, month, day, 0, 0, 0);
    return c.getTime();
  }

  public static Date date(int yyyymmdd) {
    int year = yyyymmdd / 10000;
    int month = (yyyymmdd - 10000 * year) / 100;
    int day = yyyymmdd - 10000 * year - 100 * month;
    Calendar c = new GregorianCalendar(year, month, day, 0, 0, 0);
    return c.getTime();
  }
}
