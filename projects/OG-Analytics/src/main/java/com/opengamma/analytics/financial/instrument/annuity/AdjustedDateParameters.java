/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Parameters required to adjust dates.
 */
public class AdjustedDateParameters {

  /**
   * The calendar used to adjust dates.
   */
  private final Calendar _calendar;
  
  /**
   * The business day convention used to adjust dates.
   */
  private final BusinessDayConvention _businessDayConvention;
  
  public AdjustedDateParameters(
      Calendar calendar,
      BusinessDayConvention businessDayConvention) {
    _calendar = calendar;
    _businessDayConvention = businessDayConvention;
  }
  
  /**
   * Returns the calendar used to adjust dates.
   * @return the calendar used to adjust dates.
   */
  public Calendar getCalendar() {
    return _calendar;
  }
  
  /**
   * Returns the business day convention used to adjust dates.
   * @return the business day convention used to adjust dates.
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }
  
  // TODO equals, hashcode
}
