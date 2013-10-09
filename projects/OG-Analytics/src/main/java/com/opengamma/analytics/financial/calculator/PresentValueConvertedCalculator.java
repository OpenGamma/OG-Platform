/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * A present value calculator that convert a multi-currency present value into a given currency.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public class PresentValueConvertedCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<YieldCurveBundle, Double> {

  /**
   * The currency in which the present value should be converted.
   */
  private final Currency _currency;
  /**
   * The present value calculator (with MultiCurrencyAmount output)
   */
  private final InstrumentDerivativeVisitorAdapter<YieldCurveBundle, MultipleCurrencyAmount> _pvCalculator;

  /**
   * Constructor.
   * @param currency The currency in which the present value should be converted.
   * @param pvCalculator The present value calculator (with MultiCurrencyAmount output).
   */
  public PresentValueConvertedCalculator(final Currency currency, final InstrumentDerivativeVisitorAdapter<YieldCurveBundle, MultipleCurrencyAmount> pvCalculator) {
    _currency = currency;
    _pvCalculator = pvCalculator;
  }

  @Override
  public Double visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    ArgumentChecker.notNull(curves, "curves");
    ArgumentChecker.notNull(derivative, "derivative");
    return curves.getFxRates().convert(derivative.accept(_pvCalculator, curves), _currency).getAmount();
  }

  @Override
  public Double visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need curves data");
  }

}
