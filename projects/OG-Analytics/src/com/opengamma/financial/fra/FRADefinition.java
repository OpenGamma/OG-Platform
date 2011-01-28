/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fra;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.bond.Convention;
import com.opengamma.financial.bond.InterestRateDerivativeProvider;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;

/**
 * 
 */
public class FRADefinition implements InterestRateDerivativeProvider<ForwardRateAgreement> {
  private final ZonedDateTime _startDate;
  private final ZonedDateTime _maturityDate;
  private final Convention _convention;
  private final double _rate;

  public FRADefinition(final ZonedDateTime startDate, final ZonedDateTime maturityDate, final double rate, final Convention convention) {
    Validate.notNull(startDate, "start date");
    Validate.notNull(maturityDate, "maturity date");
    Validate.notNull(convention, "convention");
    Validate.isTrue(maturityDate.isAfter(startDate), "maturity must be after start date");
    _startDate = startDate;
    _maturityDate = maturityDate;
    _rate = rate;
    _convention = convention;
  }

  public ZonedDateTime getStartDate() {
    return _startDate;
  }

  public ZonedDateTime getMaturity() {
    return _maturityDate;
  }

  public Convention getConvention() {
    return _convention;
  }

  public double getRate() {
    return _rate;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _convention.hashCode();
    result = prime * result + _maturityDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _startDate.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FRADefinition other = (FRADefinition) obj;
    if (!ObjectUtils.equals(_startDate, other._startDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_maturityDate, other._maturityDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return ObjectUtils.equals(_convention, other._convention);
  }

  @Override
  public ForwardRateAgreement toDerivative(final LocalDate date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0);
    Validate.isTrue(_maturityDate.toLocalDate().isAfter(date) || _maturityDate.equals(date), "Date for security is after maturity");
    return null;
  }

}
