/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CDSCoupon {
  private final double _paymentTime;
  private final double _yearFrac;
  private final double _effStart;
  private final double _effEnd;
  private final double _ycRatio;

  public CDSCoupon(final LocalDate tradeDate, final LocalDate[] couponDates, final boolean protectionFromStartOfDay, final DayCount accrualDCC, final DayCount curveDCC) {
    this(tradeDate, couponDates[0], couponDates[1], couponDates[2], protectionFromStartOfDay, accrualDCC, curveDCC);
  }

  public CDSCoupon(final LocalDate tradeDate, final LocalDate accStart, final LocalDate accEnd, final LocalDate paymentDate, final boolean protectionFromStartOfDay, final DayCount accrualDCC,
      final DayCount curveDCC) {
    ArgumentChecker.notNull(tradeDate, "tradeDate");
    ArgumentChecker.notNull(paymentDate, "paymentTime");
    ArgumentChecker.notNull(accStart, "accStart");
    ArgumentChecker.notNull(accEnd, "accEnd");
    ArgumentChecker.notNull(accrualDCC, "accrualDCC");
    ArgumentChecker.notNull(curveDCC, "curveDCC");
    ArgumentChecker.isTrue(accEnd.isAfter(accStart), "require accEnd after accStart");
    ArgumentChecker.isTrue(paymentDate.isAfter(accStart), "require paymentDate after accStart");
    ArgumentChecker.isFalse(tradeDate.isAfter(paymentDate), "coupon payment is in the past");
    _paymentTime = curveDCC.getDayCountFraction(tradeDate, paymentDate);
    final LocalDate effStart = protectionFromStartOfDay ? accStart.minusDays(1) : accStart;
    final LocalDate effEnd = protectionFromStartOfDay ? accEnd.minusDays(1) : accEnd;
    _yearFrac = accrualDCC.getDayCountFraction(effStart, effEnd);
    _ycRatio = _yearFrac / curveDCC.getDayCountFraction(effStart, effEnd);
    _effStart = effStart.isBefore(tradeDate) ? -curveDCC.getDayCountFraction(effStart, tradeDate) : curveDCC.getDayCountFraction(tradeDate, effStart);
    _effEnd = curveDCC.getDayCountFraction(tradeDate, effEnd);
  }

  public CDSCoupon(final double paymentTime, final double effAccStart, final double effAccEnd, final double accFrac, final double accRatio) {
    ArgumentChecker.isTrue(effAccEnd > effAccStart, "require accEnd>accStart");
    ArgumentChecker.isTrue(paymentTime > effAccStart, "require paymentTime>accStart");
    _paymentTime = paymentTime;
    _yearFrac = accFrac;
    _effStart = effAccStart;
    _effEnd = effAccEnd;
    _ycRatio = accRatio;
  }

  public CDSCoupon(final CDSCoupon other) {
    ArgumentChecker.notNull(other, "other");
    _paymentTime = other._paymentTime;
    _yearFrac = other._yearFrac;
    _effStart = other._effStart;
    _effEnd = other._effEnd;
    _ycRatio = other._ycRatio;
  }

  /**
   * Gets the paymentTime.
   * @return the paymentTime
   */
  public double getPaymentTime() {
    return _paymentTime;
  }

  /**
   * Gets the yearFrac.
   * @return the yearFrac
   */
  public double getYearFrac() {
    return _yearFrac;
  }

  /**
   * Gets the effStart.
   * @return the effStart
   */
  public double getEffStart() {
    return _effStart;
  }

  /**
   * Gets the effEnd.
   * @return the effEnd
   */
  public double getEffEnd() {
    return _effEnd;
  }

  /**
   * Gets the year fraction ratio.
   * @return the year fraction ratio
   */
  public double getYFRatio() {
    return _ycRatio;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_effEnd);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_effStart);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_paymentTime);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_ycRatio);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_yearFrac);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final CDSCoupon other = (CDSCoupon) obj;
    if (Double.doubleToLongBits(_effEnd) != Double.doubleToLongBits(other._effEnd)) {
      return false;
    }
    if (Double.doubleToLongBits(_effStart) != Double.doubleToLongBits(other._effStart)) {
      return false;
    }
    if (Double.doubleToLongBits(_paymentTime) != Double.doubleToLongBits(other._paymentTime)) {
      return false;
    }
    if (Double.doubleToLongBits(_ycRatio) != Double.doubleToLongBits(other._ycRatio)) {
      return false;
    }
    if (Double.doubleToLongBits(_yearFrac) != Double.doubleToLongBits(other._yearFrac)) {
      return false;
    }
    return true;
  }

}
