/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivativeVisitor;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class VariableAnnuity implements Annuity {

  private final double[] _paymentTimes;
  private final double[] _yearFractions;
  private final double[] _indexFixingTimes;
  private final double[] _indexMaturityTimes;
  // private final double[] _deltaStart;
  //private final double[] _deltaEnd;
  private final double[] _spreads;
  private final double _notional;
  private final int _n;

  private final String _fundingCurveName;
  private final String _liborCurveName;

  /**
   * A basic variable annuity - notional is 1.0, libor fixing and maturities are on payment dates, year fraction is ACT/ACT and spreads are zero
   * @param paymentTimes time in years from now of payments 
   * @param fundingCurveName liborCurveName
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public VariableAnnuity(final double[] paymentTimes, final String fundingCurveName, final String liborCurveName) {
    this(paymentTimes, 1.0, fundingCurveName, liborCurveName);
  }

  /**
   * A basic variable annuity - libor fixing and maturities are on payment dates, year fraction is ACT/ACT and spreads are zero
   * @param paymentTimes time in years from now of payments 
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fundingCurveName liborCurveName
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public VariableAnnuity(final double[] paymentTimes, final double notional, final String fundingCurveName,
      final String liborCurveName) {
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "paymentTime");
    _notional = notional;
    _n = paymentTimes.length;
    _paymentTimes = paymentTimes;
    _indexFixingTimes = new double[_n];
    _indexMaturityTimes = new double[_n];
    _spreads = new double[_n];
    _yearFractions = new double[_n];

    for (int i = 0; i < _n; i++) {
      _indexFixingTimes[i] = (i == 0 ? 0.0 : paymentTimes[i - 1]);
      _indexMaturityTimes[i] = paymentTimes[i];
      _yearFractions[i] = (_indexMaturityTimes[i] - _indexFixingTimes[i]);
    }
    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
  }

  //  /**
  //   * A variable annuity (e.g. the floating leg of a swap) - spreads are set to zero and year fraction is ACT/ACT
  //   * @param paymentTimes time in years from now of payments 
  //   * @param notional the notional amount (OK to set to 1.0) 
  //   * @param fwdStartOffsets offset in years of start of libor fixing date from <b>previous</b> floating payment time (or trade date if spot libor)
  //   * @param fwdEndOffsets  offset in years of end of libor maturity from floating payment time
  //   * @param fundingCurveName  Name of curve from which payments are discounted
  //   * @param liborCurveName Name of curve from which forward rates are calculated
  //   */
  //  public VariableAnnuity(final double[] paymentTimes, final double notional, final double[] fwdStartOffsets,
  //      final double[] fwdEndOffsets, final String fundingCurveName, final String liborCurveName) {
  //    Validate.notNull(fundingCurveName);
  //    Validate.notNull(liborCurveName);
  //    Validate.notNull(paymentTimes);
  //    ArgumentChecker.notEmpty(paymentTimes, "paymentTime");
  //    Validate.notNull(fwdStartOffsets);
  //    ArgumentChecker.notEmpty(fwdStartOffsets, "fwdStartOffsets");
  //    Validate.notNull(fwdEndOffsets);
  //    ArgumentChecker.notEmpty(fwdEndOffsets, "fwdStartOffsets");
  //    _notional = notional;
  //    _n = paymentTimes.length;
  //    Validate.isTrue(fwdStartOffsets.length == _n);
  //    Validate.isTrue(fwdEndOffsets.length == _n);
  //
  //    _paymentTimes = paymentTimes;
  //    _deltaStart = fwdStartOffsets;
  //    _deltaEnd = fwdEndOffsets;
  //    _spreads = new double[_n];
  //    _yearFractions = new double[_n];
  //    _yearFractions[0] = paymentTimes[0];
  //    for (int i = 1; i < _n; i++) {
  //      _yearFractions[i] = (paymentTimes[i] - paymentTimes[i - 1]);
  //    }
  //    _fundingCurveName = fundingCurveName;
  //    _liborCurveName = liborCurveName;
  //  }

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
  public VariableAnnuity(final double[] paymentTimes, final double[] indexFixingTimes,
      final double[] indexMaturityTimes, final double[] yearFraction, final double notional,
      final String fundingCurveName, final String liborCurveName) {

    Validate.notNull(paymentTimes);
    double[] spreads = new double[paymentTimes.length];
    argumentCheck(paymentTimes, indexFixingTimes, indexMaturityTimes, yearFraction, spreads, notional,
        fundingCurveName, liborCurveName);

    _notional = notional;
    _n = paymentTimes.length;
    _paymentTimes = paymentTimes;
    _spreads = spreads;
    _indexFixingTimes = indexFixingTimes;
    _indexMaturityTimes = indexMaturityTimes;
    _yearFractions = yearFraction;
    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
  }

  /**
   * A variable annuity (e.g. the floating leg of a swap).
   * For n payments indexed 0 to n-1, the indexFixingTimes, indexMaturityTimes and yearFractions corresponding to a payment are indexed the same way
   * @param paymentTimes time in years from now of payments 
   * @param indexFixingTimes time in years from now to the fixing dates of the reference index (e.g. Libor) 
   * @param indexMaturityTimes time in years from now to the maturity of the reference rate  
   * @param yearFraction year fractions used to calculate payment amounts and reference rate
   * @param spreads fixed payments on top of variable amounts (can be negative)
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fundingCurveName  Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public VariableAnnuity(final double[] paymentTimes, final double[] indexFixingTimes,
      final double[] indexMaturityTimes, final double[] yearFraction, final double[] spreads, final double notional,
      final String fundingCurveName, final String liborCurveName) {

    argumentCheck(paymentTimes, indexFixingTimes, indexMaturityTimes, yearFraction, spreads, notional,
        fundingCurveName, liborCurveName);

    _notional = notional;
    _n = paymentTimes.length;
    _paymentTimes = paymentTimes;
    _spreads = spreads;
    _indexFixingTimes = indexFixingTimes;
    _indexMaturityTimes = indexMaturityTimes;
    _yearFractions = yearFraction;
    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
  }

  private void argumentCheck(final double[] paymentTimes, final double[] indexFixingTimes,
      final double[] indexMaturityTimes, final double[] yearFraction, final double[] spreads, final double notional,
      final String fundingCurveName, final String liborCurveName) {
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "paymentTime");
    Validate.notNull(yearFraction);
    ArgumentChecker.notEmpty(yearFraction, "year Fraction");
    Validate.notNull(spreads);
    ArgumentChecker.notEmpty(spreads, "spreads");
    Validate.notNull(indexFixingTimes);
    ArgumentChecker.notEmpty(indexFixingTimes, "indexFixingTimes");
    Validate.notNull(indexMaturityTimes);
    ArgumentChecker.notEmpty(indexMaturityTimes, "indexMaturityTimes");

    int n = paymentTimes.length;
    Validate.isTrue(indexFixingTimes.length == n);
    Validate.isTrue(indexMaturityTimes.length == n);
    Validate.isTrue(yearFraction.length == n);
    Validate.isTrue(spreads.length == n);

    //sanity checks
    for (int i = 0; i < n; i++) {
      if (indexFixingTimes[i] >= indexMaturityTimes[i]) {
        throw new IllegalArgumentException("fixing times after maturity times");
      }
      if (indexFixingTimes[i] > paymentTimes[i]) {
        throw new IllegalArgumentException("fixing times after payment times");
      }
    }
  }

  /**
   * Gets the indexFixingTimes field.
   * @return the indexFixingTimes
   */
  public double[] getIndexFixingTimes() {
    return _indexFixingTimes;
  }

  /**
   * Gets the indexMaturityTimes field.
   * @return the indexMaturityTimes
   */
  public double[] getIndexMaturityTimes() {
    return _indexMaturityTimes;
  }

  public double[] getSpreads() {
    return _spreads;
  }

  @Override
  public String getFundingCurveName() {
    return _fundingCurveName;
  }

  public String getLiborCurveName() {
    return _liborCurveName;
  }

  @Override
  public double[] getPaymentTimes() {
    return _paymentTimes;
  }

  @Override
  public double[] getYearFractions() {
    return _yearFractions;
  }

  @Override
  public int getNumberOfPayments() {
    return _n;
  }

  @Override
  public double getNotional() {
    return _notional;
  }

  @Override
  public VariableAnnuity withZeroSpread() {
    return new VariableAnnuity(getPaymentTimes(), getIndexFixingTimes(), getIndexMaturityTimes(), getYearFractions(),
        new double[getNumberOfPayments()], getNotional(), getFundingCurveName(), getLiborCurveName());
  }

  @Override
  public FixedAnnuity withUnitCoupons() {
    final double[] coupons = new double[getNumberOfPayments()];
    for (int i = 0; i < getNumberOfPayments(); i++) {
      coupons[i] = 1.0;
    }
    return new FixedAnnuity(getPaymentTimes(), getNotional(), coupons, getYearFractions(), getFundingCurveName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_fundingCurveName == null) ? 0 : _fundingCurveName.hashCode());
    result = prime * result + Arrays.hashCode(_indexFixingTimes);
    result = prime * result + Arrays.hashCode(_indexMaturityTimes);
    result = prime * result + ((_liborCurveName == null) ? 0 : _liborCurveName.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_paymentTimes);
    result = prime * result + Arrays.hashCode(_spreads);
    result = prime * result + Arrays.hashCode(_yearFractions);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    VariableAnnuity other = (VariableAnnuity) obj;
    if (_fundingCurveName == null) {
      if (other._fundingCurveName != null) {
        return false;
      }
    } else if (!_fundingCurveName.equals(other._fundingCurveName)) {
      return false;
    }
    if (!Arrays.equals(_indexFixingTimes, other._indexFixingTimes)) {
      return false;
    }
    if (!Arrays.equals(_indexMaturityTimes, other._indexMaturityTimes)) {
      return false;
    }
    if (_liborCurveName == null) {
      if (other._liborCurveName != null) {
        return false;
      }
    } else if (!_liborCurveName.equals(other._liborCurveName)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (!Arrays.equals(_paymentTimes, other._paymentTimes)) {
      return false;
    }
    if (!Arrays.equals(_spreads, other._spreads)) {
      return false;
    }
    if (!Arrays.equals(_yearFractions, other._yearFractions)) {
      return false;
    }
    return true;
  }

  @Override
  public <T> T accept(final InterestRateDerivativeVisitor<T> visitor, final YieldCurveBundle curves) {
    return visitor.visitVariableAnnuity(this, curves);
  }

}
