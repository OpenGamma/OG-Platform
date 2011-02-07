/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.Convention;

/**
 * 
 */
public class SwapConvention extends Convention {
  private final boolean _isEOM;

  public SwapConvention(final int settlementDays, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Calendar workingDayCalendar, final boolean isEOM, 
      final String name) {
    super(settlementDays, dayCount, businessDayConvention, workingDayCalendar, name);
    _isEOM = isEOM;
  }

  public boolean isEOM() {
    return _isEOM;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isEOM ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SwapConvention other = (SwapConvention) obj;
    return _isEOM == other._isEOM;
  }
}
