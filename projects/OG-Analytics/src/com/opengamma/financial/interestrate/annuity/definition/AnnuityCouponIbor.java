/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.CouponIbor;
import com.opengamma.util.money.Currency;

/**
 * A wrapper class for a GenericAnnuity containing CouponIbor.
 */
public class AnnuityCouponIbor extends GenericAnnuity<CouponIbor> {

  /**
   * Constructor from an array of Ibor coupons.
   * @param payments The coupons array.
   */
  public AnnuityCouponIbor(final CouponIbor[] payments) {
    super(payments);
  }

  /**
   * A basic variable annuity - notional is 1.0, libor fixing and maturities are on payment dates, year fraction is ACT/ACT and spreads are zero
   * @param currency The payment currency.
   * @param paymentTimes time in years from now of payments 
   * @param index TODO
   * @param fundingCurveName liborCurveName
   * @param liborCurveName Name of curve from which forward rates are calculated
   * @param isPayer Payer flag.
   */
  public AnnuityCouponIbor(Currency currency, final double[] paymentTimes, IborIndex index, final String fundingCurveName, final String liborCurveName, boolean isPayer) {
    this(currency, paymentTimes, index, 1.0, fundingCurveName, liborCurveName, isPayer);
  }

  /**
   * A basic variable annuity - libor fixing and maturities are on payment dates, year fraction is ACT/ACT and spreads are zero
   * @param currency The payment currency.
   * @param paymentTimes time in years from now of payments 
   * @param index TODO
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fundingCurveName liborCurveName
   * @param liborCurveName Name of curve from which forward rates are calculated
   * @param isPayer Payer flag.
   */
  public AnnuityCouponIbor(Currency currency, final double[] paymentTimes, IborIndex index, final double notional, final String fundingCurveName, final String liborCurveName, boolean isPayer) {
    super(basisSetup(currency, paymentTimes, notional * (isPayer ? -1.0 : 1.0), index, fundingCurveName, liborCurveName));
  }

  /**
   * A variable annuity (e.g. the floating leg of a swap) 
   * * For n payments indexed 0 to n-1, the indexFixingTimes, indexMaturityTimes and yearFractions corresponding to a payment are indexed the same way
   * @param currency The payment currency.
   * @param paymentTimes time in years from now of payments 
   * @param indexFixingTimes time in years from now to the fixing dates of the reference index (e.g. Libor) 
   * @param index TODO
   * @param indexMaturityTimes time in years from now to the maturity of the reference rate  
   * @param yearFraction year fractions used to calculate payment amounts and reference rate
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fundingCurveName  Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   * @param isPayer Payer flag.
   */
  public AnnuityCouponIbor(Currency currency, final double[] paymentTimes, final double[] indexFixingTimes, IborIndex index, final double[] indexMaturityTimes, final double[] yearFraction,
      final double notional, final String fundingCurveName, final String liborCurveName, boolean isPayer) {
    this(currency, paymentTimes, indexFixingTimes, index, indexFixingTimes, indexMaturityTimes, yearFraction, yearFraction, new double[paymentTimes == null ? 0 : paymentTimes.length], notional,
        fundingCurveName, liborCurveName, isPayer);
  }

  /**
   * A variable annuity (e.g. the floating leg of a swap).
   * For n payments indexed 0 to n-1, the indexFixingTimes, indexMaturityTimes and yearFractions corresponding to a payment are indexed the same way
   * @param currency The payment currency.
   * @param paymentTimes time in years from now of payments 
   * @param indexFixingTimes time in years from now to the fixing dates of the reference index (e.g. Libor)  
   * @param index TODO
   * @param indexStartTimes time in years from now to the start of the reference rate  
   * @param indexMaturityTimes time in years from now to the maturity of the reference rate  
   * @param paymentYearFractions year fractions used to calculate payment amounts
   * @param forwardYearFractions year fractions used to calculate forward rates
   * @param spreads fixed payments on top of variable amounts (can be negative)
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fundingCurveName  Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   * @param isPayer Payer flag.
   */
  public AnnuityCouponIbor(Currency currency, final double[] paymentTimes, final double[] indexFixingTimes, IborIndex index, final double[] indexStartTimes, final double[] indexMaturityTimes,
      final double[] paymentYearFractions, final double[] forwardYearFractions, final double[] spreads, final double notional, final String fundingCurveName, final String liborCurveName,
      boolean isPayer) {
    super(fullSetup(currency, paymentTimes, index, indexFixingTimes, indexStartTimes, indexMaturityTimes, paymentYearFractions, forwardYearFractions, spreads, notional * (isPayer ? -1.0 : 1.0),
        fundingCurveName, liborCurveName));

  }

  private static CouponIbor[] basisSetup(Currency currency, final double[] paymentTimes, final double notional, IborIndex index, final String fundingCurveName, final String liborCurveName) {
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    Validate.notNull(paymentTimes);
    Validate.isTrue(paymentTimes.length > 0, "payment times array is empty");
    final int n = paymentTimes.length;
    // sanity checks
    for (int i = 0; i < n; i++) {
      Validate.isTrue(paymentTimes[i] >= 0.0, "payment time is negative");
    }
    final CouponIbor[] res = new CouponIbor[n];
    res[0] = new CouponIbor(currency, paymentTimes[0], fundingCurveName, paymentTimes[0], notional, 0.0, index, 0.0, paymentTimes[0], paymentTimes[0], 0.0, liborCurveName);
    for (int i = 1; i < n; i++) {
      res[i] = new CouponIbor(currency, paymentTimes[i], fundingCurveName, paymentTimes[i] - paymentTimes[i - 1], notional, paymentTimes[i - 1], index, paymentTimes[i - 1], paymentTimes[i],
          paymentTimes[i] - paymentTimes[i - 1], 0.0, liborCurveName);
    }
    return res;
  }

  private static CouponIbor[] fullSetup(Currency currency, final double[] paymentTimes, IborIndex index, final double[] indexFixingTimes, final double[] indexStartTimes,
      final double[] indexMaturityTimes, final double[] paymentYearFraction, final double[] forwardYearFraction, final double[] spreads, final double notional, final String fundingCurveName,
      final String liborCurveName) {
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
    //CouponIbor(paymentTimes[i], notional, indexFixingTimes[i], indexMaturityTimes[i], paymentYearFraction[i], forwardYearFraction[i], spreads[i], fundingCurveName, liborCurveName);
    for (int i = 0; i < n; i++) {
      Validate.isTrue(indexFixingTimes[i] >= 0.0, "index fixing < 0");
      Validate.isTrue(indexFixingTimes[i] <= paymentTimes[i], "fixing times after payment times");
      Validate.isTrue(indexFixingTimes[i] < indexMaturityTimes[i], "fixing times after maturity times");
    }

    final CouponIbor[] res = new CouponIbor[n];
    for (int i = 0; i < n; i++) {
      res[i] = new CouponIbor(currency, paymentTimes[i], fundingCurveName, paymentYearFraction[i], notional, indexFixingTimes[i], index, indexStartTimes[i], indexMaturityTimes[i],
          forwardYearFraction[i], spreads[i], liborCurveName);
    }
    return res;
  }

  public AnnuityCouponIbor withZeroSpread() {
    final int n = getNumberOfPayments();
    final CouponIbor[] temp = new CouponIbor[n];
    for (int i = 0; i < n; i++) {
      temp[i] = getNthPayment(i).withZeroSpread();
    }
    return new AnnuityCouponIbor(temp);
  }

  public AnnuityCouponFixed withUnitCoupons() {
    final int n = getNumberOfPayments();
    final CouponFixed[] temp = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      temp[i] = getNthPayment(i).withUnitCoupon();
    }
    return new AnnuityCouponFixed(temp);
  }

  public AnnuityCouponIbor withSpread(final double rate) {
    final int n = getNumberOfPayments();
    final CouponIbor[] temp = new CouponIbor[n];
    for (int i = 0; i < n; i++) {
      temp[i] = getNthPayment(i).withSpread(rate);
    }
    return new AnnuityCouponIbor(temp);
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitForwardLiborAnnuity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitForwardLiborAnnuity(this);
  }

}
