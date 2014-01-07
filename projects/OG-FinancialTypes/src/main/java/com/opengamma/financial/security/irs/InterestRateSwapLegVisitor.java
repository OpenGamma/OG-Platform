/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

/**
 * Visitor for the {@code SwapLeg} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface InterestRateSwapLegVisitor<T> {

  /**
   * Visits a fixed interest rate leg.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitFixedInterestRateSwapLeg(FixedInterestRateSwapLeg swapLeg);

  /**
   * Visits a floating interest rate leg.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitFloatingInterestRateSwapLeg(FloatingInterestRateSwapLeg swapLeg);

}
