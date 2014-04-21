/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityPaymentFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityCouponFixed;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondFixedSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.ActualActualICMA;
import com.opengamma.financial.convention.daycount.ActualActualICMANormal;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

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
   * Fixed coupon bond constructor from all the bond details. The repo type is empty and the legal
   * entity only contains the issuer name.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond fixed coupons. The coupons notional should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param couponPerYear The number of coupons per year.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   */
  public BondFixedSecurityDefinition(final AnnuityPaymentFixedDefinition nominal, final AnnuityCouponFixedDefinition coupon, final int exCouponDays,
      final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final int couponPerYear, final boolean isEOM, final String issuer) {
    this(nominal, coupon, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, couponPerYear, isEOM, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Fixed coupon bond constructor from all the bond details. The repo type is empty.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond fixed coupons. The coupons notional should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param couponPerYear The number of coupons per year
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer.
   */
  public BondFixedSecurityDefinition(final AnnuityPaymentFixedDefinition nominal, final AnnuityCouponFixedDefinition coupon, final int exCouponDays,
      final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final int couponPerYear,
      final boolean isEOM, final LegalEntity issuer) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer);
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    _yieldConvention = yieldConvention;
    _couponPerYear = couponPerYear;
    _isEOM = isEOM;
    _dayCount = dayCount;
  }

  /**
   * Fixed coupon bond constructor from all the bond details. The legal entity only contains the issuer name.
   * @param nominal The notional payments. For bullet bond, it is restricted to a single payment.
   * @param coupon The bond fixed coupons. The coupons notional should be in line with the bond nominal.
   * @param exCouponDays Number of days before the payment of the coupon is detached from the bond (and paid to the then owner).
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The calendar used to compute the standard settlement date.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param couponPerYear The number of coupons per year.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @param repoType The repo type name.
   */
  public BondFixedSecurityDefinition(final AnnuityPaymentFixedDefinition nominal, final AnnuityCouponFixedDefinition coupon, final int exCouponDays,
      final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final int couponPerYear,
      final boolean isEOM, final String issuer, final String repoType) {
    this(nominal, coupon, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, couponPerYear, isEOM, new LegalEntity(null, issuer, null, null, null),
        repoType);
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
   * @param couponPerYear The number of coupons per year
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer.
   * @param repoType The repo type name.
   */
  public BondFixedSecurityDefinition(final AnnuityPaymentFixedDefinition nominal, final AnnuityCouponFixedDefinition coupon, final int exCouponDays,
      final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final int couponPerYear,
      final boolean isEOM, final LegalEntity issuer, final String repoType) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, repoType);
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    _yieldConvention = yieldConvention;
    _couponPerYear = couponPerYear;
    _isEOM = isEOM;
    _dayCount = dayCount;
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * The default notional 1 and default ex-coupon days 0 are used; if the first coupon is non-standard, it is short; the coupon dates are computed from the maturity.
   * The legal entity contains only the issuer name.
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
   * @param issuer The issuer name.
   * @return The fixed coupon bond.
   */
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime maturityDate, final ZonedDateTime firstAccrualDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay,
      final YieldConvention yieldConvention, final boolean isEOM, final String issuer) {
    return from(currency, maturityDate, firstAccrualDate, paymentPeriod, rate, settlementDays, calendar, dayCount, businessDay, yieldConvention,
        isEOM, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * The default notional 1 and default ex-coupon days 0 are used; if the first coupon is non-standard, it is short; the coupon dates are computed from the maturity.
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
   * @param issuer The issuer.
   * @return The fixed coupon bond.
   */
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime maturityDate, final ZonedDateTime firstAccrualDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay,
      final YieldConvention yieldConvention, final boolean isEOM, final LegalEntity issuer) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(firstAccrualDate, "First accrual date");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(dayCount, "Day count");
    ArgumentChecker.notNull(businessDay, "Business day convention");
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    final int couponPerYear = (int) Math.round(12.0 / (paymentPeriod.getMonths() + paymentPeriod.getYears() * 12));
    AnnuityCouponFixedDefinition coupon;
    if ((dayCount instanceof ActualActualICMA) || (dayCount instanceof ActualActualICMANormal)) {
      coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstAccrualDate, maturityDate, paymentPeriod, couponPerYear, true, true, calendar, dayCount,
          businessDay, isEOM, DEFAULT_NOTIONAL, rate, false);
    } else {
      coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstAccrualDate, maturityDate, paymentPeriod, true, true, calendar, dayCount, businessDay,
          isEOM, DEFAULT_NOTIONAL, rate, false);
    }
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(currency, businessDay.adjustDate(calendar, maturityDate),
        DEFAULT_NOTIONAL) };
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment, calendar);
    return new BondFixedSecurityDefinition(nominal, coupon, DEFAULT_EX_COUPON_DAYS, settlementDays, calendar, dayCount, yieldConvention, couponPerYear, isEOM, issuer);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * The default notional 1 and default ex-coupon days 0 are used.
   * @param currency The currency.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param firstCouponDate The date of the first coupon.
   * @param maturityDate The maturity date.
   * @param paymentPeriod The coupon payment period.
   * @param rate The fixed rate.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @return The fixed coupon bond.
   */
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime firstAccrualDate, final ZonedDateTime firstCouponDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay,
      final YieldConvention yieldConvention, final boolean isEOM, final String issuer) {
    return from(currency, firstAccrualDate, firstCouponDate, maturityDate, paymentPeriod, rate, settlementDays, calendar, dayCount, businessDay, yieldConvention, isEOM,
        new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * The default notional 1 and default ex-coupon days 0 are used.
   * @param currency The currency.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param firstCouponDate The date of the first coupon.
   * @param maturityDate The maturity date.
   * @param paymentPeriod The coupon payment period.
   * @param rate The fixed rate.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @return The fixed coupon bond.
   */
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime firstAccrualDate, final ZonedDateTime firstCouponDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay,
      final YieldConvention yieldConvention, final boolean isEOM, final LegalEntity issuer) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(firstAccrualDate, "First accrual date");
    ArgumentChecker.notNull(firstCouponDate, "First coupon date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(dayCount, "Day count");
    ArgumentChecker.notNull(businessDay, "Business day convention");
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    // Guess the number of coupon per year.
    final int couponPerYear = (int) Math.round(12.0 / (paymentPeriod.getMonths() + paymentPeriod.getYears() * 12));
    double firstCouponAccrual;
    if (dayCount instanceof ActualActualICMA) { // In case day-count is ActualActualICMA, need to guess the stub-type
      if (firstCouponDate.minus(paymentPeriod).isAfter(firstAccrualDate)) { // Stub: Long start
        firstCouponAccrual = ((ActualActualICMA) dayCount).getAccruedInterest(firstAccrualDate, firstCouponDate, firstCouponDate, 1.0, couponPerYear, StubType.LONG_START);
      } else {
        firstCouponAccrual = ((ActualActualICMA) dayCount).getAccruedInterest(firstAccrualDate, firstCouponDate, firstCouponDate, 1.0, couponPerYear, StubType.SHORT_START);
      }
    } else {
      firstCouponAccrual = dayCount.getAccruedInterest(firstAccrualDate, firstCouponDate, firstCouponDate, 1.0, couponPerYear);
    }
    final CouponFixedDefinition[] couponAfterFirst = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstCouponDate, maturityDate, paymentPeriod, couponPerYear, true, true, calendar,
        dayCount, businessDay, isEOM, DEFAULT_NOTIONAL, rate, false).getPayments();
    final CouponFixedDefinition[] allCoupons = new CouponFixedDefinition[couponAfterFirst.length + 1];
    allCoupons[0] = new CouponFixedDefinition(currency, businessDay.adjustDate(calendar, firstCouponDate), firstAccrualDate, firstCouponDate, firstCouponAccrual, DEFAULT_NOTIONAL,
        rate);
    System.arraycopy(couponAfterFirst, 0, allCoupons, 1, couponAfterFirst.length);
    final AnnuityCouponFixedDefinition coupons = new AnnuityCouponFixedDefinition(allCoupons, calendar);
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(currency, businessDay.adjustDate(calendar, maturityDate),
        DEFAULT_NOTIONAL) };
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment, calendar);
    return new BondFixedSecurityDefinition(nominal, coupons, DEFAULT_EX_COUPON_DAYS, settlementDays, calendar, dayCount, yieldConvention, couponPerYear, isEOM, issuer);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * The default notional 1 is used.
   * @param currency The currency.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param firstCouponDate The date of the first coupon.
   * @param maturityDate The maturity date.
   * @param paymentPeriod The coupon payment period.
   * @param rate The fixed rate.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param exCouponDays The number of ex-coupon calendar days.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @return The fixed coupon bond.
   */
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime firstAccrualDate, final ZonedDateTime firstCouponDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final int exCouponDays, final Calendar calendar, final DayCount dayCount, final BusinessDayConvention businessDay,
      final YieldConvention yieldConvention, final boolean isEOM, final LegalEntity issuer) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(firstAccrualDate, "First accrual date");
    ArgumentChecker.notNull(firstCouponDate, "First coupon date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(dayCount, "Day count");
    ArgumentChecker.notNull(businessDay, "Business day convention");
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    // Guess the number of coupon per year.
    final int couponPerYear = (int) Math.round(12.0 / (paymentPeriod.getMonths() + paymentPeriod.getYears() * 12));
    double firstCouponAccrual;
    if (dayCount instanceof ActualActualICMA) { // In case day-count is ActualActualICMA, need to guess the stub-type
      if (firstCouponDate.minus(paymentPeriod).isAfter(firstAccrualDate)) { // Stub: Long start
        firstCouponAccrual = ((ActualActualICMA) dayCount).getAccruedInterest(firstAccrualDate, firstCouponDate, firstCouponDate, 1.0, couponPerYear, StubType.LONG_START);
      } else {
        firstCouponAccrual = ((ActualActualICMA) dayCount).getAccruedInterest(firstAccrualDate, firstCouponDate, firstCouponDate, 1.0, couponPerYear, StubType.SHORT_START);
      }
    } else {
      firstCouponAccrual = dayCount.getAccruedInterest(firstAccrualDate, firstCouponDate, firstCouponDate, 1.0, couponPerYear);
    }
    final CouponFixedDefinition[] couponAfterFirst = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstCouponDate, maturityDate, paymentPeriod, couponPerYear, true, true, calendar,
        dayCount, businessDay, isEOM, DEFAULT_NOTIONAL, rate, false).getPayments();
    final CouponFixedDefinition[] allCoupons = new CouponFixedDefinition[couponAfterFirst.length + 1];
    allCoupons[0] = new CouponFixedDefinition(currency, businessDay.adjustDate(calendar, firstCouponDate), firstAccrualDate, firstCouponDate, firstCouponAccrual, DEFAULT_NOTIONAL,
        rate);
    System.arraycopy(couponAfterFirst, 0, allCoupons, 1, couponAfterFirst.length);
    final AnnuityCouponFixedDefinition coupons = new AnnuityCouponFixedDefinition(allCoupons, calendar);
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(currency, businessDay.adjustDate(calendar, maturityDate),
        DEFAULT_NOTIONAL) };
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment, calendar);
    return new BondFixedSecurityDefinition(nominal, coupons, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, couponPerYear, isEOM, issuer);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * @param currency The currency.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param firstCouponDate The date of the first coupon.
   * @param maturityDate The maturity date.
   * @param paymentPeriod The coupon payment period.
   * @param rate The fixed rate.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param exCouponDays The number of ex-coupon calendar days.
   * @param notional The notional.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param businessDay The business day convention for the payments.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The issuer name.
   * @return The fixed coupon bond.
   */
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime firstAccrualDate, final ZonedDateTime firstCouponDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final double notional, final int exCouponDays, final Calendar calendar, final DayCount dayCount,
      final BusinessDayConvention businessDay, final YieldConvention yieldConvention, final boolean isEOM, final LegalEntity issuer) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(firstAccrualDate, "First accrual date");
    ArgumentChecker.notNull(firstCouponDate, "First coupon date");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(dayCount, "Day count");
    ArgumentChecker.notNull(businessDay, "Business day convention");
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    // Guess the number of coupon per year.
    final int couponPerYear = (int) Math.round(12.0 / (paymentPeriod.getMonths() + paymentPeriod.getYears() * 12));
    double firstCouponAccrual;
    if (dayCount instanceof ActualActualICMA) { // In case day-count is ActualActualICMA, need to guess the stub-type
      if (firstCouponDate.minus(paymentPeriod).isAfter(firstAccrualDate)) { // Stub: Long start
        firstCouponAccrual = ((ActualActualICMA) dayCount).getAccruedInterest(firstAccrualDate, firstCouponDate, firstCouponDate, 1.0, couponPerYear, StubType.LONG_START);
      } else {
        firstCouponAccrual = ((ActualActualICMA) dayCount).getAccruedInterest(firstAccrualDate, firstCouponDate, firstCouponDate, 1.0, couponPerYear, StubType.SHORT_START);
      }
    } else {
      firstCouponAccrual = dayCount.getAccruedInterest(firstAccrualDate, firstCouponDate, firstCouponDate, 1.0, couponPerYear);
    }
    final CouponFixedDefinition[] couponAfterFirst = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstCouponDate, maturityDate, paymentPeriod, couponPerYear, true, true, calendar,
        dayCount, businessDay, isEOM, notional, rate, false).getPayments();
    final CouponFixedDefinition[] allCoupons = new CouponFixedDefinition[couponAfterFirst.length + 1];
    allCoupons[0] = new CouponFixedDefinition(currency, businessDay.adjustDate(calendar, firstCouponDate), firstAccrualDate, firstCouponDate, firstCouponAccrual, DEFAULT_NOTIONAL,
        rate);
    System.arraycopy(couponAfterFirst, 0, allCoupons, 1, couponAfterFirst.length);
    final AnnuityCouponFixedDefinition coupons = new AnnuityCouponFixedDefinition(allCoupons, calendar);
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(currency, businessDay.adjustDate(calendar, maturityDate),
        notional) };
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment, calendar);
    return new BondFixedSecurityDefinition(nominal, coupons, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, couponPerYear, isEOM, issuer);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * The default ex-coupon days 0 is used; if the first coupon is non-standard, it is short; the coupon dates are computed from the maturity. The legal
   * entity contains only the issuer name.
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
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime maturityDate, final ZonedDateTime firstAccrualDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final double notional, final int exCouponDays, final Calendar calendar,
      final DayCount dayCount, final BusinessDayConvention businessDay, final YieldConvention yieldConvention, final boolean isEOM, final String issuer,
      final String repoType) {
    return from(currency, firstAccrualDate, maturityDate, paymentPeriod, rate, settlementDays, notional, exCouponDays, calendar, dayCount,
        businessDay, yieldConvention, isEOM, new LegalEntity(null, issuer, null, null, null), repoType);
  }

  /**
   * Fixed coupon bond builder from standard financial details. The accrual dates are unadjusted; the payment dates are adjusted according to the business day convention.
   * If the first coupon is non-standard, it is short; the coupon dates are computed from the maturity.
   * @param currency The currency.
   * @param firstAccrualDate The first accrual date (bond start date).
   * @param maturityDate The maturity date.
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
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime firstAccrualDate, final ZonedDateTime maturityDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final double notional, final int exCouponDays, final Calendar calendar,
      final DayCount dayCount, final BusinessDayConvention businessDay, final YieldConvention yieldConvention, final boolean isEOM, final LegalEntity issuer,
      final String repoType) {
    ArgumentChecker.notNull(currency, "Currency");
    ArgumentChecker.notNull(maturityDate, "Maturity date");
    ArgumentChecker.notNull(firstAccrualDate, "First accrual date");
    ArgumentChecker.notNull(paymentPeriod, "Payment period");
    ArgumentChecker.notNull(calendar, "Calendar");
    ArgumentChecker.notNull(dayCount, "Day count");
    ArgumentChecker.notNull(businessDay, "Business day convention");
    ArgumentChecker.notNull(yieldConvention, "Yield convention");
    ArgumentChecker.notNull(issuer, "Issuer");
    ArgumentChecker.notNull(repoType, "Repo type");
    // Guess the number of coupon per year.
    final int couponPerYear = (int) Math.round(12.0 / (paymentPeriod.getMonths() + paymentPeriod.getYears() * 12));
    AnnuityCouponFixedDefinition coupon;
    if ((dayCount instanceof ActualActualICMA) || (dayCount instanceof ActualActualICMANormal)) {
      coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstAccrualDate, maturityDate, paymentPeriod, couponPerYear, true, true, calendar, dayCount,
          businessDay, isEOM, notional, rate, false);
    } else {
      coupon = AnnuityCouponFixedDefinition.fromAccrualUnadjusted(currency, firstAccrualDate, maturityDate, paymentPeriod, true, true, calendar, dayCount, businessDay,
          isEOM, notional, rate, false);
    }
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(currency, businessDay.adjustDate(calendar, maturityDate),
        notional) };
    final AnnuityPaymentFixedDefinition nominal = new AnnuityPaymentFixedDefinition(nominalPayment, calendar);
    return new BondFixedSecurityDefinition(nominal, coupon, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, couponPerYear, isEOM, issuer, repoType);
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
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime maturityDate, final ZonedDateTime firstAccrualDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final double notional, final Calendar calendar, final DayCount dayCount,
      final BusinessDayConvention businessDay, final YieldConvention yieldConvention, final boolean isEOM, final String issuer, final String repoType) {
    return from(currency, maturityDate, firstAccrualDate, paymentPeriod, rate, settlementDays, notional, DEFAULT_EX_COUPON_DAYS, calendar, dayCount, businessDay,
        yieldConvention, isEOM, issuer, repoType);
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
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime maturityDate, final ZonedDateTime firstAccrualDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final double notional, final Calendar calendar, final DayCount dayCount,
      final BusinessDayConvention businessDay, final YieldConvention yieldConvention, final boolean isEOM, final LegalEntity issuer, final String repoType) {
    return from(currency, firstAccrualDate, maturityDate, paymentPeriod, rate, settlementDays, notional, DEFAULT_EX_COUPON_DAYS, calendar, dayCount, businessDay,
        yieldConvention, isEOM, issuer, repoType);
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
  public static BondFixedSecurityDefinition from(final Currency currency, final ZonedDateTime maturityDate, final ZonedDateTime firstAccrualDate,
      final Period paymentPeriod, final double rate, final int settlementDays, final double notional, final Calendar calendar, final DayCount dayCount,
      final BusinessDayConvention businessDay, final YieldConvention yieldConvention, final boolean isEOM) {
    return from(currency, maturityDate, firstAccrualDate, paymentPeriod, rate, settlementDays, notional, calendar, dayCount, businessDay, yieldConvention, isEOM, "", "");
  }

  /**
   * Return the relative (not multiplied by the notional) accrued interest rate at a given date.
   * @param date The date.
   * @return The accrued interest.
   */
  public double accruedInterest(final ZonedDateTime date) {
    double result = 0;
    final int nbCoupon = getCoupons().getNumberOfPayments();
    int couponIndex = 0;
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getCoupons().getNthPayment(loopcpn).getAccrualEndDate().isAfter(date)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final ZonedDateTime previousAccrualDate = getCoupons().getNthPayment(couponIndex).getAccrualStartDate();
    final ZonedDateTime nextAccrualDate = getCoupons().getNthPayment(couponIndex).getAccrualEndDate();
    final double accruedInterest = AccruedInterestCalculator.getAccruedInterest(getDayCount(), couponIndex, nbCoupon, previousAccrualDate, date, nextAccrualDate,
        getCoupons().getNthPayment(couponIndex).getRate(), getCouponPerYear(), isEOM()) * getCoupons().getNthPayment(couponIndex).getNotional();
    if (getExCouponDays() != 0 && nextAccrualDate.minusDays(getExCouponDays()).isBefore(date)) {
      result = accruedInterest - getCoupons().getNthPayment(couponIndex).getAmount();
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
  public AnnuityCouponFixedDefinition getCoupons() {
    return (AnnuityCouponFixedDefinition) super.getCoupons();
  }

  /**
   * @param date The valuation date
   * @param yieldCurveNames The yield curve names
   * @return A fixed-coupon bond security
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondFixedSecurity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, spot, yieldCurveNames);
  }

  /**
   * @param date The valuation date
   * @param settlementDate The settlement date
   * @param yieldCurveNames The yield curve names
   * @return A fixed-coupon bond security
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  public BondFixedSecurity toDerivative(final ZonedDateTime date, final ZonedDateTime settlementDate, final String... yieldCurveNames) {
    // Implementation note: First yield curve used for coupon and notional (credit), the second for risk free settlement.
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curves required");
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
    AnnuityCouponFixedDefinition couponDefinition = getCoupons();
    couponDefinition = getCoupons().trimBefore(settlementDate);
    final CouponFixedDefinition[] couponExPeriodArray = new CouponFixedDefinition[couponDefinition.getNumberOfPayments()];
    System.arraycopy(couponDefinition.getPayments(), 0, couponExPeriodArray, 0, couponDefinition.getNumberOfPayments());
    if (getExCouponDays() != 0) {
      final ZonedDateTime exDividendDate = ScheduleCalculator.getAdjustedDate(couponDefinition.getNthPayment(0).getPaymentDate(), -getExCouponDays(), getCalendar());
      if (settlementDate.isAfter(exDividendDate)) {
        // Implementation note: Ex-dividend period: the next coupon is not received but its date is required for yield calculation
        couponExPeriodArray[0] = new CouponFixedDefinition(couponDefinition.getNthPayment(0), 0.0);
      }
    }
    final AnnuityCouponFixedDefinition couponDefinitionExPeriod = new AnnuityCouponFixedDefinition(couponExPeriodArray, getCalendar());
    final AnnuityCouponFixed couponStandard = couponDefinitionExPeriod.toDerivative(date, yieldCurveNames);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(settleTime);
    final double factorSpot = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), settlementDate,
        couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, _couponPerYear);
    final double factorPeriod = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(),
        couponDefinition.getNthPayment(0).getAccrualEndDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, _couponPerYear);
    final double factor = (factorPeriod - factorSpot) / factorPeriod;
    final BondFixedSecurity bondStandard = new BondFixedSecurity(nominalStandard, couponStandard, settleTime, accruedInterestAtSettle, factor, getYieldConvention(),
        _couponPerYear, riskFreeCurveName, getIssuerEntity());
    return bondStandard;

  }

  @Override
  public BondFixedSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, spot);
  }

  /**
   * @param date The valuation date
   * @param settlementDate The settlement date
   * @return A fixed-coupon bond security
   */
  public BondFixedSecurity toDerivative(final ZonedDateTime date, final ZonedDateTime settlementDate) {
    ArgumentChecker.notNull(date, "date");
    double settleTime;
    double accruedInterestAtSettle;
    if (settlementDate.isBefore(date) || getCoupons().getNthPayment(0).getAccrualStartDate().isAfter(settlementDate)) {
      settleTime = 0.0;
      accruedInterestAtSettle = 0.0;
    } else {
      settleTime = TimeCalculator.getTimeBetween(date, settlementDate);
      accruedInterestAtSettle = accruedInterest(settlementDate);
    }
    final AnnuityPaymentFixed nominal = (AnnuityPaymentFixed) getNominal().toDerivative(date);
    AnnuityCouponFixedDefinition couponDefinition = getCoupons();
    couponDefinition = getCoupons().trimBefore(settlementDate);
    final CouponFixedDefinition[] couponExPeriodArray = new CouponFixedDefinition[couponDefinition.getNumberOfPayments()];
    System.arraycopy(couponDefinition.getPayments(), 0, couponExPeriodArray, 0, couponDefinition.getNumberOfPayments());
    if (getExCouponDays() != 0) {
      final ZonedDateTime exDividendDate = ScheduleCalculator.getAdjustedDate(couponDefinition.getNthPayment(0).getPaymentDate(), -getExCouponDays(), getCalendar());
      if (settlementDate.isAfter(exDividendDate)) {
        // Implementation note: Ex-dividend period: the next coupon is not received but its date is required for yield calculation
        couponExPeriodArray[0] = new CouponFixedDefinition(couponDefinition.getNthPayment(0), 0.0);
      }
    }
    final AnnuityCouponFixedDefinition couponDefinitionExPeriod = new AnnuityCouponFixedDefinition(couponExPeriodArray, getCalendar());
    final AnnuityCouponFixed couponStandard = couponDefinitionExPeriod.toDerivative(date);
    final AnnuityPaymentFixed nominalStandard = nominal.trimBefore(settleTime);
    final double factor;
    if (getYieldConvention().equals(SimpleYieldConvention.AUSTRALIA_EX_DIVIDEND) && (couponStandard.getNumberOfPayments() == 1)) {
      final DayCount dayCountDiscFactLastPeriod = DayCounts.ACT_365;
      factor = dayCountDiscFactLastPeriod.getAccruedInterest(settlementDate, couponDefinition.getNthPayment(0).getAccrualEndDate(),
          couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, _couponPerYear) * _couponPerYear;
    } else {
      if (couponDefinition.getNthPayment(0).getAccrualStartDate().isAfter(settlementDate)) {
        factor = 0;
      } else {
        final double factorSpot = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), settlementDate,
                                                                   couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, _couponPerYear);
        final double factorPeriod = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(),
                                                                     couponDefinition.getNthPayment(0).getAccrualEndDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, _couponPerYear);
        factor = (factorPeriod - factorSpot) / factorPeriod;
      }
    }
    final BondFixedSecurity bondStandard = new BondFixedSecurity(nominalStandard, couponStandard, settleTime, accruedInterestAtSettle, factor, getYieldConvention(),
        _couponPerYear, getIssuerEntity());
    return bondStandard;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondFixedSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BondFixedSecurityDefinition other = (BondFixedSecurityDefinition) obj;
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
