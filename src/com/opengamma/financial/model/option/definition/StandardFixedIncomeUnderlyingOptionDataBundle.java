/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.interestrate.definition.FixedInterestRateInstrumentDefinition;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * 
 */
public class StandardFixedIncomeUnderlyingOptionDataBundle extends StandardOptionDataBundle {
  private final FixedInterestRateInstrumentDefinition _underlyingInstrument;

  public StandardFixedIncomeUnderlyingOptionDataBundle(final DiscountCurve discountCurve, final Double b, final VolatilitySurface volatilitySurface, final ZonedDateTime date,
      final FixedInterestRateInstrumentDefinition underlyingInstrument) {
    super(discountCurve, b, volatilitySurface, null, date);
    _underlyingInstrument = underlyingInstrument;
  }

  // TODO there should be the option to have the spot price entered or
  // calculated from an underlying pricing model
  @Override
  public Double getSpot() {
    throw new NotImplementedException();
  }

  public FixedInterestRateInstrumentDefinition getUnderlyingInstrument() {
    return _underlyingInstrument;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_underlyingInstrument == null ? 0 : _underlyingInstrument.hashCode());
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
    final StandardFixedIncomeUnderlyingOptionDataBundle other = (StandardFixedIncomeUnderlyingOptionDataBundle) obj;
    if (_underlyingInstrument == null) {
      if (other._underlyingInstrument != null) {
        return false;
      }
    } else if (!_underlyingInstrument.equals(other._underlyingInstrument)) {
      return false;
    }
    return true;
  }
}
