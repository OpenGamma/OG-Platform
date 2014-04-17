/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

/**
 * Enumerate the types of PV that can be returned (usually clean or dirty)
 */
public enum PriceType {

  /**
   * Clean price
   */
  CLEAN,
  /**
   * Dirty price
   */
  DIRTY;
}
