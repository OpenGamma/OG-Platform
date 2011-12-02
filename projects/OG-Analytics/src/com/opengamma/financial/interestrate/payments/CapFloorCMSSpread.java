/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.IndexSwap;
import com.opengamma.financial.instrument.payment.CapFloor;
import com.opengamma.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;

/**
 * Class describing a caplet/floorlet on CMS spread. The notional is positive for long the option and negative for short the option.
 * The pay-off of the instrument is a cap/floor on the difference between the first CMS rate and the second CMS rate. 
 * Both swaps underlying the CMS need to have the same settlement date.
 */
public class CapFloorCMSSpread extends CouponFloating implements CapFloor {

  /**
   * The swap underlying the first CMS. The rate and notional are not used. The swap should be of vanilla type.
   */
  private final FixedCouponSwap<? extends Payment> _underlyingSwap1;
  /**
   * The index associated to the first CMS.
   */
  private final IndexSwap _cmsIndex1;
  /**
   * The swap underlying the second CMS. The rate and notional are not used. The swap should be of vanilla type.
   */
  private final FixedCouponSwap<? extends Payment> _underlyingSwap2;
  /**
   * The index associated to the second CMS.
   */
  private final IndexSwap _cmsIndex2;
  /**
   * The time (in years) to underlying swap settlement.
   */
  private final double _settlementTime;
  /**
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;

  /**
   * 
   * @param currency The payment currency.
   * @param paymentTime Time (in years) up to the payment.
   * @param paymentYearFraction The year fraction (or accrual factor) for the coupon payment.
   * @param notional Coupon notional.
   * @param fixingTime Time (in years) up to fixing.
   * @param underlyingSwap1 A swap describing the CMS underlying. The rate and notional are not used. The swap should be of vanilla type.
   * @param cmsIndex1 The index associated to the first CMS.
   * @param underlyingSwap2 A swap describing the CMS underlying. The rate and notional are not used. The swap should be of vanilla type.
   * @param cmsIndex2 The index associated to the first CMS.
   * @param settlementTime The time (in years) to underlying swap settlement.
   * @param strike The strike.
   * @param isCap The cap (true) /floor (false) flag.
   * @param fundingCurveName The discounting curve name. Should be compatible with the swaps dicsounting curve.
   */
  public CapFloorCMSSpread(final Currency currency, final double paymentTime, final double paymentYearFraction, final double notional, final double fixingTime,
      final FixedCouponSwap<? extends Payment> underlyingSwap1, final IndexSwap cmsIndex1, final FixedCouponSwap<? extends Payment> underlyingSwap2, final IndexSwap cmsIndex2,
      final double settlementTime, final double strike, final boolean isCap, final String fundingCurveName) {
    super(currency, paymentTime, fundingCurveName, paymentYearFraction, notional, fixingTime);
    Validate.notNull(underlyingSwap1, "underlying swap");
    Validate.isTrue(underlyingSwap1.isIborOrFixed(), "underlying swap not of vanilla type");
    Validate.notNull(underlyingSwap2, "underlying swap");
    Validate.isTrue(underlyingSwap2.isIborOrFixed(), "underlying swap not of vanilla type");
    Validate.isTrue(fundingCurveName == underlyingSwap1.getFixedLeg().getDiscountCurve(), "coherence in pricing");
    Validate.isTrue(fundingCurveName == underlyingSwap2.getFixedLeg().getDiscountCurve(), "coherence in pricing");
    _underlyingSwap1 = underlyingSwap1;
    _cmsIndex1 = cmsIndex1;
    _underlyingSwap2 = underlyingSwap2;
    _cmsIndex2 = cmsIndex2;
    _settlementTime = settlementTime;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Builder from a floating coupon, the CMS details and the strike and cap/floor flag.
   * @param coupon A floating coupon.
   * @param underlyingSwap1 A swap describing the CMS underlying. The rate and notional are not used. The swap should be of vanilla type.
   * @param cmsIndex1 The index associated to the first CMS.
   * @param underlyingSwap2 A swap describing the CMS underlying. The rate and notional are not used. The swap should be of vanilla type.
   * @param cmsIndex2 The index associated to the first CMS.
   * @param settlementTime The time (in years) to underlying swap settlement.
   * @param strike The strike.
   * @param isCap The cap (true) /floor (false) flag.
   * @return The CMS spread cap/floor.
   */
  public static CapFloorCMSSpread from(final CouponFloating coupon, final FixedCouponSwap<Coupon> underlyingSwap1, final IndexSwap cmsIndex1, final FixedCouponSwap<Coupon> underlyingSwap2,
      final IndexSwap cmsIndex2, final double settlementTime, final double strike, final boolean isCap) {
    Validate.notNull(coupon, "floating coupon");
    Validate.isTrue(coupon.getFundingCurveName() == underlyingSwap2.getFixedLeg().getDiscountCurve(), "coherence in pricing");
    return new CapFloorCMSSpread(coupon.getCurrency(), coupon.getPaymentTime(), coupon.getPaymentYearFraction(), coupon.getNotional(), coupon.getFixingTime(), underlyingSwap1, cmsIndex1,
        underlyingSwap2, cmsIndex2, settlementTime, strike, isCap, coupon.getFundingCurveName());
  }

  /**
   * Gets the swap underlying the first CMS.
   * @return The underlying swap.
   */
  public FixedCouponSwap<? extends Payment> getUnderlyingSwap1() {
    return _underlyingSwap1;
  }

  /**
   * Gets the index associated to the first CMS.
   * @return The CMS index.
   */
  public IndexSwap getCmsIndex1() {
    return _cmsIndex1;
  }

  /**
   * Gets the swap underlying the second CMS.
   * @return The underlying swap.
   */
  public FixedCouponSwap<? extends Payment> getUnderlyingSwap2() {
    return _underlyingSwap2;
  }

  /**
   * Gets the index associated to the first CMS.
   * @return The CMS index.
   */
  public IndexSwap getCmsIndex2() {
    return _cmsIndex2;
  }

  /**
   * Gets the underlying swaps settlement time.
   * @return The swaps settlement time.
   */
  public double getSettlementTime() {
    return _settlementTime;
  }

  @Override
  public double getStrike() {
    return _strike;
  }

  @Override
  public boolean isCap() {
    return _isCap;
  }

  @Override
  /**
   * The "fixing" is the difference between the first and the second CMS rates.
   */
  public double payOff(final double fixing) {
    final double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public CapFloorCMSSpread withNotional(double notional) {
    return new CapFloorCMSSpread(getCurrency(), getPaymentTime(), getPaymentYearFraction(), notional, getFixingTime(), _underlyingSwap1, _cmsIndex1, _underlyingSwap2, _cmsIndex2, _settlementTime,
        _strike, _isCap, getFundingCurveName());
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _cmsIndex1.hashCode();
    result = prime * result + _cmsIndex2.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_settlementTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingSwap1.hashCode();
    result = prime * result + _underlyingSwap2.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CapFloorCMSSpread other = (CapFloorCMSSpread) obj;
    if (!ObjectUtils.equals(_cmsIndex1, other._cmsIndex1)) {
      return false;
    }
    if (!ObjectUtils.equals(_cmsIndex2, other._cmsIndex2)) {
      return false;
    }
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_settlementTime) != Double.doubleToLongBits(other._settlementTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap1, other._underlyingSwap1)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingSwap2, other._underlyingSwap2)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitCapFloorCMSSpread(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    return visitor.visitCapFloorCMSSpread(this);
  }

}
