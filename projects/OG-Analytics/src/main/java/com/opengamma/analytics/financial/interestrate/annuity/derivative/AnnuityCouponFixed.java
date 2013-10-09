/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.derivative;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A wrapper class for a GenericAnnuity containing FixedCouponPayment.
 */
public class AnnuityCouponFixed extends Annuity<CouponFixed> {

  /**
   * Constructor from an array of fixed coupons.
   * @param payments The payments array.
   */
  public AnnuityCouponFixed(final CouponFixed[] payments) {
    super(payments);
  }

  /**
   * @param currency The currency, not null
   * @param paymentTimes The payment times, not null
   * @param couponRate The coupon rate
   * @param yieldCurveName The yield curve name
   * @param isPayer True if the annuity is paid
   * @deprecated Use the constructor that does not take a yield curve name
   */
  @Deprecated
  public AnnuityCouponFixed(final Currency currency, final double[] paymentTimes, final double couponRate, final String yieldCurveName, final boolean isPayer) {
    this(currency, paymentTimes, 1.0, couponRate, yieldCurveName, isPayer);
  }

  /**
   * @param currency The currency, not null
   * @param paymentTimes The payment times, not null
   * @param couponRate The coupon rate
   * @param isPayer True if the annuity is paid
   */
  public AnnuityCouponFixed(final Currency currency, final double[] paymentTimes, final double couponRate, final boolean isPayer) {
    this(currency, paymentTimes, 1.0, couponRate, isPayer);
  }

  /**
   * @param currency The currency, not null
   * @param paymentTimes The payment times, not null
   * @param notional The notional
   * @param couponRate The coupon rate
   * @param yieldCurveName The yield curve name
   * @param isPayer True if the annuity is paid
   * @deprecated Use the constructor that does not take a yield curve name
   */
  @Deprecated
  public AnnuityCouponFixed(final Currency currency, final double[] paymentTimes, final double notional, final double couponRate, final String yieldCurveName, final boolean isPayer) {
    this(currency, paymentTimes, notional, couponRate, initBasisYearFraction(paymentTimes), yieldCurveName, isPayer);
  }

  /**
   * @param currency The currency, not null
   * @param paymentTimes The payment times, not null
   * @param notional The notional
   * @param couponRate The coupon rate
   * @param isPayer True if the annuity is paid
   */
  public AnnuityCouponFixed(final Currency currency, final double[] paymentTimes, final double notional, final double couponRate, final boolean isPayer) {
    this(currency, paymentTimes, notional, couponRate, initBasisYearFraction(paymentTimes), isPayer);
  }

  /**
   * Constructor from payment times and year fractions and unique notional and rate.
   * @param currency The payment currency.
   * @param paymentTimes The times (in year) of payment.
   * @param notional The common notional.
   * @param couponRate The common coupon rate.
   * @param yearFractions The year fraction of each payment.
   * @param yieldCurveName The discounting curve name.
   * @param isPayer Payer flag.
   * @deprecated Use the constructor that does not take a yield curve name
   */
  @Deprecated
  public AnnuityCouponFixed(final Currency currency, final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions,
      final String yieldCurveName, final boolean isPayer) {
    super(init(currency, paymentTimes, notional * (isPayer ? -1.0 : 1.0), couponRate, yearFractions, yieldCurveName));
  }

  /**
   * Constructor from payment times and year fractions and unique notional and rate.
   * @param currency The payment currency.
   * @param paymentTimes The times (in year) of payment.
   * @param notional The common notional.
   * @param couponRate The common coupon rate.
   * @param yearFractions The year fraction of each payment.
   * @param isPayer Payer flag.
   */
  public AnnuityCouponFixed(final Currency currency, final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions,
      final boolean isPayer) {
    super(init(currency, paymentTimes, notional * (isPayer ? -1.0 : 1.0), couponRate, yearFractions));
  }

  /**
   * Return the coupon rate of the annuity first coupon.
   * @return The rate.
   */
  public double getCouponRate() {
    return getNthPayment(0).getFixedRate();
  }

  /**
   * Creates a new annuity with the same characteristics, except the rate which is 1.0.
   * @return The new annuity.
   */
  public AnnuityCouponFixed withUnitCoupon() {
    final CouponFixed[] cpn = new CouponFixed[getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < getNumberOfPayments(); loopcpn++) {
      cpn[loopcpn] = getNthPayment(loopcpn).withUnitCoupon();
    }
    return new AnnuityCouponFixed(cpn);
  }

  /**
   * Creates a new annuity with the same characteristics, except that the rate of all coupons is the one given.
   * @param rate The rate.
   * @return The new annuity.
   */
  public AnnuityCouponFixed withRate(final double rate) {
    final CouponFixed[] cpn = new CouponFixed[getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < getNumberOfPayments(); loopcpn++) {
      cpn[loopcpn] = getNthPayment(loopcpn).withRate(rate);
    }
    return new AnnuityCouponFixed(cpn);
  }

  /**
   * Creates a new annuity with the same characteristics, except that the rate of all coupons are shifted by the given amount.
   * @param spread The spread.
   * @return The new annuity.
   */
  public AnnuityCouponFixed withRateShifted(final double spread) {
    final CouponFixed[] cpn = new CouponFixed[getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < getNumberOfPayments(); loopcpn++) {
      cpn[loopcpn] = getNthPayment(loopcpn).withRateShifted(spread);
    }
    return new AnnuityCouponFixed(cpn);
  }

  /**
   * Creates a new annuity with the same characteristics, except that the notional all coupons is the one given.
   * @param notional The notional.
   * @return The new annuity.
   */
  public AnnuityCouponFixed withNotional(final double notional) {
    final CouponFixed[] cpn = new CouponFixed[getNumberOfPayments()];
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
  public AnnuityCouponFixed trimBefore(final double trimTime) {
    final List<CouponFixed> list = new ArrayList<>();
    for (final CouponFixed payment : getPayments()) {
      if (payment.getPaymentTime() > trimTime) {
        list.add(payment);
      }
    }
    return new AnnuityCouponFixed(list.toArray(new CouponFixed[list.size()]));
  }

  /**
   * Remove the payments paying strictly after before the given time.
   * @param trimTime The time.
   * @return The trimmed annuity.
   */
  @Override
  public AnnuityCouponFixed trimAfter(final double trimTime) {
    final List<CouponFixed> list = new ArrayList<>();
    for (final CouponFixed payment : getPayments()) {
      if (payment.getPaymentTime() <= trimTime) {
        list.add(payment);
      }
    }
    return new AnnuityCouponFixed(list.toArray(new CouponFixed[list.size()]));
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
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  private static CouponFixed[] init(final Currency currency, final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    ArgumentChecker.notNull(paymentTimes, "payment times");
    ArgumentChecker.isTrue(paymentTimes.length > 0, "payment times array is empty");
    ArgumentChecker.notNull(yearFractions, "year fractions");
    ArgumentChecker.isTrue(yearFractions.length > 0, "year fraction array is empty");
    ArgumentChecker.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    ArgumentChecker.isTrue(yearFractions.length == n, "Number of year fractions must equal the number of payments");
    final CouponFixed[] temp = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      temp[i] = new CouponFixed(currency, paymentTimes[i], yieldCurveName, yearFractions[i], notional, couponRate);
    }
    return temp;
  }

  /**
   * A list of fixed coupon from payment times and year fractions and unique notional and rate.
   * @param currency The payment currency.
   * @param paymentTimes The times (in year) of payment.
   * @param notional The common notional.
   * @param couponRate The common coupon rate.
   * @param yearFractions The year fraction of each payment.
   * @return The array of fixed coupons.
   */
  private static CouponFixed[] init(final Currency currency, final double[] paymentTimes, final double notional, final double couponRate, final double[] yearFractions) {
    ArgumentChecker.notNull(paymentTimes, "payment times");
    ArgumentChecker.isTrue(paymentTimes.length > 0, "payment times array is empty");
    ArgumentChecker.notNull(yearFractions, "year fractions");
    ArgumentChecker.isTrue(yearFractions.length > 0, "year fraction array is empty");
    final int n = paymentTimes.length;
    ArgumentChecker.isTrue(yearFractions.length == n, "Number of year fractions must equal the number of payments");
    final CouponFixed[] temp = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      temp[i] = new CouponFixed(currency, paymentTimes[i], yearFractions[i], notional, couponRate);
    }
    return temp;
  }

  private static double[] initBasisYearFraction(final double[] paymentTimes) {
    ArgumentChecker.notNull(paymentTimes, "payment times");
    ArgumentChecker.isTrue(paymentTimes.length > 0, "payment times array is empty");
    final int n = paymentTimes.length;
    final double[] res = new double[n];
    for (int i = 0; i < n; i++) {
      res[i] = (i == 0 ? paymentTimes[0] : paymentTimes[i] - paymentTimes[i - 1]); // TODO ????????? so the payment year fractions could be 2.5, 0.5, 0.5, 0.5?
    }
    return res;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFixedCouponAnnuity(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFixedCouponAnnuity(this);
  }

}
