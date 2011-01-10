/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeWithRate;
import com.opengamma.financial.interestrate.payments.FixedCouponPayment;
import com.opengamma.financial.interestrate.payments.ForwardLiborPayment;

/**
 * 
 */
public class ForwardLiborAnnuity extends GenericAnnuity<ForwardLiborPayment> implements InterestRateDerivativeWithRate {

  public ForwardLiborAnnuity(final ForwardLiborPayment[] payments) {
    super(payments);
  }

  /**
   * A basic variable annuity - notional is 1.0, libor fixing and maturities are on payment dates, year fraction is ACT/ACT and spreads are zero
   * @param paymentTimes time in years from now of payments 
   * @param fundingCurveName liborCurveName
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public ForwardLiborAnnuity(final double[] paymentTimes, final String fundingCurveName, final String liborCurveName) {
    this(paymentTimes, 1.0, fundingCurveName, liborCurveName);
  }

  /**
   * A basic variable annuity - libor fixing and maturities are on payment dates, year fraction is ACT/ACT and spreads are zero
   * @param paymentTimes time in years from now of payments 
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fundingCurveName liborCurveName
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public ForwardLiborAnnuity(final double[] paymentTimes, final double notional, final String fundingCurveName, final String liborCurveName) {
    super(basisSetup(paymentTimes, notional, fundingCurveName, liborCurveName));
  }

  /**
   * A variable annuity (e.g. the floating leg of a swap) 
   * * For n payments indexed 0 to n-1, the indexFixingTimes, indexMaturityTimes and yearFractions corresponding to a payment are indexed the same way
   * @param paymentTimes time in years from now of payments 
   * @param indexFixingTimes time in years from now to the fixing dates of the reference index (e.g. Libor) 
   * @param indexMaturityTimes time in years from now to the maturity of the reference rate  
   * @param yearFraction year fractions used to calculate payment amounts and reference rate
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fundingCurveName  Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public ForwardLiborAnnuity(final double[] paymentTimes, final double[] indexFixingTimes, final double[] indexMaturityTimes, final double[] yearFraction, final double notional,
      final String fundingCurveName, final String liborCurveName) {
    this(paymentTimes, indexFixingTimes, indexMaturityTimes, yearFraction, yearFraction, new double[paymentTimes == null ? 0 : paymentTimes.length], notional, fundingCurveName, liborCurveName);
  }

  /**
   * A variable annuity (e.g. the floating leg of a swap).
   * For n payments indexed 0 to n-1, the indexFixingTimes, indexMaturityTimes and yearFractions corresponding to a payment are indexed the same way
   * @param paymentTimes time in years from now of payments 
   * @param indexFixingTimes time in years from now to the fixing dates of the reference index (e.g. Libor) 
   * @param indexMaturityTimes time in years from now to the maturity of the reference rate  
   * @param paymentYearFractions year fractions used to calculate payment amounts
   * @param forwardYearFractions year fractions used to calculate forward rates
   * @param spreads fixed payments on top of variable amounts (can be negative)
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fundingCurveName  Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public ForwardLiborAnnuity(final double[] paymentTimes, final double[] indexFixingTimes, final double[] indexMaturityTimes, final double[] paymentYearFractions, final double[] forwardYearFractions,
      final double[] spreads, final double notional, final String fundingCurveName, final String liborCurveName) {
    super(fullSetup(paymentTimes, indexFixingTimes, indexMaturityTimes, paymentYearFractions, forwardYearFractions, spreads, notional, fundingCurveName, liborCurveName));

  }

  private static ForwardLiborPayment[] basisSetup(final double[] paymentTimes, final double notional, final String fundingCurveName, final String liborCurveName) {
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    Validate.notNull(paymentTimes);
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    final int n = paymentTimes.length;
    // sanity checks
    for (int i = 0; i < n; i++) {
      Validate.isTrue(paymentTimes[i] >= 0.0, "payment time is negative");
    }
    final ForwardLiborPayment[] res = new ForwardLiborPayment[n];
    res[0] = new ForwardLiborPayment(paymentTimes[0], notional, 0.0, paymentTimes[0], paymentTimes[0], paymentTimes[0], 0.0, fundingCurveName, liborCurveName);
    for (int i = 1; i < n; i++) {
      res[i] = new ForwardLiborPayment(paymentTimes[i], notional, paymentTimes[i - 1], paymentTimes[i], paymentTimes[i] - paymentTimes[i - 1], paymentTimes[i] - paymentTimes[i - 1], 0.0,
          fundingCurveName, liborCurveName);
    }
    return res;
  }

  private static ForwardLiborPayment[] fullSetup(final double[] paymentTimes, final double[] indexFixingTimes, final double[] indexMaturityTimes, final double[] paymentYearFraction,
      final double[] forwardYearFraction, final double[] spreads, final double notional, final String fundingCurveName, final String liborCurveName) {
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    Validate.notNull(paymentTimes);
    Validate.isTrue(paymentTimes.length > 0, "payment time array is empty");
    Validate.notNull(paymentYearFraction);
    Validate.isTrue(paymentYearFraction.length > 0, "payment year array is empty");
    Validate.notNull(forwardYearFraction);
    Validate.isTrue(forwardYearFraction.length > 0, "payment year fraction is empty");
    Validate.notNull(spreads);
    Validate.isTrue(spreads.length > 0, "spreads array is empty");
    Validate.notNull(indexFixingTimes);
    Validate.isTrue(indexFixingTimes.length > 0, "index fixing times array is empty");
    Validate.notNull(indexMaturityTimes);
    Validate.isTrue(indexMaturityTimes.length > 0, "index maturity times is empty");
    final int n = paymentTimes.length;
    Validate.isTrue(indexFixingTimes.length == n);
    Validate.isTrue(indexMaturityTimes.length == n);
    Validate.isTrue(paymentYearFraction.length == n);
    Validate.isTrue(forwardYearFraction.length == n);
    Validate.isTrue(spreads.length == n);
    // sanity checks
    for (int i = 0; i < n; i++) {
      Validate.isTrue(indexFixingTimes[i] >= 0.0, "index fixing < 0");
      Validate.isTrue(indexFixingTimes[i] <= paymentTimes[i], "fixing times after payment times");
      Validate.isTrue(indexFixingTimes[i] < indexMaturityTimes[i], "fixing times after maturity times");
    }

    final ForwardLiborPayment[] res = new ForwardLiborPayment[n];
    for (int i = 0; i < n; i++) {
      res[i] = new ForwardLiborPayment(paymentTimes[i], notional, indexFixingTimes[i], indexMaturityTimes[i], paymentYearFraction[i], forwardYearFraction[i], spreads[i], fundingCurveName,
          liborCurveName);
    }
    return res;
  }

  public ForwardLiborAnnuity withZeroSpread() {
    final int n = getNumberOfPayments();
    final ForwardLiborPayment[] temp = new ForwardLiborPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = getNthPayment(i).withZeroSpread();
    }
    return new ForwardLiborAnnuity(temp);
  }

  public FixedCouponAnnuity withUnitCoupons() {
    final int n = getNumberOfPayments();
    final FixedCouponPayment[] temp = new FixedCouponPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = getNthPayment(i).withUnitCoupon();
    }
    return new FixedCouponAnnuity(temp);
  }

  public ForwardLiborAnnuity withSpread(final double rate) {
    final int n = getNumberOfPayments();
    final ForwardLiborPayment[] temp = new ForwardLiborPayment[n];
    for (int i = 0; i < n; i++) {
      temp[i] = getNthPayment(i).withSpread(rate);
    }
    return new ForwardLiborAnnuity(temp);
  }

  @Override
  public ForwardLiborAnnuity withRate(final double rate) {
    return withSpread(rate);
  }

}
