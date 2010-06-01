/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.Holiday;

/**
 * Represents one leg of a swap.
 */
public class SwapLeg {
  private DayCount _daycount;
  private Frequency _frequency;
  private Holiday _holiday;
  private BusinessDayConvention _businessDayConvention;
  private Notional _notional;

  /**
   * @param daycount
   * @param frequency
   * @param holiday
   * @param businessDayConvention
   * @param notional
   */
  public SwapLeg(DayCount daycount, Frequency frequency, Holiday holiday, BusinessDayConvention businessDayConvention,
      Notional notional) {
    super();
    _daycount = daycount;
    _frequency = frequency;
    _holiday = holiday;
    _businessDayConvention = businessDayConvention;
    _notional = notional;
  }
  /**
   * @return the daycount
   */
  public DayCount getDaycount() {
    return _daycount;
  }
  /**
   * @return the frequency
   */
  public Frequency getFrequency() {
    return _frequency;
  }
  /**
   * @return the holiday
   */
  public Holiday getHoliday() {
    return _holiday;
  }
  /**
   * @return the businessDayConvention
   */
  public BusinessDayConvention getBusinessDayConvention() {
    return _businessDayConvention;
  }
  /**
   * @return the notional
   */
  public Notional getNotional() {
    return _notional;
  }

}
