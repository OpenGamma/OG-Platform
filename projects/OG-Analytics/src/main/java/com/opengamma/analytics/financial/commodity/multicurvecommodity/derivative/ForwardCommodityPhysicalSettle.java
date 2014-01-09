/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ForwardCommodityPhysicalSettle extends CouponCommodityPhysicalSettle {

  /**
   * The Forward rate.
   */
  private final double _rate;

  /**
   * Constructor with all details.
   *  @param rate The Forward rate.
   * @param paymentYearFraction The payment year fraction, positive
   * @param underlying The commodity underlying, not null
   * @param unitName name of the unit of the commodity delivered, not null
   * @param notional notional The number of unit
   * @param settlementTime The settlement time, , positive
   * @param calendar The holiday calendar, not null
   * @param noticeFirstTime The first notice time.
   * @param noticeLastTime The last notice time.
   * @param firstDeliveryTime The first delivery time
   * @param lastDeliveryTime The last  delivery time 
   */
  public ForwardCommodityPhysicalSettle(final double rate, final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName, final double notional,
      final double settlementTime,
      final Calendar calendar, final double noticeFirstTime, final double noticeLastTime, final double firstDeliveryTime, final double lastDeliveryTime) {
    super(paymentYearFraction, underlying, unitName, notional, settlementTime, calendar, noticeFirstTime, noticeLastTime, firstDeliveryTime, lastDeliveryTime);
    _rate = rate;
  }

  /**
   * @return the Forward rate
   */
  public double getRate() {
    return _rate;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForwardCommodityPhysicalSettle(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForwardCommodityPhysicalSettle(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ForwardCommodityphysicalSettle [_rate=" + _rate + "]";
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final ForwardCommodityPhysicalSettle other = (ForwardCommodityPhysicalSettle) obj;
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

}
