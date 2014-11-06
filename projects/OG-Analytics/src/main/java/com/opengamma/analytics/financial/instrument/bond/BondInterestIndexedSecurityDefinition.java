/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationWithMargin;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearInterpolationWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationYearOnYearMonthlyWithMarginDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondInterestIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.interestrate.payments.derivative.PaymentFixed;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 *  Describes an interest inflation indexed bond issue. Only the coupon (trough the interest)is indexed on a price index,  the nominal is paid at the end (maturity) without any indexation.
 *  @param <N> Type of fixed payment.
 *  @param <C> Type of inflation coupon.
 */
public class BondInterestIndexedSecurityDefinition<N extends PaymentFixedDefinition, C extends CouponInflationDefinition> extends BondSecurityDefinition<N, C> implements
    InstrumentDefinitionWithData<BondSecurity<? extends Payment, ? extends Coupon>, DoubleTimeSeries<ZonedDateTime>> {

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
   * The lag in month between the index validity and the coupon dates.
   */
  private final int _monthLag;
  /**
   * The price index associated to the bond.
   */
  private final IndexPrice _priceIndex;

  /**
   * Constructor of the Interest inflation indexed bond. The repo type is empty and the legal entity contains
   * only the issuer name.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param exCouponDays The ex-coupon period.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param issuer The bond issuer name.
   */
  public BondInterestIndexedSecurityDefinition(final AnnuityDefinition<N> nominal, final AnnuityDefinition<C> coupon, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final String issuer) {
    this(nominal, coupon, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, isEOM, monthLag, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Constructor of the Interest inflation indexed bond. The repo type is empty.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param exCouponDays The ex-coupon period.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param issuer The bond issuer name.
   */
  public BondInterestIndexedSecurityDefinition(final AnnuityDefinition<N> nominal, final AnnuityDefinition<C> coupon, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final LegalEntity issuer) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, "");
    for (int loopcpn = 0; loopcpn < coupon.getNumberOfPayments(); loopcpn++) {
      ArgumentChecker.isTrue(coupon.getNthPayment(loopcpn) instanceof CouponInflationWithMargin, "Not inflation coupons");
    }
    _yieldConvention = yieldConvention;
    _monthLag = monthLag;
    _couponPerYear = (int) Math.round(1.0 / TimeCalculator.getTimeBetween(coupon.getNthPayment(0).getPaymentDate(), coupon.getNthPayment(1).getPaymentDate()));
    _isEOM = isEOM;
    _dayCount = dayCount;
    _priceIndex = coupon.getNthPayment(0).getPriceIndex();
  }

  /**
   * Constructor of the Interest inflation indexed bond. The legal entity contains only the issuer name.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param exCouponDays The ex-coupon period.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param issuer Issuer name.
   * @param repoType The repo type name.
   */
  public BondInterestIndexedSecurityDefinition(final AnnuityDefinition<N> nominal, final AnnuityDefinition<C> coupon, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final String issuer, final String repoType) {
    this(nominal, coupon, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, isEOM, monthLag, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Constructor of the Interest inflation indexed bond.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param exCouponDays The ex-coupon period.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param issuer Issuer name.
   * @param repoType The repo type name.
   */
  public BondInterestIndexedSecurityDefinition(final AnnuityDefinition<N> nominal, final AnnuityDefinition<C> coupon, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final LegalEntity issuer, final String repoType) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, repoType);
    for (int loopcpn = 0; loopcpn < coupon.getNumberOfPayments(); loopcpn++) {
      ArgumentChecker.isTrue(coupon.getNthPayment(loopcpn) instanceof CouponInflationWithMargin, "Not inflation coupons");
    }
    _yieldConvention = yieldConvention;
    _monthLag = monthLag;
    _couponPerYear = (int) Math.round(TimeCalculator.getTimeBetween(coupon.getNthPayment(1).getAccrualStartDate(), coupon.getNthPayment(1).getAccrualEndDate()) / 365);
    _isEOM = isEOM;
    _dayCount = dayCount;
    _priceIndex = coupon.getNthPayment(0).getPriceIndex();
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are monthly index (no interpolation).
   * The legal entity contains only the issuer name.
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param notional The bond notional.
   * @param realRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The bond issuer name.
   * @return The bond.
   */
  public static BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> fromMonthly(final IndexPrice priceIndex, final int monthLag,
      final ZonedDateTime startDate, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate, final BusinessDayConvention businessDay,
      final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final String issuer) {
    return fromMonthly(priceIndex, monthLag, startDate, maturityDate, couponPeriod, notional, realRate, businessDay, settlementDays, calendar, dayCount, yieldConvention, isEOM,
        new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are monthly index (no interpolation).
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param notional The bond notional.
   * @param realRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The bond issuer name.
   * @return The bond.
   */
  public static BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> fromMonthly(final IndexPrice priceIndex, final int monthLag,
      final ZonedDateTime startDate, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate, final BusinessDayConvention businessDay,
      final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final LegalEntity issuer) {
    // Nominal construction
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(priceIndex.getCurrency(), businessDay.adjustDate(calendar, maturityDate),
        notional) };

    final AnnuityDefinition<PaymentFixedDefinition> nominalAnnuity = new AnnuityDefinition<>(nominalPayment, calendar);
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(startDate, maturityDate, couponPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar, false);
    final CouponInflationYearOnYearMonthlyWithMarginDefinition[] coupons = new CouponInflationYearOnYearMonthlyWithMarginDefinition[paymentDates.length];
    coupons[0] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(realRate, startDate, paymentDates[0], notional, priceIndex, monthLag, monthLag,
        true);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(realRate, paymentDatesUnadjusted[loopcpn - 1], paymentDates[loopcpn], notional, priceIndex,
          monthLag, monthLag, true);
    }
    final AnnuityDefinition<CouponInflationYearOnYearMonthlyWithMarginDefinition> couponAnnuity = new AnnuityDefinition<>(coupons, calendar);
    return new BondInterestIndexedSecurityDefinition<>(nominalAnnuity, couponAnnuity, DEFAULT_EX_COUPON_DAYS, settlementDays,
        calendar, dayCount, yieldConvention, isEOM, monthLag, issuer);
  }

  /**
   * Builder of Inflation capital index bond from financial details. The first coupon date is provided to cope with short or long first coupons.
   * The legal entity contains only the issuer name.
   * The notional and the coupon reference index are monthly index (no interpolation).
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param firstCouponDate The bond first coupon date. Used for short/long first coupon.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param notional The bond notional.
   * @param realRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The bond issuer name.
   * @return The bond.
   */
  public static BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> fromMonthly(final IndexPrice priceIndex, final int monthLag,
      final ZonedDateTime startDate, final ZonedDateTime firstCouponDate, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate,
      final BusinessDayConvention businessDay, final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM,
      final String issuer) {
    return fromMonthly(priceIndex, monthLag, startDate, firstCouponDate, maturityDate, couponPeriod, notional, realRate,
        businessDay, settlementDays, calendar, dayCount, yieldConvention, isEOM, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Builder of Inflation capital index bond from financial details. The first coupon date is provided to cope with short or long first coupons.
   * The notional and the coupon reference index are monthly index (no interpolation).
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param firstCouponDate The bond first coupon date. Used for short/long first coupon.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param notional The bond notional.
   * @param realRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The bond issuer name.
   * @return The bond.
   */
  public static BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearMonthlyWithMarginDefinition> fromMonthly(final IndexPrice priceIndex, final int monthLag,
      final ZonedDateTime startDate, final ZonedDateTime firstCouponDate, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate,
      final BusinessDayConvention businessDay, final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM,
      final LegalEntity issuer) {
    // Nominal construction
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(priceIndex.getCurrency(), businessDay.adjustDate(calendar, maturityDate),
        notional) };

    final AnnuityDefinition<PaymentFixedDefinition> nominalAnnuity = new AnnuityDefinition<>(nominalPayment, calendar);
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(firstCouponDate, maturityDate, couponPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar, false);
    final CouponInflationYearOnYearMonthlyWithMarginDefinition[] coupons = new CouponInflationYearOnYearMonthlyWithMarginDefinition[paymentDates.length + 1];
    coupons[0] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(realRate, startDate, ScheduleCalculator.getAdjustedDate(firstCouponDate, 0, calendar), notional, priceIndex,
        monthLag, monthLag, true);
    coupons[1] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(realRate, firstCouponDate, paymentDates[0], notional, priceIndex, monthLag,
        monthLag, true);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn + 1] = CouponInflationYearOnYearMonthlyWithMarginDefinition.from(realRate, paymentDatesUnadjusted[loopcpn - 1], paymentDates[loopcpn], notional, priceIndex,
          monthLag, monthLag, true);
    }
    final AnnuityDefinition<CouponInflationYearOnYearMonthlyWithMarginDefinition> couponAnnuity = new AnnuityDefinition<>(coupons, calendar);
    return new BondInterestIndexedSecurityDefinition<>(nominalAnnuity, couponAnnuity, DEFAULT_EX_COUPON_DAYS, settlementDays,
        calendar, dayCount, yieldConvention, isEOM, monthLag, issuer);
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are interpolated index.
   * The coupon dates are computed from the maturity and have a short first coupon if required. The legal entity contains only the
   * issuer name.
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param indexStartValue The index value at the start of the bond.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param notional The bond notional.
   * @param realRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The bond issuer name.
   * @return The bond.
   */
  public static BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearInterpolationWithMarginDefinition> fromInterpolation(final IndexPrice priceIndex,
      final int monthLag, final ZonedDateTime startDate, final double indexStartValue, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate,
      final BusinessDayConvention businessDay, final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM,
      final String issuer) {
    return fromInterpolation(priceIndex, monthLag, startDate, indexStartValue, maturityDate, couponPeriod, notional, realRate,
        businessDay, settlementDays, calendar, dayCount, yieldConvention, isEOM, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are interpolated index.
   * The coupon dates are computed from the maturity and have a short first coupon if required.
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param indexStartValue The index value at the start of the bond.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param notional The bond notional.
   * @param realRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The bond issuer name.
   * @return The bond.
   */
  public static BondInterestIndexedSecurityDefinition<PaymentFixedDefinition, CouponInflationYearOnYearInterpolationWithMarginDefinition> fromInterpolation(final IndexPrice priceIndex,
      final int monthLag, final ZonedDateTime startDate, final double indexStartValue, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate,
      final BusinessDayConvention businessDay, final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM,
      final LegalEntity issuer) {
    // Nominal construction
    final PaymentFixedDefinition[] nominalPayment = new PaymentFixedDefinition[] {new PaymentFixedDefinition(priceIndex.getCurrency(), businessDay.adjustDate(calendar, maturityDate),
        notional) };

    final AnnuityDefinition<PaymentFixedDefinition> nominalAnnuity = new AnnuityDefinition<>(nominalPayment, calendar);
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(startDate, maturityDate, couponPeriod, true, true);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar, false);
    final CouponInflationYearOnYearInterpolationWithMarginDefinition[] coupons = new CouponInflationYearOnYearInterpolationWithMarginDefinition[paymentDates.length];
    coupons[0] = CouponInflationYearOnYearInterpolationWithMarginDefinition.from(realRate, startDate, paymentDates[0], notional, priceIndex, monthLag,
        monthLag, true);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationYearOnYearInterpolationWithMarginDefinition.from(realRate, paymentDatesUnadjusted[loopcpn - 1], paymentDates[loopcpn], notional,
          priceIndex, monthLag, monthLag, true);
    }
    final AnnuityDefinition<CouponInflationYearOnYearInterpolationWithMarginDefinition> couponAnnuity = new AnnuityDefinition<>(coupons, calendar);
    return new BondInterestIndexedSecurityDefinition<>(nominalAnnuity, couponAnnuity, DEFAULT_EX_COUPON_DAYS, settlementDays,
        calendar, dayCount, yieldConvention, isEOM, monthLag, issuer);
  }

  /**
   * Gets the bond yield convention.
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

  /**
   * Gets the lag in month between the index validity and the coupon dates.
   * @return The lag.
   */
  public int getMonthLag() {
    return _monthLag;
  }

  /**
   * Gets the price index associated to the bond.
   * @return The price index.
   */
  public IndexPrice getPriceIndex() {
    return _priceIndex;
  }

  @Override
  public BondInterestIndexedSecurity<PaymentFixed, Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BondInterestIndexedSecurity<PaymentFixed, Coupon> toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, spot, ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC));
  }

  @Override
  public BondInterestIndexedSecurity<PaymentFixed, Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(data, "data");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, spot, data);
  }

  /**
   * @param date The date to use when converting to the derivative form, not null
   * @param settlementDate The settlement date, not null
   * @param data The index time series
   * @return The derivative form
   */
  public BondInterestIndexedSecurity<PaymentFixed, Coupon> toDerivative(final ZonedDateTime date, final ZonedDateTime settlementDate, final DoubleTimeSeries<ZonedDateTime> data) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    double settlementTime;
    if (settlementDate.isBefore(date)) {
      settlementTime = 0.0;
    } else {
      settlementTime = TimeCalculator.getTimeBetween(date, settlementDate);
    }
    final Annuity<PaymentFixed> nominal = (Annuity<PaymentFixed>) getNominal().toDerivative(date, data);
    final AnnuityDefinition<CouponDefinition> couponDefinition = (AnnuityDefinition<CouponDefinition>) getCoupons().trimBefore(settlementDate);
    final CouponDefinition[] couponExPeriodArray = new CouponDefinition[couponDefinition.getNumberOfPayments()];
    System.arraycopy(couponDefinition.getPayments(), 0, couponExPeriodArray, 0, couponDefinition.getNumberOfPayments());
    if (getExCouponDays() != 0) {
      final ZonedDateTime exDividendDate = ScheduleCalculator.getAdjustedDate(couponDefinition.getNthPayment(0).getPaymentDate(), -getExCouponDays(), getCalendar());
      if (settlementDate.isAfter(exDividendDate)) {
        // Implementation note: Ex-dividend period: the next coupon is not received but its date is required for yield calculation
        couponExPeriodArray[0] = new CouponFixedDefinition(couponDefinition.getNthPayment(0), 0.0);
      }
    }
    final AnnuityDefinition<PaymentDefinition> couponDefinitionExPeriod = new AnnuityDefinition<PaymentDefinition>(couponExPeriodArray, getCalendar());
    final Annuity<Coupon> couponStandard = (Annuity<Coupon>) couponDefinitionExPeriod.toDerivative(date, data);
    final Annuity<PaymentFixed> nominalStandard = nominal.trimBefore(settlementTime);
    final double accruedInterest = accruedInterest(settlementDate);
    final double factorSpot = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), settlementDate, couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0,
        _couponPerYear);
    final double factorPeriod = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(),
        couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, _couponPerYear);
    final double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    final PaymentFixedDefinition nominalLast = getNominal().getNthPayment(getNominal().getNumberOfPayments() - 1);
    final ZonedDateTime settlementDate2 = settlementDate.isBefore(date) ? date : settlementDate;
    final double notional = settlementDate.isBefore(date) ? 0.0 : 1.0;
    final PaymentFixedDefinition settlementDefinition = new PaymentFixedDefinition(nominalLast.getCurrency(), settlementDate2, notional);
    final PaymentFixed settlement = settlementDefinition.toDerivative(date);
    return new BondInterestIndexedSecurity<>(nominalStandard, couponStandard, settlementTime, accruedInterest, factorToNextCoupon, _yieldConvention,
        _couponPerYear, settlement, getIssuerEntity(), _priceIndex);
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
    final CouponInflationWithMargin currentCoupon = ((CouponInflationWithMargin) getCoupons().getNthPayment(couponIndex));
    final double accruedInterest = AccruedInterestCalculator.getAccruedInterest(getDayCount(), couponIndex, nbCoupon, previousAccrualDate, date, nextAccrualDate, currentCoupon.getFactor(),
        getCouponPerYear(), isEOM()) * getCoupons().getNthPayment(couponIndex).getNotional();
    if (getExCouponDays() != 0 && nextAccrualDate.minusDays(getExCouponDays()).isBefore(date)) {
      result = accruedInterest - currentCoupon.getFactor() / _couponPerYear;
    } else {
      result = accruedInterest;
    }
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondInterestIndexedSecurity(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondInterestIndexedSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _couponPerYear;
    result = prime * result + ((_dayCount == null) ? 0 : _dayCount.hashCode());
    result = prime * result + (_isEOM ? 1231 : 1237);
    result = prime * result + _monthLag;
    result = prime * result + ((_priceIndex == null) ? 0 : _priceIndex.hashCode());
    result = prime * result + ((_yieldConvention == null) ? 0 : _yieldConvention.hashCode());
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
    final BondInterestIndexedSecurityDefinition<?, ?> other = (BondInterestIndexedSecurityDefinition<?, ?>) obj;
    if (_couponPerYear != other._couponPerYear) {
      return false;
    }
    if (_dayCount == null) {
      if (other._dayCount != null) {
        return false;
      }
    } else if (!_dayCount.equals(other._dayCount)) {
      return false;
    }
    if (_isEOM != other._isEOM) {
      return false;
    }
    if (_monthLag != other._monthLag) {
      return false;
    }
    if (_priceIndex == null) {
      if (other._priceIndex != null) {
        return false;
      }
    } else if (!_priceIndex.equals(other._priceIndex)) {
      return false;
    }
    if (_yieldConvention == null) {
      if (other._yieldConvention != null) {
        return false;
      }
    } else if (!_yieldConvention.equals(other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
