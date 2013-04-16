/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.Period;

/**
 * Class with the attributed required to generate an interest rate (IR) instrument from the market quotes.
 * The attributes are composed of one or two tenors (the start period and the end period).
 */
public class GeneratorAttributeIR extends GeneratorAttribute {

  /**
   * The start period. 
   */
  private final Period _startPeriod;
  /**
   * The end period. 
   */
  private final Period _endPeriod;

  /**
   * Constructor.
   * @param startPeriod The start period.
   * @param endPeriod The end period.
   */
  public GeneratorAttributeIR(final Period startPeriod, final Period endPeriod) {
    super();
    _startPeriod = startPeriod;
    _endPeriod = endPeriod;
  }

  /**
   * Constructor. By default the start period is set to ZERO.
   * @param endPeriod The end period.
   */
  public GeneratorAttributeIR(final Period endPeriod) {
    _startPeriod = Period.ZERO;
    _endPeriod = endPeriod;
  }

  /**
   * Gets the startPeriod field.
   * @return the startPeriod
   */
  public Period getStartPeriod() {
    return _startPeriod;
  }

  /**
   * Gets the endPeriod field.
   * @return the endPeriod
   */
  public Period getEndPeriod() {
    return _endPeriod;
  }

}
