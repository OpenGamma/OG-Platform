/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskfactor;

import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;

/**
 * Calculates the value (or dollar) greek of an option given market data used to calculate the greek and the greek. 
 */
public interface ValueGreekCalculator {

  /**
   * @param derivative The option, not null
   * @param market The market data used to calculate the greek, not null
   * @param greek The greek
   * @return The value greek
   */
  double valueGreek(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final double greek);

}
