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
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.FederalFundsFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.future.provider.InterestRateFutureSecurityDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
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
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityDiscountingCalculator PVMQSMC = PresentValueMarketQuoteSensitivityDiscountingCalculator.getInstance();
  private static final PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator PVMQSCSMC = PresentValueMarketQuoteSensitivityCurveSensitivityDiscountingCalculator.getInstance();
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final ForwardRateAgreementDiscountingProviderMethod METHOD_FRA = ForwardRateAgreementDiscountingProviderMethod.getInstance();
  private static final InterestRateFutureSecurityDiscountingMethod METHOD_STIR_FUT = InterestRateFutureSecurityDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FOREX_SWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final FederalFundsFutureSecurityDiscountingMethod METHOD_FED_FUNDS = FederalFundsFutureSecurityDiscountingMethod.getInstance();

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

  /**
   * For swaps, the par spread is the spread to be added to the first leg to have a present value of zero.
   * @param swap The swap
   * @param multicurves The multi-curve provider
   * @return The spread.
   */
  @Override
  public MulticurveSensitivity visitSwap(final Swap<?, ?> swap, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(multicurves, "multicurve");
    ArgumentChecker.notNull(swap, "Swap");
    // if the swap is an On compounded (ie Brazilian like), the parspread formula is not the same.
    if (swap.getSecondLeg().getNthPayment(0) instanceof CouponONCompounded && swap.getFirstLeg().getNthPayment(0) instanceof CouponFixedAccruedCompounding &&
        swap.getFirstLeg().getNumberOfPayments() == 1) {
      // Implementation note: check if the swap is a Brazilian swap.

      final MulticurveSensitivity pvcsFirstLeg = swap.getFirstLeg().accept(PVCSDC, multicurves).getSensitivity(swap.getFirstLeg().getCurrency());
      final MulticurveSensitivity pvcsSecondLeg = swap.getSecondLeg().accept(PVCSDC, multicurves).getSensitivity(swap.getSecondLeg().getCurrency());

      final CouponFixedAccruedCompounding cpnFixed = (CouponFixedAccruedCompounding) swap.getFirstLeg().getNthPayment(0);
      final double pvONCompoundedLeg = swap.getSecondLeg().accept(PVDC, multicurves).getAmount(swap.getSecondLeg().getCurrency());
      final double discountFactor = multicurves.getDiscountFactor(swap.getFirstLeg().getCurrency(), cpnFixed.getPaymentTime());
      final double paymentYearFraction = cpnFixed.getPaymentYearFraction();

      final double notional = ((CouponONCompounded) swap.getSecondLeg().getNthPayment(0)).getNotional();
      final double intermediateVariable = (1 / paymentYearFraction) * Math.pow(pvONCompoundedLeg / discountFactor / notional, 1 / paymentYearFraction - 1) / (discountFactor * notional);
      final MulticurveSensitivity modifiedpvcsFirstLeg = pvcsFirstLeg.multipliedBy(pvONCompoundedLeg * intermediateVariable / discountFactor);
      final MulticurveSensitivity modifiedpvcsSecondLeg = pvcsSecondLeg.multipliedBy(-intermediateVariable);

      return modifiedpvcsFirstLeg.plus(modifiedpvcsSecondLeg);
    }
    final Currency ccy1 = swap.getFirstLeg().getCurrency();
    final MultipleCurrencyMulticurveSensitivity pvcs = swap.accept(PVCSDC, multicurves);
    final MulticurveSensitivity pvcs1 = pvcs.converted(ccy1, multicurves.getFxRates()).getSensitivity(ccy1);
    final MulticurveSensitivity pvmqscs = swap.getFirstLeg().accept(PVMQSCSMC, multicurves);
    final double pvmqs = swap.getFirstLeg().accept(PVMQSMC, multicurves);
    final double pv = multicurves.getFxRates().convert(swap.accept(PVDC, multicurves), ccy1).getAmount();
    // Implementation note: Total pv in currency 1.
    return pvcs1.multipliedBy(-1.0 / pvmqs).plus(pvmqscs.multipliedBy(pv / (pvmqs * pvmqs)));
  }

  @Override
  public MulticurveSensitivity visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurve) {
    return visitSwap(swap, multicurve);
  }

  /**
   * For swaps, the par spread is the spread to be added to the first leg to have a present value of zero.
   * @param swap The swap
   * @param multicurves The multi-curve provider
   * @return The spread.
   */
  @Override
  public MulticurveSensitivity visitSwapMultileg(final SwapMultileg swap, final MulticurveProviderInterface multicurves) {
    ArgumentChecker.notNull(multicurves, "multicurve");
    ArgumentChecker.notNull(swap, "Swap");
    final Currency ccy1 = swap.getLegs()[0].getCurrency();
    final MultipleCurrencyMulticurveSensitivity pvcs = swap.accept(PVCSDC, multicurves);
    final MulticurveSensitivity pvcs1 = pvcs.converted(ccy1, multicurves.getFxRates()).getSensitivity(ccy1);
    final MulticurveSensitivity pvmqscs = swap.getLegs()[0].accept(PVMQSCSMC, multicurves);
    final double pvmqs = swap.getLegs()[0].accept(PVMQSMC, multicurves);
    final double pv = multicurves.getFxRates().convert(swap.accept(PVDC, multicurves), ccy1).getAmount();
    // Implementation note: Total pv in currency 1.
    return pvcs1.multipliedBy(-1.0 / pvmqs).plus(pvmqscs.multipliedBy(pv / (pvmqs * pvmqs)));
  }

  //     -----     Futures     -----

  @Override
  public MulticurveSensitivity visitInterestRateFutureTransaction(final InterestRateFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return METHOD_STIR_FUT.priceCurveSensitivity(futures.getUnderlyingFuture(), multicurves);
  }

  @Override
  public MulticurveSensitivity visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction future, final MulticurveProviderInterface multicurves) {
    return METHOD_FED_FUNDS.priceCurveSensitivity(future.getUnderlyingFuture(), multicurves);
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
