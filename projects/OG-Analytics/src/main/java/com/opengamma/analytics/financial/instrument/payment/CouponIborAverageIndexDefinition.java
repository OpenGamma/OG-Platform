/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborAverage;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an average Ibor-like floating coupon (weighted mean of two different indexes).
 */

public class CouponIborAverageIndexDefinition extends CouponFloatingDefinition {

  /**
   * Ibor-like index1 on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IborIndex _index1;
  /**
   * Ibor-like index2 on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IborIndex _index2;
  /**
   * The weight for the first index.
   */
  private final double _weight1;
  /**
   * The weight of the second index.
   */
  private final double _weight2;
  /**
   * The start date of the fixing period of the first index.
   */
  private final ZonedDateTime _fixingPeriodStartDate1;
  /**
   * The end date of the fixing period of the first index.
   */
  private final ZonedDateTime _fixingPeriodEndDate1;
  /**
   * The accrual factor (or year fraction) associated to the fixing period of the first index in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor1;
  /**
   * The start date of the fixing period of the second index.
   */
  private final ZonedDateTime _fixingPeriodStartDate2;
  /**
   * The end date of the fixing period of the second index.
   */
  private final ZonedDateTime _fixingPeriodEndDate2;
  /**
   * The accrual factor (or year fraction) associated to the fixing period of the second index in the Index day count convention.
   */
  private final double _fixingPeriodAccrualFactor2;

  /**
   * Constructor of an average Ibor-like floating coupon from the coupon details and the Ibor indices. The currency is the same for both index.
   * The payment currency is the indices currency.
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period of the first index.
   * @param accrualEndDate The end date of the accrual period of the first index.
   * @param paymentAccrualFactor The accrual factor of the accrual period of the first index.
   * @param notional The coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index1 The first coupon Ibor index. Should have the same currency as the payment.
   * @param index2 The second coupon Ibor index. Should have the same currency as the payment.
   * @param weight1 The weight of the first index.
   * @param weight2 The weight of the second index.
   * @param iborCalendar1 The holiday calendar for the first ibor index.
   * @param iborCalendar2 The holiday calendar for the second ibor index.
   */
  public CouponIborAverageIndexDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final ZonedDateTime fixingDate, final IborIndex index1, final IborIndex index2, final double weight1,
      final double weight2, final Calendar iborCalendar1, final Calendar iborCalendar2) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index1, "index1");
    ArgumentChecker.notNull(index2, "index2");
    ArgumentChecker.isTrue(currency.equals(index1.getCurrency()), "index1 currency different from payment currency");
    ArgumentChecker.isTrue(currency.equals(index2.getCurrency()), "index2 currency different from payment currency");
    _index1 = index1;
    _index2 = index2;
    _weight1 = weight1;
    _weight2 = weight2;
    _fixingPeriodStartDate1 = ScheduleCalculator.getAdjustedDate(fixingDate, _index1.getSpotLag(), iborCalendar1);
    _fixingPeriodEndDate1 = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate1, index1.getTenor(), index1.getBusinessDayConvention(), iborCalendar1,
        index1.isEndOfMonth());
    _fixingPeriodAccrualFactor1 = index1.getDayCount().getDayCountFraction(_fixingPeriodStartDate1, _fixingPeriodEndDate1, iborCalendar1);
    _fixingPeriodStartDate2 = ScheduleCalculator.getAdjustedDate(fixingDate, _index2.getSpotLag(), iborCalendar2);
    _fixingPeriodEndDate2 = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate2, index2.getTenor(), index2.getBusinessDayConvention(), iborCalendar2,
        index2.isEndOfMonth());
    _fixingPeriodAccrualFactor2 = index2.getDayCount().getDayCountFraction(_fixingPeriodStartDate2, _fixingPeriodEndDate2, iborCalendar2);
  }

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index. The payment currency is the index currency.
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param fixingPeriodStartDate1 The start date of the fixing period.
   * @param fixingPeriodEndDate1 The end date of the fixing period.
   * @param fixingPeriodAccrualFactor1 The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @param fixingPeriodStartDate2 The start date of the fixing period.
   * @param fixingPeriodEndDate2 The end date of the fixing period.
   * @param fixingPeriodAccrualFactor2 The accrual factor (or year fraction) associated to the fixing period in the Index day count convention.
   * @param index1 The first coupon Ibor index. Should of the same currency as the payment.
   * @param index2 The second coupon Ibor index. Should of the same currency as the payment.
   * @param weight1 The weight of the first index.
   * @param weight2 The weight of the second index.
   */
  public CouponIborAverageIndexDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final ZonedDateTime fixingDate, final ZonedDateTime fixingPeriodStartDate1,
      final ZonedDateTime fixingPeriodEndDate1, final double fixingPeriodAccrualFactor1, final ZonedDateTime fixingPeriodStartDate2,
      final ZonedDateTime fixingPeriodEndDate2, final double fixingPeriodAccrualFactor2, final IborIndex index1, final IborIndex index2, final double weight1,
      final double weight2) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index1, "index1");
    ArgumentChecker.notNull(index2, "index2");
    ArgumentChecker.isTrue(currency.equals(index1.getCurrency()), "index1 currency different from payment currency");
    ArgumentChecker.isTrue(currency.equals(index2.getCurrency()), "index2 currency different from payment currency");
    _index1 = index1;
    _index2 = index2;
    _weight1 = weight1;
    _weight2 = weight2;
    _fixingPeriodStartDate1 = fixingPeriodStartDate1;
    _fixingPeriodEndDate1 = fixingPeriodEndDate1;
    _fixingPeriodAccrualFactor1 = fixingPeriodAccrualFactor1;
    _fixingPeriodStartDate2 = fixingPeriodStartDate2;
    _fixingPeriodEndDate2 = fixingPeriodEndDate2;
    _fixingPeriodAccrualFactor2 = fixingPeriodAccrualFactor2;

  }

  /**
   * Constructor of a Ibor-like floating coupon from the coupon details and the Ibor index. The payment currency is the index currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period of the first index.
   * @param accrualEndDate End date of the accrual period of the first index.
   * @param paymentAccrualFactor Accrual factor of the accrual period of the first index.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index1 The first coupon Ibor index.
   * @param index2 The second coupon Ibor index.
   * @param weight1 The weight of the first index.
   * @param weight2 The weight of the second index.
   * @param iborCalendar1 The calendar associated to the first index.
   * @param iborCalendar2 The calendar associated to the second index.
   * @return The Ibor coupon.
   */
  public static CouponIborAverageIndexDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final ZonedDateTime fixingDate, final IborIndex index1, final IborIndex index2, final double weight1,
      final double weight2, final Calendar iborCalendar1, final Calendar iborCalendar2) {
    ArgumentChecker.notNull(index1, "index1");
    ArgumentChecker.notNull(index2, "index2");
    ArgumentChecker.isTrue(index1.getCurrency().equals(index2.getCurrency()), "index1 currency different from index2 currency");
    return new CouponIborAverageIndexDefinition(index1.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, fixingDate, index1,
        index2, weight1, weight2, iborCalendar1, iborCalendar2);
  }

  /**
   * Builder of Ibor-like coupon from an underlying coupon, the fixing date, the weights and the indeces. The fixing period dates are deduced from the index and the fixing date.
   * @param coupon Underlying coupon.
   * @param fixingDate The coupon fixing date.
   * @param index1 The first coupon Ibor index.
   * @param index2 The second coupon Ibor index.
   * @param weight1 The weight of the first index.
   * @param weight2 The weight of the second index.
   * @param iborCalendar1 The calendar associated to the first index.
   * @param iborCalendar2 The calendar associated to the second index.
   * @return The Ibor coupon.
   */
  public static CouponIborAverageIndexDefinition from(final CouponDefinition coupon, final ZonedDateTime fixingDate, final IborIndex index1, final IborIndex index2,
      final double weight1, final double weight2, final Calendar iborCalendar1, final Calendar iborCalendar2) {
    ArgumentChecker.notNull(coupon, "coupon");
    ArgumentChecker.notNull(fixingDate, "fixing date");
    ArgumentChecker.notNull(index1, "index1");
    ArgumentChecker.notNull(index2, "index1");
    ArgumentChecker.isTrue(index1.getCurrency().equals(index2.getCurrency()), "index1 currency different from index2 currency");
    return new CouponIborAverageIndexDefinition(index1.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(), coupon.getAccrualEndDate(),
        coupon.getPaymentYearFraction(), coupon.getNotional(), fixingDate, index1, index2, weight1, weight2, iborCalendar1, iborCalendar2);
  }

  /**
   * Gets the first Ibor index of the instrument.
   * @return The first index.
   */
  public IborIndex getIndex1() {
    return _index1;
  }

  /**
   * Gets the second Ibor index of the instrument.
   * @return The second index.
   */
  public IborIndex getIndex2() {
    return _index2;
  }

  /**
   * Gets the weight of the first index.
   * @return The first weight.
   */
  public double getWeight1() {
    return _weight1;
  }

  /**
   * Gets the weight of the second index.
   * @return The second weight.
   */
  public double getWeight2() {
    return _weight2;
  }

  /**
   * Gets the start date of the fixing period.
   * @return The start date of the fixing period.
   */
  public ZonedDateTime getFixingPeriodStartDate1() {
    return _fixingPeriodStartDate1;
  }

  /**
   * Gets the end date of the fixing period of the first index.
   * @return The end date of the fixing period of the first index.
   */
  public ZonedDateTime getFixingPeriodEndDate1() {
    return _fixingPeriodEndDate1;
  }

  /**
   * Gets the start date of the fixing period of the second index.
   * @return The start date of the fixing period of the second index.
   */
  public ZonedDateTime getFixingPeriodStartDate2() {
    return _fixingPeriodStartDate2;
  }

  /**
   * Gets the end date of the fixing period of the second index.
   * @return The end date of the fixing period of the second index.
   */
  public ZonedDateTime getFixingPeriodEndDate2() {
    return _fixingPeriodEndDate2;
  }

  /**
   * Gets the accrual factor (or year fraction) associated to the fixing period of the first index in the Index day count convention.
   * @return The accrual factor of the first index.
   */
  public double getFixingPeriodAccrualFactor1() {
    return _fixingPeriodAccrualFactor1;
  }

  /**
   * Gets the accrual factor (or year fraction) associated to the fixing period  of the second index in the Index day count convention.
   * @return The accrual factor of the second index.
   */
  public double getFixingPeriodAccrualFactor2() {
    return _fixingPeriodAccrualFactor2;
  }

  /**
   * Creates a new coupon with all the details the same except the notional which is replaced by the notional provided.
   * @param notional The notional.
   * @return The coupon.
   */
  public CouponIborAverageIndexDefinition withNotional(final double notional) {
    return new CouponIborAverageIndexDefinition(getCurrency(), getPaymentDate(), getAccrualStartDate(), getAccrualEndDate(), getPaymentYearFraction(), notional,
        getFixingDate(), _fixingPeriodStartDate1, _fixingPeriodEndDate1, _fixingPeriodAccrualFactor1, _fixingPeriodStartDate2, _fixingPeriodEndDate2,
        _fixingPeriodAccrualFactor2, _index1, _index2, _weight1, _weight2);
  }

  /**
   * {@inheritDoc}
   * If the fixing date is strictly before the conversion date and the fixing rate is not available, an exception is thrown; if the fixing rate is available a fixed coupon is returned.
   * If the fixing date is equal to the conversion date, if the fixing rate is available a fixed coupon is returned, if not a coupon Ibor with spread is returned.
   * If the fixing date is strictly after the conversion date, a coupon Ibor is returned.
   * All the comparisons are between dates without time.
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dayConversion.isAfter(getFixingDate().toLocalDate()), "Do not have any fixing data but are asking for a derivative at " + dateTime
        + " which is after fixing date " + getFixingDate());
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime1 = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate1());
    final double fixingPeriodEndTime1 = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate1());
    final double fixingPeriodStartTime2 = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate2());
    final double fixingPeriodEndTime2 = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate2());
    return new CouponIborAverage(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, getIndex1(), fixingPeriodStartTime1,
        fixingPeriodEndTime1, getFixingPeriodAccrualFactor1(), getIndex2(), fixingPeriodStartTime2, fixingPeriodEndTime2, getFixingPeriodAccrualFactor2(), getWeight1(),
        getWeight2());
  }

  /**
   * {@inheritDoc}
   * If the fixing date is strictly before the conversion date and the fixing rate is not available, an exception is thrown; if the fixing rate is available a fixed coupon is returned.
   * If the fixing date is equal to the conversion date, if the fixing rate is available a fixed coupon is returned, if not a coupon Ibor with spread is returned.
   * If the fixing date is strictly after the conversion date, a coupon Ibor is returned.
   * All the comparisons are between dates without time.
   */
  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(dateTime, "date");
    final LocalDate dayConversion = dateTime.toLocalDate();
    ArgumentChecker.notNull(indexFixingTimeSeries, "Index fixing time series");
    ArgumentChecker.isTrue(!dayConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final LocalDate dayFixing = getFixingDate().toLocalDate();
    if (dayConversion.equals(dayFixing)) { // The fixing is on the reference date; if known the fixing is used and if not, the floating coupon is created.
      final Double fixedRate = indexFixingTimeSeries.getValue(getFixingDate());
      if (fixedRate != null) {
        return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixedRate);
      }
    }
    if (dayConversion.isAfter(dayFixing)) { // The fixing is required
      final ZonedDateTime rezonedFixingDate = ZonedDateTime.of(LocalDateTime.of(getFixingDate().toLocalDate(), LocalTime.of(0, 0)), ZoneOffset.UTC);
      final Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDate); // TODO: remove time from fixing date.
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + getFixingDate());
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixedRate);
    }
    final double fixingTime = TimeCalculator.getTimeBetween(dateTime, getFixingDate());
    final double fixingPeriodStartTime1 = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate1());
    final double fixingPeriodEndTime1 = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate1());
    final double fixingPeriodStartTime2 = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodStartDate2());
    final double fixingPeriodEndTime2 = TimeCalculator.getTimeBetween(dateTime, getFixingPeriodEndDate2());
    return new CouponIborAverage(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, getIndex1(), fixingPeriodStartTime1,
        fixingPeriodEndTime1, getFixingPeriodAccrualFactor1(), getIndex2(), fixingPeriodStartTime2, fixingPeriodEndTime2, getFixingPeriodAccrualFactor2(), getWeight1(),
        getWeight2());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborAverageDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + ((_fixingPeriodEndDate1 == null) ? 0 : _fixingPeriodEndDate1.hashCode());
    result = prime * result + ((_fixingPeriodEndDate2 == null) ? 0 : _fixingPeriodEndDate2.hashCode());
    result = prime * result + ((_fixingPeriodStartDate1 == null) ? 0 : _fixingPeriodStartDate1.hashCode());
    result = prime * result + ((_fixingPeriodStartDate2 == null) ? 0 : _fixingPeriodStartDate2.hashCode());
    result = prime * result + ((_index1 == null) ? 0 : _index1.hashCode());
    result = prime * result + ((_index2 == null) ? 0 : _index2.hashCode());
    temp = Double.doubleToLongBits(_weight1);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_weight2);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final CouponIborAverageIndexDefinition other = (CouponIborAverageIndexDefinition) obj;
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor1) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor1)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingPeriodAccrualFactor2) != Double.doubleToLongBits(other._fixingPeriodAccrualFactor2)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodEndDate1, other._fixingPeriodEndDate1)) {
      return false;
    }

    if (!ObjectUtils.equals(_fixingPeriodEndDate2, other._fixingPeriodEndDate2)) {
      return false;
    }

    if (!ObjectUtils.equals(_fixingPeriodStartDate1, other._fixingPeriodStartDate1)) {
      return false;
    }
    if (!ObjectUtils.equals(_fixingPeriodStartDate2, other._fixingPeriodStartDate2)) {
      return false;
    }
    if (!ObjectUtils.equals(_index1, other._index1)) {
      return false;
    }
    if (!ObjectUtils.equals(_index2, other._index2)) {
      return false;
    }
    if (Double.doubleToLongBits(_weight1) != Double.doubleToLongBits(other._weight1)) {
      return false;
    }
    if (Double.doubleToLongBits(_weight2) != Double.doubleToLongBits(other._weight2)) {
      return false;
    }
    return true;
  }

}
