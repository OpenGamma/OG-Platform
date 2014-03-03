/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import static com.opengamma.timeseries.date.localdate.LocalDateToIntConverter.convertToLocalDate;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.ExceptionCalendar;

/**
 * The TYO holiday calendar should be used in all JPY fee calculations calculated after 11:59 PM, Tokyo local time on
 *  September 12, 2009. The data is taken from http://www.cdsmodel.com/cdsmodel/fee-computations.page
 */
public class TYOCalendar extends ExceptionCalendar {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final int[] INT_DATES = new int[] {20090320, 20090921, 20090922, 20090923, 20100322, 20100920, 20110321, 20120320, 20130320, 20150921, 20150922, 20150923, 20160321, 20170320,
    20200320, 20200921, 20200922, 20210920, 20220321, 20240320, 20250320, 20260320, 20260921, 20260922, 20260923, 20270322, 20270920, 20280320, 20290320, 20300320, 20320920, 20320921, 20320922,
    20330321, 20340320, 20360320, 20360922, 20370320, 20370921, 20370922, 20370923, 20380920, 20390321, 20400320, 20410320, 20420320, 20430921, 20440321, 20450320, 20460320, 20480320, 20480921,
    20480922, 20490920 };
  private static final Set<LocalDate> DATES;

  static {
    DATES = new HashSet<>(INT_DATES.length);
    for (final int intDate : INT_DATES) {
      DATES.add(convertToLocalDate(intDate));
    }
  }

  /**
   * @param name Name
   */
  public TYOCalendar(final String name) {
    super(name);
  }

  @Override
  protected boolean isNormallyWorkingDay(final LocalDate date) {
    final DayOfWeek day = date.getDayOfWeek();
    if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
      return false;
    }
    if (DATES.contains(date)) {
      return false;
    }
    return true;
  }
}
