/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureOptionMarginSecurity;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Method for the pricing of interest rate future options with margin process.
 * @param <DATA_TYPE> Data type. Extends ParameterProviderInterface.
 */
public abstract class InterestRateFutureOptionMarginSecurityGenericMethod<DATA_TYPE extends ParameterProviderInterface> {

  /**
   * Computes the option security price. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param data The ParameterProviderInterface with the relevant data.
   * @return The security price.
   */
  public abstract double price(final InterestRateFutureOptionMarginSecurity security, final DATA_TYPE data);

  /**
   * Computes the option security price curve sensitivity. The future price is computed without convexity adjustment.
   * @param security The future option security.
   * @param data The ParameterProviderInterface with the relevant data.
   * @return The security price curve sensitivity.
   */
  public abstract MulticurveSensitivity priceCurveSensitivity(final InterestRateFutureOptionMarginSecurity security, final DATA_TYPE data);

}
