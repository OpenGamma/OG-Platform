/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.bumpers;

/**
 * Enumerate the types of interest rate bumps that can be applied to a term structure of interest rates
 */
@Deprecated
public enum InterestRateBumpType {
  /**
   * Same as ADDITIVE_BUCKETED
   */
  ADDITIVE,
  /**
   * Same as MULTIPLICATIVE_BUCKETED
   */
  MULTIPLICATIVE,
  /**
   * r(t,T) -> r(t,T) + dr i.e. each point on the curve is shifted by an equal amount dr
   */
  ADDITIVE_PARALLEL,
  /**
   * r(t,T) -> r(t,T) + dr(T) i.e. each point on the curve is shifted by a seperate amount (e.g. bump one point at a time for bucketed risk)
   */
  ADDITIVE_BUCKETED,
  /**
   * r(t,T) -> [1 + dr] x r(t,T)
   */
  MULTIPLICATIVE_PARALLEL,
  /**
   * r(t,T) -> [1 + dr(T)] x r(t,T)
   */
  MULTIPLICATIVE_BUCKETED;
}
