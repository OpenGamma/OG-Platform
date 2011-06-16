/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.financial.interestrate.payments.Payment;

/**
 * Describes a floating coupon bond (or Floating Rate Note) issue with Ibor-like coupon.
 */
public class BondIborSecurityDefinition extends BondSecurityDefinition<CouponIborDefinition> {

  /**
   * The default notional for the security.
   */
  private static final double DEFAULT_NOTIONAL = 1.0;
  /**
   * The default ex-coupn number of days.
   */
  private static final int DEFAULT_EX_COUPON_DAYS = 0;

  /**
   * Fixed coupon bond constructor from all the bond details.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond Ibor coupons. The coupons notional and currency should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   */
  public BondIborSecurityDefinition(AnnuityPaymentFixedDefinition nominal, AnnuityCouponIborDefinition coupon, int exCouponDays, int settlementDays, Calendar calendar, DayCount dayCount) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, dayCount);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * @param maturityDate The maturity date.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param index The coupon Ibor index.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param isEOM The end-of-month flag.
   * @return The fixed coupon bond.
   */
  public static BondIborSecurityDefinition from(ZonedDateTime maturityDate, ZonedDateTime firstAccrualDate, IborIndex index, int settlementDays, DayCount dayCount, BusinessDayConvention businessDay,
      boolean isEOM) {
    Validate.notNull(maturityDate, "Maturity date");
    Validate.notNull(firstAccrualDate, "First accrual date");
    Validate.notNull(index, "Ibor index");
    Validate.notNull(dayCount, "Day count");
    Validate.notNull(businessDay, "Business day convention");
    AnnuityCouponIborDefinition coupon = AnnuityCouponIborDefinition.fromAccrualUnadjusted(firstAccrualDate, maturityDate, DEFAULT_NOTIONAL, index, false);
    PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(index.getCurrency(), businessDay.adjustDate(index.getCalendar(), maturityDate), 
        DEFAULT_NOTIONAL)};
    AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment);
    return new BondIborSecurityDefinition(nominal, coupon, DEFAULT_EX_COUPON_DAYS, settlementDays, index.getCalendar(), dayCount);
  }

  @Override
  public BondSecurity<? extends Payment> toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    return null;
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitBondIborSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondIborSecurityDefinition(this);
  }

}
