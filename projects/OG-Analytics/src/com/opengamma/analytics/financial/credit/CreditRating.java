/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * Enumerate the possible credit rating states for a reference entity
 */
public enum CreditRating {
  /**
   * 
   */
  AAA,
  /**
   * 
   */
  AA,
  /**
   * 
   */
  A,
  /**
   * 
   */
  BBB,
  /**
   * 
   */
  BB,
  /**
   * 
   */
  B,
  /**
   * 
   */
  CCC,
  /**
   * 
   */
  CC,
  /**
   * 
   */
  C,
  /**
   * Reference entity has already defaulted
   */
  DEFAULT,
  /**
   * 
   */
  NR;

  // TODO: Extend this list to include Moodys, S&P and Fitch rating classifications
}
