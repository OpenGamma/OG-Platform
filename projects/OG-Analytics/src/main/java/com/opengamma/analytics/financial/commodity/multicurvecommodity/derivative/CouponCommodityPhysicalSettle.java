/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.multicurvecommodity.derivative;

import com.opengamma.analytics.financial.commodity.multicurvecommodity.underlying.CommodityUnderlying;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Class describing a physical settle commodity coupon.
 */
public class CouponCommodityPhysicalSettle extends CouponCommodity {

  /**
   * The first notice time.
   * Some future contracts have no notice date. The seller of the future have to notice the delivery but without an explicit notice date.
   * In this case the notice time can be null
   */
  private final double _noticeFirstTime;
  /**
   * The last notice time.
   * Some future contracts have no notice date. The seller of the future have to notice the delivery but without an explicit notice date.
   * In this case the notice time can be null
   */
  private final double _noticeLastTime;

  /** 
   * time of first delivery 
   *   
   */
  private final double _firstDeliveryTime;

  /** 
   * Date of last delivery 
   * The delivery is usually done during a month. 
   */
  private final double _lastDeliveryTime;

  public CouponCommodityPhysicalSettle(final double paymentYearFraction, final CommodityUnderlying underlying, final String unitName, final double notional, final double settlementTime,
      final Calendar calendar, final double noticeFirstTime, final double noticeLastTime, final double firstDeliveryTime, final double lastDeliveryTime) {
    super(paymentYearFraction, underlying, unitName, notional, settlementTime, calendar);
    _noticeFirstTime = noticeFirstTime;
    _noticeLastTime = noticeLastTime;
    _firstDeliveryTime = firstDeliveryTime;
    _lastDeliveryTime = lastDeliveryTime;
  }

  /**
   * @return the _noticeFirstTime
   */
  public double getNoticeFirstTime() {
    return _noticeFirstTime;
  }

  /**
   * @return the _noticeLastTime
   */
  public double getNoticeLastTime() {
    return _noticeLastTime;
  }

  /**
   * @return the _firstDeliveryTime
   */
  public double getFirstDeliveryTime() {
    return _firstDeliveryTime;
  }

  /**
   * @return the _lastDeliveryTime
   */
  public double getLastDeliveryTime() {
    return _lastDeliveryTime;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_firstDeliveryTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lastDeliveryTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_noticeFirstTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_noticeLastTime);
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CouponCommodityPhysicalSettle other = (CouponCommodityPhysicalSettle) obj;
    if (Double.doubleToLongBits(_firstDeliveryTime) != Double.doubleToLongBits(other._firstDeliveryTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_lastDeliveryTime) != Double.doubleToLongBits(other._lastDeliveryTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_noticeFirstTime) != Double.doubleToLongBits(other._noticeFirstTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_noticeLastTime) != Double.doubleToLongBits(other._noticeLastTime)) {
      return false;
    }
    return true;
  }

}
