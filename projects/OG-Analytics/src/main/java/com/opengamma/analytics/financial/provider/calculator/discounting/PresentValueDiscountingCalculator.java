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
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.future.provider.FederalFundsFutureTransactionDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponArithmeticAverageON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixedCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounding;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.method.CouponIborAverageDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponArithmeticAverageONDiscountingApproxMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedCompoundingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponOISDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponONSpreadDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
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
  private static final CashDiscountingProviderMethod METHOD_DEPOSIT = CashDiscountingProviderMethod.getInstance();
  private static final PaymentFixedDiscountingMethod METHOD_PAY_FIXED = PaymentFixedDiscountingMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingMethod METHOD_CPN_FIXED = CouponFixedDiscountingMethod.getInstance();
  private static final CouponFixedCompoundingDiscountingMethod METHOD_CPN_FIXED_COMPOUNDING = CouponFixedCompoundingDiscountingMethod.getInstance();
  private static final CouponIborDiscountingMethod METHOD_CPN_IBOR = CouponIborDiscountingMethod.getInstance();
  private static final CouponIborAverageDiscountingMethod METHOD_CPN_IBOR_AVERAGE = CouponIborAverageDiscountingMethod.getInstance();
  private static final CouponIborSpreadDiscountingMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingMethod.getInstance();
  private static final CouponIborGearingDiscountingMethod METHOD_CPN_IBOR_GEARING = CouponIborGearingDiscountingMethod.getInstance();
  private static final CouponIborCompoundedDiscountingMethod METHOD_CPN_IBOR_COMP = CouponIborCompoundedDiscountingMethod.getInstance();
  private static final CouponOISDiscountingMethod METHOD_CPN_ON = CouponOISDiscountingMethod.getInstance();
  private static final CouponONSpreadDiscountingMethod METHOD_CPN_ON_SPREAD = CouponONSpreadDiscountingMethod.getInstance();
  private static final CouponArithmeticAverageONDiscountingApproxMethod METHOD_CPN_AAON_APPROX = CouponArithmeticAverageONDiscountingApproxMethod.getInstance();
  private static final ForwardRateAgreementDiscountingProviderMethod METHOD_FRA = ForwardRateAgreementDiscountingProviderMethod.getInstance();
  private static final ForexDiscountingMethod METHOD_FOREX = ForexDiscountingMethod.getInstance();
  private static final FederalFundsFutureTransactionDiscountingMethod METHOD_FFFUT_TRA = FederalFundsFutureTransactionDiscountingMethod.getInstance();
  private static final ForexSwapDiscountingMethod METHOD_FOREX_SWAP = ForexSwapDiscountingMethod.getInstance();
  private static final ForexNonDeliverableForwardDiscountingMethod METHOD_FOREX_NDF = ForexNonDeliverableForwardDiscountingMethod.getInstance();

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
  public MultipleCurrencyAmount visitCouponFixed(final CouponFixed payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_FIXED.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponFixedCompounding(final CouponFixedCompounding payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_FIXED_COMPOUNDING.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborAverage(final CouponIborAverage payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_AVERAGE.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborSpread(final CouponIborSpread payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_SPREAD.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborGearing(final CouponIborGearing payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_GEARING.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponIborCompounding(final CouponIborCompounding payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponOIS(final CouponON payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_ON.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponONSpread(final CouponONSpread payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_ON_SPREAD.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponArithmeticAverageON(final CouponArithmeticAverageON payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_AAON_APPROX.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitForwardRateAgreement(final ForwardRateAgreement fra, final MulticurveProviderInterface multicurve) {
    return METHOD_FRA.presentValue(fra, multicurve);
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

  // -----     Futures     ------

  @Override
  public MultipleCurrencyAmount visitFederalFundsFutureTransaction(final FederalFundsFutureTransaction futures, final MulticurveProviderInterface multicurves) {
    return METHOD_FFFUT_TRA.presentValue(futures, multicurves);
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
