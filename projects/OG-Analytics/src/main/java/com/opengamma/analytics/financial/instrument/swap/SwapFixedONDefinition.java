/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.swap;

import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.NotionalProvider;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponONDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.swap.derivative.SwapFixedCoupon;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a fixed for ON rate swap. Both legs are in the same currency.
 * The payment dates on the fixed leg a slightly different from the FixedIbor swap due to the lag in payment at the end of each coupon.
 */
public class SwapFixedONDefinition extends SwapDefinition {

  /**
   * Constructor of the fixed-OIS swap from its two legs.
   * @param fixedLeg The fixed leg.
   * @param oisLeg The OIS leg.
   */
  public SwapFixedONDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityCouponONDefinition oisLeg) {
    super(fixedLeg, oisLeg);
    ArgumentChecker.isTrue(fixedLeg.getCurrency() == oisLeg.getCurrency(), "Legs should have the same currency");
  }

  /**
   * Builder of OIS swap from financial description (start date and tenor).
   * @param settlementDate The annuity settlement or first fixing date.
   * @param tenorAnnuity The total tenor of the annuity.
   * @param notional The annuity notional.
   * @param generator The OIS generator.
   * @param fixedRate The rate of the swap fixed leg.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The swap.
   */
  public static SwapFixedONDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final GeneratorSwapFixedON generator, final double fixedRate,
      final boolean isPayer) {
    final AnnuityCouponONDefinition oisLeg = AnnuityCouponONDefinition.from(settlementDate, tenorAnnuity, notional, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    return from(oisLeg, notionalSigned, fixedRate, generator.getOvernightCalendar());
  }

  /**
   * Builder of OIS swap from financial description (start date and end date).
   * @param settlementDate The annuity settlement or first fixing date.
   * @param endFixingPeriodDate  The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notional The annuity notional.
   * @param generator The OIS generator.
   * @param fixedRate The rate of the swap fixed leg.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The swap.
   */
  public static SwapFixedONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notional, final GeneratorSwapFixedON generator,
      final double fixedRate, final boolean isPayer) {
    final AnnuityCouponONDefinition oisLeg = AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDate, notional, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notional;
    return from(oisLeg, notionalSigned, fixedRate, generator.getOvernightCalendar());
  }

  /**
   * Builder of OIS swap from financial description (start date and end date, the fixed leg and floating leg notionals can be different).
   * @param settlementDate The annuity settlement or first fixing date.
   * @param endFixingPeriodDate  The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notionalFixed The notional of the fixed leg.
   * @param notionalOIS The notional of the OIS leg.
   * @param generator The OIS generator.
   * @param fixedRate The rate of the swap fixed leg.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The swap.
   */
  public static SwapFixedONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final double notionalFixed, final double notionalOIS,
      final GeneratorSwapFixedON generator, final double fixedRate, final boolean isPayer) {
    final AnnuityCouponONDefinition oisLeg = AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDate, notionalOIS, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    final double notionalSigned = sign * notionalFixed;
    return from(oisLeg, notionalSigned, fixedRate, generator.getOvernightCalendar());
  }

  /**
   * Builder of OIS swap from financial description (start date and end date, the fixed leg and floating leg notionals can be different).
   * @param settlementDate The annuity settlement or first fixing date.
   * @param endFixingPeriodDate  The end date of the OIS accrual period. Also called the maturity date of the annuity even if the actual payment can take place one or two days later. Not null.
   * @param notionalFixed The notional of the fixed leg.
   * @param notionalOIS The notional of the OIS leg.
   * @param generator The OIS generator.
   * @param fixedRate The rate of the swap fixed leg.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The swap.
   */
  public static SwapFixedONDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime endFixingPeriodDate, final NotionalProvider notionalFixed, final NotionalProvider notionalOIS,
                                           final GeneratorSwapFixedON generator, final double fixedRate, final boolean isPayer) {
    final AnnuityCouponONDefinition oisLeg = AnnuityCouponONDefinition.from(settlementDate, endFixingPeriodDate, notionalOIS, generator, !isPayer);
    return from(oisLeg, notionalFixed, fixedRate, generator.getOvernightCalendar(), isPayer);
  }

  private static SwapFixedONDefinition from(final AnnuityCouponONDefinition oisLeg, final double notionalSigned, final double fixedRate, final Calendar calendar) {
    final CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[oisLeg.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < oisLeg.getNumberOfPayments(); loopcpn++) {
      cpnFixed[loopcpn] = new CouponFixedDefinition(oisLeg.getCurrency(), oisLeg.getNthPayment(loopcpn).getPaymentDate(), oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), oisLeg.getNthPayment(
          loopcpn).getAccrualEndDate(), oisLeg.getNthPayment(loopcpn).getPaymentYearFraction(), notionalSigned, fixedRate);
    }
    return new SwapFixedONDefinition(new AnnuityCouponFixedDefinition(cpnFixed, calendar), oisLeg);
  }

  private static SwapFixedONDefinition from(final AnnuityCouponONDefinition oisLeg, final NotionalProvider notional, final double fixedRate, final Calendar calendar, final boolean isPayer) {
    final CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[oisLeg.getNumberOfPayments()];
    final double sign = isPayer ? -1.0 : 1.0;
    for (int loopcpn = 0; loopcpn < oisLeg.getNumberOfPayments(); loopcpn++) {
      cpnFixed[loopcpn] = new CouponFixedDefinition(oisLeg.getCurrency(), oisLeg.getNthPayment(loopcpn).getPaymentDate(), oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), oisLeg.getNthPayment(
          loopcpn).getAccrualEndDate(), oisLeg.getNthPayment(loopcpn).getPaymentYearFraction(), sign * notional.getAmount(oisLeg.getNthPayment(loopcpn).getAccrualStartDate().toLocalDate()), fixedRate);
    }
    return new SwapFixedONDefinition(new AnnuityCouponFixedDefinition(cpnFixed, calendar), oisLeg);
  }

  /**
   * The fixed leg of the swap.
   * @return Fixed leg.
   */
  public AnnuityCouponFixedDefinition getFixedLeg() {
    return (AnnuityCouponFixedDefinition) getFirstLeg();
  }

  /**
   * The Ibor leg of the swap.
   * @return Ibor leg.
   */
  public AnnuityCouponONDefinition getOISLeg() {
    return (AnnuityCouponONDefinition) getSecondLeg();
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public SwapFixedCoupon<Coupon> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public SwapFixedCoupon<Coupon> toDerivative(final ZonedDateTime date) {
    final Annuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date);
    final Annuity<? extends Coupon> oisLeg = (Annuity<? extends Coupon>) this.getOISLeg().toDerivative(date);
    return new SwapFixedCoupon<>(fixedLeg, (Annuity<Coupon>) oisLeg);
  }

  @SuppressWarnings("unchecked")
  @Override
  public SwapFixedCoupon<Coupon> toDerivative(final ZonedDateTime date, final ZonedDateTimeDoubleTimeSeries[] indexDataTS) {
    ArgumentChecker.notNull(indexDataTS, "index data time series array");
    // ArgumentChecker.isTrue(indexDataTS.length > 0, "index data time series must contain at least one element");
    final Annuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date);
    final Annuity<? extends Coupon> oisLeg = this.getOISLeg().toDerivative(date, indexDataTS[0]);
    return new SwapFixedCoupon<>(fixedLeg, (Annuity<Coupon>) oisLeg);
  }
}
