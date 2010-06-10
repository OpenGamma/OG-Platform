/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.Region;

/**
 * Represents one leg of a swap.
 */
public class SwapLeg {
  private DayCount _daycount;
  private Frequency _frequency;
  private Region _region;
  private BusinessDayConvention _businessDayConvention;
  private Notional _notional;

  /**
   * @param daycount day count convention
   * @param frequency the frequency for payments
   * @param region the region of issue
   * @param businessDayConvention the business day convention
   * @param notional the notional value of this leg
   */
  public SwapLeg(DayCount daycount, Frequency frequency, Region region, BusinessDayConvention businessDayConvention,
      Notional notional) {
    super();
    _daycount = daycount;
    _frequency = frequency;
    _region = region;
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
  public Region getRegion() {
    return _region;
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
