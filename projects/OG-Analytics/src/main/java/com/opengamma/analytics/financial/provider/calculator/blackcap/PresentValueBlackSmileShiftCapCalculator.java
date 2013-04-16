/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.blackcap;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.provider.CapFloorIborBlackSmileShiftMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSmileShiftCapProviderInterface;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount for Black smile cap/floor provider.
 */
public final class PresentValueBlackSmileShiftCapCalculator extends InstrumentDerivativeVisitorAdapter<BlackSmileShiftCapProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackSmileShiftCapCalculator INSTANCE = new PresentValueBlackSmileShiftCapCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackSmileShiftCapCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueBlackSmileShiftCapCalculator() {
  }

  /**
   * Pricing methods.
   */
  private static final CapFloorIborBlackSmileShiftMethod METHOD_CAP = CapFloorIborBlackSmileShiftMethod.getInstance();

  // -----     Payments     ------

  @Override
  public MultipleCurrencyAmount visitCapFloorIbor(final CapFloorIbor cap, final BlackSmileShiftCapProviderInterface black) {
    return METHOD_CAP.presentValue(cap, black);
  }

}
