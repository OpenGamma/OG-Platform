/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * The restructuring clause to apply in the event of a credit event deemed to be a restructuring
 * 
 * Reference: https://www.markit.com/news/XMLGuide.pdf, 
 *            "XML User Guide - Markit Data",
 *            Version 10.3.8, November 2010.
 */
public enum RestructuringClause {
  /**
   * With Restructuring (Markit notation)
   */
  CR,
  /**
   * Modified-Modified Restructuring (Markit notation)
   */
  MM,
  /**
   * Modified Restructuring (Markit notation)
   */
  MR,
  /**
   * No-Restructuring (Markit notation)
   */
  XR,
  /**
   * No restructuring. (Not used by Markit).
   */
  NONE
}
