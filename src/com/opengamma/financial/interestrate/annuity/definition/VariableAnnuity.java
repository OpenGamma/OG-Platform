/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.annuity.definition;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
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
  private final double[] _deltaStart;
  private final double[] _deltaEnd;
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
  public VariableAnnuity(final double[] paymentTimes, final double notional, final String fundingCurveName, final String liborCurveName) {
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "paymentTime");
    _notional = notional;
    _n = paymentTimes.length;
    _paymentTimes = paymentTimes;
    _deltaStart = new double[_n];
    _deltaEnd = new double[_n];
    _spreads = new double[_n];
    _yearFractions = new double[_n];
    _yearFractions[0] = paymentTimes[0];
    for (int i = 1; i < _n; i++) {
      _yearFractions[i] = (paymentTimes[i] - paymentTimes[i - 1]);
    }
    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
  }

  /**
   * A variable annuity (e.g. the floating leg of a swap) - spreads are set to zero and year fraction is ACT/ACT
   * @param paymentTimes time in years from now of payments 
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fwdStartOffsets offset in years of start of libor fixing date from <b>previous</b> floating payment time (or trade date if spot libor)
   * @param fwdEndOffsets  offset in years of end of libor maturity from floating payment time
   * @param fundingCurveName  Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public VariableAnnuity(final double[] paymentTimes, final double notional, final double[] fwdStartOffsets, final double[] fwdEndOffsets, final String fundingCurveName, final String liborCurveName) {
    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "paymentTime");
    Validate.notNull(fwdStartOffsets);
    ArgumentChecker.notEmpty(fwdStartOffsets, "fwdStartOffsets");
    Validate.notNull(fwdEndOffsets);
    ArgumentChecker.notEmpty(fwdEndOffsets, "fwdStartOffsets");
    _notional = notional;
    _n = paymentTimes.length;
    Validate.isTrue(fwdStartOffsets.length == _n);
    Validate.isTrue(fwdEndOffsets.length == _n);

    _paymentTimes = paymentTimes;
    _deltaStart = fwdStartOffsets;
    _deltaEnd = fwdEndOffsets;
    _spreads = new double[_n];
    _yearFractions = new double[_n];
    _yearFractions[0] = paymentTimes[0];
    for (int i = 1; i < _n; i++) {
      _yearFractions[i] = (paymentTimes[i] - paymentTimes[i - 1]);
    }
    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
  }

  /**
   * A variable annuity (e.g. the floating leg of a swap) 
   * @param paymentTimes time in years from now of payments 
   * @param notional the notional amount (OK to set to 1.0) 
   * @param fwdStartOffsets offset in years of start of libor fixing date from <b>previous</b> floating payment time (or trade date if spot libor)
   * @param fwdEndOffsets  offset in years of end of libor maturity from floating payment time
   * @param yearFraction year fractions used to calculate payment amounts and reference rate
   * @param spreads fixed payments on top of variable amounts (can be negative)
   * @param fundingCurveName  Name of curve from which payments are discounted
   * @param liborCurveName Name of curve from which forward rates are calculated
   */
  public VariableAnnuity(final double[] paymentTimes, final double notional, final double[] fwdStartOffsets, final double[] fwdEndOffsets, final double[] yearFraction, final double[] spreads,
      final String fundingCurveName, final String liborCurveName) {

    Validate.notNull(fundingCurveName);
    Validate.notNull(liborCurveName);
    Validate.notNull(paymentTimes);
    ArgumentChecker.notEmpty(paymentTimes, "paymentTime");
    Validate.notNull(yearFraction);
    ArgumentChecker.notEmpty(yearFraction, "year Fraction");
    Validate.notNull(spreads);
    ArgumentChecker.notEmpty(spreads, "spreads");
    Validate.notNull(fwdStartOffsets);
    ArgumentChecker.notEmpty(fwdStartOffsets, "fwdStartOffsets");
    Validate.notNull(fwdEndOffsets);
    ArgumentChecker.notEmpty(fwdEndOffsets, "fwdStartOffsets");
    _notional = notional;
    _n = paymentTimes.length;
    Validate.isTrue(fwdStartOffsets.length == _n);
    Validate.isTrue(fwdEndOffsets.length == _n);
    Validate.isTrue(yearFraction.length == _n);
    Validate.isTrue(spreads.length == _n);
    _paymentTimes = paymentTimes;
    _spreads = spreads;
    _deltaStart = fwdStartOffsets;
    _deltaEnd = fwdEndOffsets;
    _yearFractions = yearFraction;
    _fundingCurveName = fundingCurveName;
    _liborCurveName = liborCurveName;
  }

  public double[] getDeltaStart() {
    return _deltaStart;
  }

  public double[] getDeltaEnd() {
    return _deltaEnd;
  }

  public double[] getSpreads() {
    return _spreads;
  }

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

  public double getNotional() {
    return _notional;
  }

  public VariableAnnuity toZeroSpreadVariableAnnuity() {
    return new VariableAnnuity(getPaymentTimes(), getNotional(), getDeltaStart(), getDeltaEnd(), getYearFractions(), new double[getNumberOfPayments()], getFundingCurveName(), getLiborCurveName());
  }

  public FixedAnnuity toUnitCouponFixedAnnuity() {
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
    result = prime * result + Arrays.hashCode(_deltaEnd);
    result = prime * result + Arrays.hashCode(_deltaStart);
    result = prime * result + ((_fundingCurveName == null) ? 0 : _fundingCurveName.hashCode());
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
    final VariableAnnuity other = (VariableAnnuity) obj;
    if (!Arrays.equals(_deltaEnd, other._deltaEnd)) {
      return false;
    }
    if (!Arrays.equals(_deltaStart, other._deltaStart)) {
      return false;
    }
    if (!ObjectUtils.equals(_fundingCurveName, other._fundingCurveName)) {
      return false;
    }
    if (!ObjectUtils.equals(_liborCurveName, other._liborCurveName)) {
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
