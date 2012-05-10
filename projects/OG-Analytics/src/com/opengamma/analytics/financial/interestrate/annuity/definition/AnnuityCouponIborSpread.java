/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.annuity.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
import com.opengamma.util.money.Currency;

/**
 * A wrapper class for a GenericAnnuity containing CouponIbor.
 */
/**
 * @deprecated When a AnnuityCouponIborSpreadDefinition is converted, the result is not necessarily a AnnuityCouponIborSpread as some Ibor coupons may have fixed already. 
 * This instrument is never used in the natural flow "Definition->toDerivative->Derivative".
 */
@Deprecated
public class AnnuityCouponIborSpread extends GenericAnnuity<CouponIborSpread> {

  /**
   * Constructor from an array of Ibor coupons.
   * @param payments The coupons array.
   */
  public AnnuityCouponIborSpread(final CouponIborSpread[] payments) {
    super(payments);
  }

  //  /**
  //   * A variable annuity (e.g. the floating leg of a swap) 
  //   * * For n payments indexed 0 to n-1, the indexFixingTimes, indexMaturityTimes and yearFractions corresponding to a payment are indexed the same way
  //   * @param currency The payment currency.
  //   * @param paymentTimes time in years from now of payments 
  //   * @param indexFixingTimes time in years from now to the fixing dates of the reference index (e.g. Libor) 
  //   * @param index TODO
  //   * @param indexMaturityTimes time in years from now to the maturity of the reference rate  
  //   * @param yearFraction year fractions used to calculate payment amounts and reference rate
  //   * @param notional the notional amount (OK to set to 1.0) 
  //   * @param fundingCurveName  Name of curve from which payments are discounted
  //   * @param liborCurveName Name of curve from which forward rates are calculated
  //   * @param isPayer Payer flag.
  //   */
  //  public AnnuityCouponIborSpread(Currency currency, final double[] paymentTimes, final double[] indexFixingTimes, IborIndex index, final double[] indexMaturityTimes, final double[] yearFraction,
  //      final double notional, final String fundingCurveName, final String liborCurveName, boolean isPayer) {
  //    this(currency, paymentTimes, indexFixingTimes, index, indexFixingTimes, indexMaturityTimes, yearFraction, yearFraction, new double[paymentTimes == null ? 0 : paymentTimes.length], notional,
  //        fundingCurveName, liborCurveName, isPayer);
  //  }

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
  public AnnuityCouponIborSpread(Currency currency, final double[] paymentTimes, final double[] indexFixingTimes, IborIndex index, final double[] indexStartTimes, final double[] indexMaturityTimes,
      final double[] paymentYearFractions, final double[] forwardYearFractions, final double[] spreads, final double notional, final String fundingCurveName, final String liborCurveName,
      boolean isPayer) {
    super(fullSetup(currency, paymentTimes, index, indexFixingTimes, indexStartTimes, indexMaturityTimes, paymentYearFractions, forwardYearFractions, spreads, notional * (isPayer ? -1.0 : 1.0),
        fundingCurveName, liborCurveName));

  }

  private static CouponIborSpread[] fullSetup(Currency currency, final double[] paymentTimes, IborIndex index, final double[] indexFixingTimes, final double[] indexStartTimes,
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

    final CouponIborSpread[] res = new CouponIborSpread[n];
    for (int i = 0; i < n; i++) {
      res[i] = new CouponIborSpread(currency, paymentTimes[i], fundingCurveName, paymentYearFraction[i], notional, indexFixingTimes[i], index, indexStartTimes[i], indexMaturityTimes[i],
          forwardYearFraction[i], spreads[i], liborCurveName);
    }
    return res;
  }

  public AnnuityCouponIborSpread withZeroSpread() {
    final int n = getNumberOfPayments();
    final CouponIborSpread[] temp = new CouponIborSpread[n];
    for (int i = 0; i < n; i++) {
      temp[i] = getNthPayment(i).withZeroSpread();
    }
    return new AnnuityCouponIborSpread(temp);
  }

  public AnnuityCouponFixed withUnitCoupons() {
    final int n = getNumberOfPayments();
    final CouponFixed[] temp = new CouponFixed[n];
    for (int i = 0; i < n; i++) {
      temp[i] = getNthPayment(i).withUnitCoupon();
    }
    return new AnnuityCouponFixed(temp);
  }

  public AnnuityCouponIborSpread withSpread(final double rate) {
    final int n = getNumberOfPayments();
    final CouponIborSpread[] temp = new CouponIborSpread[n];
    for (int i = 0; i < n; i++) {
      temp[i] = getNthPayment(i).withSpread(rate);
    }
    return new AnnuityCouponIborSpread(temp);
  }

  @Override
  public <S, T> T accept(InstrumentDerivativeVisitor<S, T> visitor, S data) {
    return visitor.visitGenericAnnuity(this, data);
  }

  @Override
  public <T> T accept(InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitGenericAnnuity(this);
  }

}
