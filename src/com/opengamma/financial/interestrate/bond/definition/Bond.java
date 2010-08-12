/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.annuity.definition.FixedAnnuity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Bond implements InterestRateDerivative {

  private final FixedAnnuity _annuity;

  public Bond(final double[] paymentTimes, final double couponRate, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    final double[] modCoupons = new double[n];
    double[] yearFractions = new double[n];

    for (int i = 0; i < n; i++) {
      yearFractions[i] = paymentTimes[i] - (i == 0 ? 0.0 : paymentTimes[i - 1]);
      modCoupons[i] = couponRate + (i == (n - 1) ? 1.0 / yearFractions[i] : 0.0);
    }
    _annuity = new FixedAnnuity(paymentTimes, 1.0, modCoupons, yearFractions, yieldCurveName);
  }

  public Bond(final double[] paymentTimes, final double couponRate, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.notNull(yearFractions, "year fractions");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    ArgumentChecker.notEmpty(yearFractions, "year fractions");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    if (n != yearFractions.length) {
      throw new IllegalArgumentException("Must have a year fraction for each payment time");
    }
    final double[] modCoupons = new double[n];
    for (int i = 0; i < n; i++) {
      modCoupons[i] = couponRate + (i == (n - 1) ? 1.0 / yearFractions[i] : 0.0);
    }
    _annuity = new FixedAnnuity(paymentTimes, 1.0, modCoupons, yearFractions, yieldCurveName);
  }

  public Bond(final double[] paymentTimes, final double[] coupons, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.notNull(coupons, "coupons");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    ArgumentChecker.notEmpty(coupons, "coupons");
    Validate.notNull(yieldCurveName, "yield curve name");
    if (paymentTimes.length != coupons.length) {
      throw new IllegalArgumentException("Must have a payment for each payment time");
    }
    final int n = paymentTimes.length;
    final double[] modCoupons = new double[n];
    double[] yearFractions = new double[n];
    for (int i = 0; i < n; i++) {
      yearFractions[i] = paymentTimes[i] - (i == 0 ? 0.0 : paymentTimes[i - 1]);
      modCoupons[i] = coupons[i] + (i == (n - 1) ? 1.0 / yearFractions[i] : 0.0);
    }
    _annuity = new FixedAnnuity(paymentTimes, 1.0, modCoupons, yearFractions, yieldCurveName);
  }

  public Bond(final double[] paymentTimes, final double[] coupons, final double[] yearFractions, final String yieldCurveName) {
    Validate.notNull(paymentTimes, "payment times");
    Validate.notNull(coupons, "coupons");
    Validate.notNull(yearFractions, "year fractions");
    ArgumentChecker.notEmpty(paymentTimes, "payment times");
    ArgumentChecker.notEmpty(coupons, "coupons");
    ArgumentChecker.notEmpty(yearFractions, "year fractions");
    Validate.notNull(yieldCurveName, "yield curve name");
    final int n = paymentTimes.length;
    if (n != coupons.length) {
      throw new IllegalArgumentException("Must have a payment for each payment time");
    }
    if (n != yearFractions.length) {
      throw new IllegalArgumentException("Must have a year fraction for each payment time");
    }
    final double[] modCoupons = new double[n];
    for (int i = 0; i < n; i++) {
      modCoupons[i] = coupons[i] + (i == (n - 1) ? 1.0 / yearFractions[i] : 0.0);
    }
    _annuity = new FixedAnnuity(paymentTimes, 1.0, modCoupons, yearFractions, yieldCurveName);
  }

  public double[] getPaymentTimes() {
    return _annuity.getPaymentTimes();
  }

  public double[] getPayments() {
    return _annuity.getPaymentAmounts();
  }

  public String getCurveName() {
    return _annuity.getFundingCurveName();
  }

  public FixedAnnuity getFixedAnnuity() {
    return _annuity;
  }

  public double getMaturity() {
    return _annuity.getPaymentTimes()[_annuity.getNumberOfPayments() - 1];
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_annuity == null) ? 0 : _annuity.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Bond other = (Bond) obj;
    if (!ObjectUtils.equals(_annuity, other._annuity)) {
      return false;
    }
    return true;
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<T> visitor, final YieldCurveBundle curves) {
    return visitor.visitBond(this, curves);
  }
}
