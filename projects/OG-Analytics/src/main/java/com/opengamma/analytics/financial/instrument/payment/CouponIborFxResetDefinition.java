/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing Ibor-like floating coupon with a spread and FX reset.
 * The currency is the currency of the payment. 
 * The notional is expressed in the reference currency, from which the FX reset will be computed.
 * For exact description of the instrument, see reference.
 * <P>
 * Reference: Coupon with FX Reset Notional, OpenGamma Documentation 26, September 2014.
 */
public class CouponIborFxResetDefinition extends CouponDefinition implements
    InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>[]> {

  /**
   * The coupon fixing date.
   */
  private final ZonedDateTime _fixingDate;
  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IborIndex _index;
  /**
   * The start date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodStartDate;
  /**
   * The end date of the fixing period.
   */
  private final ZonedDateTime _fixingPeriodEndDate;
  /**
   * The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * The spread paid above the Ibor rate.
   */
  private final double _spread;
  /**
   * The holiday calendar for the ibor index.
   */
  private final Calendar _calendar;
  /** 
   * The reference currency. 
   */
  private final Currency _referenceCurrency;
  /** 
   * The FX fixing date. The notional used for the payment is the FX rate between the reference currency (RC) and the 
   *  payment currency (PC): 1 RC = X . PC. 
   */
  private final ZonedDateTime _fxFixingDate;
  /** 
   * The spot (delivery) date for the FX transaction underlying the FX fixing. 
   */
  private final ZonedDateTime _fxDeliveryDate;

  /**
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentAccrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional in the reference currency.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param spread The spread paid above the Ibor rate.
   * @param calendar The holiday calendar for the ibor index.
   * @param referenceCurrency The reference currency for the FX reset.
   * @param fxFixingDate The FX fixing or reset date. The notional used for the payment is the FX rate between the 
   * reference currency (RC) and the payment currency (PC): 1 RC = X . PC.
   * @param fxDeliveryDate The spot or delivery date for the FX transaction underlying the FX fixing.
   */
  public CouponIborFxResetDefinition(final Currency currency, final ZonedDateTime paymentDate,
      final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double paymentAccrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index, final double spread,
      final Calendar calendar,
      final Currency referenceCurrency, final ZonedDateTime fxFixingDate, final ZonedDateTime fxDeliveryDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(fixingDate, "fixing date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(referenceCurrency, "reference currency");
    ArgumentChecker.notNull(fxFixingDate, "FX fixing date");
    ArgumentChecker.notNull(fxDeliveryDate, "FX delivery date");
    _fixingDate = fixingDate;
    _index = index;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getSpotLag(), calendar);
    _fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate, index.getTenor(),
        index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate,
        calendar);
    _spread = spread;
    _calendar = calendar;
    _referenceCurrency = referenceCurrency;
    _fxFixingDate = fxFixingDate;
    _fxDeliveryDate = fxDeliveryDate;
  }

  /**
   * @param currency The coupn currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional Coupon notional in the reference currency.
   * @param fixingDate The coupon fixing date.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param fixingPeriodEndDate The end date of the fixing period.
   * @param fixingPeriodAccrualFactor The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @param index The coupon Ibor index. Should of the same currency as the payment.
   * @param spread The spread paid above the Ibor rate.
   * @param calendar The holiday calendar for the ibor index.
   * @param referenceCurrency The reference currency for the FX reset.
   * @param fxFixingDate The FX fixing or reset date. The notional used for the payment is the FX rate between the 
   * reference currency (RC) and the payment currency (PC): 1 RC = X . PC.
   * @param fxDeliveryDate The spot or delivery date for the FX transaction underlying the FX fixing.
   */
  public CouponIborFxResetDefinition(final Currency currency, final ZonedDateTime paymentDate,
      final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double paymentAccrualFactor,
      final double notional, final ZonedDateTime fixingDate, final ZonedDateTime fixingPeriodStartDate,
      final ZonedDateTime fixingPeriodEndDate, final double fixingPeriodAccrualFactor, final IborIndex index,
      final double spread, final Calendar calendar, final Currency referenceCurrency, final ZonedDateTime fxFixingDate,
      final ZonedDateTime fxDeliveryDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(referenceCurrency, "reference currency");
    ArgumentChecker.notNull(fxFixingDate, "FX fixing date");
    ArgumentChecker.notNull(fxDeliveryDate, "FX delivery date");
    _fixingPeriodStartDate = fixingPeriodStartDate;
    _fixingPeriodEndDate = fixingPeriodEndDate;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _fixingDate = fixingDate;
    _index = index;
    _spread = spread;
    _calendar = calendar;
    _referenceCurrency = referenceCurrency;
    _fxFixingDate = fxFixingDate;
    _fxDeliveryDate = fxDeliveryDate;
  }

  /**
   * Gets the fixing date.
   * @return The fixing date.
   */
  public ZonedDateTime getFixingDate() {
    return _fixingDate;
  }

  /**
   * Gets the spread.
   * @return The spread.
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Gets the Ibor index of the instrument.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Gets the start date of the fixing period.
   * @return The start date of the fixing period.
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the end date of the fixing period.
   * @return The end date of the fixing period.
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @return The accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the holiday calendar for the ibor index.
   * @return The holiday calendar
   */
  public Calendar getCalendar() {
    return _calendar;
  }

  /**
   * Returns the reference currency.
   * @return The currency.
   */
  public Currency getReferenceCurrency() {
    return _referenceCurrency;
  }

  /**
   * Returns the FX fixing date.
   * @return The date.
   */
  public ZonedDateTime getFxFixingDate() {
    return _fxFixingDate;
  }

  /**
   * Returns the FX delivery date.
   * @return The date.
   */
  public ZonedDateTime getFxDeliveryDate() {
    return _fxDeliveryDate;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Override
  @Deprecated()
  public CouponIborFxReset toDerivative(ZonedDateTime dateTime, String... yieldCurveNames) {
    throw new UnsupportedOperationException(
        "CouponIborFxResetDefinition does not support toDerivative with yield curve name - deprecated method");
  }

  @Override
  public CouponIborFxReset toDerivative(ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "dateTime");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getFixingDate().toLocalDate()),
        "Do not have any fixing data but are asking for a derivative at " + dateTime + " which is after fixing date "
            + getFixingDate());
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    double fxFixingTime = TimeCalculator.getTimeBetween(dateTime, _fxFixingDate);
    double fxDeliveryTime = TimeCalculator.getTimeBetween(dateTime, _fxDeliveryDate);
    return new CouponIborFxReset(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime,
        getIndex(), fixingPeriodStartTime, fixingPeriodEndTime, getFixingPeriodAccrualFactor(), _spread,
        getReferenceCurrency(), fxFixingTime, fxDeliveryTime);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Override
  @Deprecated
  public Payment toDerivative(ZonedDateTime dateTime, DoubleTimeSeries<ZonedDateTime>[] timeSeries,
      String... yieldCurveNames) {
    throw new UnsupportedOperationException(
        "CouponIborFxResetDefinition does not support toDerivative with yield curve name - deprecated method");
  }

  /**
   * {@inheritDoc}
   * @param timeSeries Array of timeSeries: timeSeries[0] is index fixing time series, timeSeries[1] is fx fixing time series
   */
  @Override
  public Coupon toDerivative(ZonedDateTime dateTime, DoubleTimeSeries<ZonedDateTime>[] timeSeries) {
    ArgumentChecker.notNull(dateTime, "dateTime");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    ArgumentChecker.noNulls(timeSeries, "Time series");
    ArgumentChecker.isTrue(timeSeries.length >= 2, "At least two time series");
    DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries = timeSeries[0];
    DoubleTimeSeries<ZonedDateTime> fxFixingTimeSeries = timeSeries[1];

    final LocalDate indexFixingDate = getFixingDate().toLocalDate();
    final LocalDate fxFixingDate = getFxFixingDate().toLocalDate();

    if (dayConversion.isAfter(indexFixingDate)) { // Index fixing should be known
      final ZonedDateTime rezonedFixingDate = getFixingDate().toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDate);
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
      }
      CouponFixedFxResetDefinition fixedFxResetDfn = new CouponFixedFxResetDefinition(getCurrency(),
          getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), getNotional(),
          fixedRate + _spread, _referenceCurrency, _fxFixingDate, _fxDeliveryDate);
      return fixedFxResetDfn.toDerivative(dateTime, fxFixingTimeSeries);
    }
    if (dayConversion.equals(indexFixingDate)) { // On index fixing date: use fixing if present
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      if (fixedRate != null) {
        CouponFixedFxResetDefinition fixedFxResetDfn = new CouponFixedFxResetDefinition(getCurrency(),
            getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), getNotional(),
            fixedRate + _spread, _referenceCurrency, _fxFixingDate, _fxDeliveryDate);
        return fixedFxResetDfn.toDerivative(dateTime, fxFixingTimeSeries);
      }
    }

    if (dayConversion.isAfter(fxFixingDate)) { // FX fixing should be known
      ZonedDateTime rezonedFixingDate = _fxFixingDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      Double fxRate = fxFixingTimeSeries.getValue(rezonedFixingDate);
      if (fxRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + _fxFixingDate);
      }
      double notional = getNotional() * fxRate;
      CouponIborSpreadDefinition dfn = new CouponIborSpreadDefinition(getCurrency(), getPaymentDate(),
          getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), notional, _fixingDate,
          _fixingPeriodStartDate, _fixingPeriodEndDate, _fixingPeriodAccrualFactor, _index, _spread, _calendar);
      return dfn.toDerivative(dateTime, indexFixingTimeSeries);
    }
    if (dayConversion.equals(fxFixingDate)) { // On FX fixing date: use fixing if present
      ZonedDateTime rezonedFixingDate = _fxFixingDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      Double fxRate = fxFixingTimeSeries.getValue(rezonedFixingDate);
      if (fxRate != null) {
        double notional = getNotional() * fxRate;
        CouponIborSpreadDefinition dfn = new CouponIborSpreadDefinition(getCurrency(), getPaymentDate(),
            getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), notional, _fixingDate,
            _fixingPeriodStartDate, _fixingPeriodEndDate, _fixingPeriodAccrualFactor, _index, _spread, _calendar);
        return dfn.toDerivative(dateTime, indexFixingTimeSeries);
      }
    }

    // Default: no fixing
    double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate());
    double fxFixingTime = TimeCalculator.getTimeBetween(dateTime, _fxFixingDate);
    double fxDeliveryTime = TimeCalculator.getTimeBetween(dateTime, _fxDeliveryDate);
    return new CouponIborFxReset(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime,
        _index, fixingPeriodStartTime, fixingPeriodEndTime, _fixingPeriodAccrualFactor, _spread, _referenceCurrency,
        fxFixingTime, fxDeliveryTime);
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborFxResetDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborFxResetDefinition(this);
  }

}