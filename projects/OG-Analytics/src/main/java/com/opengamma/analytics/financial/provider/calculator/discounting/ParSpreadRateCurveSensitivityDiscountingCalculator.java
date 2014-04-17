/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorDelegate;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.FederalFundsFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;

/**
 * Compute the spread to be added to the rate-like quote of the instrument for which the present value of the instrument is zero.
 * The notion of "rate" will depend of each instrument. The "market quote" will be used for most instruments. 
 * The exceptions are: STIR futures, Fed Funds futres,
 */
public final class ParSpreadRateCurveSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorDelegate<MulticurveProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadRateCurveSensitivityDiscountingCalculator INSTANCE = new ParSpreadRateCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadRateCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ParSpreadRateCurveSensitivityDiscountingCalculator() {
    super(ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance());
  }

  /**
   * The methods and calculators.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_STIR_FUT = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final FederalFundsFutureSecurityDiscountingMethod METHOD_FED_FUNDS = FederalFundsFutureSecurityDiscountingMethod.getInstance();

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return METHOD_STIR_FUT.priceCurveSensitivity(futures.getUnderlyingSecurity(), multicurves).multipliedBy(-1);
  }

  @Override
  public MulticurveSensitivity visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final MulticurveProviderInterface multicurves) {
    return METHOD_FED_FUNDS.priceCurveSensitivity(future.getUnderlyingSecurity(), multicurves).multipliedBy(-1);
  }

}
