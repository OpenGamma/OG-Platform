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

/**
 * Compute the spread to be added to the rate-like quote of the instrument for which the present value of the instrument is zero.
 * The notion of "rate" will depend of each instrument. The "market quote" will be used for most instruments. 
 * The exceptions are: STIR futures, Fed Funds futres,
 */
public final class ParSpreadRateDiscountingCalculator extends InstrumentDerivativeVisitorDelegate<MulticurveProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadRateDiscountingCalculator INSTANCE = new ParSpreadRateDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadRateDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Private constructor.
   */
  private ParSpreadRateDiscountingCalculator() {
    super(ParSpreadMarketQuoteDiscountingCalculator.getInstance());
  }

  /**
   * The methods and calculators.
   */
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_STIR_FUT = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final FederalFundsFutureSecurityDiscountingMethod METHOD_FED_FUNDS = FederalFundsFutureSecurityDiscountingMethod.getInstance();

  //     -----     Futures     -----

  @Override
  /**
   * @return The futures rate spread for the rate = 1-price.
   */
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return METHOD_STIR_FUT.parRate(futures.getUnderlyingFuture(), multicurves) - (1.0d - futures.getReferencePrice());
  }

  @Override
  /**
   * @return The futures rate spread for the rate = 1-price.
   */
  public Double visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final MulticurveProviderInterface multicurve) {
    return -(METHOD_FED_FUNDS.price(future.getUnderlyingFuture(), multicurve) - future.getReferencePrice());
  }

}
