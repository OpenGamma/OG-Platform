/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.util.money.Currency;

/**
 * A wrapper class for a GenericAnnuity containing FixedCouponPayment.
 */
public class AnnuityCouponFixed extends GenericAnnuity<CouponFixed> {

  /**
   * Constructor from an array of fixed coupons.
   * @param payments The payments array.
   */
  public AnnuityCouponFixed(final CouponFixed[] payments) {
    super(payments);
  }

  public AnnuityCouponFixed(Currency currency, final double[] paymentTimes, final double couponRate, final String yieldCurveName, boolean isPayer) {
    this(currency, paymentTimes, 1.0, couponRate, yieldCurveName, isPayer);
  }

  public AnnuityCouponFixed(Currency currency, final double[] paymentTimes, final double notional, final double couponRate, final String yieldCurveName, boolean isPayer) {
    this(currency, paymentTimes, notional, couponRate, initBasisYearFraction(paymentTimes), yieldCurveName, isPayer);
  }

  /**
   * Constructor from payment times and year fractions and unique notional and rate. 
   * @param currency The payment currency.
   * @param paymentTimes The times (in year) of payment.
   * @param notional The common notional.
   * @param couponRate The common coupon rate.
   * @param yearFractions The year fraction of each payment.
   * @param yieldCurveName The discounting curve name.
   * @param isPayer TODO
   */
  public AnnuityCouponFixed(Currency currency, final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName, 
      boolean isPayer) {
    super(init(currency, paymentTimes, notional * (isPayer ? -1.0 : 1.0), couponRate, yearFractions, yieldCurveName));
  }

  /**
   * Return the coupon rate of the annuity first coupon.
   * @return The rate.
   */
  public double getCouponRate() {
    return getNthPayment(0).getFixedRate();
  }

  /**
   * Creates a new annuity with the same characteristics, except that the notional all coupons is the one given.
   * @param notional The notional.
   * @return The new annuity.
   */
  public AnnuityCouponFixed withNotional(double notional) {
    CouponFixed[] cpn = new CouponFixed[getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < getNumberOfPayments(); loopcpn++) {
      cpn[loopcpn] = getNthPayment(loopcpn).withNotional(notional);
    }
    return new AnnuityCouponFixed(cpn);
  }

  /**
   * Remove the payments paying on or before the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @Override
  public AnnuityCouponFixed trimBefore(double trimTime) {
    List<CouponFixed> list = new ArrayList<CouponFixed>();
    for (CouponFixed payment : getPayments()) {
      if (payment.getPaymentTime() > trimTime) {
        list.add(payment);
      }
    }
    return new AnnuityCouponFixed(list.toArray(new CouponFixed[0]));
  }

  /**
   * Remove the payments paying strictly after before the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @Override
  public AnnuityCouponFixed trimAfter(double trimTime) {
    List<CouponFixed> list = new ArrayList<CouponFixed>();
    for (CouponFixed payment : getPayments()) {
      if (payment.getPaymentTime() <= trimTime) {
        list.add(payment);
      }
    }
    return new AnnuityCouponFixed(list.toArray(new CouponFixed[0]));
  }

  /**
   * A list of fixed coupon from payment times and year fractions and unique notional and rate. 
   * @param currency The payment currency.
   * @param paymentTimes The times (in year) of payment.
   * @param notional The common notional.
   * @param couponRate The common coupon rate.
   * @param yearFractions The year fraction of each payment.
   * @param yieldCurveName The discounting curve name.
   * @return The array of fixed coupons.
   */
  private static CouponFixed[] init(Currency currency, final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes);
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    Validate.notNull(yearFractions);
    Validate.isTrue(yearFractions.length > 0, "year fraction array is empty");
    Validate.notNull(yieldCurveName);
    final int n = paymentTimes.length;
    Validate.isTrue(yearFractions.length == n);
    final CouponFixed[] temp = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      temp[i] = new CouponFixed(currency, paymentTimes[i], yieldCurveName, yearFractions[i], notional, couponRate);
    }
    return temp;
  }

  private static double[] initBasisYearFraction(final double[] paymentTimes) {
    Validate.notNull(paymentTimes);
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    final int n = paymentTimes.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = (i == 0 ? paymentTimes[0] : paymentTimes[i] - paymentTimes[i - 1]); // TODO ????????? so the payment year fractions could be 2.5, 0.5, 0.5, 0.5?
    }
    return res;
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitFixedCouponAnnuity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitFixedCouponAnnuity(this);
  }

}
