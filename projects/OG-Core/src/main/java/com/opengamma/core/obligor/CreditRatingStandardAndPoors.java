/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.obligor;

/**
 * Enumerate the possible credit rating states for a reference entity (Standard & Poors rating classifications)
 */
public enum CreditRatingStandardAndPoors {
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
   * No rating
   */
  NR;

  // TODO : Add the correct rating states

}
