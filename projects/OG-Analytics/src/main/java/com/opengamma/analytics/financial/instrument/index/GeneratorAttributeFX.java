/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index;

import org.threeten.bp.Period;

import com.opengamma.analytics.financial.forex.method.FXMatrix;

/**
 * Class with the attributed required to generate an Forex (FX) related instrument from the market quotes.
 * The attributes are composed of one or two tenors (the start period and the end period) and the exchange rate.
 */
public class GeneratorAttributeFX extends GeneratorAttribute {

  /**
   * The start period. 
   */
  private final Period _startPeriod;
  /**
   * The end period. 
   */
  private final Period _endPeriod;
  /**
   * The exchange rate.
   */
  private final FXMatrix _fxMatrix;

  /**
   * Constructor.
   * @param startPeriod The start period.
   * @param endPeriod The end period.
   * @param fxMatrix FX Matrix.
   */
  public GeneratorAttributeFX(final Period startPeriod, final Period endPeriod, final FXMatrix fxMatrix) {
    _startPeriod = startPeriod;
    _endPeriod = endPeriod;
    _fxMatrix = fxMatrix;
  }

  /**
   * Constructor. By default the start period is set to ZERO.
   * @param endPeriod The end period.
   * @param fxMatrix FX Matrix.
   */
  public GeneratorAttributeFX(final Period endPeriod, final FXMatrix fxMatrix) {
    _startPeriod = Period.ZERO;
    _endPeriod = endPeriod;
    _fxMatrix = fxMatrix;
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

  /**
   * Gets the fxRate field.
   * @return the fxRate
   */
  public FXMatrix getFXMatrix() {
    return _fxMatrix;
  }

}
