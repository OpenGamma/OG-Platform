/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod;
import com.opengamma.analytics.financial.interestrate.inflation.provider.CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.priceindexmarketmodel.BlackSmileCapInflationYearOnYearProviderAdapter;
import com.opengamma.analytics.financial.provider.description.inflation.BlackSmileCapInflationYearOnYearProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 *  Calculator of the present value as a multiple currency amount for inflation year on year cap floor without convexity adjustment.
 */
public final class PresentValueBlackSmileInflationYearOnYearCalculator extends InstrumentDerivativeVisitorDelegate<BlackSmileCapInflationYearOnYearProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueBlackSmileInflationYearOnYearCalculator INSTANCE = new PresentValueBlackSmileInflationYearOnYearCalculator();

  /**
   * Constructor.
   */
  private PresentValueBlackSmileInflationYearOnYearCalculator() {
    super(new BlackSmileCapInflationYearOnYearProviderAdapter<>(PresentValueDiscountingCalculator.getInstance()));
  }

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueBlackSmileInflationYearOnYearCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Pricing methods.
   */

  private static final CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod METHOD_CAPFLOOR_INTERPOLATION = CapFloorInflationYearOnYearInterpolationBlackNormalSmileMethod.getInstance();
  private static final CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY = CapFloorInflationYearOnYearMonthlyBlackNormalSmileMethod.getInstance();

  //-----     Caplet/Floorlet year on year     -----

  @Override
  public MultipleCurrencyAmount visitCapFloorInflationYearOnYearInterpolation(final CapFloorInflationYearOnYearInterpolation cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_INTERPOLATION.presentValue(cap, black);
  }

  @Override
  public MultipleCurrencyAmount visitCapFloorInflationYearOnYearMonthly(final CapFloorInflationYearOnYearMonthly cap, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    return METHOD_CAPFLOOR_YEAR_ON_YEAR_MONTHLY.presentValue(cap, black);
  }

  //-----     Cap/Floor year on year     -----

  @Override
  public MultipleCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final BlackSmileCapInflationYearOnYearProviderInterface black) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(black, "multicurve");
    MultipleCurrencyAmount pv = annuity.getNthPayment(0).accept(this, black);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pv = pv.plus(annuity.getNthPayment(loopp).accept(this, black));
    }
    return pv;
  }
}
