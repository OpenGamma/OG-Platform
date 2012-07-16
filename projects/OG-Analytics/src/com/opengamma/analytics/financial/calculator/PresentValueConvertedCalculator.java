/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * A present value calculator that convert a multi-currency present value into a given currency.
 */
public class PresentValueConvertedCalculator extends AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Double> {

  /**
   * The currency in which the present value should be converted.
   */
  private final Currency _currency;
  /**
   * The present value calculator (with MultiCurrencyAmount output)
   */
  private final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyAmount> _pvCalculator;

  /**
   * Constructor.
   * @param currency The currency in which the present value should be converted.
   * @param pvCalculator The present value calculator (with MultiCurrencyAmount output).
   */
  public PresentValueConvertedCalculator(Currency currency, AbstractInstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyAmount> pvCalculator) {
    super();
    _currency = currency;
    _pvCalculator = pvCalculator;
  }

  @Override
  public Double visit(final InstrumentDerivative derivative, final YieldCurveBundle curves) {
    Validate.notNull(curves);
    Validate.notNull(derivative);
    return curves.convert(_pvCalculator.visit(derivative, curves), _currency).getAmount();
  }

}
