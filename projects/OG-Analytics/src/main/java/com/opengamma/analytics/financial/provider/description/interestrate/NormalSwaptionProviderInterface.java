/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;

/**
 * Provider of normal (Bachelier) smile for swaptions. 
 * The volatility is time to expiration/tenor/strike price/underlying forward swap rate dependent. 
 */
public interface NormalSwaptionProviderInterface extends ParameterProviderInterface {

  /**
   * Create a new copy of the provider
   * @return The bundle
   */
  @Override
  NormalSwaptionProviderInterface copy();

  /**
   * Gets the normal volatility at a given expiry-tenor-strike-forward point.
   * @param expiry The time to expiration.
   * @param tenor The tenor (in year).
   * @param strikeRate The strike rate.
   * @param forwardRate The forward rate of the underlying swap. Used for relative moneyness smile description.
   * @return The normal implied volatility.
   */
  double getVolatility(final double expiry, final double tenor, final double strikeRate, double forwardRate);

  /**
   * Returns the swap generator for which the SABR data is valid, 
   * i.e. the data is calibrated to swaption on vanilla swaps with the description contains in the generator.
   * @return The generator.
   */
  GeneratorSwapFixedIbor getSwapGenerator();

}
