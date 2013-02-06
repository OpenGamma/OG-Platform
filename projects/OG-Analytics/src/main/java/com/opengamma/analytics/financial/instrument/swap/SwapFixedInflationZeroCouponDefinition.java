/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import static org.threeten.bp.temporal.ChronoUnit.YEARS;

import org.apache.commons.lang.Validate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a zero-coupon inflation (with interpolated index) swap.
 */
public class SwapFixedInflationZeroCouponDefinition extends SwapDefinition {

  /**
   * Zero-coupon inflation swap constructor for the fixed and inflation coupons.
   * @param fixedCpn The swap fixed leg.
   * @param inflationCpn The swap inflation leg.
   */
  public SwapFixedInflationZeroCouponDefinition(final CouponFixedCompoundingDefinition fixedCpn, final CouponInflationDefinition inflationCpn) {
    super(new AnnuityDefinition<PaymentDefinition>(new CouponFixedCompoundingDefinition[] {fixedCpn }), new AnnuityDefinition<PaymentDefinition>(new CouponInflationDefinition[] {inflationCpn }));
  }

  /**
   * Builder from financial details and the time series of exiting price index values.
   * @param index The price index.
   * @param settlementDate The swap settlement date.
   * @param tenor The swap tenor in years.
   * @param fixedRate The swap fixed rate (annual compounding). The fixed payment is (1+fixedRate)^tenor-1.
   * @param notional The swap notional.
   * @param isPayer The flag 
   * @param businessDayConvention The business day convention used to compute the payment date.
   * @param calendar The calendar used to compute the payment date.
   * @param endOfMonth The end-of-month convention used to compute the payment date.
   * @param monthLag The price index fixing lag in months.
   * @param priceIndexTimeSeries The time series with the relevant price index values.
   * @return The zero coupon inflation swap.
   */
  public static SwapFixedInflationZeroCouponDefinition fromInterpolation(final IndexPrice index, final ZonedDateTime settlementDate, int tenor, double fixedRate, double notional,
      final boolean isPayer, final BusinessDayConvention businessDayConvention, final Calendar calendar, final boolean endOfMonth, final int monthLag,
      final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries) {
    Validate.notNull(index, "Price index");
    Validate.notNull(settlementDate, "Settlement date");
    Validate.notNull(businessDayConvention, "Business day convention");
    Validate.notNull(calendar, "Calendar");
    Validate.notNull(priceIndexTimeSeries, "Time series of price index");
    ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(settlementDate, Period.of(tenor, YEARS), businessDayConvention, calendar, endOfMonth);
    CouponFixedCompoundingDefinition fixedCpn = CouponFixedCompoundingDefinition.from(index.getCurrency(), paymentDate, settlementDate, (isPayer ? -1.0 : 1.0) * notional, Period.of(tenor, YEARS),
        fixedRate);
    CouponInflationZeroCouponInterpolationDefinition inflationCpn = CouponInflationZeroCouponInterpolationDefinition.from(settlementDate, paymentDate, (isPayer ? 1.0 : -1.0) * notional, index,
        priceIndexTimeSeries, monthLag, false);
    return new SwapFixedInflationZeroCouponDefinition(fixedCpn, inflationCpn);
  }

}
