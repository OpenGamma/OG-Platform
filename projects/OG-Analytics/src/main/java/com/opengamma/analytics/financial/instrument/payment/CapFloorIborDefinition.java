/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import org.threeten.bp.LocalTime;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CapFloorIbor;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a caplet/floorlet on Ibor. The notional is positive for long the option and negative for short the option.
 */
public class CapFloorIborDefinition extends CouponFloatingDefinition implements CapFloor {

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
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;
  /**
   * The calendar of the ibor leg.
   */
  private final Calendar _calendar;

  /**
   * Constructor from all the cap/floor details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period; used for the payment.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index. The index currency should be the same as the payment currency.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @param calendar The holiday calendar for the ibor leg.
   */
  public CapFloorIborDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index, final double strike, final boolean isCap, final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate);
    ArgumentChecker.notNull(index, "index");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "index currency different from payment currency");
    _index = index;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(fixingDate, _index.getSpotLag(), calendar);
    _fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(_fixingPeriodStartDate, index.getTenor(), index.getBusinessDayConvention(), calendar, index.isEndOfMonth());
    _fixingPeriodAccrualFactor = index.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate, calendar);
    _strike = strike;
    _isCap = isCap;
    _calendar = calendar;
  }

  /**
   * Builder from all the cap/floor details.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param accrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The cap/floor.
   */
  public static CapFloorIborDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double accrualFactor,
      final double notional, final ZonedDateTime fixingDate, final IborIndex index, final double strike, final boolean isCap, final Calendar calendar) {
    ArgumentChecker.notNull(index, "index");
    return new CapFloorIborDefinition(index.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, accrualFactor, notional, fixingDate, index, strike, isCap, calendar);
  }

  /**
   * Builder from a Ibor coupon the cap/floor strike and isCap flag.
   * @param couponIbor The underlying Ibor coupon.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor
   */
  public static CapFloorIborDefinition from(final CouponIborDefinition couponIbor, final double strike, final boolean isCap) {
    ArgumentChecker.notNull(couponIbor, "coupon Ibor");
    return new CapFloorIborDefinition(couponIbor.getCurrency(), couponIbor.getPaymentDate(), couponIbor.getAccrualStartDate(), couponIbor.getAccrualEndDate(), couponIbor.getPaymentYearFraction(),
        couponIbor.getNotional(), couponIbor.getFixingDate(), couponIbor.getIndex(), strike, isCap, couponIbor.getCalendar());
  }

  /**
   * Builder from notional, fixing date, index, strike and cap flag.
   * @param notional Coupon notional.
   * @param fixingDate The coupon fixing date.
   * @param index The coupon Ibor index.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The cap/floor
   */
  public static CapFloorIborDefinition from(final double notional, final ZonedDateTime fixingDate, final IborIndex index, final double strike, final boolean isCap, final Calendar calendar) {
    return from(CouponIborDefinition.from(notional, fixingDate, index, calendar), strike, isCap);
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
   * {@inheritDoc}
   */
  @Override
  public double getStrike() {
    return _strike;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isCap() {
    return _isCap;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double payOff(final double fixing) {
    final double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getFixingDate()), "Do not have any fixing data but are asking for a derivative after the fixing date " + getFixingDate() + " " + date);
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date " + date + " " + getPaymentDate());
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    // Ibor is not fixed yet, all the details are required.
    final double fixingTime = TimeCalculator.getTimeBetween(date, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(date, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(date, getFixingPeriodEndDate());
    //TODO: Definition has no spread and time version has one: to be standardized.
    return new CapFloorIbor(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), _strike, _isCap);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTS) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(indexFixingTS, "index fixing time series");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date " + date + " " + getPaymentDate());
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    if (date.isAfter(getFixingDate()) || date.equals(getFixingDate())) { // The Ibor cap/floor has already fixed, it is now a fixed coupon.
      Double fixedRate = indexFixingTS.getValue(getFixingDate());
      //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
      if (fixedRate == null) {
        final ZonedDateTime fixingDateAtLiborFixingTime = getFixingDate().with(LocalTime.of(11, 0));
        fixedRate = indexFixingTS.getValue(fixingDateAtLiborFixingTime);
      }
      if (fixedRate == null) {
        final ZonedDateTime previousBusinessDay = BusinessDayConventions.PRECEDING.adjustDate(_calendar, getFixingDate().minusDays(1));
        fixedRate = indexFixingTS.getValue(previousBusinessDay);
        //TODO remove me when times are sorted out in the swap definitions or we work out how to deal with this another way
        if (fixedRate == null) {
          final ZonedDateTime previousBusinessDayAtLiborFixingTime = previousBusinessDay.with(LocalTime.of(11, 0));
          fixedRate = indexFixingTS.getValue(previousBusinessDayAtLiborFixingTime);
        }
        if (fixedRate == null) {
          fixedRate = indexFixingTS.getLatestValue(); //TODO remove me as soon as possible
        }
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), payOff(fixedRate));
    }
    // Ibor is not fixed yet, all the details are required.
    final double fixingTime = TimeCalculator.getTimeBetween(date, getFixingDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(date, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(date, getFixingPeriodEndDate());
    //TODO: Definition has no spread and time version has one: to be standardized.
    return new CapFloorIbor(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixingTime, getIndex(), fixingPeriodStartTime, fixingPeriodEndTime,
        getFixingPeriodAccrualFactor(), _strike, _isCap);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorIborDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorIborDefinition(this);
  }

  @Override
  public String toString() {
    return super.toString() + ", IsCap = " + _isCap + ", Strike = " + _strike;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + (_isCap ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_strike);
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
    final CapFloorIborDefinition other = (CapFloorIborDefinition) obj;
    if (_isCap != other._isCap) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    return true;
  }

}
