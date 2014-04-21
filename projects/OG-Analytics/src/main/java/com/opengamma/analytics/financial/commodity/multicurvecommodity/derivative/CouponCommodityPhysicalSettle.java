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
   * The first delivery time
   */
  private final double _firstDeliveryTime;

  /** 
   * The last  delivery time 
   * The delivery is usually done during a month. 
   */
  private final double _lastDeliveryTime;

  /**
   * Constructor with all details.
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
  public double getReferenceAmount() {
    return getNotional();
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponCommodityPhysicalSettle(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponCommodityPhysicalSettle(this);
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
