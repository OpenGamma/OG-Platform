/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.bumpers;

/**
 * Enumerate the types of spread bumps that can be applied to a term structure of credit spreads
 *@deprecated this will be deleted 
 */
@Deprecated
public enum SpreadBumpType {
  /**
   * Same as ADDITIVE_BUCKETED
   */
  ADDITIVE,
  /**
   * Same as MULTIPLICATIVE_BUCKETED
   */
  MULTIPLICATIVE,
  /**
   * s(t,T) -> s(t,T) + ds i.e. each point on the curve is shifted by an equal amount ds
   */
  ADDITIVE_PARALLEL,
  /**
   * s(t,T) -> s(t,T) + ds(T) i.e. each point on the curve is shifted by a seperate amount (e.g. bump one point at a time for bucketed risk)
   */
  ADDITIVE_BUCKETED,
  /**
   * s(t,T) -> [1 + ds] x s(t,T)
   */
  MULTIPLICATIVE_PARALLEL,
  /**
   * s(t,T) -> [1 + ds(T)] x s(t,T)
   */
  MULTIPLICATIVE_BUCKETED;
}
