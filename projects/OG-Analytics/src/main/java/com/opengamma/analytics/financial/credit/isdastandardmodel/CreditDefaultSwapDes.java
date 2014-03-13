/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CreditDefaultSwapDes {

  private final CDSCouponDes[] _coupons;
  private final boolean _payAccOnDefault;
  private final DayCount _accrualDayCount;

  public CreditDefaultSwapDes(final LocalDate accStartDate, final LocalDate protectionStartDate, final LocalDate protectionEndDate, final boolean payAccOnDefault, final Period paymentInterval,
      final StubType stubType, final boolean isProtectStart, final BusinessDayConvention businessdayAdjustmentConvention, final Calendar calendar, final DayCount accrualDayCount) {
    ArgumentChecker.notNull(accStartDate, "accStartDate");
    ArgumentChecker.notNull(protectionStartDate, "protectionStartDate");
    ArgumentChecker.notNull(protectionEndDate, "protectionEndDate");
    ArgumentChecker.notNull(paymentInterval, "tenor");
    ArgumentChecker.notNull(stubType, "stubType");
    ArgumentChecker.notNull(businessdayAdjustmentConvention, "businessdayAdjustmentConvention");
    ArgumentChecker.notNull(accrualDayCount, "accuralDayCount");
    ArgumentChecker.isTrue(protectionEndDate.isAfter(protectionStartDate), "protectionEndDate ({}) must be after protectionStartDate ({})", protectionStartDate, protectionEndDate);

    final ISDAPremiumLegSchedule fullPaymentSchedule = new ISDAPremiumLegSchedule(accStartDate, protectionEndDate, paymentInterval, stubType, businessdayAdjustmentConvention, calendar, isProtectStart);
    final ISDAPremiumLegSchedule paymentSchedule = ISDAPremiumLegSchedule.truncateSchedule(protectionStartDate, fullPaymentSchedule);

    _coupons = CDSCouponDes.makeCoupons(paymentSchedule, accrualDayCount);
    _payAccOnDefault = payAccOnDefault;
    _accrualDayCount = accrualDayCount;
  }

  /**
   * Gets the coupons.
   * @return the coupons
   */
  public CDSCouponDes[] getCoupons() {
    return _coupons;
  }

  /**
   * Gets the payAccOnDefault.
   * @return the payAccOnDefault
   */
  public boolean isPayAccOnDefault() {
    return _payAccOnDefault;
  }

  /**
   * Gets the accrualDayCount.
   * @return the accrualDayCount
   */
  public DayCount getAccrualDayCount() {
    return _accrualDayCount;
  }

}
