/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.bond;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

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
import com.opengamma.analytics.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.analytics.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.analytics.financial.interestrate.inflation.derivatives.CouponInflation;
import com.opengamma.analytics.financial.interestrate.payments.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.AccruedInterestCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Describes a capital inflation indexed bond issue. Both the coupon and the nominal are indexed on a price index.
 * @param <C> Type of inflation coupon. Can be {@link CouponInflationZeroCouponMonthlyGearingDefinition} or {@link CouponInflationZeroCouponInterpolationGearingDefinition}.
 */
public class BondCapitalIndexedSecurityDefinition<C extends CouponInflationDefinition> extends BondSecurityDefinition<C, C> implements
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
   * The index value at the start of the bond.
   */
  private final double _indexStartValue;
  /**
   * The price index associated to the bond.
   */
  private final IndexPrice _priceIndex;

  /**
   * Constructor of the Capital inflation indexed bond. The repo type is set to "". 
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
      final Calendar calendar,
      final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final String issuer) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, "");
    for (int loopcpn = 0; loopcpn < coupon.getNumberOfPayments(); loopcpn++) {
      Validate.isTrue(coupon.getNthPayment(loopcpn) instanceof CouponInflationGearing, "Not inflation coupons");
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
      final Calendar calendar,
      final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final int monthLag, final String issuer, final String repoType) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, repoType);
    for (int loopcpn = 0; loopcpn < coupon.getNumberOfPayments(); loopcpn++) {
      Validate.isTrue(coupon.getNthPayment(loopcpn) instanceof CouponInflationGearing, "Not inflation coupons");
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
      final int settlementDays, final Calendar calendar,
      final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final String issuer) {
    // Nominal construction
    final CouponInflationZeroCouponMonthlyGearingDefinition nominalPayment = CouponInflationZeroCouponMonthlyGearingDefinition.from(startDate, maturityDate, notional, priceIndex, indexStartValue,
        monthLag,
        true, 1.0);
    final AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> nominalAnnuity = new AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition>(
        new CouponInflationZeroCouponMonthlyGearingDefinition[] {nominalPayment });
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(startDate, maturityDate, couponPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar, false);
    final CouponInflationZeroCouponMonthlyGearingDefinition[] coupons = new CouponInflationZeroCouponMonthlyGearingDefinition[paymentDates.length];
    coupons[0] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[0], startDate, paymentDatesUnadjusted[0], notional, priceIndex, indexStartValue, monthLag, true, realRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], notional, priceIndex,
          indexStartValue, monthLag, true, realRate);
    }
    final AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> couponAnnuity = new AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition>(coupons);
    return new BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition>(nominalAnnuity, couponAnnuity, indexStartValue, DEFAULT_EX_COUPON_DAYS, settlementDays,
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
      final BusinessDayConvention businessDay, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final String issuer) {
    // Nominal construction
    final CouponInflationZeroCouponMonthlyGearingDefinition nominalPayment = CouponInflationZeroCouponMonthlyGearingDefinition.from(startDate, maturityDate, notional, priceIndex, indexStartValue,
        monthLag,
        true, 1.0);
    final AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> nominalAnnuity = new AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition>(
        new CouponInflationZeroCouponMonthlyGearingDefinition[] {nominalPayment });
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(firstCouponDate, maturityDate, couponPeriod, true, false);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar, false);
    final CouponInflationZeroCouponMonthlyGearingDefinition[] coupons = new CouponInflationZeroCouponMonthlyGearingDefinition[paymentDates.length + 1];
    coupons[0] = CouponInflationZeroCouponMonthlyGearingDefinition.from(ScheduleCalculator.getAdjustedDate(firstCouponDate, 0, calendar), startDate, firstCouponDate, notional, priceIndex,
        indexStartValue, monthLag, true, realRate);
    coupons[1] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[0], firstCouponDate, paymentDatesUnadjusted[0], notional, priceIndex, indexStartValue, monthLag, true, realRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn + 1] = CouponInflationZeroCouponMonthlyGearingDefinition.from(paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], notional, priceIndex,
          indexStartValue, monthLag, true, realRate);
    }
    final AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition> couponAnnuity = new AnnuityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition>(coupons);
    return new BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponMonthlyGearingDefinition>(nominalAnnuity, couponAnnuity, indexStartValue, DEFAULT_EX_COUPON_DAYS, settlementDays,
        calendar, dayCount, yieldConvention, isEOM, monthLag, issuer);
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are interpolated index.
   * The coupon dates are computed from the maturity and have a short first coupon if required.
   * @param priceIndex
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
      final BusinessDayConvention businessDay, final int settlementDays,
      final Calendar calendar, final DayCount dayCount, final YieldConvention yieldConvention, final boolean isEOM, final String issuer) {
    // Nominal construction
    final CouponInflationZeroCouponInterpolationGearingDefinition nominalPayment = CouponInflationZeroCouponInterpolationGearingDefinition.from(startDate, maturityDate, notional, priceIndex,
        indexStartValue, monthLag, true, 1.0);
    final AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> nominalAnnuity = new AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>(
        new CouponInflationZeroCouponInterpolationGearingDefinition[] {nominalPayment });
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(startDate, maturityDate, couponPeriod, true, true);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar, false);
    final CouponInflationZeroCouponInterpolationGearingDefinition[] coupons = new CouponInflationZeroCouponInterpolationGearingDefinition[paymentDates.length];
    coupons[0] = CouponInflationZeroCouponInterpolationGearingDefinition.from(paymentDates[0], startDate, paymentDatesUnadjusted[0], notional, priceIndex, indexStartValue, monthLag, true, realRate);
    for (int loopcpn = 1; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationZeroCouponInterpolationGearingDefinition.from(paymentDates[loopcpn], paymentDatesUnadjusted[loopcpn - 1], paymentDatesUnadjusted[loopcpn], notional,
          priceIndex, indexStartValue, monthLag, true, realRate);
    }
    final AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition> couponAnnuity = new AnnuityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>(coupons);
    return new BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponInterpolationGearingDefinition>(nominalAnnuity, couponAnnuity, indexStartValue, DEFAULT_EX_COUPON_DAYS, settlementDays,
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

  @Override
  public BondCapitalIndexedSecurity<Coupon> toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, spot, new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[0], new double[0]));
  }

  @Override
  public BondCapitalIndexedSecurity<Coupon> toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> data, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(data, "data");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getSettlementDays(), getCalendar());
    return toDerivative(date, spot, data);
  }

  public BondCapitalIndexedSecurity<Coupon> toDerivative(final ZonedDateTime date, final ZonedDateTime settlementDate, final DoubleTimeSeries<ZonedDateTime> data) {
    Validate.notNull(date, "date");
    Validate.notNull(settlementDate, "settlement date");
    double settlementTime;
    if (settlementDate.isBefore(date)) {
      settlementTime = 0.0;
    } else {
      settlementTime = TimeCalculator.getTimeBetween(date, settlementDate);
    }
    final GenericAnnuity<Coupon> nominal = (GenericAnnuity<Coupon>) getNominal().toDerivative(date, data, "Not used");
    final AnnuityDefinition<CouponDefinition> couponDefinition = (AnnuityDefinition<CouponDefinition>) getCoupon().trimBefore(settlementDate);
    final CouponDefinition[] couponExPeriodArray = new CouponDefinition[couponDefinition.getNumberOfPayments()];
    System.arraycopy(couponDefinition.getPayments(), 0, couponExPeriodArray, 0, couponDefinition.getNumberOfPayments());
    if (getExCouponDays() != 0) {
      final ZonedDateTime exDividendDate = ScheduleCalculator.getAdjustedDate(couponDefinition.getNthPayment(0).getPaymentDate(), -getExCouponDays(), getCalendar());
      if (settlementDate.isAfter(exDividendDate)) {
        // Implementation note: Ex-dividend period: the next coupon is not received but its date is required for yield calculation
        couponExPeriodArray[0] = new CouponFixedDefinition(couponDefinition.getNthPayment(0), 0.0);
      }
    }
    final AnnuityDefinition<PaymentDefinition> couponDefinitionExPeriod = new AnnuityDefinition<PaymentDefinition>(couponExPeriodArray);
    final GenericAnnuity<Coupon> couponStandard = (GenericAnnuity<Coupon>) couponDefinitionExPeriod.toDerivative(date, data, "Not used");
    final GenericAnnuity<Coupon> nominalStandard = nominal.trimBefore(settlementTime);
    final double accruedInterest = accruedInterest(settlementDate);
    final double factorSpot = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), settlementDate, couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0,
        _couponPerYear);
    final double factorPeriod = getDayCount().getAccruedInterest(couponDefinition.getNthPayment(0).getAccrualStartDate(), couponDefinition.getNthPayment(0).getAccrualEndDate(),
        couponDefinition.getNthPayment(0).getAccrualEndDate(), 1.0, _couponPerYear);
    final double factorToNextCoupon = (factorPeriod - factorSpot) / factorPeriod;
    final CouponInflationDefinition nominalLast = getNominal().getNthPayment(getNominal().getNumberOfPayments() - 1);
    final ZonedDateTime settlementDate2 = settlementDate.isBefore(date) ? date : settlementDate;
    final double notional = settlementDate.isBefore(date) ? 0.0 : 1.0;
    final CouponInflationDefinition settlementDefinition = nominalLast.with(settlementDate2, nominalLast.getAccrualStartDate(), settlementDate2, notional);
    final CouponInflation settlement = (CouponInflation) settlementDefinition.toDerivative(date, "Not used");
    return new BondCapitalIndexedSecurity<Coupon>(nominalStandard, couponStandard, settlementTime, accruedInterest, factorToNextCoupon, _yieldConvention, _couponPerYear, settlement, _indexStartValue,
        getIssuer());
  }

  /**
   * Return the relative (not multiplied by the notional) accrued interest rate at a given date.
   * @param date The date.
   * @return The accrued interest.
   */
  public double accruedInterest(final ZonedDateTime date) {
    double result = 0;
    final int nbCoupon = getCoupon().getNumberOfPayments();
    int couponIndex = 0;
    for (int loopcpn = 0; loopcpn < nbCoupon; loopcpn++) {
      if (getCoupon().getNthPayment(loopcpn).getAccrualEndDate().isAfter(date)) {
        couponIndex = loopcpn;
        break;
      }
    }
    final ZonedDateTime previousAccrualDate = getCoupon().getNthPayment(couponIndex).getAccrualStartDate();
    final ZonedDateTime nextAccrualDate = getCoupon().getNthPayment(couponIndex).getAccrualEndDate();
    final CouponInflationGearing currentCoupon = ((CouponInflationGearing) getCoupon().getNthPayment(couponIndex));
    final double accruedInterest = AccruedInterestCalculator.getAccruedInterest(getDayCount(), couponIndex, nbCoupon, previousAccrualDate, date, nextAccrualDate, currentCoupon.getFactor(),
        getCouponPerYear(), isEOM()) * getCoupon().getNthPayment(couponIndex).getNotional();
    if (getExCouponDays() != 0 && nextAccrualDate.minusDays(getExCouponDays()).isBefore(date)) {
      result = accruedInterest - currentCoupon.getFactor() / _couponPerYear;
    } else {
      result = accruedInterest;
    }
    return result;
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitBondCapitalIndexedSecurity(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondCapitalIndexedSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _couponPerYear;
    result = prime * result + _dayCount.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + (_isEOM ? 1231 : 1237);
    result = prime * result + _monthLag;
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
    final BondCapitalIndexedSecurityDefinition<?> other = (BondCapitalIndexedSecurityDefinition<?>) obj;
    if (_couponPerYear != other._couponPerYear) {
      return false;
    }
    if (!ObjectUtils.equals(_dayCount, other._dayCount)) {
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
    if (!ObjectUtils.equals(_yieldConvention, other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
