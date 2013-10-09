/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackforex;

import com.opengamma.analytics.financial.forex.method.PresentValueForexBlackVolatilityQuoteSensitivityDataBundle;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;

/**
 * Calculates the bucketed vega matrix (first order derivative with respect to the implied volatility) for Forex derivatives in the
 * Black (Garman-Kohlhagen) world. The matrix axes are (time to expiry, ATM / risk reversal / butterfly).
 */
public class QuoteBucketedVegaForexBlackSmileCalculator extends
  InstrumentDerivativeVisitorSameMethodAdapter<BlackForexSmileProviderInterface, PresentValueForexBlackVolatilityQuoteSensitivityDataBundle> {

  /**
   * The unique instance of the calculator.
   */
  private static final QuoteBucketedVegaForexBlackSmileCalculator INSTANCE = new QuoteBucketedVegaForexBlackSmileCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static QuoteBucketedVegaForexBlackSmileCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  QuoteBucketedVegaForexBlackSmileCalculator() {
  }

  /**
   * The bucketed vega calculator.
   */
  private static final BucketedVegaForexBlackSmileCalculator VEGA_CALCULATOR = BucketedVegaForexBlackSmileCalculator.getInstance();

  @Override
  public PresentValueForexBlackVolatilityQuoteSensitivityDataBundle visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Cannot calculated bucketed vega quote matrix without market data");
  }

  @Override
  public PresentValueForexBlackVolatilityQuoteSensitivityDataBundle visit(final InstrumentDerivative derivative, final BlackForexSmileProviderInterface data) {
    return derivative.accept(VEGA_CALCULATOR, data).quoteSensitivity();
  }
}
