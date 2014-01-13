/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.FuturesSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;

/**
 * Interface to generic futures security pricing method.
 */
public interface FuturesSecurityMethod {
  
  /**
   * Returns the index used in the futures margining from a quoted price. Correspond to the unit amount multiplied by the price.
   * @param futures The futures security.
   * @param quotedPrice The quoted price in the futures convention.
   * @return The figure used in margining.
   */
  double marginIndex(final FuturesSecurity futures, final double quotedPrice);
  
  /**
   * Computes the quoted price of a futures from a multicurve provider.
   * @param futures The futures security.
   * @param multicurve The multicurve provider.
   * @return The price.
   */
  double price(final FuturesSecurity futures, final ParameterProviderInterface multicurve);

}
