/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.inflation;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.payment.CapFloor;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearMonthly;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *  Class describing a Year on Year inflation caplet (or floorlet) .
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is paymentYearFraction* /[(Index_End / Index_Start - 1)-strike/]^{+} t.
 */
public class CapFloorInflationYearOnYearMonthlyDefinition extends CouponInflationDefinition implements CapFloor {

  /**
   * The fixing date (always the first of a month) of the last known fixing.
   */
  private final ZonedDateTime _lastKnownFixingDate;
  /**
   * The reference date for the index at the coupon start. Two months are required for the interpolation.
   *  May not be relevant as the index value is known.
   */
  private final ZonedDateTime _referenceStartDate;
  /**
  * The reference dates for the index at the coupon end. Two months are required for the interpolation.
  * There is usually a difference of two or three month between the reference date and the payment date.
  */
  private final ZonedDateTime _referenceEndDate;

  /**
   * The lag in month between the index validity and the coupon dates for the standard product (the one in exchange market and used for the calibration, this lag is in most cases 3 month).
   */
  private final int _conventionalMonthLag;

  /**
   * The lag in month between the index validity and the coupon dates for the actual product. (In most of the cases,lags are standard so _conventionalMonthLag=_monthLag)
   */
  private final int _monthLag;

  /**
   * The cap/floor strike.
   */
  private final double _strike;
  /**
   * The cap (true) / floor (false) flag.
   */
  private final boolean _isCap;

  /**
   * Constructor from all the cap/floor details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period; used for the payment.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param referenceStartDate The reference date for the index at the coupon start.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   */
  public CapFloorInflationYearOnYearMonthlyDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex, final ZonedDateTime lastKnownFixingDate,
      final int conventionalMonthLag, final int monthLag, final ZonedDateTime referenceStartDate, final ZonedDateTime referenceEndDate, final double strike, final boolean isCap) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, priceIndex);
    ArgumentChecker.notNull(lastKnownFixingDate, "Last known fixing date");
    ArgumentChecker.notNull(referenceStartDate, "Reference start date");
    ArgumentChecker.notNull(referenceEndDate, "Reference end date");
    _lastKnownFixingDate = lastKnownFixingDate;
    _referenceStartDate = referenceStartDate;
    _referenceEndDate = referenceEndDate;
    _conventionalMonthLag = conventionalMonthLag;
    _monthLag = monthLag;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Builder for inflation Year on Yearn based on an inflation lag and index publication. The fixing date is the publication lag after the last reference month. The month lag is the conventional one.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The inflation zero-coupon cap/floor.
   */
  public static CapFloorInflationYearOnYearMonthlyDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int conventionalMonthLag, final ZonedDateTime lastKnownFixingDate, final double strike, final boolean isCap) {
    ZonedDateTime referenceStartDate = accrualStartDate.minusMonths(conventionalMonthLag);
    ZonedDateTime referenceEndDate = paymentDate.minusMonths(conventionalMonthLag);
    referenceStartDate = referenceStartDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    referenceEndDate = referenceEndDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

    return new CapFloorInflationYearOnYearMonthlyDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, lastKnownFixingDate, conventionalMonthLag,
        conventionalMonthLag, referenceStartDate, referenceEndDate, strike, isCap);
  }

  /**
   * Builder for inflation Year on Yearn based on an inflation lag and index publication. The fixing date is the publication lag after the last reference month.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product..
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The inflation zero-coupon cap/floor.
   */
  public static CapFloorInflationYearOnYearMonthlyDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int conventionalMonthLag, final int monthLag, final ZonedDateTime lastKnownFixingDate, final double strike, final boolean isCap) {
    final ZonedDateTime referenceStartDate = accrualStartDate.minusMonths(monthLag).with(TemporalAdjusters.lastDayOfMonth());
    final ZonedDateTime referenceEndDate = paymentDate.minusMonths(monthLag).with(TemporalAdjusters.lastDayOfMonth());

    return new CapFloorInflationYearOnYearMonthlyDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, lastKnownFixingDate, conventionalMonthLag,
        monthLag, referenceStartDate, referenceEndDate, strike, isCap);
  }

  /**
   * Builder from a zero-coupon inflation interpolation coupon the cap/floor strike and isCap flag.
   * @param coupon The underlying inflation coupon.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor
   */
  public static CapFloorInflationYearOnYearMonthlyDefinition from(final CouponInflationYearOnYearMonthlyDefinition coupon, final ZonedDateTime lastKnownFixingDate,
      final double strike, final boolean isCap) {
    Validate.notNull(coupon, "coupon year on year monthly Inflation");
    return new CapFloorInflationYearOnYearMonthlyDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(),
        coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(), coupon.getPriceIndex(), lastKnownFixingDate,
        coupon.getConventionalMonthLag(), coupon.getMonthLag(), coupon.getReferenceStartDate(), coupon.getReferenceEndDate(), strike, isCap);
  }

  /**
   * Gets the fixing date (always the first of a month) of the last known fixing..
   * @return the last known fixing date.
   */
  public ZonedDateTime getLastKnownFixingDate() {
    return _lastKnownFixingDate;
  }

  public int getConventionalMonthLag() {
    return _conventionalMonthLag;
  }

  /**
   * Gets the lag in month between the index validity and the coupon dates for the actual product.
   * @return The lag.
   */
  public int getMonthLag() {
    return _monthLag;
  }

  /**
   * Gets the reference date for the index at the coupon start.
   * @return The reference date for the index at the coupon start.
   */
  public ZonedDateTime getReferenceStartDate() {
    return _referenceStartDate;
  }

  /**
   * Gets the reference date for the index at the coupon end.
   * @return The reference date for the index at the coupon end.
   */
  public ZonedDateTime getReferenceEndDate() {
    return _referenceEndDate;
  }

  /**
   * Gets the cap/floor strike in years.
   * @return The strike.
   */
  @Override
  public double getStrike() {
    return _strike;
  }

  /**
   * Gets The cap (true) / floor (false) flag.
   * @return The flag.
   */
  @Override
  public boolean isCap() {
    return _isCap;
  }

  @Override
  public double payOff(final double fixing) {
    final double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - _strike), 0);
  }

  @Override
  public CouponInflationDefinition with(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional) {
    return null;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final String... yieldCurveNames) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CapFloorInflationYearOnYearMonthly toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double lastKnownFixingTime = TimeCalculator.getTimeBetween(date, _lastKnownFixingDate);
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double referenceStartTime = TimeCalculator.getTimeBetween(date, _referenceStartDate);
    final double referenceEndTime = TimeCalculator.getTimeBetween(date, _referenceEndDate);
    final ZonedDateTime naturalPaymentEndDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentEndTime = TimeCalculator.getTimeBetween(date, naturalPaymentEndDate);
    final ZonedDateTime naturalPaymentstartDate = naturalPaymentEndDate.minusMonths(12);
    final double naturalPaymentStartTime = TimeCalculator.getTimeBetween(date, naturalPaymentstartDate);
    return new CapFloorInflationYearOnYearMonthly(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), lastKnownFixingTime,
        referenceStartTime, naturalPaymentStartTime, referenceEndTime, naturalPaymentEndTime, _strike, _isCap);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final LocalDate dayConversion = date.toLocalDate();
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final LocalDate dayFixing = getReferenceEndDate().toLocalDate();
    if (dayConversion.isAfter(dayFixing)) {
      final Double fixedEndIndex = priceIndexTimeSeries.getValue(_referenceEndDate);

      if (fixedEndIndex != null) {
        final Double fixedStartIndex = priceIndexTimeSeries.getValue(_referenceStartDate);
        final Double fixedRate = (fixedEndIndex / fixedStartIndex - 1.0);
        return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), payOff(fixedRate));
      }
    }
    final double lastKnownFixingTime = TimeCalculator.getTimeBetween(date, _lastKnownFixingDate);
    final double referenceStartTime = TimeCalculator.getTimeBetween(date, _referenceStartDate);
    final double referenceEndTime = TimeCalculator.getTimeBetween(date, _referenceEndDate);
    final ZonedDateTime naturalPaymentEndDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentEndTime = TimeCalculator.getTimeBetween(date, naturalPaymentEndDate);
    final ZonedDateTime naturalPaymentstartDate = naturalPaymentEndDate.minusMonths(12);
    final double naturalPaymentStartTime = TimeCalculator.getTimeBetween(date, naturalPaymentstartDate);
    return new CapFloorInflationYearOnYearMonthly(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), lastKnownFixingTime,
        referenceStartTime, naturalPaymentStartTime, referenceEndTime, naturalPaymentEndTime, _strike, _isCap);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorInflationYearOnYearMonthlyDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorInflationYearOnYearMonthlyDefinition(this);
  }

  @Override
  public String toString() {
    return "CapFloorInflationYearOnYearMonthlyDefinition [_strike=" + _strike + ", _isCap=" + _isCap + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _conventionalMonthLag;
    result = prime * result + (_isCap ? 1231 : 1237);
    result = prime * result + ((_lastKnownFixingDate == null) ? 0 : _lastKnownFixingDate.hashCode());
    result = prime * result + _monthLag;
    result = prime * result + ((_referenceEndDate == null) ? 0 : _referenceEndDate.hashCode());
    result = prime * result + ((_referenceStartDate == null) ? 0 : _referenceStartDate.hashCode());
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
    final CapFloorInflationYearOnYearMonthlyDefinition other = (CapFloorInflationYearOnYearMonthlyDefinition) obj;
    if (_conventionalMonthLag != other._conventionalMonthLag) {
      return false;
    }
    if (_isCap != other._isCap) {
      return false;
    }
    if (_lastKnownFixingDate == null) {
      if (other._lastKnownFixingDate != null) {
        return false;
      }
    } else if (!_lastKnownFixingDate.equals(other._lastKnownFixingDate)) {
      return false;
    }
    if (_monthLag != other._monthLag) {
      return false;
    }
    if (_referenceEndDate == null) {
      if (other._referenceEndDate != null) {
        return false;
      }
    } else if (!_referenceEndDate.equals(other._referenceEndDate)) {
      return false;
    }
    if (_referenceStartDate == null) {
      if (other._referenceStartDate != null) {
        return false;
      }
    } else if (!_referenceStartDate.equals(other._referenceStartDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    return true;
  }

}
