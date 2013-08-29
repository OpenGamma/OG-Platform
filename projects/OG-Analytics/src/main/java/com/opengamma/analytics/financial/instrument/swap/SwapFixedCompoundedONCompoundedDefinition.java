/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedCompoundedONCompounded;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedAccruedCompoundingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponONCompoundedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.convention.calendar.Calendar;

/**
 *   Class describing a fixed Accrued Compoundin for ON compounded rate swap. Both legs are in the same currency.
 *   The payment dates on the fixed leg a slightly different from the FixedIbor swap due to the lag in payment at the end of each coupon.
 *   This class is specially developed for Brazilian-like swaps
 */
public class SwapFixedCompoundedONCompoundedDefinition extends SwapDefinition {

  /**
   * Constructor of the swap from its two legs.
   * @param fixedCoupon The swap fixed leg.
   * @param onCoupon The swap on leg.
   * @param calendar The holiday calendar
   */
  public SwapFixedCompoundedONCompoundedDefinition(final CouponFixedAccruedCompoundingDefinition fixedCoupon, final CouponONCompoundedDefinition onCoupon,
      final Calendar calendar) {
    super(new AnnuityDefinition<PaymentDefinition>(new CouponFixedAccruedCompoundingDefinition[] {fixedCoupon }, calendar),
        new AnnuityDefinition<PaymentDefinition>(new CouponONCompoundedDefinition[] {onCoupon }, calendar));
  }

  /**
   * Builder of OIS swap from financial description (start date and tenor).
   * @param settlementDate The annuity settlement or first fixing date.
   * @param tenorAnnuity The total tenor of the annuity.
   * @param notional The annuity notional.
   * @param generator The ON generator.
   * @param fixedRate The rate of the swap fixed leg.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The swap.
   */
  public static SwapFixedCompoundedONCompoundedDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional,
      final GeneratorSwapFixedCompoundedONCompounded generator, final double fixedRate, final boolean isPayer) {
    final CouponONCompoundedDefinition onCompoundedCoupon = CouponONCompoundedDefinition.from(generator, settlementDate, tenorAnnuity, (isPayer ? 1.0 : -1.0) * notional);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    return from(onCompoundedCoupon, notionalSigned, fixedRate, generator.getOvernightCalendar());
  }

  /**
   * Builder of the swap from financial description (start date and end date).
   * @param settlementDate The annuity settlement or first fixing date.
   * @param endFixingPeriodDate  The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The ON generator.
   * @param fixedRate The rate of the swap fixed leg.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The swap.
   */
  public static SwapFixedCompoundedONCompoundedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional,
      final GeneratorSwapFixedCompoundedONCompounded generator, final double fixedRate, final boolean isPayer) {
    final CouponONCompoundedDefinition onCompoundedCoupon = CouponONCompoundedDefinition.from(generator, settlementDate, endFixingPeriodDate, (isPayer ? 1.0 : -1.0) * notional);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    return from(onCompoundedCoupon, notionalSigned, fixedRate, generator.getOvernightCalendar());
  }

  private static SwapFixedCompoundedONCompoundedDefinition from(final CouponONCompoundedDefinition onCoupon, final double notionalSigned, final double fixedRate, final Calendar calendar) {
    final CouponFixedAccruedCompoundingDefinition cpnFixed = new CouponFixedAccruedCompoundingDefinition(onCoupon.getCurrency(), onCoupon.getPaymentDate(), onCoupon.getAccrualStartDate(), onCoupon
        .getAccrualEndDate(), onCoupon.getPaymentYearFraction(), notionalSigned, fixedRate);
    return new SwapFixedCompoundedONCompoundedDefinition(cpnFixed, onCoupon, calendar);
  }

  /**
   * The fixed leg of the swap.
   * @return Fixed leg.
   */
  public AnnuityDefinition<PaymentDefinition> getFixedLeg() {
    return (AnnuityDefinition<PaymentDefinition>) getFirstLeg();
  }

  /**
   * The Ibor leg of the swap.
   * @return Ibor leg.
   */
  public AnnuityDefinition<PaymentDefinition> getONLeg() {
    return (AnnuityDefinition<PaymentDefinition>) getSecondLeg();
  }

}
