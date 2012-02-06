/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponOISDefinition;
import com.opengamma.financial.instrument.index.GeneratorOIS;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a fixed for ON rate swap. Both legs are in the same currency. 
 * The payment dates on the fixed leg a slightly different from the FixedIbor swap due to the lag in payment at the end of each coupon.
 */
public class SwapFixedOISDefinition extends SwapDefinition {

  /**
   * Constructor of the fixed-OIS swap from its two legs.
   * @param fixedLeg The fixed leg.
   * @param oisLeg The OIS leg.
   */
  public SwapFixedOISDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityCouponOISDefinition oisLeg) {
    super(fixedLeg, oisLeg);
    Validate.isTrue(fixedLeg.getCurrency() == oisLeg.getCurrency(), "Legs should have the same currency");
  }

  /**
   * Builder of OIS swap from financial description.
   * @param settlementDate The annuity settlement or first fixing date.
   * @param tenorAnnuity The total tenor of the annuity.
   * @param notional The annuity notional.
   * @param generator The OIS generator.
   * @param fixedRate The rate of the swap fixed leg.
   * @param isPayer The flag indicating if the annuity is paying (true) or receiving (false).
   * @return The swap.
   */
  public static SwapFixedOISDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final double notional, final GeneratorOIS generator, final double fixedRate,
      final boolean isPayer) {
    AnnuityCouponOISDefinition oisLeg = AnnuityCouponOISDefinition.from(settlementDate, tenorAnnuity, notional, generator, !isPayer);
    final double sign = isPayer ? -1.0 : 1.0;
    double notionalSigned = sign * notional;
    return from(oisLeg, notionalSigned, fixedRate);
  }

  private static SwapFixedOISDefinition from(final AnnuityCouponOISDefinition oisLeg, final double notionalSigned, final double fixedRate) {
    CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[oisLeg.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < oisLeg.getNumberOfPayments(); loopcpn++) {
      cpnFixed[loopcpn] = new CouponFixedDefinition(oisLeg.getCurrency(), oisLeg.getNthPayment(loopcpn).getPaymentDate(), oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), oisLeg.getNthPayment(
          loopcpn).getAccrualEndDate(), oisLeg.getNthPayment(loopcpn).getPaymentYearFraction(), notionalSigned, fixedRate);
    }
    return new SwapFixedOISDefinition(new AnnuityCouponFixedDefinition(cpnFixed), oisLeg);
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
  public AnnuityCouponOISDefinition getOISLeg() {
    return (AnnuityCouponOISDefinition) getSecondLeg();
  }

  @SuppressWarnings("unchecked")
  @Override
  public FixedCouponSwap<Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final GenericAnnuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Coupon> oisLeg = (GenericAnnuity<? extends Coupon>) this.getOISLeg().toDerivative(date, yieldCurveNames);
    return new FixedCouponSwap<Coupon>(fixedLeg, (GenericAnnuity<Coupon>) oisLeg);
  }

  @SuppressWarnings("unchecked")
  @Override
  public FixedCouponSwap<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime>[] indexDataTS, final String... yieldCurveNames) {
    Validate.notNull(indexDataTS, "index data time series array");
    Validate.isTrue(indexDataTS.length > 0, "index data time series must contain at least one element");
    final GenericAnnuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Coupon> oisLeg = this.getOISLeg().toDerivative(date, indexDataTS[0], yieldCurveNames);
    return new FixedCouponSwap<Coupon>(fixedLeg, (GenericAnnuity<Coupon>) oisLeg);
  }

}
