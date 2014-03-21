/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;

/**
 * A date based description of a CDS accrual period 
 */
public class CDSCouponDes {

  private static final DayCount DEFAULT_ACCURAL_DCC = DayCounts.ACT_360;

  private final LocalDate _accStart;
  private final LocalDate _accEnd;
  private final LocalDate _paymentDate;
  private final double _yearFrac;

  /**
  * Make a set of CDSCouponDes. 
   * @param leg schedule of the accrual periods
   * @return A set of CDSCouponDes
   */
  public static CDSCouponDes[] makeCoupons(final ISDAPremiumLegSchedule leg) {
    return makeCoupons(leg, DEFAULT_ACCURAL_DCC);
  }

  /**
   * Make a set of CDSCouponDes. 
   * @param leg schedule of the accrual periods
   * @param accrualDCC The day count used for the accrual 
   * @return A set of CDSCouponDes
   */
  public static CDSCouponDes[] makeCoupons(final ISDAPremiumLegSchedule leg, final DayCount accrualDCC) {
    ArgumentChecker.notNull(leg, "leg");
    final int n = leg.getNumPayments();
    final CDSCouponDes[] coupons = new CDSCouponDes[n];
    for (int i = 0; i < n; i++) {
      coupons[i] = new CDSCouponDes(leg.getAccStartDate(i), leg.getAccEndDate(i), leg.getPaymentDate(i), accrualDCC);
    }
    return coupons;
  }

  /**
   * A date based description of a CDS accrual period.  The day count used for the accrual is ACT/360
   * @param accStart The start date of the period 
   * @param accEnd The end date of the period 
   * @param paymentDate The payment date for the period 
   */
  public CDSCouponDes(final LocalDate accStart, final LocalDate accEnd, final LocalDate paymentDate) {
    this(accStart, accEnd, paymentDate, DEFAULT_ACCURAL_DCC);
  }

  /**
   * A date based description of a CDS accrual period 
   * @param accStart The start date of the period 
   * @param accEnd The end date of the period 
   * @param paymentDate The payment date for the period 
   * @param accrualDCC The day count used for the accrual 
   */
  public CDSCouponDes(final LocalDate accStart, final LocalDate accEnd, final LocalDate paymentDate, final DayCount accrualDCC) {
    ArgumentChecker.notNull(accStart, "accStart");
    ArgumentChecker.notNull(accEnd, "accEnd");
    ArgumentChecker.notNull(paymentDate, "paymentDate");
    ArgumentChecker.isTrue(accEnd.isAfter(accStart), "accEnd must be after accStart");
    ArgumentChecker.notNull(accrualDCC, "accrualDCC");
    _accStart = accStart;
    _accEnd = accEnd;
    _paymentDate = paymentDate;
    _yearFrac = accrualDCC.getDayCountFraction(accStart, accEnd);
  }

  /**
   * Gets the accStart.
   * @return the accStart
   */
  public LocalDate getAccStart() {
    return _accStart;
  }

  /**
   * Gets the accEnd.
   * @return the accEnd
   */
  public LocalDate getAccEnd() {
    return _accEnd;
  }

  /**
   * Gets the paymentDate.
   * @return the paymentDate
   */
  public LocalDate getPaymentDate() {
    return _paymentDate;
  }

  /**
   * Gets the yearFrac.
   * @return the yearFrac
   */
  public double getYearFrac() {
    return _yearFrac;
  }

}
