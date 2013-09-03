/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A present value curve sensitivity calculator that convert a multi-currency rate sensitivity into a given currency.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueCurveSensitivityConvertedCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<YieldCurveBundle, InterestRateCurveSensitivity> {

  /**
   * The currency in which the present value should be converted.
   */
  private final Currency _currency;
  /**
   * The present value curve sensitivity calculator (with MultiCurrencyAmount output)
   */
  private final InstrumentDerivativeVisitorAdapter<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> _pvcsCalculator;

  /**
   * Constructor.
   * @param currency The currency in which the present value should be converted.
   * @param pvcsCalculator The present value curve sensitivity calculator (with MultipleCurrencyInterestRateCurveSensitivity output).
   */
  public PresentValueCurveSensitivityConvertedCalculator(final Currency currency,
      final InstrumentDerivativeVisitorAdapter<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> pvcsCalculator) {
    _currency = currency;
    _pvcsCalculator = pvcsCalculator;
  }

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(derivative, "derivative");
    return derivative.accept(_pvcsCalculator, curves).converted(_currency, curves.getFxRates()).getSensitivity(_currency);
  }

  @Override
  public InterestRateCurveSensitivity visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need curves data");
  }

}
