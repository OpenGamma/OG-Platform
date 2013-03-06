/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "spread" will depend of each instrument.
 */
public final class ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<InflationProviderInterface, InflationSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator INSTANCE = new ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVISC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVSC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  //-----     Inflation Swaps     -----

  /**
  * For swaps the ParSpread is the spread to be added on each coupon of the first leg to obtain a present value of zero.
  * It is computed as the opposite of the present value of the swap in currency of the first leg divided by the present value of a basis point
  * of the first leg (as computed by the PresentValueBasisPointCalculator).
  * @param swap The swap.
  * @param inflation The inflation curves and multi-curves provider.
  * @return The par spread sensitivity.
  */
  @Override
  public InflationSensitivity visitSwap(final Swap<?, ?> swap, final InflationProviderInterface inflation) {
    ArgumentChecker.notNull(inflation, "Market");
    ArgumentChecker.notNull(swap, "Swap");
    ArgumentChecker.isTrue(swap.getFirstLeg().getNumberOfPayments() == 1, "the first leg of an inflation ZC swap should be a fixed compounded leg");
    ArgumentChecker.isTrue(swap.getFirstLeg().getNthPayment(0) instanceof CouponFixedCompounding, "the first leg of an inflation ZC swap should be a fixed compounded leg");
    final InflationSensitivity pvcis = swap.getSecondLeg().accept(PVISC, inflation).getSensitivity(swap.getSecondLeg().getCurrency());
    final MulticurveSensitivity pvcs = swap.getFirstLeg().accept(PVSC, inflation.getMulticurveProvider()).getSensitivity(swap.getFirstLeg().getCurrency());

    CouponFixedCompounding cpn = (CouponFixedCompounding) swap.getFirstLeg().getNthPayment(0);
    final double pvInflationLeg = swap.getSecondLeg().accept(PVIC, inflation).getAmount(swap.getSecondLeg().getCurrency());
    final double discountFactor = inflation.getDiscountFactor(swap.getFirstLeg().getCurrency(), cpn.getPaymentTime());
    final double tenor = cpn.getPaymentAccrualFactors().length;

    final double intermediateVariable = (1 / tenor) * Math.pow(pvInflationLeg / discountFactor + 1, 1 / tenor - 1) / (discountFactor);
    final MulticurveSensitivity modifiedpvcs = pvcs.multipliedBy(-pvInflationLeg * intermediateVariable / discountFactor);
    final InflationSensitivity modifiedpvcis = pvcis.multipliedBy(intermediateVariable);

    return InflationSensitivity.of(modifiedpvcs.plus(modifiedpvcis.getMulticurveSensitivity()), modifiedpvcis.getPriceCurveSensitivities());

  }
}
