/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Compute the sensitivity of the spread to the curve; the spread is the number to be added to the market standard quote of the instrument for which the present value of the instrument is zero.
 * The notion of "spread" will depend of each instrument.
 */
public final class ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator INSTANCE = new ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator() {
  }

  /**
   * The methods and calculators.
   */
  private static final PresentValueDiscountingCalculator PVMC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSMC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSMC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator PVMQSCSMC = PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator.getInstance();
  private static final CashDiscountingProviderMethod METHOD_DEPOSIT = CashDiscountingProviderMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingProviderMethod METHOD_FRA = ForwardRateAgreementDiscountingProviderMethod.getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_STIR_FUT = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FOREX_SWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();

  //     -----     Deposit     -----

  @Override
  public MulticurveSensitivity visitCash(final Cash deposit, final MulticurveProviderInterface multicurves) {
    return METHOD_DEPOSIT.parSpreadCurveSensitivity(deposit, multicurves);
  }

  @Override
  public MulticurveSensitivity visitDepositIbor(final DepositIbor deposit, final MulticurveProviderInterface multicurves) {
    return METHOD_DEPOSIT_IBOR.parSpreadCurveSensitivity(deposit, multicurves);
  }

  // -----     Payment/Coupon     ------

  @Override
  public MulticurveSensitivity visitForwardRateAgreement(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurves) {
    return METHOD_FRA.parSpreadCurveSensitivity(fra, multicurves);
  }

  //     -----     Swaps     -----

  @Override
  public MulticurveSensitivity visitSwap(final Swap<?, ?> swap, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(multicurves, "multicurve");
    ArgumentChecker.notNull(swap, "Swap");
    final Currency ccy1 = swap.getFirstLeg().getCurrency();
    final MultipleCurrencyMulticurveSensitivity pvcs = swap.accept(PVCSMC, multicurves);
    final MulticurveSensitivity pvcs1 = pvcs.converted(ccy1, multicurves.getFxRates()).getSensitivity(ccy1);
    final MulticurveSensitivity pvmqscs = swap.getFirstLeg().accept(PVMQSCSMC, multicurves);
    final double pvmqs = swap.getFirstLeg().accept(PVMQSMC, multicurves);
    final double pv = multicurves.getFxRates().convert(swap.accept(PVMC, multicurves), ccy1).getAmount();
    // Implementation note: Total pv in currency 1.
    return pvcs1.multipliedBy(-1.0 / pvmqs).plus(pvmqscs.multipliedBy(pv / (pvmqs * pvmqs)));
  }

  @Override
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return METHOD_STIR_FUT.priceCurveSensitivity(futures.getUnderlying(), multicurves);
  }

  //     -----     Forex     -----

  @Override
  public MulticurveSensitivity visitForexSwap(final ForexSwap fx, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX_SWAP.parSpreadCurveSensitivity(fx, multicurves);
  }

  @Override
  public MulticurveSensitivity visitForex(final Forex fx, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX.parSpreadCurveSensitivity(fx, multicurves);
  }

}
