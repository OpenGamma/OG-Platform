/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponIborSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborON;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.payment.CouponIborSpreadDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.swap.derivative.Swap;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

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
  public SwapIborONDefinition(final AnnuityCouponIborSpreadDefinition iborLeg, final AnnuityCouponONDefinition oisLeg) {
    super(iborLeg, oisLeg);
    ArgumentChecker.isTrue(iborLeg.getCurrency() == oisLeg.getCurrency(), "Legs should have the same currency");
  }

  /**
   * Builder of Ibor/ON swap from financial description (start date and tenor).
   * @param settlementDate The settlement date.
   * @param tenorSwap The total tenor of the swap.
   * @param generator The Ibor/ON generator.
   * @param notional The swap notional.
   * @param spread The spread on the Ibor leg.
   * @param isPayer The flag indicating if first leg (Ibor) is payer (true) or receiver (false).
   * @param iborCalendar The holiday calendar for the ibor leg.
   * @return The swap.
   */
  public static SwapIborONDefinition from(final ZonedDateTime settlementDate, final Period tenorSwap, final GeneratorSwapIborON generator, final double notional, final double spread,
      final boolean isPayer, final Calendar iborCalendar) {
    final AnnuityCouponONDefinition oisLeg = AnnuityCouponONDefinition.from(settlementDate, tenorSwap, notional, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    return from(oisLeg, notionalSigned, generator.getIndexIbor(), spread, iborCalendar);
  }

  /**
   * Builder of OIS swap from financial description (start date and end date).
   * @param settlementDate The annuity settlement or first fixing date.
   * @param endFixingPeriodDate  The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The Ibor/ON generator.
   * @param spread The spread on the Ibor leg.
   * @param isPayer The flag indicating if first leg (Ibor) is payer (true) or receiver (false).
   * @param calendar The holiday calendar for the ibor leg.
   * @return The swap.
   */
  public static SwapIborONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final GeneratorSwapIborON generator, final double spread,
      final boolean isPayer, final Calendar calendar) {
    final AnnuityCouponONDefinition oisLeg = AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    return from(oisLeg, notionalSigned, generator.getIndexIbor(), spread, calendar);
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
    final AnnuityCouponONDefinition oisLeg = AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDate, notionalOIS, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notionalFixed;
    return from(oisLeg, notionalSigned, generator.getIndexIbor(), spread, generator.getIborCalendar());
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
  public static SwapIborONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final NotionalProvider notionalFixed, final NotionalProvider notionalOIS,
                                          final GeneratorSwapIborON generator, final double spread, final boolean isPayer) {
    final AnnuityCouponONDefinition oisLeg = AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDate, notionalOIS, generator, !isPayer);
    return from(oisLeg, notionalFixed, generator.getIndexIbor(), spread, generator.getIborCalendar(), isPayer);
  }

  private static SwapIborONDefinition from(final AnnuityCouponONDefinition oisLeg, final double notionalSigned, final IborIndex indexIbor, final double spread,
      final Calendar calendar) {
    final CouponIborSpreadDefinition[] cpnIbor = new CouponIborSpreadDefinition[oisLeg.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < oisLeg.getNumberOfPayments(); loopcpn++) {
      final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), indexIbor.getSpotLag(), calendar);
      cpnIbor[loopcpn] = new CouponIborSpreadDefinition(oisLeg.getCurrency(), oisLeg.getNthPayment(loopcpn).getPaymentDate(), oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), oisLeg
          .getNthPayment(loopcpn).getAccrualEndDate(), oisLeg.getNthPayment(loopcpn).getPaymentYearFraction(), notionalSigned, fixingDate, indexIbor, spread, calendar);
    }
    return new SwapIborONDefinition(new AnnuityCouponIborSpreadDefinition(cpnIbor, calendar), oisLeg);
  }

  private static SwapIborONDefinition from(final AnnuityCouponONDefinition oisLeg, final NotionalProvider notional, final IborIndex indexIbor, final double spread,
                                           final Calendar calendar, boolean isPayer) {
    final double sign = isPayer ? -1 : 1;
    final CouponIborSpreadDefinition[] cpnIbor = new CouponIborSpreadDefinition[oisLeg.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < oisLeg.getNumberOfPayments(); loopcpn++) {
      final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), indexIbor.getSpotLag(), calendar);
      cpnIbor[loopcpn] = new CouponIborSpreadDefinition(oisLeg.getCurrency(), oisLeg.getNthPayment(loopcpn).getPaymentDate(), oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), oisLeg
          .getNthPayment(loopcpn).getAccrualEndDate(), oisLeg.getNthPayment(loopcpn).getPaymentYearFraction(), sign * notional.getAmount(oisLeg.getNthPayment(loopcpn).getAccrualStartDate().toLocalDate()), fixingDate, indexIbor, spread, calendar);
    }
    return new SwapIborONDefinition(new AnnuityCouponIborSpreadDefinition(cpnIbor, calendar), oisLeg);
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
  public AnnuityCouponONDefinition getOISLeg() {
    return (AnnuityCouponONDefinition) getSecondLeg();
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  @Override
  public Swap<Coupon, Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    // Curves should be: discounting, ibor, ois
    final Annuity<? extends Coupon> iborLeg = this.getIborLeg().toDerivative(date, yieldCurveNames);
    final Annuity<? extends Coupon> oisLeg = (Annuity<? extends Coupon>) this.getOISLeg().toDerivative(date, new String[] {yieldCurveNames[0], yieldCurveNames[2] });
    return new Swap<>((Annuity<Coupon>) iborLeg, (Annuity<Coupon>) oisLeg);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  @Override
  public Swap<Coupon, Coupon> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS, final String... yieldCurveNames) {
    // Curves should be: discounting, ibor, ois
    ArgumentChecker.notNull(indexDataTS, "index data time series array");
    ArgumentChecker.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    final Annuity<? extends Coupon> iborLeg = this.getIborLeg().toDerivative(date, indexDataTS[0], yieldCurveNames);
    final Annuity<? extends Coupon> oisLeg = this.getOISLeg().toDerivative(date, indexDataTS[1], new String[] {yieldCurveNames[0], yieldCurveNames[2] });
    return new Swap<>((Annuity<Coupon>) iborLeg, (Annuity<Coupon>) oisLeg);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Swap<Coupon, Coupon> toDerivative(final ZonedDateTime date) {
    final Annuity<? extends Coupon> iborLeg = this.getIborLeg().toDerivative(date);
    final Annuity<? extends Coupon> oisLeg = (Annuity<? extends Coupon>) this.getOISLeg().toDerivative(date);
    return new Swap<>((Annuity<Coupon>) iborLeg, (Annuity<Coupon>) oisLeg);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Swap<Coupon, Coupon> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS) {
    ArgumentChecker.notNull(indexDataTS, "index data time series array");
    ArgumentChecker.isTrue(indexDataTS.length > 1, "index data time series must contain at least two elements");
    final Annuity<? extends Coupon> iborLeg = this.getIborLeg().toDerivative(date, indexDataTS[0]);
    final Annuity<? extends Coupon> oisLeg = this.getOISLeg().toDerivative(date, indexDataTS[1]);
    return new Swap<>((Annuity<Coupon>) iborLeg, (Annuity<Coupon>) oisLeg);
  }
}
