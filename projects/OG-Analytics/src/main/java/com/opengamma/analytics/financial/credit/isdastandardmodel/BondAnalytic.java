/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.apache.log4j.Logger;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple analytic representation of a fixed coupon bond that allows it to be prices consistently with a CDS (i.e. using the ISDA model) 
 */
public class BondAnalytic {
  private static final Logger LOG = Logger.getLogger(BondAnalytic.class);
  private static final DayCount ACT_365 = DayCounts.ACT_365;

  private final double[] _paymentTimes;
  private final double[] _paymentAmounts;
  private final int _nPayments;
  private final double _recoveryRate;

  private final double _accuredInterest;

  /**
   * Simple analytic representation of a fixed coupon bond that allows it to be priced consistently with a CDS (i.e. using the ISDA model) 
   * @param today today's date
   * @param coupon The bond coupon (as a fraction). Can be zero.
   * @param schedule The accrual start and end dates, and the payment dates 
   * @param recoveryRate The expected recovery rate for the bond (this should be the same as that use for the CDS)
   * @param accrualDCC The day count used to calculate the length of an accrual period and thus the amount of the payments 
   */
  public BondAnalytic(final LocalDate today, final double coupon, final ISDAPremiumLegSchedule schedule, final double recoveryRate, final DayCount accrualDCC) {
    this(today, coupon, schedule, recoveryRate, accrualDCC, ACT_365);
  }

  /**
   * Simple analytic representation of a fixed coupon bond that allows it to be priced consistently with a CDS (i.e. using the ISDA model) 
   * @param today today's date
   * @param coupon The bond coupon (as a fraction). Can be zero.
   * @param schedule The accrual start and end dates, and the payment dates 
   * @param recoveryRate The expected recovery rate for the bond (this should be the same as that use for the CDS)
   * @param accrualDCC The day count used to calculate the length of an accrual period and thus the amount of the payments 
   * @param curveDCC Day count used on curve (NOTE ISDA uses ACT/365 (fixed) and it is not recommended to change this)
   */
  public BondAnalytic(final LocalDate today, final double coupon, final ISDAPremiumLegSchedule schedule, final double recoveryRate, final DayCount accrualDCC, final DayCount curveDCC) {
    ArgumentChecker.notNull(today, "today");
    ArgumentChecker.isTrue(coupon >= 0.0, "coupon is negative");
    if (coupon > 1.0) {
      LOG.warn("coupon should be given as fraction. Value of " + coupon + "seems high.");
    }
    ArgumentChecker.notNull(schedule, "schedule");
    ArgumentChecker.isTrue(recoveryRate >= 0.0 && recoveryRate <= 1.0, "recovery rate must be in range 0 - 1. value gives: ", recoveryRate);
    ArgumentChecker.notNull(accrualDCC, "accrualDCC");
    ArgumentChecker.notNull(curveDCC, "curveDCC");
    final ISDAPremiumLegSchedule tSch = schedule.truncateSchedule(today);
    _nPayments = tSch.getNumPayments();
    _paymentTimes = new double[_nPayments];
    _paymentAmounts = new double[_nPayments];
    for (int i = 0; i < _nPayments; i++) {
      _paymentAmounts[i] = coupon * accrualDCC.getDayCountFraction(schedule.getAccStartDate(i), schedule.getAccEndDate(i));
      _paymentTimes[i] = curveDCC.getDayCountFraction(today, schedule.getPaymentDate(i));
    }
    _paymentAmounts[_nPayments - 1] += 1.0;
    _accuredInterest = coupon * accrualDCC.getDayCountFraction(schedule.getAccStartDate(0), today);
    _recoveryRate = recoveryRate;
  }

  /**
   * Simple analytic representation of a fixed coupon bond that allows it to be priced consistently with a CDS (i.e. using the ISDA model) 
   * @param paymentTimes The payment times. Year fraction between today and the payment dates. This should use the same year fraction as the ISDA curves (i.e. normally ACT/365F) 
   * @param paymentAmounts Actual payment amounts paid on the payment dates. The final value should include the return of par. 
   * @param recoveryRate The expected recovery rate for the bond (this should be the same as that use for the CDS)
   * @param accuredInterest Amount of accrued interest on the bond today
   */
  public BondAnalytic(final double[] paymentTimes, final double[] paymentAmounts, final double recoveryRate, final double accuredInterest) {
    ArgumentChecker.notEmpty(paymentTimes, "paymentTimes");
    ArgumentChecker.notNull(paymentAmounts, "paymentAmounts");
    _nPayments = paymentTimes.length;
    ArgumentChecker.isTrue(paymentAmounts.length == _nPayments, "number of payment times ({}) does not match number of payment amounts ({})", _nPayments, paymentAmounts.length);
    ArgumentChecker.isTrue(recoveryRate >= 0.0 && recoveryRate <= 1.0, "recovery rate must be in range 0 - 1. value gives: ", recoveryRate);
    ArgumentChecker.isTrue(accuredInterest >= 0.0, "accrued intrest must be give as positive");
    ArgumentChecker.isTrue(paymentTimes[0] >= 0.0, "payments times must be positive. first value: ", paymentTimes[0]);

    for (int i = 1; i < _nPayments; i++) {
      ArgumentChecker.isTrue(paymentTimes[i] > paymentTimes[i - 1], "payment times must be assending");
    }
    _paymentTimes = paymentTimes.clone();
    _paymentAmounts = paymentAmounts.clone();
    _recoveryRate = recoveryRate;
    _accuredInterest = accuredInterest;
  }

  /**
   * Gets the accrued interest.
   * @return the accuredInterest
   */
  public double getAccruedInterest() {
    return _accuredInterest;
  }

  /**
   * Gets the payment time for the given index.
  * @param index the index 
   * @return the paymentTime
   */
  public double getPaymentTime(final int index) {
    return _paymentTimes[index];
  }

  /**
   * Gets the payment amount for the given index.
   * @param index the index 
   * @return the paymentAmount
   */
  public double getPaymentAmount(final int index) {
    return _paymentAmounts[index];
  }

  /**
   * Gets the number of payments 
   * @return the nPayments
   */
  public int getnPayments() {
    return _nPayments;
  }

  /**
   * Gets the recovery rate.
   * @return the recoveryRate
   */
  public double getRecoveryRate() {
    return _recoveryRate;
  }

}
