/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesOptionMarginSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Method for the pricing of bond future options with margin process.
 * @param <DATA_TYPE> Data type. Extends ParameterProviderInterface.
 */
public abstract class BondFutureOptionMarginSecurityGenericMethod<DATA_TYPE extends ParameterProviderInterface> {

  /**
   * Computes the option security price. The future price is computed without convexity adjustment.
   * @param security The bond option security.
   * @param data The ParameterProviderInterface with the relevant data.
   * @return The security price.
   */
  public abstract double price(final BondFuturesOptionMarginSecurity security, final DATA_TYPE data);

  /**
   * Computes the option security price curve sensitivity. The future price is computed without convexity adjustment.
   * @param security The bond option security.
   * @param data The ParameterProviderInterface with the relevant data.
   * @return The security price curve sensitivity.
   */
  public abstract MulticurveSensitivity priceCurveSensitivity(final BondFuturesOptionMarginSecurity security,
      final DATA_TYPE data);

}
