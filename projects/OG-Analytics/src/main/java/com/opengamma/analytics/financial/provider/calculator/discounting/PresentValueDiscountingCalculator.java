/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexNonDeliverableForward;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexDiscountingMethod;
import com.opengamma.analytics.financial.forex.provider.ForexNonDeliverableForwardDiscountingMethod;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingMethod;
import com.opengamma.analytics.financial.instrument.index.IndexDeposit;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.derivative.SwapFuturesPriceDeliverableTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.FuturesTransactionMulticurveMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedAccruedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingFlatSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverageSpreadSimplified;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.DepositIndexCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.InterpolatedStubCoupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedAccruedCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborAverageDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundingFlatSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundingSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONArithmeticAverageDiscountingApproxMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONArithmeticAverageSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONArithmeticAverageSpreadSimplifiedDiscountingApproxMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONCompoundedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapMultileg;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount using cash-flow discounting and forward estimation.
 */
public final class PresentValueDiscountingCalculator extends InstrumentDerivativeVisitorAdapter<MulticurveProviderInterface, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueDiscountingCalculator INSTANCE = new PresentValueDiscountingCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueDiscountingCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private PresentValueDiscountingCalculator() {
  }

  /**
   * The methods used by the different instruments.
   */
  private static final CashDiscountingMethod METHOD_DEPOSIT = CashDiscountingMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final CouponFixedCompoundingDiscountingMethod METHOD_CPN_FIXED_COMPOUNDING = CouponFixedCompoundingDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponIborAverageDiscountingMethod METHOD_CPN_IBOR_AVERAGE = CouponIborAverageDiscountingMethod.getInstance();
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  private static final CouponIborGearingDiscountingMethod METHOD_CPN_IBOR_GEARING = CouponIborGearingDiscountingMethod.getInstance();
  private static final CouponIborCompoundingDiscountingMethod METHOD_CPN_IBOR_COMP = CouponIborCompoundingDiscountingMethod.getInstance();
  private static final CouponIborCompoundingSpreadDiscountingMethod METHOD_CPN_IBOR_COMP_SPREAD = CouponIborCompoundingSpreadDiscountingMethod.getInstance();
  private static final CouponIborCompoundingFlatSpreadDiscountingMethod METHOD_CPN_IBOR_COMP_FLAT_SPREAD = CouponIborCompoundingFlatSpreadDiscountingMethod.getInstance();
  private static final CouponONDiscountingMethod METHOD_CPN_ON = CouponONDiscountingMethod.getInstance();
  private static final CouponONSpreadDiscountingMethod METHOD_CPN_ON_SPREAD = CouponONSpreadDiscountingMethod.getInstance();
  private static final CouponONArithmeticAverageDiscountingApproxMethod METHOD_CPN_AAON_APPROX = CouponONArithmeticAverageDiscountingApproxMethod.getInstance();
  private static final CouponONArithmeticAverageSpreadDiscountingMethod METHOD_CPN_AAON_SPREAD = CouponONArithmeticAverageSpreadDiscountingMethod.getInstance();
  private static final CouponONArithmeticAverageSpreadSimplifiedDiscountingApproxMethod METHOD_CPN_ONAA_SPREADSIMPL_APPROX =
      CouponONArithmeticAverageSpreadSimplifiedDiscountingApproxMethod.getInstance();
  private static final ForwardRateAgreementDiscountingProviderMethod METHOD_FRA = ForwardRateAgreementDiscountingProviderMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FOREX_SWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_FOREX_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();
  private static final FuturesTransactionMulticurveMethod METHOD_FUT = new FuturesTransactionMulticurveMethod();
  private static final CouponFixedAccruedCompoundingDiscountingMethod METHOD_CPN_FIXED_ACCRUED_COMPOUNDING = CouponFixedAccruedCompoundingDiscountingMethod.getInstance();
  private static final CouponONCompoundedDiscountingMethod METHOD_CPN_ON_COMPOUNDING = CouponONCompoundedDiscountingMethod.getInstance();
  private static final InterpolatedStubPresentValueDiscountingCalculator METHOD_CPN_INTERP_STUB = InterpolatedStubPresentValueDiscountingCalculator.getInstance();

  //     -----     Deposit     -----

  @Override
  public MultipleCurrencyAmount visitCash(final Cash deposit, final MulticurveProviderInterface multicurve) {
    return METHOD_DEPOSIT.presentValue(deposit, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitDepositIbor(final DepositIbor deposit, final MulticurveProviderInterface multicurve) {
    return METHOD_DEPOSIT_IBOR.presentValue(deposit, multicurve);
  }

  // -----     Payment/Coupon     ------

  @Override
  public MultipleCurrencyAmount visitFixedPayment(final PaymentFixed payment, final MulticurveProviderInterface multicurve) {
    return METHOD_PAY_FIXED.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponFixed(final CouponFixed coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_FIXED.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponFixedCompounding(final CouponFixedCompounding coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_FIXED_COMPOUNDING.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitInterpolatedStubCoupon(
      final InterpolatedStubCoupon<? extends DepositIndexCoupon<? extends IndexDeposit>, ? extends IndexDeposit> payment,
      final MulticurveProviderInterface data) {
    return payment.getFullCoupon().accept(METHOD_CPN_INTERP_STUB, InterpolatedStubData.of(data, payment));
  }

  @Override
  public MultipleCurrencyAmount visitCouponIbor(final CouponIbor coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborAverage(final CouponIborAverage coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_AVERAGE.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborSpread(final CouponIborSpread coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_SPREAD.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborGearing(final CouponIborGearing coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_GEARING.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborCompounding(final CouponIborCompounding coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborCompoundingSpread(final CouponIborCompoundingSpread coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP_SPREAD.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborCompoundingFlatSpread(final CouponIborCompoundingFlatSpread coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP_FLAT_SPREAD.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponOIS(final CouponON coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_ON.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponONSpread(final CouponONSpread coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_ON_SPREAD.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponONArithmeticAverage(final CouponONArithmeticAverage coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_AAON_APPROX.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponONArithmeticAverageSpread(CouponONArithmeticAverageSpread coupon, MulticurveProviderInterface multicurve) {
    return METHOD_CPN_AAON_SPREAD.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponONArithmeticAverageSpreadSimplified(final CouponONArithmeticAverageSpreadSimplified coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_ONAA_SPREADSIMPL_APPROX.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitForwardRateAgreement(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    return METHOD_FRA.presentValue(fra, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponFixedAccruedCompounding(final CouponFixedAccruedCompounding coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_FIXED_ACCRUED_COMPOUNDING.presentValue(coupon, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponONCompounded(final CouponONCompounded coupon, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_ON_COMPOUNDING.presentValue(coupon, multicurve);
  }

  // -----     Annuity     ------

  @Override
  public MultipleCurrencyAmount visitGenericAnnuity(final Annuity<? extends Payment> annuity, final MulticurveProviderInterface multicurve) {
    ArgumentChecker.notNull(annuity, "Annuity");
    ArgumentChecker.notNull(multicurve, "multicurve");
    MultipleCurrencyAmount pv = annuity.getNthPayment(0).accept(this, multicurve);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pv = pv.plus(annuity.getNthPayment(loopp).accept(this, multicurve));
    }
    return pv;
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponAnnuity(final AnnuityCouponFixed annuity, final MulticurveProviderInterface multicurve) {
    return visitGenericAnnuity(annuity, multicurve);
  }

  // -----     Swap     ------

  @Override
  public MultipleCurrencyAmount visitSwap(final Swap<?, ?> swap, final MulticurveProviderInterface multicurve) {
    final MultipleCurrencyAmount pv1 = swap.getFirstLeg().accept(this, multicurve);
    final MultipleCurrencyAmount pv2 = swap.getSecondLeg().accept(this, multicurve);
    return pv1.plus(pv2);
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurves) {
    return visitSwap(swap, multicurves);
  }

  @Override
  public MultipleCurrencyAmount visitSwapMultileg(final SwapMultileg swap, final MulticurveProviderInterface multicurve) {
    final int nbLegs = swap.getLegs().length;
    MultipleCurrencyAmount pv = swap.getLegs()[0].accept(this, multicurve);
    for (int loopleg = 1; loopleg < nbLegs; loopleg++) {
      pv = pv.plus(swap.getLegs()[loopleg].accept(this, multicurve));
    }
    return pv;
  }

  // -----     Futures     ------

  @Override
  public MultipleCurrencyAmount visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return METHOD_FUT.presentValue(futures, multicurves);
  }

  @Override
  public MultipleCurrencyAmount visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final MulticurveProviderInterface multicurves) {
    return METHOD_FUT.presentValue(future, multicurves);
  }

  @Override
  public MultipleCurrencyAmount visitSwapFuturesPriceDeliverableTransaction(final SwapFuturesPriceDeliverableTransaction future, final MulticurveProviderInterface multicurves) {
    return METHOD_FUT.presentValue(future, multicurves);
  }

  // -----     Forex     ------

  @Override
  public MultipleCurrencyAmount visitForex(final Forex derivative, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX.presentValue(derivative, multicurves);
  }

  @Override
  public MultipleCurrencyAmount visitForexSwap(final ForexSwap derivative, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX_SWAP.presentValue(derivative, multicurves);
  }

  @Override
  public MultipleCurrencyAmount visitForexNonDeliverableForward(final ForexNonDeliverableForward derivative, final MulticurveProviderInterface multicurves) {
    return METHOD_FOREX_NDF.presentValue(derivative, multicurves);
  }

}
