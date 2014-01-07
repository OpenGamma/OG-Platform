/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.datasets;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.ExceptionCalendar;

/**
 * Calendar with the dates of the ECB meeting decisions (settlement dates for the change of rate).
 */
public class CalendarECBSettlements extends ExceptionCalendar {

  /**
   * Defaults serial ID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The list of settlement dates (for 2013-2014).
   */
  private static final LocalDate SETTLE[] = new LocalDate[] {LocalDate.of(2013, 5, 8), LocalDate.of(2013, 10, 9), LocalDate.of(2013, 11, 13), LocalDate.of(2013, 12, 11),
    LocalDate.of(2014, 1, 15), LocalDate.of(2014, 2, 12), LocalDate.of(2014, 3, 12), LocalDate.of(2014, 4, 9), LocalDate.of(2014, 5, 14), LocalDate.of(2014, 6, 11),
    LocalDate.of(2014, 7, 9), LocalDate.of(2014, 8, 13), LocalDate.of(2014, 9, 10), LocalDate.of(2014, 10, 8), LocalDate.of(2014, 11, 12), LocalDate.of(2014, 12, 10) };

  private static final String NAME = "ECB decision settlement dates";

  /**
   * Constructor of the calendar
   * @param name
   */
  public CalendarECBSettlements() {
    super(NAME);
    for (int loopy = 0; loopy < SETTLE.length; loopy++) {
      addNonWorkingDay(SETTLE[loopy]);
    }
  }

  @Override
  protected boolean isNormallyWorkingDay(LocalDate date) {
    return true;
  }

}
