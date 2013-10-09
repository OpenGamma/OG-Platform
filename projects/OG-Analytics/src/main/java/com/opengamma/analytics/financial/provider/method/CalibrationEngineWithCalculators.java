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
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Generic calibration engine for interest rate instruments. This calibrate a model using calculators.
 * @param <DATA_TYPE> The type of the data for the base calculator
 */
public abstract class CalibrationEngineWithCalculators<DATA_TYPE extends ParameterProviderInterface> extends CalibrationEngineWithPrices<DATA_TYPE> {

  /**
  * The calculator used to compute calibrating prices.
  */
  private final List<InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount>> _calculators;

  /**
   * The exchange rate to convert the present values in a unique currency.
   */
  private final FXMatrix _fxMatrix;
  /**
   * The unique currency in which all present values are converted.
   */
  private final Currency _ccy;

  /**
   * Constructor of the calibration engine. The basket and calculator list are empty.
   * @param fxMatrix The exchange rate to convert the present values in a unique currency.
   * @param ccy The unique currency in which all present values are converted.
   */
  public CalibrationEngineWithCalculators(final FXMatrix fxMatrix, final Currency ccy) {
    super(fxMatrix, ccy);
    _calculators = new ArrayList<>();
    _fxMatrix = fxMatrix;
    _ccy = ccy;
  }

  /**
   * Computes the price of the instrument in the calibration basket using the engine calculator and the yield curves.
   * @param data Data.
   */
  public void computeCalibrationPrice(final DATA_TYPE data) {
    final int nbInstrument = getBasket().size();
    for (int loopins = 0; loopins < nbInstrument; loopins++) {
      final MultipleCurrencyAmount pvMCA = getBasket().get(loopins).accept(_calculators.get(loopins), data);
      final double pv = _fxMatrix.convert(pvMCA, _ccy).getAmount();
      getCalibrationPrices().set(loopins, pv);
    }
  }

  /**
   * Add an instrument to the basket and the associated calculator.
   * @param instrument An interest rate derivative.
   * @param calculator The calculator.
   */
  public void addInstrument(final InstrumentDerivative instrument, final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount> calculator) {
    getBasket().add(instrument);
    _calculators.add(calculator);
    getCalibrationPrices().add(0.0);
  }

  /**
   * Add an array of instruments to the basket and the associated calculator. The same method is used for all the instruments.
   * @param instrument An interest rate derivative array.
   * @param calculator The calculator.
   */
  public void addInstrument(final InstrumentDerivative[] instrument, final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount> calculator) {
    Validate.notNull(instrument, "Instrument");
    for (final InstrumentDerivative element : instrument) {
      addInstrument(element, calculator);
    }
  }

  /**
   * Gets the method list.
   * @return the method.
   */
  public List<InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyAmount>> getMethod() {
    return _calculators;
  }

}
