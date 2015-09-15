/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueMarketQuoteSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * Get the single fixed rate that makes the PV of the instrument zero
 */
public final class ParRateInflationDiscountingCalculator 
    extends InstrumentDerivativeVisitorAdapter<ParameterInflationProviderInterface, Double> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParRateInflationDiscountingCalculator INSTANCE = new ParRateInflationDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParRateInflationDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParRateInflationDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueDiscountingCalculator PVMC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();

  //-----      Swaps     -----

  /**
  * Get the single fixed rate that makes the PV of the instrument zero
  * @param swap The swap.
  * @param inflation The inflation curves and multi-curves provider.
  * @return The par spread.
  */
  @Override
  public Double visitSwap(final Swap<?, ?> swap, final ParameterInflationProviderInterface inflation) {
    ArgumentChecker.notNull(inflation, "Market");
    ArgumentChecker.notNull(swap, "Swap");
    if (swap.getFirstLeg().getNumberOfPayments() == 1 && swap.getFirstLeg().getNthPayment(0) instanceof CouponFixedCompounding) {
      final CouponFixedCompounding cpn = (CouponFixedCompounding) swap.getFirstLeg().getNthPayment(0);
      final double pvInflationLeg = swap.getSecondLeg().accept(PVIC, inflation).getAmount(swap.getSecondLeg().getCurrency());
      final double discountFactor = inflation.getInflationProvider().getDiscountFactor(swap.getFirstLeg().getCurrency(), cpn.getPaymentTime());
      final double tenor = cpn.getPaymentAccrualFactors().length;
      final double notional = ((CouponInflation) swap.getSecondLeg().getNthPayment(0)).getNotional();
      return Math.pow(pvInflationLeg / discountFactor / notional + 1, 1 / tenor) - 1;
    }
    final MulticurveProviderInterface multicurves = inflation.getMulticurveProvider();
    return -multicurves.getFxRates().convert(swap.accept(PVMC, multicurves), swap.getFirstLeg().getCurrency()).getAmount() 
        / swap.getFirstLeg().accept(PVMQSC, multicurves);
  }

  @Override
  public Double visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final ParameterInflationProviderInterface inflation) {
    return visitSwap(swap, inflation);
  }

}
