/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit;

/**
 * The method for generating the cashflow schedule for a premium payment leg
 */
public enum ScheduleGenerationMethod {
  /**
   * Step forward in time from the valuation date towards the maturity date
   */
  FORWARD, 
  /**
   * Step back from the maturity date towards the valuation date (applicable to standard CDS contracts)
   */
  BACKWARD;
}
