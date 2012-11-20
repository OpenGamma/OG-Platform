/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.derivative.ForexSwap;
import com.opengamma.analytics.financial.forex.provider.ForexDiscountingProviderMethod;
import com.opengamma.analytics.financial.forex.provider.ForexSwapDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.AbstractInstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.cash.derivative.Cash;
import com.opengamma.analytics.financial.interestrate.cash.derivative.DepositIbor;
import com.opengamma.analytics.financial.interestrate.cash.provider.CashDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.cash.provider.DepositIborDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.fra.derivative.ForwardRateAgreement;
import com.opengamma.analytics.financial.interestrate.fra.provider.ForwardRateAgreementDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponFixedDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborCompoundedDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborGearingDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponIborSpreadDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.CouponOISDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.payments.provider.PaymentFixedDiscountingProviderMethod;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the present value as a multiple currency amount.
 */
public final class PresentValueDiscountingCalculator extends AbstractInstrumentDerivativeVisitor<MulticurveProviderInterface, MultipleCurrencyAmount> {

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
  private static final PaymentFixedDiscountingProviderMethod METHOD_PAY_FIXED = PaymentFixedDiscountingProviderMethod.getInstance();
  private static final DepositIborDiscountingMethod METHOD_DEPOSIT_IBOR = DepositIborDiscountingMethod.getInstance();
  private static final CouponFixedDiscountingProviderMethod METHOD_CPN_FIXED = CouponFixedDiscountingProviderMethod.getInstance();
  private static final CouponIborDiscountingProviderMethod METHOD_CPN_IBOR = CouponIborDiscountingProviderMethod.getInstance();
  private static final CouponIborSpreadDiscountingProviderMethod METHOD_CPN_IBOR_SPREAD = CouponIborSpreadDiscountingProviderMethod.getInstance();
  private static final CouponIborGearingDiscountingProviderMethod METHOD_CPN_IBOR_GEARING = CouponIborGearingDiscountingProviderMethod.getInstance();
  private static final CouponIborCompoundedDiscountingMethod METHOD_CPN_IBOR_COMP = CouponIborCompoundedDiscountingMethod.getInstance();
  private static final CouponOISDiscountingProviderMethod METHOD_CPN_ON = CouponOISDiscountingProviderMethod.getInstance();
  private static final ForwardRateAgreementDiscountingProviderMethod METHOD_FRA = ForwardRateAgreementDiscountingProviderMethod.getInstance();
  private static final ForexDiscountingProviderMethod METHOD_FOREX = ForexDiscountingProviderMethod.getInstance();
  private static final ForexSwapDiscountingProviderMethod METHOD_FOREX_SWAP = ForexSwapDiscountingProviderMethod.getInstance();

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
  public MultipleCurrencyAmount visitCouponIbor(final CouponIbor payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR.presentValue(payment, multicurve);
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
  public MultipleCurrencyAmount visitCouponIborCompounded(final CouponIborCompounded payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_IBOR_COMP.presentValue(payment, multicurve);
  }

  @Override
  public MultipleCurrencyAmount visitCouponOIS(final CouponOIS payment, final MulticurveProviderInterface multicurve) {
    return METHOD_CPN_ON.presentValue(payment, multicurve);
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
    MultipleCurrencyAmount pv = visit(annuity.getNthPayment(0), multicurve);
    for (int loopp = 1; loopp < annuity.getNumberOfPayments(); loopp++) {
      pv = pv.plus(visit(annuity.getNthPayment(loopp), multicurve));
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
    final MultipleCurrencyAmount pv1 = visit(swap.getFirstLeg(), multicurve);
    final MultipleCurrencyAmount pv2 = visit(swap.getSecondLeg(), multicurve);
    return pv1.plus(pv2);
  }

  @Override
  public MultipleCurrencyAmount visitFixedCouponSwap(final SwapFixedCoupon<?> swap, final MulticurveProviderInterface multicurves) {
    return visitSwap(swap, multicurves);
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

}
