/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedTransaction;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolation;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponInterpolationDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponInterpolationGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponMonthlyDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.inflation.method.CouponInflationZeroCouponMonthlyGearingDiscountingMethod;
import com.opengamma.analytics.financial.interestrate.market.MarketBundle;
import com.opengamma.analytics.financial.interestrate.market.MarketDiscountingDecorated;
import com.opengamma.analytics.financial.interestrate.payments.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.interestrate.payments.PaymentFixed;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public class PresentValueInflationCalculator extends AbstractInstrumentDerivativeVisitor<MarketBundle, CurrencyAmount> {

  /**
   * Pricing method for zero-coupon with monthly reference index.
   */
  private static final CouponInflationZeroCouponMonthlyDiscountingMethod METHOD_ZC_MONTHLY = new CouponInflationZeroCouponMonthlyDiscountingMethod();
  /**
   * Pricing method for zero-coupon with interpolated reference index.
   */
  private static final CouponInflationZeroCouponInterpolationDiscountingMethod METHOD_ZC_INTERPOLATION = new CouponInflationZeroCouponInterpolationDiscountingMethod();
  /**
   * Pricing method for zero-coupon with monthly reference index.
   */
  private static final CouponInflationZeroCouponMonthlyGearingDiscountingMethod METHOD_ZC_MONTHLY_GEARING = new CouponInflationZeroCouponMonthlyGearingDiscountingMethod();
  /**
   * Pricing method for zero-coupon with interpolated reference index.
   */
  private static final CouponInflationZeroCouponInterpolationGearingDiscountingMethod METHOD_ZC_INTERPOLATION_GEARING = new CouponInflationZeroCouponInterpolationGearingDiscountingMethod();

  /**
   * The unique instance of the calculator.
   */
  private static final PresentValueInflationCalculator s_instance = new PresentValueInflationCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static PresentValueInflationCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  PresentValueInflationCalculator() {
  }

  @Override
  public CurrencyAmount visit(final InstrumentDerivative derivative, final MarketBundle market) {
    Validate.notNull(market);
    Validate.notNull(derivative);
    return derivative.accept(this, market);
  }

  @Override
  public CurrencyAmount visitFixedPayment(final PaymentFixed payment, final MarketBundle market) {
    return CurrencyAmount.of(payment.getCurrency(), market.getDiscountingFactor(payment.getCurrency(), payment.getPaymentTime()) * payment.getAmount());
  }

  @Override
  public CurrencyAmount visitFixedCouponPayment(final CouponFixed coupon, final MarketBundle market) {
    return CurrencyAmount.of(coupon.getCurrency(), market.getDiscountingFactor(coupon.getCurrency(), coupon.getPaymentTime()) * coupon.getAmount());
  }

  @Override
  public CurrencyAmount visitGenericAnnuity(final GenericAnnuity<? extends Payment> annuity, final MarketBundle market) {
    Validate.notNull(annuity);
    CurrencyAmount pv = CurrencyAmount.of(annuity.getCurrency(), 0.0);
    for (final Payment p : annuity.getPayments()) {
      pv = pv.plus(visit(p, market));
    }
    return pv;
  }

  @Override
  public CurrencyAmount visitCouponInflationZeroCouponMonthly(final CouponInflationZeroCouponMonthly coupon, final MarketBundle market) {
    return METHOD_ZC_MONTHLY.presentValue(coupon, market);
  }

  @Override
  public CurrencyAmount visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final MarketBundle market) {
    return METHOD_ZC_INTERPOLATION.presentValue(coupon, market);
  }

  @Override
  public CurrencyAmount visitCouponInflationZeroCouponMonthlyGearing(final CouponInflationZeroCouponMonthlyGearing coupon, final MarketBundle market) {
    return METHOD_ZC_MONTHLY_GEARING.presentValue(coupon, market);
  }

  @Override
  public CurrencyAmount visitCouponInflationZeroCouponInterpolationGearing(final CouponInflationZeroCouponInterpolationGearing coupon, final MarketBundle market) {
    return METHOD_ZC_INTERPOLATION_GEARING.presentValue(coupon, market);
  }

  @Override
  public CurrencyAmount visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final MarketBundle market) {
    Validate.notNull(bond, "Bond");
    MarketBundle creditDiscounting = new MarketDiscountingDecorated(market, bond.getCurrency(), market.getCurve(bond.getIssuer()));
    final CurrencyAmount pvNominal = visit(bond.getNominal(), creditDiscounting);
    final CurrencyAmount pvCoupon = visit(bond.getCoupon(), creditDiscounting);
    return pvNominal.plus(pvCoupon);
  }

  @Override
  public CurrencyAmount visitBondCapitalIndexedTransaction(final BondCapitalIndexedTransaction<?> bond, final MarketBundle market) {
    Validate.notNull(bond, "Bond");
    final CurrencyAmount pvBond = visit(bond.getBondTransaction(), market);
    CurrencyAmount pvSettlement = visit(bond.getBondTransaction().getSettlement(), market).multipliedBy(bond.getQuantity() * bond.getBondTransaction().getCoupon().getNthPayment(0).getNotional());
    return pvBond.multipliedBy(bond.getQuantity()).plus(pvSettlement);
  }

}
