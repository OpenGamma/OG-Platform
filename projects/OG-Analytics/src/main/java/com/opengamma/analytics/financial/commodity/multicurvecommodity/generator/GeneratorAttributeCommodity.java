/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.generator;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;

/**
 *  Class with the attributed required to generate a commodity instrument from the market quotes.
 * The attribute is composed of one period.
 */
public class GeneratorAttributeCommodity extends GeneratorAttribute {

  /**
   * The  period. 
   */
  private final Period _period;

  /**
   * Constructor.
   * @param period The start period.
   */
  public GeneratorAttributeCommodity(final Period period) {
    super();
    _period = period;
  }

  /**
   * Gets the period field.
   * @return the period
   */
  public Period getPeriod() {
    return _period;
  }

}
