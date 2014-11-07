/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;

/**
 * Utilities to construct specific capital index bonds.
 */
public class BondCapitalIndexSecurityDefinitionUtils {
  
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> fromBrazilType(
      IndexPrice priceIndex, ZonedDateTime startDate, double indexStartValue, 
      ZonedDateTime firstCouponDate, ZonedDateTime maturityDate, Period couponPeriod, double notional, double realRate, 
      BusinessDayConvention businessDay, int settlementDays, Calendar calendar, DayCount dayCount, LegalEntity issuer) {
    YieldConvention yieldConvention = SimpleYieldConvention.BRAZIL_IL_BOND;
    /** Notional description */
    CouponInflationZeroCouponMonthlyGearingDefinition nominalPayment = 
        CouponInflationZeroCouponMonthlyGearingDefinition.from(startDate, maturityDate, notional, priceIndex, indexStartValue,
        1, 1, true, 1.0); // The reference month is the month before the coupon payment, hence the 1 month lag.
    AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> nominalAnnuity = new AnnuityDefinition<>(
        new CouponInflationZeroCouponMonthlyGearingDefinition[] {nominalPayment }, calendar);
    /** Coupons description */
    long couponPerYear = 12 / couponPeriod.toTotalMonths();
    ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(firstCouponDate, 
        maturityDate, couponPeriod, true, false);
    ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, 
        calendar, false);
    CouponInflationZeroCouponMonthlyGearingDefinition[] coupons = 
        new CouponInflationZeroCouponMonthlyGearingDefinition[paymentDates.length + 1];
    ZonedDateTime firstCouponAdjusted = businessDay.adjustDate(calendar, firstCouponDate);
    double couponPaid = Math.pow(1.0d + realRate, 1.0d / couponPerYear) - 1.0d; // Coupon is quoted on a period rate basis.
    coupons[0] = CouponInflationZeroCouponMonthlyGearingDefinition.from(firstCouponAdjusted, startDate, firstCouponDate, 
        notional, priceIndex, indexStartValue, 1, 1, true, couponPaid);
    // The coupon for short periods is not adapted; a full coupon is paid.
    coupons[1] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[0], firstCouponDate, 
        paymentDatesUnadjusted[0], notional, priceIndex, indexStartValue, 1, 1, true, couponPaid);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn + 1] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[loopcpn], 
          paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], notional, priceIndex,
              indexStartValue, 1, 1, true, couponPaid);
    }
    AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> couponAnnuity = 
        new AnnuityDefinition<>(coupons, calendar);
    return new BondCapitalIndexedSecurityDefinition<>(nominalAnnuity, couponAnnuity, indexStartValue, 
        0, settlementDays, calendar, dayCount, yieldConvention, false, 1, issuer);
  }

}
