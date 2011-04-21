/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.instrument.Convention;

/**
 * 
 */
public class BondConvention extends Convention {
  private final int _exDividendDays;
  private final YieldConvention _yieldConvention;
  private final boolean _isEOM;

  public BondConvention(final int settlementDays, final DayCount dayCount, final BusinessDayConvention businessDayConvention, final Calendar workingDayCalendar, final boolean isEOM,
      final String name, final int exDividendDays, final YieldConvention yieldConvention) {
    super(settlementDays, dayCount, businessDayConvention, workingDayCalendar, name);
    Validate.isTrue(exDividendDays >= 0);
    Validate.notNull(yieldConvention, "yield convention");
    _exDividendDays = exDividendDays;
    _isEOM = isEOM;
    _yieldConvention = yieldConvention;
  }

  public boolean isEOM() {
    return _isEOM;
  }

  public int getExDividendDays() {
    return _exDividendDays;
  }

  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _exDividendDays;
    result = prime * result + (_isEOM ? 1231 : 1237);
    result = prime * result + _yieldConvention.hashCode();
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
    final BondConvention other = (BondConvention) obj;
    if (_exDividendDays != other._exDividendDays) {
      return false;
    }
    if (_isEOM != other._isEOM) {
      return false;
    }
    return ObjectUtils.equals(_yieldConvention, other._yieldConvention);
  }
}
