/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.Convention;

/**
 * 
 */
public class IRFutureConvention extends Convention {
  //TODO year fraction is not the right name but should wait until renaming of variable is done in InterestRateFuture
  private final double _yearFraction;

  public IRFutureConvention(int settlementDays, DayCount dayCount, BusinessDayConvention businessDayConvention, Calendar workingDayCalendar, double yearFraction, String name) {
    super(settlementDays, dayCount, businessDayConvention, workingDayCalendar, name);
    Validate.isTrue(yearFraction > 0, "year fraction must be positive");
    _yearFraction = yearFraction;
  }

  public double getYearFraction() {
    return _yearFraction;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_yearFraction);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    IRFutureConvention other = (IRFutureConvention) obj;
    return Double.doubleToLongBits(_yearFraction) == Double.doubleToLongBits(other._yearFraction);
  }

}
