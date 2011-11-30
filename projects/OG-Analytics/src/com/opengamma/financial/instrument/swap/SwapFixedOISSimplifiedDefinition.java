/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponOISSimplifiedDefinition;
import com.opengamma.financial.instrument.index.IndexOIS;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;

/**
 * Class describing a fixed for OIS swap. Both legs are in the same currency. 
 * The payment dates on the fixed leg a slightly different from the FixedIbor swap due to the lag in payment at the end of each coupon.
 */
public class SwapFixedOISSimplifiedDefinition extends SwapDefinition {

  /**
   * Constructor of the fixed-OIS swap from its two legs.
   * @param fixedLeg The fixed leg.
   * @param oisLeg The OIS leg.
   */
  public SwapFixedOISSimplifiedDefinition(final AnnuityCouponFixedDefinition fixedLeg, final AnnuityCouponOISSimplifiedDefinition oisLeg) {
    super(fixedLeg, oisLeg);
    Validate.isTrue(fixedLeg.getCurrency() == oisLeg.getCurrency(), "Legs should have the same currency");
  }

  /**
   * Swap builder from the financial details. On the fixed leg, the accrual dates are not the same as the payment dates. 
   * There is a difference due to the settlement lag required on the OIS coupons.
   * @param settlementDate The settlement date.
   * @param tenorAnnuity The swap tenor.
   * @param tenorCoupon The coupons tenor. The tenor is teh same on the fixed and OIS legs.
   * @param notional The notional.
   * @param index The OIS index.
   * @param fixedRate The fixed leg rate.
   * @param isPayer The flag indicating if the fixed leg is paying (true) or receiving (false).
   * @param settlementDays The number of days between last fixing of each coupon and the coupon payment (also called spot lag). 
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @return The annuity.
   */
  public static SwapFixedOISSimplifiedDefinition from(final ZonedDateTime settlementDate, final Period tenorAnnuity, final Period tenorCoupon, final double notional, final IndexOIS index,
      final double fixedRate, final boolean isPayer, final int settlementDays, final BusinessDayConvention businessDayConvention, final boolean isEOM) {
    AnnuityCouponOISSimplifiedDefinition oisLeg = AnnuityCouponOISSimplifiedDefinition.from(settlementDate, tenorAnnuity, tenorCoupon, notional, index, !isPayer, settlementDays,
        businessDayConvention, isEOM);
    final double sign = isPayer ? -1.0 : 1.0;
    double notionalSigned = sign * notional;
    return from(oisLeg, notionalSigned, fixedRate);
  }

  /**
   * Swap builder from the financial details. On the fixed leg, the accrual dates are not the same as the payment dates. 
   * There is a difference due to the settlement lag required on the OIS coupons.
   * @param settlementDate The settlement date.
   * @param maturityDate The maturity date. This is the last date of the fixing period, not the last payment date.
   * @param frequency The payments frequency.
   * @param notional The notional.
   * @param index The OIS index.
   * @param fixedRate The fixed leg rate.
   * @param isPayer The flag indicating if the fixed leg is paying (true) or receiving (false).
   * @param settlementDays The number of days between last fixing of each coupon and the coupon payment (also called spot lag). 
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @return The annuity.
   */
  public static SwapFixedOISSimplifiedDefinition from(final ZonedDateTime settlementDate, final ZonedDateTime maturityDate, final Frequency frequency, final double notional, final IndexOIS index,
      final double fixedRate, final boolean isPayer, final int settlementDays, final BusinessDayConvention businessDayConvention, final boolean isEOM) {
    AnnuityCouponOISSimplifiedDefinition oisLeg = AnnuityCouponOISSimplifiedDefinition.from(settlementDate, maturityDate, frequency, notional, index, !isPayer, settlementDays, businessDayConvention,
        isEOM);
    final double sign = isPayer ? -1.0 : 1.0;
    double notionalSigned = sign * notional;
    return from(oisLeg, notionalSigned, fixedRate);
  }

  private static SwapFixedOISSimplifiedDefinition from(final AnnuityCouponOISSimplifiedDefinition oisLeg, final double notionalSigned, final double fixedRate) {
    CouponFixedDefinition[] cpnFixed = new CouponFixedDefinition[oisLeg.getNumberOfPayments()];
    for (int loopcpn = 0; loopcpn < oisLeg.getNumberOfPayments(); loopcpn++) {
      cpnFixed[loopcpn] = new CouponFixedDefinition(oisLeg.getCurrency(), oisLeg.getNthPayment(loopcpn).getPaymentDate(), oisLeg.getNthPayment(loopcpn).getAccrualStartDate(), oisLeg.getNthPayment(
          loopcpn).getAccrualEndDate(), oisLeg.getNthPayment(loopcpn).getPaymentYearFraction(), notionalSigned, fixedRate);
    }
    return new SwapFixedOISSimplifiedDefinition(new AnnuityCouponFixedDefinition(cpnFixed), oisLeg);
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
  public AnnuityCouponOISSimplifiedDefinition getOISLeg() {
    return (AnnuityCouponOISSimplifiedDefinition) getSecondLeg();
  }

  @SuppressWarnings({"unchecked"})
  @Override
  public FixedCouponSwap<Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    final GenericAnnuity<CouponFixed> fixedLeg = this.getFixedLeg().toDerivative(date, yieldCurveNames);
    final GenericAnnuity<? extends Coupon> oisLeg = (GenericAnnuity<? extends Coupon>) this.getOISLeg().toDerivative(date, yieldCurveNames);
    return new FixedCouponSwap<Coupon>(fixedLeg, (GenericAnnuity<Coupon>) oisLeg);
  }

}
