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
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.FixedIncomeInstrumentWithDataConverter;
import com.opengamma.financial.instrument.annuity.AnnuityDefinition;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponFirstOfMonthDefinition;
import com.opengamma.financial.instrument.inflation.CouponInflationZeroCouponInterpolationDefinition;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.PaymentDefinition;
import com.opengamma.financial.interestrate.annuity.definition.GenericAnnuity;
import com.opengamma.financial.interestrate.bond.definition.BondCapitalIndexedSecurity;
import com.opengamma.financial.interestrate.bond.definition.BondSecurity;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;

/**
 * Describes a capital inflation indexed bond issue. Both the coupon and the nominal are indexed on a price index.
 * @param <C> Type of inflation coupon. Can be {@link CouponInflationZeroCouponFirstOfMonthDefinition} or {@link CouponInflationZeroCouponInterpolationDefinition}.
 */
public class BondCapitalIndexedSecurityDefinition<C extends CouponDefinition> extends BondSecurityDefinition<C, C> implements
    FixedIncomeInstrumentWithDataConverter<BondSecurity<? extends Payment, ? extends Coupon>, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The default notional for the security.
   */
  private static final double DEFAULT_NOTIONAL = 1.0;
  /**
   * The default ex-coupon number of days.
   */
  private static final int DEFAULT_EX_COUPON_DAYS = 0;
  /**
   * The default number of month between reference index and payment.
   */
  private static final int DEFAULT_MONTH_LAG = 3;
  /**
   * The yield (to maturity) computation convention.
   */
  private final YieldConvention _yieldConvention;

  /**
   * Constructor of the Capital inflation indexed bond.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param exCouponDays The ex-coupon period.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param issuer The bond issuer name.
   */
  public BondCapitalIndexedSecurityDefinition(AnnuityDefinition<C> nominal, AnnuityDefinition<C> coupon, int exCouponDays, int settlementDays, Calendar calendar, YieldConvention yieldConvention,
      String issuer) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, "");
    _yieldConvention = yieldConvention;
  }

  /**
   * Constructor of the Capital inflation indexed bond.
   * @param nominal The nominal annuity.
   * @param coupon The coupon annuity.
   * @param exCouponDays The ex-coupon period.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param issuer Issuer name.
   * @param repoType The repo type name.
   */
  public BondCapitalIndexedSecurityDefinition(AnnuityDefinition<C> nominal, AnnuityDefinition<C> coupon, int exCouponDays, int settlementDays, Calendar calendar, YieldConvention yieldConvention,
      final String issuer, final String repoType) {
    super(nominal, coupon, exCouponDays, settlementDays, calendar, issuer, repoType);
    _yieldConvention = yieldConvention;
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are monthly index (no interpolation).
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param indexStartValue The index value at the start of the bond.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param nominalRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param issuer The bond issuer name.
   * @return The bond.
   */
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> fromFirstOfMonth(final PriceIndex priceIndex, final int monthLag, ZonedDateTime startDate,
      double indexStartValue, ZonedDateTime maturityDate, Period couponPeriod, double nominalRate, BusinessDayConvention businessDay, int settlementDays, Calendar calendar,
      YieldConvention yieldConvention, String issuer) {
    // Nominal construction
    CouponInflationZeroCouponFirstOfMonthDefinition nominalPayment = CouponInflationZeroCouponFirstOfMonthDefinition.from(startDate, maturityDate, DEFAULT_NOTIONAL, priceIndex, indexStartValue,
        monthLag, true);
    AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> nominalAnnuity = new AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition>(
        new CouponInflationZeroCouponFirstOfMonthDefinition[] {nominalPayment});
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(startDate, maturityDate, couponPeriod);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar);
    final CouponInflationZeroCouponFirstOfMonthDefinition[] coupons = new CouponInflationZeroCouponFirstOfMonthDefinition[paymentDates.length];
    for (int loopcpn = 0; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn] = CouponInflationZeroCouponFirstOfMonthDefinition.from(startDate, paymentDates[loopcpn], DEFAULT_NOTIONAL * nominalRate, priceIndex, indexStartValue, monthLag, true);
    }
    AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> couponAnnuity = new AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition>(coupons);
    return new BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition>(nominalAnnuity, couponAnnuity, DEFAULT_EX_COUPON_DAYS, settlementDays, calendar, yieldConvention,
        issuer);
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are monthly index (no interpolation).
   * @param priceIndex The price index associated to the bond.
   * @param startDate The bond start date.
   * @param indexStartValue The index value at the start of the bond.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param nominalRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param issuer The bond issuer name.
   * @return The bond.
   */
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> fromFirstOfMonth(final PriceIndex priceIndex, ZonedDateTime startDate, double indexStartValue,
      ZonedDateTime maturityDate, Period couponPeriod, double nominalRate, BusinessDayConvention businessDay, int settlementDays, Calendar calendar, YieldConvention yieldConvention, String issuer) {
    return BondCapitalIndexedSecurityDefinition.fromFirstOfMonth(priceIndex, DEFAULT_MONTH_LAG, startDate, indexStartValue, maturityDate, couponPeriod, nominalRate, businessDay, settlementDays,
        calendar, yieldConvention, issuer);
  }

  /**
   * Builder of Inflation capital index bond from financial details. The notional and the coupon reference index are monthly index (no interpolation).
   * @param priceIndex The price index associated to the bond.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param startDate The bond start date.
   * @param indexStartValue The index value at the start of the bond.
   * @param firstCouponDate The bond first coupon date. Used for short/long first coupon.
   * @param maturityDate The bond maturity date.
   * @param couponPeriod The period between coupon payments.
   * @param nominalRate The bond nominal rate.
   * @param businessDay The business day convention to compute the payment days.
   * @param settlementDays Standard number of days between trade date and trade settlement. Used for clean price and yield computation.
   * @param calendar The payment calendar.
   * @param yieldConvention The yield (to maturity) computation convention.
   * @param issuer The bond issuer name.
   * @return The bond.
   */
  public static BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> fromFirstOfMonth(final PriceIndex priceIndex, final int monthLag, ZonedDateTime startDate,
      double indexStartValue, ZonedDateTime firstCouponDate, ZonedDateTime maturityDate, Period couponPeriod, double nominalRate, BusinessDayConvention businessDay, int settlementDays,
      Calendar calendar, YieldConvention yieldConvention, String issuer) {
    // Nominal construction
    CouponInflationZeroCouponFirstOfMonthDefinition nominalPayment = CouponInflationZeroCouponFirstOfMonthDefinition.from(startDate, maturityDate, DEFAULT_NOTIONAL, priceIndex, indexStartValue,
        monthLag, true);
    AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> nominalAnnuity = new AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition>(
        new CouponInflationZeroCouponFirstOfMonthDefinition[] {nominalPayment});
    // Coupon construction
    final ZonedDateTime[] paymentDatesUnadjusted = ScheduleCalculator.getUnadjustedDateSchedule(firstCouponDate, maturityDate, couponPeriod);
    final ZonedDateTime[] paymentDates = ScheduleCalculator.getAdjustedDateSchedule(paymentDatesUnadjusted, businessDay, calendar);
    final CouponInflationZeroCouponFirstOfMonthDefinition[] coupons = new CouponInflationZeroCouponFirstOfMonthDefinition[paymentDates.length + 1];
    coupons[0] = CouponInflationZeroCouponFirstOfMonthDefinition.from(startDate, firstCouponDate, DEFAULT_NOTIONAL * nominalRate, priceIndex, indexStartValue, monthLag, true);
    for (int loopcpn = 0; loopcpn < paymentDates.length; loopcpn++) {
      coupons[loopcpn + 1] = CouponInflationZeroCouponFirstOfMonthDefinition.from(startDate, paymentDates[loopcpn], DEFAULT_NOTIONAL * nominalRate, priceIndex, indexStartValue, monthLag, true);
    }
    AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition> couponAnnuity = new AnnuityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition>(coupons);
    return new BondCapitalIndexedSecurityDefinition<CouponInflationZeroCouponFirstOfMonthDefinition>(nominalAnnuity, couponAnnuity, DEFAULT_EX_COUPON_DAYS, settlementDays, calendar, yieldConvention,
        issuer);
  }

  /**
   * Gets the bond yield convention.
   * @return The yield convention.
   */
  public YieldConvention getYieldConvention() {
    return _yieldConvention;
  }

  @Override
  public BondCapitalIndexedSecurity<Coupon> toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getCalendar(), getSettlementDays());
    return toDerivative(date, spot, new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTime[0], new double[0]));
  }

  @Override
  public BondCapitalIndexedSecurity<Coupon> toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> data, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(data, "data");
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(date, getCalendar(), getSettlementDays());
    return toDerivative(date, spot, data);
  }

  @SuppressWarnings("unchecked")
  public BondCapitalIndexedSecurity<Coupon> toDerivative(ZonedDateTime date, ZonedDateTime settlementDate, DoubleTimeSeries<ZonedDateTime> data) {
    Validate.notNull(date, "date");
    Validate.notNull(settlementDate, "settlement date");
    double settlementTime;
    if (settlementDate.isBefore(date)) {
      settlementTime = 0.0;
    } else {
      settlementTime = TimeCalculator.getTimeBetween(date, settlementDate);
    }
    final GenericAnnuity<Coupon> nominal = (GenericAnnuity<Coupon>) getNominal().toDerivative(date, data, "Not used");
    AnnuityDefinition<CouponDefinition> couponDefinition = (AnnuityDefinition<CouponDefinition>) getCoupon().trimBefore(settlementDate);
    CouponDefinition[] couponExPeriodArray = new CouponDefinition[couponDefinition.getNumberOfPayments()];
    System.arraycopy(couponDefinition.getPayments(), 0, couponExPeriodArray, 0, couponDefinition.getNumberOfPayments());
    if (getExCouponDays() != 0) {
      ZonedDateTime exDividendDate = ScheduleCalculator.getAdjustedDate(couponDefinition.getNthPayment(0).getPaymentDate(), getCalendar(), -getExCouponDays());
      if (settlementDate.isAfter(exDividendDate)) {
        // Implementation note: Ex-dividend period: the next coupon is not received but its date is required for yield calculation
        couponExPeriodArray[0] = new CouponFixedDefinition(couponDefinition.getNthPayment(0), 0.0);
      }
    }
    AnnuityDefinition<PaymentDefinition> couponDefinitionExPeriod = new AnnuityDefinition<PaymentDefinition>(couponExPeriodArray);
    final GenericAnnuity<Coupon> couponStandard = (GenericAnnuity<Coupon>) couponDefinitionExPeriod.toDerivative(date, data, "Not used");
    final GenericAnnuity<Coupon> nominalStandard = nominal.trimBefore(settlementTime);
    return new BondCapitalIndexedSecurity<Coupon>(nominalStandard, couponStandard, settlementTime, _yieldConvention, getIssuer());
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitBondCapitalIndexedSecurity(this, data);
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitBondCapitalIndexedSecurity(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
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
    BondCapitalIndexedSecurityDefinition<?> other = (BondCapitalIndexedSecurityDefinition<?>) obj;
    if (!ObjectUtils.equals(_yieldConvention, other._yieldConvention)) {
      return false;
    }
    return true;
  }

}
