/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationGearing;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponInterpolationGearingDefinition;
import com.opengamma.analytics.financial.instrument.inflation.CouponInflationZeroCouponMonthlyGearingDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponDefinition;
import com.opengamma.analytics.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentDefinition;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.Annuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflation;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponMonthlyGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
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
 * Describes a capital inflation indexed bond issue. Both the coupon and the nominal are indexed on a price index.
 * @param <C> Type of inflation coupon. Can be {@link CouponInflationZeroCouponMonthlyGearingDefinition} or {@link CouponInflationZeroCouponInterpolationGearingDefinition}.
 */
public class BondCapitalIndexedSecurityDefinition<C extends CouponInflationDefinition>
    extends BondSecurityDefinition<C, C> implements InstrumentDefinitionWithData<BondSecurity<? extends Payment, ? extends Coupon>, DoubleTimeSeries<ZonedDateTime>> {

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
   * The index value at the start of the bond.
   */
  private final double _indexStartValue;
  /**
   * The price index associated to the bond.
   */
  private final IndexPrice _priceIndex;

  /**
   * Constructor of the Capital inflation indexed bond. The repo type is set to an empty string and the legal entity
   * only contains the issuer name.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param indexStartDate The index value at the start of the bond.
   * @param exCouponDays The ex-coupon period.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param issuer The bond issuer name.
   */
  public BondCapitalIndexedSecurityDefinition(final AnnuityDefinition<C> nominal, final AnnuityDefinition<C> coupon, final double indexStartDate, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final String issuer) {
    this(nominal, coupon, indexStartDate, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, isEOM, monthLag, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Constructor of the Capital inflation indexed bond. The repo type is set to empty string.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param indexStartDate The index value at the start of the bond.
   * @param exCouponDays The ex-coupon period.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param issuer The bond issuer.
   */
  public BondCapitalIndexedSecurityDefinition(final AnnuityDefinition<C> nominal, final AnnuityDefinition<C> coupon, final double indexStartDate, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final LegalEntity issuer) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, "");
    for (int loopcpn = 0; loopcpn < coupon.getNumberOfPayments(); loopcpn++) {
      ArgumentChecker.isTrue(coupon.getNthPayment(loopcpn) instanceof CouponInflationGearing, "Not inflation coupons");
    }
    _yieldConvention = yieldConvention;
    _monthLag = monthLag;
    _indexStartValue = indexStartDate;
    _couponPerYear = (int) Math.round(1.0 / TimeCalculator.getTimeBetween(coupon.getNthPayment(0).getPaymentDate(), coupon.getNthPayment(1).getPaymentDate()));
    _isEOM = isEOM;
    _dayCount = dayCount;
    _priceIndex = nominal.getNthPayment(0).getPriceIndex();
  }

  /**
   * Constructor of the Capital inflation indexed bond. The legal entity only contains the issuer name.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param indexStartDate The index value at the start of the bond.
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
  public BondCapitalIndexedSecurityDefinition(final AnnuityDefinition<C> nominal, final AnnuityDefinition<C> coupon, final double indexStartDate, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final String issuer, final String repoType) {
    this(nominal, coupon, indexStartDate, exCouponDays, settlementDays, calendar, dayCount, yieldConvention, isEOM, monthLag,
        new LegalEntity(null, issuer, null, null, null), repoType);
  }

  /**
   * Constructor of the Capital inflation indexed bond.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param indexStartDate The index value at the start of the bond.
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
  public BondCapitalIndexedSecurityDefinition(final AnnuityDefinition<C> nominal, final AnnuityDefinition<C> coupon, final double indexStartDate, final int exCouponDays, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final LegalEntity issuer, final String repoType) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, repoType);
    for (int loopcpn = 0; loopcpn < coupon.getNumberOfPayments(); loopcpn++) {
      ArgumentChecker.isTrue(coupon.getNthPayment(loopcpn) instanceof CouponInflationGearing, "Not inflation coupons");
    }
    _yieldConvention = yieldConvention;
    _monthLag = monthLag;
    _indexStartValue = indexStartDate;
    _couponPerYear = (int) Math.round(TimeCalculator.getTimeBetween(coupon.getNthPayment(1).getAccrualStartDate(), coupon.getNthPayment(1).getAccrualEndDate()) / 365);
    _isEOM = isEOM;
    _dayCount = dayCount;
    _priceIndex = nominal.getNthPayment(0).getPriceIndex();
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are monthly index (no interpolation).
   * The legal entity contains only the issuer name.
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
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> fromMonthly(final IndexPrice priceIndex, final int monthLag, final ZonedDateTime startDate,
      final double indexStartValue, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate, final BusinessDayConvention businessDay,
      final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final String issuer) {
    return fromMonthly(priceIndex, monthLag, startDate, indexStartValue, maturityDate, couponPeriod, notional, realRate, businessDay, settlementDays,
        calendar, dayCount, yieldConvention, isEOM, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are monthly index (no interpolation).
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
   * @param issuer The bond issuer.
   * @return The bond.
   */
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> fromMonthly(final IndexPrice priceIndex, final int monthLag, final ZonedDateTime startDate,
      final double indexStartValue, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate, final BusinessDayConvention businessDay,
      final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final LegalEntity issuer) {
    // Nominal construction
    final CouponInflationZeroCouponMonthlyGearingDefinition nominalPayment = CouponInflationZeroCouponMonthlyGearingDefinition.from(startDate, maturityDate, notional, priceIndex, indexStartValue,
        monthLag,
        monthLag, true, 1.0);
    final AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> nominalAnnuity = new AnnuityDefinition<>(
        new CouponInflationZeroCouponMonthlyGearingDefinition[] {nominalPayment }, calendar);
    // Coupon construction

    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(startDate, maturityDate, couponPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar, false);
    final CouponInflationZeroCouponMonthlyGearingDefinition[] coupons = new CouponInflationZeroCouponMonthlyGearingDefinition[paymentDates.length];
    coupons[0] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[0], startDate, paymentDatesUnadjusted[0], notional, priceIndex, indexStartValue, monthLag, monthLag, true,
        realRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], notional, priceIndex,
          indexStartValue, monthLag, monthLag, true, realRate);
    }
    final AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> couponAnnuity = new AnnuityDefinition<>(coupons, calendar);
    return new BondCapitalIndexedSecurityDefinition<>(nominalAnnuity, couponAnnuity, indexStartValue, DEFAULT_EX_COUPON_DAYS, settlementDays,
        calendar, dayCount, yieldConvention, isEOM, monthLag, issuer);
  }

  /**
   * Builder of Inflation capital index bond from financial details. The first coupon date is provided to cope with short or long first coupons.
   * The notional and the coupon reference index are monthly index (no interpolation).
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param indexStartValue The index value at the start of the bond.
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
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> fromMonthly(final IndexPrice priceIndex, final int monthLag, final ZonedDateTime startDate,
      final double indexStartValue, final ZonedDateTime firstCouponDate, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate,
      final BusinessDayConvention businessDay, final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM,
      final String issuer) {
    return fromMonthly(priceIndex, monthLag, startDate, indexStartValue, firstCouponDate, maturityDate, couponPeriod, notional, realRate, businessDay, settlementDays, calendar, dayCount,
        yieldConvention, isEOM, new LegalEntity(null, issuer, null, null, null));
  }

  /**
   * Builder of Inflation capital index bond from financial details. The first coupon date is provided to cope with short or long first coupons.
   * The notional and the coupon reference index are monthly index (no interpolation).
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param indexStartValue The index value at the start of the bond.
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
   * @param issuer The bond issuer.
   * @return The bond.
   */
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> fromMonthly(final IndexPrice priceIndex, final int monthLag, final ZonedDateTime startDate,
      final double indexStartValue, final ZonedDateTime firstCouponDate, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate,
      final BusinessDayConvention businessDay, final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM,
      final LegalEntity issuer) {
    // Nominal construction+
    final CouponInflationZeroCouponMonthlyGearingDefinition nominalPayment = CouponInflationZeroCouponMonthlyGearingDefinition.from(startDate, maturityDate, notional, priceIndex, indexStartValue,
        monthLag, monthLag, true, 1.0);
    final AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> nominalAnnuity = new AnnuityDefinition<>(
        new CouponInflationZeroCouponMonthlyGearingDefinition[] {nominalPayment }, calendar);
    // Coupon construction
    final double periodicRate = realRate * couponPeriod.toTotalMonths() / 12.0;
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(firstCouponDate, maturityDate, couponPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar, false);
    final CouponInflationZeroCouponMonthlyGearingDefinition[] coupons = new CouponInflationZeroCouponMonthlyGearingDefinition[paymentDates.length + 1];
    coupons[0] = CouponInflationZeroCouponMonthlyGearingDefinition.from(ScheduleCalculator.getAdjustedDate(firstCouponDate, 0, calendar), startDate, firstCouponDate, notional, priceIndex,
        indexStartValue, monthLag, monthLag, true, periodicRate);
    coupons[1] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[0], firstCouponDate, paymentDatesUnadjusted[0], notional, priceIndex, indexStartValue, monthLag, monthLag,
        true, periodicRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn + 1] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], notional, priceIndex,
          indexStartValue, monthLag, monthLag, true, periodicRate);
    }
    final AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> couponAnnuity = new AnnuityDefinition<>(coupons, calendar);
    return new BondCapitalIndexedSecurityDefinition<>(nominalAnnuity, couponAnnuity, indexStartValue, DEFAULT_EX_COUPON_DAYS, settlementDays,
        calendar, dayCount, yieldConvention, isEOM, monthLag, issuer);
  }

  /**
   * Builder of Inflation capital index bond from financial details. The first coupon date is provided to cope with short or long first coupons.
   * The notional and the coupon reference index are monthly index (no interpolation).
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param indexStartValue The index value at the start of the bond.
   * @param firstCouponDate The bond first coupon date. Used for short/long first coupon.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param realRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param dayCount The coupon day count convention.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param isEOM The end-of-month flag.
   * @param issuer The bond issuer.
   * @return The bond.
   */
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> fromMonthly(final IndexPrice priceIndex, final int monthLag, final ZonedDateTime startDate,
      final double indexStartValue, final ZonedDateTime firstCouponDate, final ZonedDateTime maturityDate, final Period couponPeriod, final double realRate,
      final BusinessDayConvention businessDay, final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM,
      final LegalEntity issuer) {
    return fromMonthly(priceIndex, monthLag, startDate, indexStartValue, firstCouponDate, maturityDate, couponPeriod, 1.0, realRate,
        businessDay, settlementDays, calendar, dayCount, yieldConvention, isEOM, issuer);
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
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> fromInterpolation(final IndexPrice priceIndex, final int monthLag,
      final ZonedDateTime startDate, final double indexStartValue, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate,
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
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> fromInterpolation(final IndexPrice priceIndex, final int monthLag,
      final ZonedDateTime startDate, final double indexStartValue, final ZonedDateTime maturityDate, final Period couponPeriod, final double notional, final double realRate,
      final BusinessDayConvention businessDay, final int settlementDays, final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM,
      final LegalEntity issuer) {
    // Nominal construction
    final CouponInflationZeroCouponInterpolationGearingDefinition nominalPayment = CouponInflationZeroCouponInterpolationGearingDefinition.from(startDate, maturityDate, notional, priceIndex,
        indexStartValue, monthLag, monthLag, true, 1.0);
    final AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> nominalAnnuity = new AnnuityDefinition<>(
        new CouponInflationZeroCouponInterpolationGearingDefinition[] {nominalPayment }, calendar);
    // Coupon construction
    final double periodicRate = realRate * couponPeriod.toTotalMonths() / 12.0;
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(startDate, maturityDate, couponPeriod, true, true);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar, false);
    final CouponInflationZeroCouponInterpolationGearingDefinition[] coupons = new CouponInflationZeroCouponInterpolationGearingDefinition[paymentDates.length];
    coupons[0] = CouponInflationZeroCouponInterpolationGearingDefinition.from(paymentDates[0], startDate, paymentDatesUnadjusted[0], notional, priceIndex, indexStartValue, monthLag, monthLag,
        true, periodicRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationZeroCouponInterpolationGearingDefinition.from(paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], notional,
          priceIndex, indexStartValue, monthLag, monthLag, true, periodicRate);
    }
    final AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> couponAnnuity = new AnnuityDefinition<>(coupons, calendar);
    return new BondCapitalIndexedSecurityDefinition<>(nominalAnnuity, couponAnnuity, indexStartValue, DEFAULT_EX_COUPON_DAYS, settlementDays,
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
   * Gets the index value at the start of the bond.
   * @return The index value.
   */
  public double getIndexStartValue() {
    return _indexStartValue;
  }

  /**
   * Gets the price index associated to the bond.
   * @return The price index.
   */
  public IndexPrice getPriceIndex() {
    return _priceIndex;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondCapitalIndexedSecurity<Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public BondCapitalIndexedSecurity<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data, final String... yieldCurveNames) {
    return toDerivative(date, data);
  }

  @Override
  public BondCapitalIndexedSecurity<Coupon> toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, spot, ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(ZoneOffset.UTC));
  }

  @Override
  public BondCapitalIndexedSecurity<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data) {
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
  public BondCapitalIndexedSecurity<Coupon> toDerivative(final ZonedDateTime date, final ZonedDateTime settlementDate, final DoubleTimeSeries<ZonedDateTime> data) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(settlementDate, "settlement date");
    double settlementTime;
    if (settlementDate.isBefore(date)) {
      settlementTime = 0.0;
    } else {
      settlementTime = TimeCalculator.getTimeBetween(date, settlementDate);
    }
    double lasKnownFixingTime = 0;
    double lasKnownIndexFixing = 0;

    if (!data.isEmpty()) {
      lasKnownFixingTime = TimeCalculator.getTimeBetween(date, data.getLatestTime());
      lasKnownIndexFixing = data.getLatestValue();
    }
    final Annuity<Coupon> nominal = (Annuity<Coupon>) getNominal().toDerivative(date, data);
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
    final Annuity<Coupon> nominalStandard = nominal.trimBefore(settlementTime);
    final double accruedInterest = accruedInterest(settlementDate);
    final double factorSpot = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), settlementDate, couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0,
        _couponPerYear);
    final double factorPeriod = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(),
        couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, _couponPerYear);
    final double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    final double nbDayToSpot = couponDefinition.getNthPayment(0).getAccrualEndDate().getDayOfYear() - settlementDate.getDayOfYear();
    final double nbDaysPeriod = couponDefinition.getNthPayment(0).getAccrualEndDate().getDayOfYear() - couponDefinition.getNthPayment(0).getAccrualStartDate().getDayOfYear();
    final double ratioPeriodToNextCoupon = nbDayToSpot / nbDaysPeriod;
    final CouponInflationDefinition nominalLast = getNominal().getNthPayment(getNominal().getNumberOfPayments() - 1);
    final ZonedDateTime settlementDate2 = settlementDate.isBefore(date) ? date : settlementDate;
    final double notional = nominalLast.getNotional() * (settlementDate.isBefore(date) ? 0.0 : 1.0);
    final CouponInflationDefinition settlementDefinition = nominalLast.with(settlementDate2, nominalLast.getAccrualStartDate(), settlementDate2, notional);
    final CouponInflation settlement = (CouponInflation) settlementDefinition.toDerivative(date);
    double indexRatio = 1;
    if (settlement instanceof CouponInflationZeroCouponInterpolationGearing) {
      final int monthLag = ((CouponInflationZeroCouponInterpolationGearingDefinition) settlementDefinition).getMonthLag();
      final ZonedDateTime[] referenceStartDates = new ZonedDateTime[2];
      final ZonedDateTime refInterpolatedStartDate = settlementDate2.minusMonths(monthLag);
      referenceStartDates[0] = refInterpolatedStartDate.with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
      referenceStartDates[1] = referenceStartDates[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0).withSecond(0).withNano(0);
      final double indexEnd0 = data.getValue(referenceStartDates[0]);
      final double indexEnd1 = data.getValue(referenceStartDates[1]);
      final double weight = 1.0 - (settlementDate2.getDayOfMonth() - 1.0) / settlementDate2.toLocalDate().lengthOfMonth();
      final double indexEndValue = weight * indexEnd0 + (1 - weight) * indexEnd1;
      indexRatio = indexEndValue / _indexStartValue;
    } else if (settlement instanceof CouponInflationZeroCouponMonthlyGearing) {
      indexRatio = lasKnownIndexFixing / _indexStartValue;
    } else {
      throw new OpenGammaRuntimeException("Unsupported coupon type " + getNominal().getNthPayment(0).getClass());
    }

    return new BondCapitalIndexedSecurity<>(nominalStandard, couponStandard, settlementTime, accruedInterest, factorToNextCoupon, ratioPeriodToNextCoupon, _yieldConvention, _couponPerYear,
        settlement, _indexStartValue, lasKnownIndexFixing, lasKnownFixingTime, indexRatio, getIssuerEntity());
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
    final CouponInflationGearing currentCoupon = ((CouponInflationGearing) getCoupons().getNthPayment(couponIndex));
    final double accruedInterest = AccruedInterestCalculator.getAccruedInterest(getDayCount(), couponIndex, nbCoupon, previousAccrualDate, date, nextAccrualDate, currentCoupon.getFactor() *
        getCouponPerYear(),
        getCouponPerYear(), isEOM()) *
        getCoupons().getNthPayment(couponIndex).getNotional();
    if (getExCouponDays() != 0 && nextAccrualDate.minusDays(getExCouponDays()).isBefore(date)) {
      result = accruedInterest - currentCoupon.getFactor();
    } else {
      result = accruedInterest;
    }
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondCapitalIndexedSecurity(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitBondCapitalIndexedSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _couponPerYear;
    long temp;
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isEOM ? 1231 : 1237);
    result = prime * result + _monthLag;
    result = prime * result + _priceIndex.hashCode();
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
    final BondCapitalIndexedSecurityDefinition<?> other = (BondCapitalIndexedSecurityDefinition<?>) obj;
    if (_couponPerYear != other._couponPerYear) {
      return false;
    }
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (_isEOM != other._isEOM) {
      return false;
    }
    if (_monthLag != other._monthLag) {
      return false;
    }
    if (!ObjectUtils.equals(_priceIndex, other._priceIndex)) {
      return false;
    }
    return true;
  }

}
