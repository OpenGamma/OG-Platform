/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * The restructuring clause to apply in the event of a credit event deemed to be a restructuring
 */
public enum RestructuringClause {
  /**
   * Old-Restructuring
   */
  OLDRE,
  /**
   * Modified Restructuring
   */
  MODRE,
  /**
   * Modified-Modified Restructuring
   */
  MODMODRE,
  /**
   * No-Restructuring
   */
  NORE,
  /**
   * Full-Restructuring (MarkIt notation)
   */
  CR,
  /**
   * No-Restructuring (MarkIt notation)
   */
  XR;
}
