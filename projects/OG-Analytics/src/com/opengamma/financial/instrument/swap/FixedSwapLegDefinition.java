/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import java.util.Arrays;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.instrument.InterestRateDerivativeProvider;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;

/**
 * 
 */
public class FixedSwapLegDefinition implements InterestRateDerivativeProvider<GenericAnnuity<FixedCouponPayment>> {
  private static final Logger s_logger = LoggerFactory.getLogger(FixedSwapLegDefinition.class);
  private final ZonedDateTime[] _nominalDates;
  private final ZonedDateTime[] _settlementDates;
  private final double _notional;
  private final double _rate;
  private final SwapConvention _convention;

  public FixedSwapLegDefinition(ZonedDateTime[] nominalDates, ZonedDateTime[] settlementDates, double notional, double rate, SwapConvention convention) {
    Validate.notNull(nominalDates, "nominal dates");
    Validate.notNull(settlementDates, "settlement dates");
    Validate.notNull(convention, "convention");
    Validate.isTrue(rate > 0, "fixed rate must be greater than 0");
    Validate.isTrue(nominalDates.length == settlementDates.length);
    _nominalDates = nominalDates;
    _settlementDates = settlementDates;
    _notional = notional;
    _rate = rate;
    _convention = convention;
  }

  public ZonedDateTime[] getNominalDates() {
    return _nominalDates;
  }

  public ZonedDateTime[] getSettlementDates() {
    return _settlementDates;
  }

  public double getNotional() {
    return _notional;
  }

  public double getRate() {
    return _rate;
  }

  public SwapConvention getConvention() {
    return _convention;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _convention.hashCode();
    result = prime * result + Arrays.hashCode(_nominalDates);
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_settlementDates);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FixedSwapLegDefinition other = (FixedSwapLegDefinition) obj;
    if (!Arrays.equals(_nominalDates, other._nominalDates)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    if (!Arrays.equals(_settlementDates, other._settlementDates)) {
      return false;
    }
    return ObjectUtils.equals(_convention, other._convention);
  }

  @Override
  public GenericAnnuity<FixedCouponPayment> toDerivative(LocalDate date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0);
    s_logger.info("Using the first yield curve name as the funding curve name");
    return null;
  }

}
