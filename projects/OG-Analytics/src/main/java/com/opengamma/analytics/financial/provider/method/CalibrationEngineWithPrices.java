/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.money.Currency;

/**
 * Generic calibration engine for interest rate instruments. This calibrate a model using prices.
 * @param <DATA_TYPE> The type of the data for the base calculator.
 */
public abstract class CalibrationEngineWithPrices<DATA_TYPE extends ParameterProviderInterface> {
  /**
   * The calibration basket.
   */
  private final List<InstrumentDerivative> _basket;

  /**
   * The prices (usually market quotes or prices calculating with a simpler (than the one we want to calibrate) models) on which we are doing the calibration.
   */
  private final List<Double> _calibrationPrices;

  /**
   * Constructor of the calibration engine. The basket and calculator list are empty.
   * @param fxMatrix The exchange rate to convert the present values in a unique currency.
   * @param ccy The unique currency in which all present values are converted.
   */
  public CalibrationEngineWithPrices(final FXMatrix fxMatrix, final Currency ccy) {
    _basket = new ArrayList<>();
    _calibrationPrices = new ArrayList<>();
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param calibrationPrice The price of the instrument we want to calibrate on.
   */
  public void addInstrument(final InstrumentDerivative instrument, final double calibrationPrice) {
    _basket.add(instrument);
    _calibrationPrices.add(calibrationPrice);
  }

  /**
   * Add an array of instruments to the basket and the associated calculator. The same method is used for all the instruments.
   * instrument and calibration{rices should have the same length, and the same order ie the price of the first instrmwent is the first double of the vector calibrationPrices etc...
   * @param instrument An interest rate derivative array.
   * @param calibrationPrices The prices of the instruments we want to calibrate on.
   */
  public void addInstrument(final InstrumentDerivative[] instrument, final double[] calibrationPrices) {
    Validate.notNull(instrument, "Instrument");
    Validate.isTrue(instrument.length == calibrationPrices.length);
    for (int loopins = 0; loopins < instrument.length; loopins++) {
      addInstrument(instrument[loopins], calibrationPrices[loopins]);
    }
  }

  /**
   * Calibrate the model using a given curve bundle.
   * @param data Data.
   */
  public abstract void calibrate(DATA_TYPE data);

  /**
   * Gets the instrument basket.
   * @return The basket.
   */
  public List<InstrumentDerivative> getBasket() {
    return _basket;
  }

  /**
   * Gets the _calibrationPrice field.
   * @return the _calibrationPrice
   */
  public List<Double> getCalibrationPrices() {
    return _calibrationPrices;
  }

}
