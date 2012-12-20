/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.availability;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * Exception used to indicate that a requirement is recognized by the market data provider, is not available, and must not be satisfied by any other means.
 */
@PublicSPI
public class MarketDataNotSatisfiableException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final ValueRequirement _requirement;

  public MarketDataNotSatisfiableException(final ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    _requirement = requirement;
  }

  public ValueRequirement getRequirement() {
    return _requirement;
  }

  public String getMessage() {
    return getRequirement().toString();
  }

}
