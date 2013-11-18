/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.irs;

/**
 * Rate averaging method, used especially in fed fund swap legs.
 */
public enum RateAveragingMethod  {

  /**
   * Weighted.
   */
  WEIGHTED,

  /**
   * Unweighted.
   */
  UNWEIGHTED

}
