package com.opengamma.financial.analytics.model.cds.date;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;

public abstract class DateFunctions
{
  /**
   * based on http://aa.usno.navy.mil/faq/docs/easter.php
   * 
   * @param YYYY
   *            the year
   * @return The date of Easter
   */
  public static ZonedDateTime EasterUSNO(int YYYY)
  {
    double C;
    double N;
    double K;
    double I;
    double J;
    double L;
    int M;
    int D;

    C = YYYY / 100;
    N = YYYY - (19 * (YYYY / 19));
    K = (C - 17) / 25;
    I = (C - (C / 4) - ((C - K) / 3)) + (19 * N) + 15;
    I = I - (30 * (I / 30));
    I = I - ((I / 28) * (1 - ((I / 28) * (29 / (I + 1)) * ((21 - N) / 11))));
    J = ((YYYY + (YYYY / 4) + I + 2) - C) + (C / 4);
    J = J - (7 * (J / 7));
    L = I - J;
    M = (int) (3 + ((L + 40) / 44));
    D = (int) ((L + 28) - (31 * (M / 4)));
    return ZonedDateTime.of(LocalDateTime.ofMidnight(YYYY, M, D), TimeZone.UTC);
  }

  public static boolean isBusinessDay(ZonedDateTime dateToTest, Calendars calendar)
  {
    if (DateFunctions.isWeekend(dateToTest))
    {
      return false;
    }

    final ZonedDateTime easterDay = DateFunctions.EasterUSNO(dateToTest.getYear());

    ZonedDateTime GoodFriday;
    ZonedDateTime EasterMonday;

    switch (calendar)
    {
      case EU:
        if (((dateToTest.getMonthOfYear().isDecember()) && (dateToTest.getDayOfMonth() == 25))
            || ((dateToTest.getMonthOfYear().isJanuary()) && (dateToTest.getDayOfMonth() == 1)))
        {
          return false;
        }
        break;

      case US:

        if (((dateToTest.getMonthOfYear().isDecember()) && (dateToTest.getDayOfMonth() == 25))
            || ((dateToTest.getMonthOfYear().isJanuary()) && (dateToTest.getDayOfMonth() == 1)))
        {
          return false;
        }

        if ((dateToTest.getMonthOfYear().isMay()) && (dateToTest.getDayOfWeek().isMonday()))
        {
          if (dateToTest.plusDays(7).getMonthOfYear().isJune())
          {
            return false;
          }
        }

        if ((dateToTest.getMonthOfYear().isJuly()) && (dateToTest.getDayOfMonth() == 4))
        {
          return false;
        }

        break;

      case UK:

        GoodFriday = easterDay.minusDays(-2);
        EasterMonday = easterDay.plusDays(1);

        if (((dateToTest.getMonthOfYear().isDecember()) && (dateToTest.getDayOfMonth() == 25))
            || ((dateToTest.getMonthOfYear().isJanuary()) && (dateToTest.getDayOfMonth() == 1)) || (dateToTest.equals(GoodFriday))
            || (dateToTest.equals(EasterMonday)))
        {
          return false;
        }
        break;
    }

    return true;
  }

  /**
   * Is this is a weekend?
   * 
   * @param dateToTest
   *            Date to test
   * @return <code>true</code> if a Sunday or Saturday, <code>false</code>
   *         otherwise
   */
  public static boolean isWeekend(ZonedDateTime dateToTest)
  {
    final DayOfWeek day = dateToTest.getDayOfWeek();
    if ((day.isSaturday()) || (day.isSunday()))
    {
      return true;
    }
    return false;
  }

  /**
   * Find the next business day after the specified date
   * 
   * @param startDate
   *            the start date
   * @param cal
   * @return The next business day
   */
  public static ZonedDateTime nextFollowingDay(ZonedDateTime startDate, Calendars cal)
  {
    ZonedDateTime result = startDate.minusSeconds(0);
    while (!DateFunctions.isBusinessDay(result, cal)) {
      result = result.plusDays(1);
    }

    return result;
  }

  public static int numberOfDates(ZonedDateTime maturity, Period term, Calendars calendar, ZonedDateTime today)
  {
    ZonedDateTime currentDate;
    int counter = 0;
    // if maturity is before current date it's an error
    if (maturity.isBefore(today)) {
      return counter;
    }

    currentDate = maturity;
    while (currentDate.isAfter(today) || currentDate.equals(today)) {
      counter = counter + 1;
      //maturity.
      currentDate = DateFunctions.nextFollowingDay(DateFunctions.subtractTerm(maturity, (int) term.totalMonths() * counter), calendar);
    }
    return counter;
  }

  /**
   * Return the date minus the period (months)
   * 
   * @param maturity
   *            The maturity date
   * @param period
   *            The period in months
   * @return The maturity date minus the period
   */
  public static ZonedDateTime subtractTerm(ZonedDateTime maturity, int period)
  {
    return maturity.minusMonths(period);
  }
}
