/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.datasets;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 * 
 */
public class CalendarGBP extends MondayToFridayCalendar {

  /**
   * Calendar for Target non-good business days. Only for test purposes, is not accurate enough for production.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * @param name The name
   */
  public CalendarGBP(final String name) {
    super(name);
    final int startYear = 2012;
    final int endYear = 2063;

    for (int loopy = startYear; loopy <= endYear; loopy++) {
      addNonWorkingDay(LocalDate.of(loopy, 1, 1));
      addNonWorkingDay(LocalDate.of(loopy, 12, 25));
      addNonWorkingDay(LocalDate.of(loopy, 12, 26));
    }
    final LocalDate easter[] = new LocalDate[] {LocalDate.of(2012, 4, 8), LocalDate.of(2013, 3, 31), LocalDate.of(2014, 4, 20), LocalDate.of(2015, 4, 5),
      LocalDate.of(2016, 3, 27), LocalDate.of(2017, 4, 16), LocalDate.of(2018, 4, 1), LocalDate.of(2019, 4, 21), LocalDate.of(2020, 4, 12),
      LocalDate.of(2021, 4, 4), LocalDate.of(2022, 4, 17), LocalDate.of(2023, 4, 9), LocalDate.of(2024, 3, 31), LocalDate.of(2025, 4, 20),
      LocalDate.of(2026, 4, 5), LocalDate.of(2027, 3, 28), LocalDate.of(2028, 4, 16), LocalDate.of(2029, 4, 1), LocalDate.of(2030, 4, 21),
      LocalDate.of(2031, 4, 13), LocalDate.of(2032, 3, 28), LocalDate.of(2033, 4, 17), LocalDate.of(2034, 4, 9), LocalDate.of(2035, 3, 25),
      LocalDate.of(2036, 4, 13), LocalDate.of(2037, 4, 5), LocalDate.of(2038, 4, 25), LocalDate.of(2039, 4, 10), LocalDate.of(2040, 4, 1),
      LocalDate.of(2041, 4, 21), LocalDate.of(2042, 4, 6), LocalDate.of(2043, 3, 29), LocalDate.of(2044, 4, 17), LocalDate.of(2045, 4, 9),
      LocalDate.of(2046, 3, 25), LocalDate.of(2047, 4, 14), LocalDate.of(2048, 4, 5), LocalDate.of(2049, 4, 18) };
    for (final LocalDate element : easter) {
      addNonWorkingDay(element.minusDays(2)); // Easter Friday
      addNonWorkingDay(element.plusDays(1)); // Easter Monday
    }

    //bank holidays (ex. Christmas, new year and Easter)
    addNonWorkingDay(LocalDate.of(2012, 5, 7));
    addNonWorkingDay(LocalDate.of(2012, 6, 4));
    addNonWorkingDay(LocalDate.of(2012, 6, 5));
    addNonWorkingDay(LocalDate.of(2012, 8, 27));
    addNonWorkingDay(LocalDate.of(2013, 5, 6));
    addNonWorkingDay(LocalDate.of(2013, 5, 27));
    addNonWorkingDay(LocalDate.of(2013, 8, 26));
    addNonWorkingDay(LocalDate.of(2014, 5, 5));
    addNonWorkingDay(LocalDate.of(2014, 5, 26));
    addNonWorkingDay(LocalDate.of(2014, 8, 25));
    addNonWorkingDay(LocalDate.of(2015, 5, 4));
    addNonWorkingDay(LocalDate.of(2015, 5, 25));
    addNonWorkingDay(LocalDate.of(2015, 8, 31));
    addNonWorkingDay(LocalDate.of(2015, 12, 28)); //boxing day
    addNonWorkingDay(LocalDate.of(2016, 5, 2));
    addNonWorkingDay(LocalDate.of(2016, 5, 30));
    addNonWorkingDay(LocalDate.of(2016, 8, 29));
    addNonWorkingDay(LocalDate.of(2016, 12, 27)); //Christmas Day (substitute day)
    addNonWorkingDay(LocalDate.of(2017, 1, 2));
    addNonWorkingDay(LocalDate.of(2017, 5, 1));
    addNonWorkingDay(LocalDate.of(2017, 5, 29));
    addNonWorkingDay(LocalDate.of(2017, 8, 28));
    addNonWorkingDay(LocalDate.of(2018, 5, 7));
    addNonWorkingDay(LocalDate.of(2018, 5, 28));
    addNonWorkingDay(LocalDate.of(2018, 8, 27));
    addNonWorkingDay(LocalDate.of(2019, 5, 6));
    addNonWorkingDay(LocalDate.of(2019, 5, 27));
    addNonWorkingDay(LocalDate.of(2019, 8, 26));
  }

}
