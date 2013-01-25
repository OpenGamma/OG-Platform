/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Class used to store future description and generate instruments.
 */
public class GeneratorInterestRateFutures extends GeneratorInstrument<GeneratorAttribute> {

  /**
   * The underlying STIR futures security.
   */
  private final InterestRateFutureDefinition _security;

  /**
   * Constructor.
   * @param name The generator name.
   * @param security The underlying STIR futures security.
   */
  public GeneratorInterestRateFutures(String name, final InterestRateFutureDefinition security) {
    super(name);
    ArgumentChecker.notNull(security, "STIR futures security");
    _security = security;
  }

  /**
   * Gets the STIR futures security.
   * @return The futures.
   */
  public InterestRateFutureDefinition getFutures() {
    return _security;
  }

  @Override
  /**
   * The quantity is modified to be in line with the required quantity.
   */
  public InterestRateFutureDefinition generateInstrument(ZonedDateTime date, double marketQuote, double notional, final GeneratorAttribute attribute) {
    int quantity = (int) Math.ceil(notional / _security.getNotional());
    return InterestRateFutureDefinition.fromFixingPeriodStartDate(date, marketQuote, quantity, _security.getFixingPeriodStartDate(), _security.getIborIndex(), _security.getNotional(),
        _security.getFixingPeriodAccrualFactor(), _security.getName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _security.hashCode();
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
    GeneratorInterestRateFutures other = (GeneratorInterestRateFutures) obj;
    if (!ObjectUtils.equals(_security, other._security)) {
      return false;
    }
    return true;
  }

}
