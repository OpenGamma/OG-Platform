/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.method;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Generic calibration engine for interest rate instruments.
 * @param <DATA_TYPE> The type of the data for the base calculator.
 */
public abstract class CalibrationEngine<DATA_TYPE extends ParameterProviderInterface> {

  /**
   * The calibration basket.
   */
  private final List<InstrumentDerivative> _basket;
  /**
   * The calculator used to compute calibrating prices.
   */
  private final List<InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount>> _calculators;
  /**
   * The calibrating prices.
   */
  private final List<Double> _calibrationPrice;
  /**
   * The exchange rate to convert the present values in a unique currency.
   */
  private final FXMatrix _fxMatrix;
  /**
   * The unique currency in which all present values are converted.
   */
  private final Currency _ccy;

  //TODO: Should there exists also a way to add an instrument with its direct price (not the calculator)?

  /**
   * Constructor of the calibration engine. The basket and calculator list are empty.
   * @param fxMatrix The exchange rate to convert the present values in a unique currency.
   * @param ccy The unique currency in which all present values are converted.
   */
  public CalibrationEngine(final FXMatrix fxMatrix, final Currency ccy) {
    _basket = new ArrayList<InstrumentDerivative>();
    _calculators = new ArrayList<InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount>>();
    _calibrationPrice = new ArrayList<Double>();
    _fxMatrix = fxMatrix;
    _ccy = ccy;
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param calculator The calculator.
   */
  public void addInstrument(final InstrumentDerivative instrument, final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount> calculator) {
    _basket.add(instrument);
    _calculators.add(calculator);
    _calibrationPrice.add(0.0);
  }

  /**
   * Add an array of instruments to the basket and the associated calculator. The same method is used for all the instruments.
   * @param instrument An interest rate derivative array.
   * @param calculator The calculator.
   */
  public void addInstrument(final InstrumentDerivative[] instrument, final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount> calculator) {
    Validate.notNull(instrument, "Instrument");
    for (int loopins = 0; loopins < instrument.length; loopins++) {
      addInstrument(instrument[loopins], calculator);
    }
  }

  /**
   * Computes the price of the instrument in the calibration basket using the engine calculator and the yield curves.
   * @param data Data.
   */
  public void computeCalibrationPrice(DATA_TYPE data) {
    int nbInstrument = _basket.size();
    for (int loopins = 0; loopins < nbInstrument; loopins++) {
      MultipleCurrencyAmount pvMCA = _basket.get(loopins).accept(_calculators.get(loopins), data);
      double pv = _fxMatrix.convert(pvMCA, _ccy).getAmount();
      _calibrationPrice.set(loopins, pv);
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
   * Gets the method list.
   * @return the method.
   */
  public List<InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount>> getMethod() {
    return _calculators;
  }

  /**
   * Gets the _calibrationPrice field.
   * @return the _calibrationPrice
   */
  public List<Double> getCalibrationPrice() {
    return _calibrationPrice;
  }

}
