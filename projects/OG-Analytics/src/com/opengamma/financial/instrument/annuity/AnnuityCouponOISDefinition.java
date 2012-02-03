/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.annuity;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.index.GeneratorOIS;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.instrument.payment.CouponOISDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * A wrapper class for a AnnuityDefinition containing CouponOISDefinition.
 */
public class AnnuityCouponOISDefinition extends AnnuityCouponDefinition<CouponOISDefinition> {

  /** Empty array for array conversion of list */
  protected static final Coupon[] EMPTY_ARRAY_COUPON = new Coupon[0];

  /**
   * Constructor from a list of OIS coupons.
   * @param payments The coupons.
   */
  public AnnuityCouponOISDefinition(CouponOISDefinition[] payments) {
    super(payments);
  }

  /**
   * Build a annuity of OIS coupons from financial details.
   * @param settlementDate The annuity settlement or first fixing date.
   * @param tenorAnnuity The total tenor of the annuity.
   * @param notional The annuity notional.
   * @param generator The OIS generator.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The annuity.
   */
  public static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final GeneratorOIS generator, final boolean isPayer) {
    final ZonedDateTime[] endFixingPeriodDate = ScheduleCalculator.getAdjustedDateSchedule(settlementDate, tenorAnnuity, generator.getLegsPeriod(), generator.isStubShort(), generator.isFromEnd(),
        generator.getBusinessDayConvention(), generator.getCalendar(), generator.isEndOfMonth());
    return AnnuityCouponOISDefinition.from(settlementDate, endFixingPeriodDate, notional, generator.getIndex(), isPayer, generator.getSpotLag());
  }

  /**
   * Annuity builder from the financial details.
   * @param settlementDate The settlement date.
   * @param endFixingPeriodDate An array of date with the end fixing period date for each coupon.
   * @param notional The notional.
   * @param index The OIS index.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @param settlementDays The number of days between last fixing of each coupon and the coupon payment (also called spot lag). 
   * @return
   */
  private static AnnuityCouponOISDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime[] endFixingPeriodDate, final double notional, final IndexON index, final boolean isPayer,
      final int settlementDays) {
    final double sign = isPayer ? -1.0 : 1.0;
    double notionalSigned = sign * notional;
    final CouponOISDefinition[] coupons = new CouponOISDefinition[endFixingPeriodDate.length];
    coupons[0] = CouponOISDefinition.from(index, settlementDate, endFixingPeriodDate[0], notionalSigned, settlementDays);
    for (int loopcpn = 1; loopcpn < endFixingPeriodDate.length; loopcpn++) {
      coupons[loopcpn] = CouponOISDefinition.from(index, endFixingPeriodDate[loopcpn - 1], endFixingPeriodDate[loopcpn], notionalSigned, settlementDays);
    }
    return new AnnuityCouponOISDefinition(coupons);
  }

  @Override
  public GenericAnnuity<? extends Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final List<Coupon> resultList = new ArrayList<Coupon>();
    final CouponOISDefinition[] payments = getPayments();
    for (int loopcoupon = 0; loopcoupon < payments.length; loopcoupon++) {
      if (!date.isAfter(payments[loopcoupon].getPaymentDate())) {
        resultList.add(payments[loopcoupon].toDerivative(date, indexFixingTS, yieldCurveNames));
      }
    }
    return new GenericAnnuity<Coupon>(resultList.toArray(EMPTY_ARRAY_COUPON));
  }

}
