/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.sabrcap;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborSABRCapMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.SABRCapProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public final class PresentValueSABRCapCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<SABRCapProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueSABRCapCalculator INSTANCE = new PresentValueSABRCapCalculator();

  /**
   * Constructor.
   */
  private PresentValueSABRCapCalculator() {
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueSABRCapCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */
  private static final CapFloorIborSABRCapMethod METHOD_CAP_IBOR = CapFloorIborSABRCapMethod.getInstance();

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative, final SABRCapProviderInterface sabr) {
    return derivative.accept(this, sabr);
  }

  // -----     Payment/Coupon     ------

  @Override
  public MultipleCurrencyAmount visitCapFloorIbor(final CapFloorIbor cap, final SABRCapProviderInterface sabr) {
    return METHOD_CAP_IBOR.presentValue(cap, sabr);
  }

  @Override
  public MultipleCurrencyAmount visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException();
  }

}
