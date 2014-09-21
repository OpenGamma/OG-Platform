package com.opengamma.analytics.financial.datasets;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

public class CalendarUSD extends MondayToFridayCalendar {

  /**
   * Calendar with USD non-good business days. Only for test purposes, is not accurate enough for production.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * @param name The name
   */
  public CalendarUSD(String name) {
    super(name);
    final int startYear = 2013;
    final int endYear = 2063;
    for (int loopy = startYear; loopy <= endYear; loopy++) {
      addNonWorkingDay(LocalDate.of(loopy, 1, 1));
      addNonWorkingDay(LocalDate.of(loopy, 7, 4));
      addNonWorkingDay(LocalDate.of(loopy, 12, 25));
    }
    addNonWorkingDay(LocalDate.of(2014, 1, 20));
    addNonWorkingDay(LocalDate.of(2015, 1, 19));
    addNonWorkingDay(LocalDate.of(2015, 2, 16));
    addNonWorkingDay(LocalDate.of(2015, 5, 25));
    addNonWorkingDay(LocalDate.of(2015, 9, 7));
    addNonWorkingDay(LocalDate.of(2015, 10, 12));
    addNonWorkingDay(LocalDate.of(2015, 11, 11));
    addNonWorkingDay(LocalDate.of(2015, 11, 26));
    addNonWorkingDay(LocalDate.of(2016, 1, 18));
    addNonWorkingDay(LocalDate.of(2016, 2, 15));
    addNonWorkingDay(LocalDate.of(2016, 5, 30));
    addNonWorkingDay(LocalDate.of(2016, 9, 5));
    addNonWorkingDay(LocalDate.of(2016, 10, 10));
    addNonWorkingDay(LocalDate.of(2016, 11, 11));
    addNonWorkingDay(LocalDate.of(2016, 11, 24));
    addNonWorkingDay(LocalDate.of(2016, 12, 26));
    addNonWorkingDay(LocalDate.of(2017, 1, 2));
    addNonWorkingDay(LocalDate.of(2017, 1, 16));
    addNonWorkingDay(LocalDate.of(2017, 2, 20));
    addNonWorkingDay(LocalDate.of(2017, 5, 29));
    addNonWorkingDay(LocalDate.of(2017, 9, 4));
    addNonWorkingDay(LocalDate.of(2017, 10, 9));
    addNonWorkingDay(LocalDate.of(2017, 11, 23));
  }

}
