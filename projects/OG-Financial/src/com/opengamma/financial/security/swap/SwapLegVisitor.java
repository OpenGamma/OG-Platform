/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

  T visitFixedInterestRateLeg(FixedInterestRateLeg swapLeg);

  T visitFloatingInterestRateLeg(FloatingInterestRateLeg swapLeg);

}
