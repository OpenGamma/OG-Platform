/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.bond;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.ActualActualICMA;
import com.opengamma.financial.convention.daycount.ActualActualICMANormal;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.util.money.Currency;

/**
 * Describes a fixed coupon bond issue.
 */
public class BondFixedDescriptionDefinition extends BondDescriptionDefinition<CouponFixedDefinition> {

  private static final double DEFAULT_NOTIONAL = 1.0;
  private static final int DEFAULT_EX_COUPON_DAYS = 0;
  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;
  /**
   * Number of coupon per year.
   */
  private final int _couponPerYear;
  /**
   * Flag indicating if the bond uses the end-of-month convention.
   */
  private final boolean _isEOM;

  /**
   * Fixed coupon bond constructor from all the bond details.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond fixed coupons. The coupons notional should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM TODO
   */
  public BondFixedDescriptionDefinition(AnnuityPaymentFixedDefinition nominal, AnnuityCouponFixedDefinition coupon, int exCouponDays, int settlementDays, Calendar calendar, DayCount dayCount,
      YieldConvention yieldConvention, boolean isEOM) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, dayCount);
    Validate.notNull(yieldConvention, "Yield convention");
    _yieldConvention = yieldConvention;
    _couponPerYear = (int) Math.round(1.0 / coupon.getNthPayment(0).getPaymentYearFraction());
    _isEOM = isEOM;
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * @param currency The currency.
   * @param maturityDate The maturity date.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param paymentPeriod The coupon payment period.
   * @param rate The fixed rate.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @return The fixed coupon bond.
   */
  public static BondFixedDescriptionDefinition from(Currency currency, ZonedDateTime maturityDate, ZonedDateTime firstAccrualDate, Period paymentPeriod, double rate, int settlementDays,
      Calendar calendar, DayCount dayCount, BusinessDayConvention businessDay, YieldConvention yieldConvention, boolean isEOM) {
    Validate.notNull(currency, "Currency");
    Validate.notNull(maturityDate, "Maturity date");
    Validate.notNull(firstAccrualDate, "First accrual date");
    Validate.notNull(paymentPeriod, "Payment period");
    Validate.notNull(calendar, "Calendar");
    Validate.notNull(dayCount, "Day count");
    Validate.notNull(businessDay, "Business day convention");
    Validate.notNull(yieldConvention, "Yield convention");
    AnnuityCouponFixedDefinition coupon;
    if ((dayCount instanceof ActualActualICMA) || (dayCount instanceof ActualActualICMANormal)) {
      int couponPerYear = (int) Math.round(365.0 / (firstAccrualDate.plus(paymentPeriod).toLocalDate().toModifiedJulianDays() - firstAccrualDate.toLocalDate().toModifiedJulianDays()));
      coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstAccrualDate, maturityDate, paymentPeriod, couponPerYear, calendar, dayCount, businessDay, isEOM, DEFAULT_NOTIONAL,
          rate, false);
    } else {
      coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstAccrualDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, DEFAULT_NOTIONAL, rate, false);
    }
    PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(currency, businessDay.adjustDate(calendar, maturityDate), DEFAULT_NOTIONAL)};
    AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment);
    return new BondFixedDescriptionDefinition(nominal, coupon, DEFAULT_EX_COUPON_DAYS, settlementDays, calendar, dayCount, yieldConvention, isEOM);
  }

  /**
   * Return the accrued interest rate at a given date.
   * @param date The date.
   * @return The accrued interest.
   */
  public double accruedInterest(ZonedDateTime date) {
    double result = 0;
    int nbCoupon = getCoupon().getNumberOfPayments();
    int couponIndex = 0;
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(date)) {
        couponIndex = loopcpn;
        break;
      }
    }
    ZonedDateTime previousAccrualDate = getCoupon().getNthPayment(couponIndex).getAccrualStartDate();
    ZonedDateTime nextAccrualDate = getCoupon().getNthPayment(couponIndex).getAccrualEndDate();
    final double accruedInterest = AccruedInterestCalculator.getAccruedInterest(getDayCount(), couponIndex, nbCoupon, previousAccrualDate, date, nextAccrualDate, getCoupon()
        .getNthPayment(couponIndex).getRate(), getCouponPerYear(), isEOM());
    if (getExCouponDays() != 0 && nextAccrualDate.minusDays(getExCouponDays()).isBefore(date)) {
      result = accruedInterest - getCoupon().getNthPayment(couponIndex).getRate();
    } else {
      result = accruedInterest;
    }
    return result;
  }

  /**
   * Gets the _yieldConvention field.
   * @return the _yieldConvention
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  /**
   * Gets the _couponPerYear field.
   * @return The number of coupon per year.
   */
  public int getCouponPerYear() {
    return _couponPerYear;
  }

  /**
   * Gets the _isEOM field.
   * @return The end-of-month convention flag.
   */
  public boolean isEOM() {
    return _isEOM;
  }

  @Override
  public AnnuityCouponFixedDefinition getCoupon() {
    return (AnnuityCouponFixedDefinition) super.getCoupon();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _couponPerYear;
    result = prime * result + (_isEOM ? 1231 : 1237);
    result = prime * result + _yieldConvention.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    BondFixedDescriptionDefinition other = (BondFixedDescriptionDefinition) obj;
    if (_couponPerYear != other._couponPerYear) {
      return false;
    }
    if (_isEOM != other._isEOM) {
      return false;
    }
    if (!ObjectUtils.equals(_yieldConvention, other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
