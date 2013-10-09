/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

/**
 * Visitor for the {@code SwapLeg} subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface SwapLegVisitor<T> {

  /**
   * Visits a fixed interest rate leg.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg);

  /**
   * Visits a floating interest rate leg.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg);

  /**
   * Visits a floating interest rate leg with spread.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitFloatingSpreadIRLeg(FloatingSpreadIRLeg swapLeg);

  /**
   * Visits a floating interest rate leg with gearing.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitFloatingGearingIRLeg(FloatingGearingIRLeg swapLeg);

  /**
   * Visits a fixed variance swap leg.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitFixedVarianceSwapLeg(FixedVarianceSwapLeg swapLeg);

  /**
   * Visits a floating variance swap leg.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitFloatingVarianceSwapLeg(FloatingVarianceSwapLeg swapLeg);

  /**
   * Visits a fixed inflation swap leg.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitFixedInflationSwapLeg(FixedInflationSwapLeg swapLeg);

  /**
   * Visits a index-linked inflation swap leg.
   * @param swapLeg The swap leg, not null
   * @return The return value
   */
  T visitInflationIndexSwapLeg(InflationIndexSwapLeg swapLeg);
}
