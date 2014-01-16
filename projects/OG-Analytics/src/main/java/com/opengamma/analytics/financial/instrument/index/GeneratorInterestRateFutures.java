/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Class used to store future description and generate instruments.
 */
public class GeneratorInterestRateFutures extends GeneratorInstrument<GeneratorAttribute> {

  /**
   * The underlying STIR futures security.
   */
  private final InterestRateFutureSecurityDefinition _security;

  /**
   * Constructor.
   * @param name The generator name.
   * @param security The underlying STIR futures security.
   */
  public GeneratorInterestRateFutures(final String name, final InterestRateFutureSecurityDefinition security) {
    super(name);
    ArgumentChecker.notNull(security, "STIR futures security");
    _security = security;
  }

  /**
   * Gets the STIR futures security.
   * @return The futures.
   */
  public InterestRateFutureSecurityDefinition getFutures() {
    return _security;
  }

  /**
   * {@inheritDoc}
   * The quantity is modified to be in line with the required notional.
   */
  @Override
  public InterestRateFutureTransactionDefinition generateInstrument(final ZonedDateTime date, final double marketQuote, final double notional, final GeneratorAttribute attribute) {
    final int quantity = (int) Math.ceil(notional / _security.getNotional());
    return new InterestRateFutureTransactionDefinition(_security, quantity, date, marketQuote);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _security.hashCode();
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
    final GeneratorInterestRateFutures other = (GeneratorInterestRateFutures) obj;
    if (!ObjectUtils.equals(_security, other._security)) {
      return false;
    }
    return true;
  }

}
