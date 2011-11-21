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
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityCouponFixed;
import com.opengamma.financial.interestrate.annuity.definition.AnnuityPaymentFixed;
import com.opengamma.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;

/**
 * Describes a fixed coupon bond issue.
 */
public class BondFixedSecurityDefinition extends BondSecurityDefinition<PaymentFixedDefinition, CouponFixedDefinition> {

  /**
   * The default notional for the security.
   */
  private static final double DEFAULT_NOTIONAL = 1.0;
  /**
   * The default ex-coupon number of days.
   */
  private static final int DEFAULT_EX_COUPON_DAYS = 0;
  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;
  /**
   * Number of coupon per year. If needed, estimated from the first coupon.
   */
  private final int _couponPerYear;
  /**
   * Flag indicating if the bond uses the end-of-month convention.
   */
  private final boolean _isEOM;
  /**
   * The coupon day count convention.
   */
  private final DayCount _dayCount;

  /**
   * Fixed coupon bond constructor from all the bond details.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond fixed coupons. The coupons notional should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   */
  public BondFixedSecurityDefinition(AnnuityPaymentFixedDefinition nominal, AnnuityCouponFixedDefinition coupon, int exCouponDays, int settlementDays, Calendar calendar, DayCount dayCount,
      YieldConvention yieldConvention, boolean isEOM) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar);
    Validate.notNull(yieldConvention, "Yield convention");
    _yieldConvention = yieldConvention;
    _couponPerYear = (int) Math.round(1.0 / coupon.getNthPayment(0).getPaymentYearFraction());
    _isEOM = isEOM;
    _dayCount = dayCount;
  }

  /**
   * Fixed coupon bond constructor from all the bond details.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond fixed coupons. The coupons notional should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @param repoType The repo type name.
   */
  public BondFixedSecurityDefinition(AnnuityPaymentFixedDefinition nominal, AnnuityCouponFixedDefinition coupon, int exCouponDays, int settlementDays, Calendar calendar, DayCount dayCount,
      YieldConvention yieldConvention, boolean isEOM, final String issuer, final String repoType) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, repoType);
    Validate.notNull(yieldConvention, "Yield convention");
    _yieldConvention = yieldConvention;
    _couponPerYear = (int) Math.round(1.0 / coupon.getNthPayment(0).getPaymentYearFraction());
    _isEOM = isEOM;
    _dayCount = dayCount;
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention. 
   * The default notional 1 and default ex-coupon days 0 are used.
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
  public static BondFixedSecurityDefinition from(Currency currency, ZonedDateTime maturityDate, ZonedDateTime firstAccrualDate, Period paymentPeriod, double rate, int settlementDays,
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
    return new BondFixedSecurityDefinition(nominal, coupon, DEFAULT_EX_COUPON_DAYS, settlementDays, calendar, dayCount, yieldConvention, isEOM);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention. 
   * The default ex-coupon days 0 is used.
   * @param currency The currency.
   * @param maturityDate The maturity date.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param paymentPeriod The coupon payment period.
   * @param rate The fixed rate.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param notional The bond security notional. Usually is a conventional figure like 1, 100 or 1,000,000.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @param repoType The repo type name.
   * @return The fixed coupon bond.
   */
  public static BondFixedSecurityDefinition from(Currency currency, ZonedDateTime maturityDate, ZonedDateTime firstAccrualDate, Period paymentPeriod, double rate, int settlementDays,
      final double notional, final int exCouponDays, final Calendar calendar, DayCount dayCount, BusinessDayConvention businessDay, YieldConvention yieldConvention, boolean isEOM,
      final String issuer, final String repoType) {
    Validate.notNull(currency, "Currency");
    Validate.notNull(maturityDate, "Maturity date");
    Validate.notNull(firstAccrualDate, "First accrual date");
    Validate.notNull(paymentPeriod, "Payment period");
    Validate.notNull(calendar, "Calendar");
    Validate.notNull(dayCount, "Day count");
    Validate.notNull(businessDay, "Business day convention");
    Validate.notNull(yieldConvention, "Yield convention");
    Validate.notNull(issuer, "Issuer");
    Validate.notNull(repoType, "Repo type");
    AnnuityCouponFixedDefinition coupon;
    if ((dayCount instanceof ActualActualICMA) || (dayCount instanceof ActualActualICMANormal)) {
      int couponPerYear = (int) Math.round(365.0 / (firstAccrualDate.plus(paymentPeriod).toLocalDate().toModifiedJulianDays() - firstAccrualDate.toLocalDate().toModifiedJulianDays()));
      coupon = AnnuityCouponFixedDefinition
          .fromAccrualUnadjusted(currency, firstAccrualDate, maturityDate, paymentPeriod, couponPerYear, calendar, dayCount, businessDay, isEOM, notional, rate, false);
    } else {
      coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstAccrualDate, maturityDate, paymentPeriod, calendar, dayCount, businessDay, isEOM, notional, rate, false);
    }
    PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(currency, businessDay.adjustDate(calendar, maturityDate), notional)};
    AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment);
    return new BondFixedSecurityDefinition(nominal, coupon, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, isEOM, issuer, repoType);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention. 
   * The default ex-coupon days 0 is used.
   * @param currency The currency.
   * @param maturityDate The maturity date.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param paymentPeriod The coupon payment period.
   * @param rate The fixed rate.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param notional The bond security notional. Usually is a conventional figure like 1, 100 or 1,000,000.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @param repoType The repo type name.
   * @return The fixed coupon bond.
   */
  public static BondFixedSecurityDefinition from(Currency currency, ZonedDateTime maturityDate, ZonedDateTime firstAccrualDate, Period paymentPeriod, double rate, int settlementDays,
      final double notional, Calendar calendar, DayCount dayCount, BusinessDayConvention businessDay, YieldConvention yieldConvention, boolean isEOM, final String issuer, final String repoType) {
    return from(currency, maturityDate, firstAccrualDate, paymentPeriod, rate, settlementDays, notional, DEFAULT_EX_COUPON_DAYS, calendar, dayCount, businessDay, yieldConvention, isEOM, issuer,
        repoType);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention. 
   * The default ex-coupon days 0 is used.
   * @param currency The currency.
   * @param maturityDate The maturity date.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param paymentPeriod The coupon payment period.
   * @param rate The fixed rate.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param notional The bond security notional. Usually is a conventional figure like 1, 100 or 1,000,000.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @return The fixed coupon bond.
   */
  public static BondFixedSecurityDefinition from(Currency currency, ZonedDateTime maturityDate, ZonedDateTime firstAccrualDate, Period paymentPeriod, double rate, int settlementDays,
      final double notional, Calendar calendar, DayCount dayCount, BusinessDayConvention businessDay, YieldConvention yieldConvention, boolean isEOM) {
    return from(currency, maturityDate, firstAccrualDate, paymentPeriod, rate, settlementDays, notional, calendar, dayCount, businessDay, yieldConvention, isEOM, "", "");
  }

  /**
   * Return the relative (not multiplied by the notional) accrued interest rate at a given date.
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
        .getNthPayment(couponIndex).getRate(), getCouponPerYear(), isEOM())
        * getCoupon().getNthPayment(couponIndex).getNotional();
    if (getExCouponDays() != 0 && nextAccrualDate.minusDays(getExCouponDays()).isBefore(date)) {
      result = accruedInterest - getCoupon().getNthPayment(couponIndex).getAmount();
    } else {
      result = accruedInterest;
    }
    return result;
  }

  /**
   * Gets the yield convention.
   * @return The yield convention.
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  /**
   * Gets the number of coupon per year field.
   * @return The number of coupon per year.
   */
  public int getCouponPerYear() {
    return _couponPerYear;
  }

  /**
   * Gets the end-of-month flag.
   * @return The end-of-month convention flag.
   */
  public boolean isEOM() {
    return _isEOM;
  }

  /**
   * Gets the coupon day count.
   * @return The day count.
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  @Override
  public AnnuityCouponFixedDefinition getCoupon() {
    return (AnnuityCouponFixedDefinition) super.getCoupon();
  }

  @Override
  public BondFixedSecurity toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getCalendar(), getSettlementDays());
    return toDerivative(date, spot, yieldCurveNames);
  }

  public BondFixedSecurity toDerivative(ZonedDateTime date, ZonedDateTime settlementDate, String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for risk free settlement.
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    final String creditCurveName = yieldCurveNames[0];
    final String riskFreeCurveName = yieldCurveNames[1];
    double settleTime;
    double accruedInterestAtSettle;
    if (settlementDate.isBefore(date)) {
      settleTime = 0.0;
      accruedInterestAtSettle = 0.0;
    } else {
      settleTime = TimeCalculator.getTimeBetween(date, settlementDate);
      accruedInterestAtSettle = accruedInterest(settlementDate);
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getNominal().toDerivative(date, creditCurveName);
    AnnuityCouponFixedDefinition couponDefinition = getCoupon();
    couponDefinition = getCoupon().trimBefore(settlementDate);
    CouponFixedDefinition[] couponExPeriodArray = new CouponFixedDefinition[couponDefinition.getNumberOfPayments()];
    System.arraycopy(couponDefinition.getPayments(), 0, couponExPeriodArray, 0, couponDefinition.getNumberOfPayments());
    if (getExCouponDays() != 0) {
      ZonedDateTime exDividendDate = ScheduleCalculator.getAdjustedDate(couponDefinition.getNthPayment(0).getPaymentDate(), getCalendar(), -getExCouponDays());
      if (settlementDate.isAfter(exDividendDate)) {
        // Implementation note: Ex-dividend period: the next coupon is not received but its date is required for yield calculation
        couponExPeriodArray[0] = new CouponFixedDefinition(couponDefinition.getNthPayment(0), 0.0);
      }
    }
    AnnuityCouponFixedDefinition couponDefinitionExPeriod = new AnnuityCouponFixedDefinition(couponExPeriodArray);
    final AnnuityCouponFixed couponStandard = couponDefinitionExPeriod.toDerivative(date, yieldCurveNames);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(settleTime);
    final double factorSpot = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), settlementDate, couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0,
        _couponPerYear);
    final double factorPeriod = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(),
        couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, _couponPerYear);
    final double factor = (factorPeriod - factorSpot) / factorPeriod;
    final BondFixedSecurity bondStandard = new BondFixedSecurity(nominalStandard, couponStandard, settleTime, accruedInterestAtSettle, factor, getYieldConvention(), _couponPerYear, riskFreeCurveName,
        "");
    return bondStandard;

  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitBondFixedSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondFixedSecurityDefinition(this);
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
    BondFixedSecurityDefinition other = (BondFixedSecurityDefinition) obj;
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
