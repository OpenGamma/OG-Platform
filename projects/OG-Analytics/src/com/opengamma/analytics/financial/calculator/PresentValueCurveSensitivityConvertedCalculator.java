/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;

/**
 * A present value curve sensitivity calculator that convert a multi-currency rate sensitivity into a given currency.
 */
public class PresentValueCurveSensitivityConvertedCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> {

  /**
   * The currency in which the present value should be converted.
   */
  private final Currency _currency;
  /**
   * The present value curve sensitivity calculator (with MultiCurrencyAmount output)
   */
  private final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> _pvcsCalculator;

  /**
   * Constructor.
   * @param currency The currency in which the present value should be converted.
   * @param pvcsCalculator The present value curve sensitivity calculator (with MultipleCurrencyInterestRateCurveSensitivity output).
   */
  public PresentValueCurveSensitivityConvertedCalculator(Currency currency, AbstractInstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> pvcsCalculator) {
    _currency = currency;
    _pvcsCalculator = pvcsCalculator;
  }

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return _pvcsCalculator.visit(derivative, curves).convert(_currency, curves.getFxRates());
  }

}
