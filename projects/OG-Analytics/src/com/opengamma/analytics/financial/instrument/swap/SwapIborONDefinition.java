/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponOISDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing an Ibor for overnight swap. Both legs are in the same currency. 
 * The payment dates on the ibor leg a slightly different from the FixedIbor swap due to the lag in payment at the end of each coupon.
 */
public class SwapIborONDefinition extends SwapDefinition {

  /**
   * Constructor of the fixed-OIS swap from its two legs.
   * @param iborLeg The Ibor leg.
   * @param oisLeg The OIS leg.
   */
  public SwapIborONDefinition(final AnnuityCouponIborSpreadDefinition iborLeg, final AnnuityCouponOISDefinition oisLeg) {
    super(iborLeg, oisLeg);
    Validate.isTrue(iborLeg.getCurrency() == oisLeg.getCurrency(), "Legs should have the same currency");
  }

  /**
   * Builder of Ibor/ON swap from financial description (start date and tenor).
   * @param settlementDate The settlement date.
   * @param tenorSwap The total tenor of the swap.
   * @param generator The Ibor/ON generator.
   * @param notional The swap notional.
   * @param spread The spread on the Ibor leg.
   * @param isPayer The flag indicating if first leg (Ibor) is payer (true) or receiver (false).
   * @return The swap.
   */
  public static SwapIborONDefinition from(final ZonedDateTime settlementDate, final Period tenorSwap, final GeneratorSwapIborON generator, final double notional, final double spread,
      final boolean isPayer) {
    AnnuityCouponOISDefinition oisLeg = AnnuityCouponOISDefinition.from(settlementDate, tenorSwap, notional, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    double notionalSigned = sign * notional;
    return from(oisLeg, notionalSigned, generator.getIndexIbor(), spread);
  }

  /**
   * Builder of OIS swap from financial description (start date and end date).
   * @param settlementDate The annuity settlement or first fixing date.
   * @param endFixingPeriodDate  The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The Ibor/ON generator.
   * @param spread The spread on the Ibor leg.
   * @param isPayer The flag indicating if first leg (Ibor) is payer (true) or receiver (false).
   * @return The swap.
   */
  public static SwapIborONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final GeneratorSwapIborON generator, final double spread,
      final boolean isPayer) {
    AnnuityCouponOISDefinition oisLeg = AnnuityCouponOISDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    double notionalSigned = sign * notional;
    return from(oisLeg, notionalSigned, generator.getIndexIbor(), spread);
  }

  /**
   * Builder of OIS swap from financial description (start date and end date, the fixed leg and floating leg notionals can be different).
   * @param settlementDate The annuity settlement or first fixing date.
   * @param endFixingPeriodDate  The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notionalFixed The notional of the fixed leg.
   * @param notionalOIS The notional of the OIS leg.
   * @param generator The Ibor/ON generator.
   * @param spread The spread on the Ibor leg.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The swap.
   */
  public static SwapIborONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notionalFixed, final double notionalOIS,
      final GeneratorSwapIborON generator, final double spread, final boolean isPayer) {
    AnnuityCouponOISDefinition oisLeg = AnnuityCouponOISDefinition.from(settlementDate, endFixingPeriodDate, notionalOIS, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    double notionalSigned = sign * notionalFixed;
    return from(oisLeg, notionalSigned, generator.getIndexIbor(), spread);
  }

  private static SwapIborONDefinition from(final AnnuityCouponOISDefinition oisLeg, final double notionalSigned, final IborIndex indexIbor, final double spread) {
    CouponIborSpreadDefinition[] cpnIbor = new CouponIborSpreadDefinition[oisLeg.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < oisLeg.getNumberOfPayments(); loopcpn++) {
      ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), indexIbor.getSpotLag(), indexIbor.getCalendar());
      cpnIbor[loopcpn] = new CouponIborSpreadDefinition(oisLeg.getCurrency(), oisLeg.getNthPayment(loopcpn).getPaymentDate(), oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), oisLeg
          .getNthPayment(loopcpn).getAccrualEndDate(), oisLeg.getNthPayment(loopcpn).getPaymentYearFraction(), notionalSigned, fixingDate, indexIbor, spread);
    }
    return new SwapIborONDefinition(new AnnuityCouponIborSpreadDefinition(cpnIbor), oisLeg);
  }

  /**
   * The Ibor leg of the swap.
   * @return The leg.
   */
  public AnnuityCouponIborSpreadDefinition getIborLeg() {
    return (AnnuityCouponIborSpreadDefinition) getFirstLeg();
  }

  /**
   * The ON leg of the swap.
   * @return The leg.
   */
  public AnnuityCouponOISDefinition getOISLeg() {
    return (AnnuityCouponOISDefinition) getSecondLeg();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Swap<Coupon, Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    // Curves should be: discounting, ibor, ois
    final Annuity<? extends Coupon> iborLeg = this.getIborLeg().toDerivative(date, yieldCurveNames);
    final Annuity<? extends Coupon> oisLeg = (Annuity<? extends Coupon>) this.getOISLeg().toDerivative(date, new String[] {yieldCurveNames[0], yieldCurveNames[2]});
    return new Swap<Coupon, Coupon>((Annuity<Coupon>) iborLeg, (Annuity<Coupon>) oisLeg);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Swap<Coupon, Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] indexDataTS, final String... yieldCurveNames) {
    // Curves should be: discounting, ibor, ois
    Validate.notNull(indexDataTS, "index data time series array");
    Validate.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    final Annuity<? extends Coupon> iborLeg = this.getIborLeg().toDerivative(date, indexDataTS[0], yieldCurveNames);
    final Annuity<? extends Coupon> oisLeg = this.getOISLeg().toDerivative(date, indexDataTS[1], new String[] {yieldCurveNames[0], yieldCurveNames[2]});
    return new Swap<Coupon, Coupon>((Annuity<Coupon>) iborLeg, (Annuity<Coupon>) oisLeg);
  }

}
