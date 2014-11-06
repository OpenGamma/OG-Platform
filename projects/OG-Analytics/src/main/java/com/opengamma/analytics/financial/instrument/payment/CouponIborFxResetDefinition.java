/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborFxReset;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborSpread;
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
   * The ibor index fixing date.
   */
  private final ZonedDateTime _iborIndexFixingDate;
  /**
   * Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IborIndex _index;
  /**
   * The start date of the ibor index fixing period.
   */
  private final ZonedDateTime _iborIndexFixingPeriodStartDate;
  /**
   * The end date of the ibor index fixing period.
   */
  private final ZonedDateTime _iborIndexFixingPeriodEndDate;
  /**
   * The accrual factor (or year fraction) associated to the ibor index fixing period in the Index day count convention.
   */
  private final double _iborIndexFixingPeriodAccrualFactor;
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
   * @param iborIndexFixingDate The ibor index fixing date.
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
      final double notional, final ZonedDateTime iborIndexFixingDate, final IborIndex index, final double spread,
      final Calendar calendar, final Currency referenceCurrency, final ZonedDateTime fxFixingDate,
      final ZonedDateTime fxDeliveryDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(iborIndexFixingDate, "ibor index fixing date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(referenceCurrency, "reference currency");
    ArgumentChecker.notNull(fxFixingDate, "FX fixing date");
    ArgumentChecker.notNull(fxDeliveryDate, "FX delivery date");
    _iborIndexFixingDate = iborIndexFixingDate;
    _index = index;
    _iborIndexFixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(iborIndexFixingDate, _index.getSpotLag(),
        calendar);
    _iborIndexFixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_iborIndexFixingPeriodStartDate,
        index.getTenor(), index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
    _iborIndexFixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_iborIndexFixingPeriodStartDate,
        _iborIndexFixingPeriodEndDate, calendar);
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
   * @param iborIndexFixingDate The ibor index fixing date.
   * @param iborIndexFixingPeriodStartDate The start date of the ibor index fixing period.
   * @param iborIndexFixingPeriodEndDate The end date of the ibor index fixing period.
   * @param iborIndexFixingPeriodAccrualFactor The accrual factor (or year fraction) associated to the ibor index fixing period in the Index day count convention.
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
      final double notional, final ZonedDateTime iborIndexFixingDate,
      final ZonedDateTime iborIndexFixingPeriodStartDate,
      final ZonedDateTime iborIndexFixingPeriodEndDate, final double iborIndexFixingPeriodAccrualFactor,
      final IborIndex index,
      final double spread, final Calendar calendar, final Currency referenceCurrency, final ZonedDateTime fxFixingDate,
      final ZonedDateTime fxDeliveryDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(iborIndexFixingDate, "ibor index fixing date");
    ArgumentChecker.notNull(iborIndexFixingPeriodStartDate, "ibor index fixing period start date");
    ArgumentChecker.notNull(iborIndexFixingPeriodEndDate, "ibor index fixing period end date");
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.notNull(calendar, "calendar");
    ArgumentChecker.notNull(referenceCurrency, "reference currency");
    ArgumentChecker.notNull(fxFixingDate, "FX fixing date");
    ArgumentChecker.notNull(fxDeliveryDate, "FX delivery date");
    _iborIndexFixingPeriodStartDate = iborIndexFixingPeriodStartDate;
    _iborIndexFixingPeriodEndDate = iborIndexFixingPeriodEndDate;
    _iborIndexFixingPeriodAccrualFactor = iborIndexFixingPeriodAccrualFactor;
    _iborIndexFixingDate = iborIndexFixingDate;
    _index = index;
    _spread = spread;
    _calendar = calendar;
    _referenceCurrency = referenceCurrency;
    _fxFixingDate = fxFixingDate;
    _fxDeliveryDate = fxDeliveryDate;
  }

  /**
   * Gets the ibor index fixing date.
   * @return The ibor index fixing date.
   */
  public ZonedDateTime getIborIndexFixingDate() {
    return _iborIndexFixingDate;
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
   * Gets the start date of the ibor index fixing period.
   * @return The start date of the ibor index fixing period.
   */
  public ZonedDateTime getIborIndexFixingPeriodStartDate() {
    return _iborIndexFixingPeriodStartDate;
  }

  /**
   * Gets the end date of the ibor index fixing period.
   * @return The end date of the ibor index fixing period.
   */
  public ZonedDateTime getIborIndexFixingPeriodEndDate() {
    return _iborIndexFixingPeriodEndDate;
  }

  /**
   * Gets the accrual factor (or year fraction) associated to the ibor index fixing period in the Index day count convention.
   * @return The accrual factor.
   */
  public double getIborIndexFixingPeriodAccrualFactor() {
    return _iborIndexFixingPeriodAccrualFactor;
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

  @Override
  public CouponIborFxReset toDerivative(ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "dateTime");
    LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getIborIndexFixingDate().toLocalDate()),
        "Do not have any fixing data but are asking for a derivative at " + dateTime +
            " which is after ibor index fixing date " + getIborIndexFixingDate());
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    double fixingTime = TimeCalculator.getTimeBetween(dateTime, getIborIndexFixingDate());
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getIborIndexFixingPeriodEndDate());
    double fxFixingTime = TimeCalculator.getTimeBetween(dateTime, _fxFixingDate);
    double fxDeliveryTime = TimeCalculator.getTimeBetween(dateTime, _fxDeliveryDate);
    return new CouponIborFxReset(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime,
        getIndex(), fixingPeriodStartTime, fixingPeriodEndTime, getIborIndexFixingPeriodAccrualFactor(), _spread,
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
    LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    ArgumentChecker.noNulls(timeSeries, "Time series");
    ArgumentChecker.isTrue(timeSeries.length >= 2, "At least two time series");
    DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries = timeSeries[0];
    DoubleTimeSeries<ZonedDateTime> fxFixingTimeSeries = timeSeries[1];

    LocalDate indexFixingDate = getIborIndexFixingDate().toLocalDate();
    LocalDate fxFixingDate = getFxFixingDate().toLocalDate();

    if (dayConversion.isAfter(indexFixingDate)) { // Index fixing should be known
      ZonedDateTime rezonedFixingDate = getIborIndexFixingDate().toLocalDate().atStartOfDay(ZoneOffset.UTC);
      Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDate);
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get ibor index fixing value for date " +
            getIborIndexFixingDate());
      }
      CouponFixedFxResetDefinition fixedFxResetDfn = new CouponFixedFxResetDefinition(getCurrency(),
          getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), getNotional(),
          fixedRate + _spread, _referenceCurrency, _fxFixingDate, _fxDeliveryDate);
      return fixedFxResetDfn.toDerivative(dateTime, fxFixingTimeSeries);
    }
    if (dayConversion.equals(indexFixingDate)) { // On index fixing date: use fixing if present
      Double fixedRate = indexFixingTimeSeries.getValue(getIborIndexFixingDate());
      if (fixedRate != null) {
        CouponFixedFxResetDefinition fixedFxResetDfn = new CouponFixedFxResetDefinition(getCurrency(),
            getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), getNotional(),
            fixedRate + _spread, _referenceCurrency, _fxFixingDate, _fxDeliveryDate);
        return fixedFxResetDfn.toDerivative(dateTime, fxFixingTimeSeries);
      }
    }

    double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    double fixingTime = TimeCalculator.getTimeBetween(dateTime, getIborIndexFixingDate());
    double fixingPeriodStartTime = TimeCalculator.getTimeBetween(dateTime, getIborIndexFixingPeriodStartDate());
    double fixingPeriodEndTime = TimeCalculator.getTimeBetween(dateTime, getIborIndexFixingPeriodEndDate());

    if (dayConversion.isAfter(fxFixingDate)) { // FX fixing should be known
      ZonedDateTime rezonedFixingDate = _fxFixingDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      Double fxRate = fxFixingTimeSeries.getValue(rezonedFixingDate);
      if (fxRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + _fxFixingDate);
      }
      double notional = getNotional() * fxRate;
      return new CouponIborSpread(getCurrency(), paymentTime, getPaymentYearFraction(), notional, fixingTime, _index,
          fixingPeriodStartTime, fixingPeriodEndTime, getIborIndexFixingPeriodAccrualFactor(), _spread);
    }
    if (dayConversion.equals(fxFixingDate)) { // On FX fixing date: use fixing if present
      ZonedDateTime rezonedFixingDate = _fxFixingDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      Double fxRate = fxFixingTimeSeries.getValue(rezonedFixingDate);
      if (fxRate != null) {
        double notional = getNotional() * fxRate;
        return new CouponIborSpread(getCurrency(), paymentTime, getPaymentYearFraction(), notional, fixingTime, _index,
            fixingPeriodStartTime, fixingPeriodEndTime, getIborIndexFixingPeriodAccrualFactor(), _spread);
      }
    }

    // Default: no fixing
    double fxFixingTime = TimeCalculator.getTimeBetween(dateTime, _fxFixingDate);
    double fxDeliveryTime = TimeCalculator.getTimeBetween(dateTime, _fxDeliveryDate);
    return new CouponIborFxReset(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime,
        _index, fixingPeriodStartTime, fixingPeriodEndTime, _iborIndexFixingPeriodAccrualFactor, _spread,
        _referenceCurrency, fxFixingTime, fxDeliveryTime);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_calendar == null) ? 0 : _calendar.hashCode());
    result = prime * result + ((_iborIndexFixingDate == null) ? 0 : _iborIndexFixingDate.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_iborIndexFixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_iborIndexFixingPeriodEndDate == null) ? 0 : _iborIndexFixingPeriodEndDate.hashCode());
    result = prime * result +
        ((_iborIndexFixingPeriodStartDate == null) ? 0 : _iborIndexFixingPeriodStartDate.hashCode());
    result = prime * result + ((_fxDeliveryDate == null) ? 0 : _fxDeliveryDate.hashCode());
    result = prime * result + ((_fxFixingDate == null) ? 0 : _fxFixingDate.hashCode());
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + ((_referenceCurrency == null) ? 0 : _referenceCurrency.hashCode());
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (!(obj instanceof CouponIborFxResetDefinition)) {
      return false;
    }
    CouponIborFxResetDefinition other = (CouponIborFxResetDefinition) obj;
    if (!ObjectUtils.equals(_calendar, other._calendar)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndexFixingDate, other._iborIndexFixingDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_iborIndexFixingPeriodAccrualFactor) != Double
        .doubleToLongBits(other._iborIndexFixingPeriodAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndexFixingPeriodEndDate, other._iborIndexFixingPeriodEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_iborIndexFixingPeriodStartDate, other._iborIndexFixingPeriodStartDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fxDeliveryDate, other._fxDeliveryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_fxFixingDate, other._fxFixingDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_referenceCurrency, other._referenceCurrency)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    return true;
  }

}