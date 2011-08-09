/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.bond.method.BondCapitalIndexedSecurityDiscountingMethod;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponFirstOfMonth;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponInterpolation;
import com.opengamma.financial.interestrate.inflation.method.CouponInflationZeroCouponFirstOfMonthDiscountingMethod;
import com.opengamma.financial.interestrate.inflation.method.CouponInflationZeroCouponInterpolationDiscountingMethod;
import com.opengamma.financial.interestrate.market.MarketBundle;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculates the present value of an inflation instruments by discounting for a given MarketBundle
 */
public class PresentValueInflationCalculator extends AbstractInterestRateDerivativeVisitor<MarketBundle, CurrencyAmount> {

  /**
   * Pricing method for zero-coupon with monthly reference index.
   */
  private static final CouponInflationZeroCouponFirstOfMonthDiscountingMethod METHOD_ZC_MONTHLY = new CouponInflationZeroCouponFirstOfMonthDiscountingMethod();
  /**
   * Pricing method for zero-coupon with interpolated reference index.
   */
  private static final CouponInflationZeroCouponInterpolationDiscountingMethod METHOD_ZC_INTERPOLATION = new CouponInflationZeroCouponInterpolationDiscountingMethod();
  /**
   * Pricing method for capital inflation indexed bond.
   */
  private static final BondCapitalIndexedSecurityDiscountingMethod METHOD_CIB = new BondCapitalIndexedSecurityDiscountingMethod();

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
  public CurrencyAmount visit(final InterestRateDerivative derivative, final MarketBundle market) {
    Validate.notNull(market);
    Validate.notNull(derivative);
    return derivative.accept(this, market);
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
  public CurrencyAmount visitCouponInflationZeroCouponFirstOfMonth(final CouponInflationZeroCouponFirstOfMonth coupon, final MarketBundle market) {
    return METHOD_ZC_MONTHLY.presentValue(coupon, market);
  }

  @Override
  public CurrencyAmount visitCouponInflationZeroCouponInterpolation(final CouponInflationZeroCouponInterpolation coupon, final MarketBundle market) {
    return METHOD_ZC_INTERPOLATION.presentValue(coupon, market);
  }

  @Override
  public CurrencyAmount visitBondCapitalIndexedSecurity(final BondCapitalIndexedSecurity<?> bond, final MarketBundle market) {
    return METHOD_CIB.presentValue(bond, market);
  }

}
