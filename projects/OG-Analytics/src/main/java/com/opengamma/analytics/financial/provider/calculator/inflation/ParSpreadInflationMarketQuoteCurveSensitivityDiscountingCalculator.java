/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueMarketQuoteSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "spread" will depend of each instrument.
 */
public final class ParSpreadInflationMarketQuoteCurveSensitivityDiscountingCalculator 
  extends InstrumentDerivativeVisitorAdapter<ParameterInflationProviderInterface, InflationSensitivity> {

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
   * The methods and calculators (specific for inflation).
   */
  private static final PresentValueDiscountingInflationCalculator PVIC = PresentValueDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingInflationCalculator PVISC = PresentValueCurveSensitivityDiscountingInflationCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVSC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingCalculator PVMC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSMC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSMC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator PVMQSCSMC = PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator.getInstance();
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingMethod METHOD_FRA = ForwardRateAgreementDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FOREX_SWAP = ForexSwapDiscountingMethod.getInstance();

  //-----    Swaps     -----

  /**
  * For swaps the ParSpread is the spread to be added on each coupon of the first leg to obtain a present value of zero.
  * It is computed as the opposite of the present value of the swap in currency of the first leg divided by the present value of a basis point
  * of the first leg (as computed by the PresentValueBasisPointCalculator).
  * @param swap The swap.
  * @param inflation The inflation curves and multi-curves provider.
  * @return The par spread sensitivity.
  */
  @Override
  public InflationSensitivity visitSwap(final Swap<?, ?> swap, final ParameterInflationProviderInterface inflation) {
    ArgumentChecker.notNull(inflation, "Market");
    ArgumentChecker.notNull(swap, "Swap");
    if (swap.getFirstLeg().getNumberOfPayments() == 1 && swap.getFirstLeg().getNthPayment(0) instanceof CouponFixedCompounding) {
      // Implementation note: check if the swap is an inflation swap.

      final CouponFixedCompounding cpn = (CouponFixedCompounding) swap.getFirstLeg().getNthPayment(0);
      final double pvInflationLeg = swap.getSecondLeg().accept(PVIC, inflation).getAmount(swap.getSecondLeg().getCurrency());
      final double discountFactor = inflation.getInflationProvider().getDiscountFactor(swap.getFirstLeg().getCurrency(), cpn.getPaymentTime());
      final double tenor = cpn.getPaymentAccrualFactors().length;
      final double notional = ((CouponInflation) swap.getSecondLeg().getNthPayment(0)).getNotional();
      final double intermediateVariable = (1 / tenor) * Math.pow(pvInflationLeg / discountFactor / notional + 1, 1 / tenor - 1);
      final InflationSensitivity pvcis = swap.getSecondLeg().accept(PVISC, inflation).getSensitivity(swap.getSecondLeg().getCurrency()).multipliedBy(1 / discountFactor / notional);
      final InflationSensitivity modifiedpvcis = pvcis.multipliedBy(intermediateVariable);
      return InflationSensitivity.ofPriceIndex(modifiedpvcis.getPriceCurveSensitivities());
    }
    final Currency ccy1 = swap.getFirstLeg().getCurrency();
    final MultipleCurrencyMulticurveSensitivity pvcs = swap.accept(PVCSMC, inflation.getMulticurveProvider());
    final MulticurveSensitivity pvcs1 = pvcs.converted(ccy1, inflation.getInflationProvider().getFxRates()).getSensitivity(ccy1);
    final MulticurveSensitivity pvmqscs = swap.getFirstLeg().accept(PVMQSCSMC, inflation.getMulticurveProvider());
    final double pvmqs = swap.getFirstLeg().accept(PVMQSMC, inflation.getMulticurveProvider());
    final double pv = inflation.getInflationProvider().getFxRates().convert(swap.accept(PVMC, inflation.getMulticurveProvider()), ccy1).getAmount();
    // Implementation note: Total pv in currency 1.

    final Map<String, List<DoublesPair>> sensitivityPriceCurve = new HashMap<>();
    return InflationSensitivity.of(pvcs1.multipliedBy(-1.0 / pvmqs).plus(pvmqscs.multipliedBy(pv / (pvmqs * pvmqs))), sensitivityPriceCurve);
  }

  @Override
  public InflationSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final ParameterInflationProviderInterface inflation) {
    return visitSwap(swap, inflation);
  }

  //     -----     Deposit     -----

  @Override
  public InflationSensitivity visitCash(final Cash deposit, final ParameterInflationProviderInterface inflation) {
    final Map<String, List<DoublesPair>> sensitivityPriceCurve = new HashMap<>();
    return InflationSensitivity.of(METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, inflation.getMulticurveProvider()), sensitivityPriceCurve);
  }

  @Override
  public InflationSensitivity visitDepositIbor(final DepositIbor deposit, final ParameterInflationProviderInterface inflation) {
    final Map<String, List<DoublesPair>> sensitivityPriceCurve = new HashMap<>();
    return InflationSensitivity.of(METHOD_DEPOSIT_IBOR.parSpreadCurveSensitivity(deposit, inflation.getMulticurveProvider()), sensitivityPriceCurve);
  }

  // -----     Payment/Coupon     ------

  @Override
  public InflationSensitivity visitForwardRateAgreement(final ForwardRateAgreement fra, final ParameterInflationProviderInterface inflation) {
    final Map<String, List<DoublesPair>> sensitivityPriceCurve = new HashMap<>();
    return InflationSensitivity.of(METHOD_FRA.parSpreadCurveSensitivity(fra, inflation.getMulticurveProvider()), sensitivityPriceCurve);
  }

  //     -----     Forex     -----

  /**
   * The par spread is the spread that should be added to the forex forward points to have a zero value.
   * @param fx The forex swap.
   * @param inflation The inflation provider.
   * @return The spread.
   */
  @Override
  public InflationSensitivity visitForexSwap(final ForexSwap fx, final ParameterInflationProviderInterface inflation) {
    final Map<String, List<DoublesPair>> sensitivityPriceCurve = new HashMap<>();
    return InflationSensitivity.of(METHOD_FOREX_SWAP.parSpreadCurveSensitivity(fx, inflation.getMulticurveProvider()), sensitivityPriceCurve);
  }

}
