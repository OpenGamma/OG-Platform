package com.opengamma.financial.security.swap;

/**
 * Visitor for the SwapLeg subclasses.
 * 
 * @param <T> visitor method return type
 */
public interface SwapLegVisitor<T> {

  T visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg);

  T visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg);

}
