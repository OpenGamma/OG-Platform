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
 * 2014 ISDA definitions - see http://www2.isda.org/attachment/NjU5Nw==/ISDA%202014%20Credit%20Definitions%20FAQ%20v12_Clean.pdf
 */
public enum RestructuringClause {
  /**
   * Restructuring - ISDA 2014 definition (Markit notation)
   */
  CR14,
  /**
   * Modified-Modified Restructuring - ISDA 2014 definition (Markit notation)
   */
  MM14,
  /**
   * Modified Restructuring - ISDA 2014 definition (Markit notation)
   */
  MR14,
  /**
   * No-Restructuring - ISDA 2014 definition (Markit notation)
   */
  XR14,
  /**
   * Restructuring - ISDA 2003 definition (Markit notation)
   */
  CR,
  /**
   * Modified-Modified Restructuring - ISDA 2003 definition (Markit notation)
   */
  MM,
  /**
   * Modified Restructuring - ISDA 2003 definition (Markit notation)
   */
  MR,
  /**
   * No-Restructuring - ISDA 2003 definition (Markit notation)
   */
  XR,
  /**
   * No restructuring. (Not used by Markit).
   */
  NONE
}
