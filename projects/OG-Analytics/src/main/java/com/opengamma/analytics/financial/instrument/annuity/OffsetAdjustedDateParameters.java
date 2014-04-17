/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.annuity;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Parameters required to adjust dates with an offset.
 */
public class OffsetAdjustedDateParameters extends AdjustedDateParameters {

  /**
   * The number of days to offset the adjusted dates by.
   */
  private int _offset;
  
  /**
   * The type of offset days, which could be business or calendar days.
   */
  private OffsetType _offsetType;
  
  public OffsetAdjustedDateParameters(
      int offset,
      OffsetType offsetType,
      Calendar calendar,
      BusinessDayConvention businessDayConvention) {
    super(calendar, businessDayConvention);
    _offset = offset;
    _offsetType = offsetType;
  }
  
  public int getOffset() {
    return _offset;
  }
  
  public OffsetType getOffsetType() {
    return _offsetType;
  }
}
