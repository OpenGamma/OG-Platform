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

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.payment.CapFloor;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationZeroCouponMonthly;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an zero-coupon inflation caplet/floorlet were the inflation figure are the one of the reference month and are not interpolated.
 * The notional is positive for long the option and negative for short the option.
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is [(Index_End / Index_Start - 1)-((1+strike)^T-1)]^{+}
 */
public class CapFloorInflationZeroCouponMonthlyDefinition extends CouponInflationDefinition implements CapFloor {

  /**
   * The reference date for the index at the coupon start. May not be relevant as the index value is known.
   */
  private final ZonedDateTime _referenceStartDate;
  /**
   * The fixing date (always the first of a month) of the last known fixing.
   */
  private final ZonedDateTime _lastKnownFixingDate;
  /**
   * The reference dates for the index at the coupon end. Two months are required for the interpolation.
   * There is usually a difference of two or three month between the reference date and the payment date.
   */
  private final ZonedDateTime _referenceEndDate;

  /**
   * The lag in month between the index validity and the coupon dates for the standard product. (the one in exchange market and used for the calibration, this lag is in most cases 3 month).
   */
  private final int _conventionalMonthLag;

  /**
   * The lag in month between the index validity and the coupon dates for the actual product. (In most of the cases,lags are standard so _conventionalMonthLag=_monthLag)
   */
  private final int _monthLag;

  /**
   * The cap/floor maturity in years.
   */

  private final int _maturity;

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
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates  for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates  for the actual product.
   * @param maturity The cap/floor maturity in years.
   * @param referenceStartDate The reference date for the index at the coupon start.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   */
  public CapFloorInflationZeroCouponMonthlyDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex, final ZonedDateTime lastKnownFixingDate,
      final int conventionalMonthLag, final int monthLag, final int maturity, final ZonedDateTime referenceStartDate,
      final ZonedDateTime referenceEndDate, final double strike, final boolean isCap) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, priceIndex);
    ArgumentChecker.notNull(lastKnownFixingDate, "Last known fixing date");
    ArgumentChecker.notNull(lastKnownFixingDate, "Last known fixing date");
    ArgumentChecker.notNull(referenceStartDate, "Reference start date");
    ArgumentChecker.notNull(referenceEndDate, "Reference end date");
    _lastKnownFixingDate = lastKnownFixingDate;
    _referenceStartDate = referenceStartDate;
    _referenceEndDate = referenceEndDate;
    _conventionalMonthLag = conventionalMonthLag;
    _monthLag = monthLag;
    _maturity = maturity;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Builder from all the cap/floor details, using details inside the price index.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate Coupon payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates.
   * @param monthLag  The lag in month between the index validity and the coupon dates.
   * @param maturity The cap/floor maturity in years.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param referenceStartDate TODO
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor.
   */
  public static CapFloorInflationZeroCouponMonthlyDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int conventionalMonthLag, final int monthLag, final int maturity, final ZonedDateTime lastKnownFixingDate, final ZonedDateTime referenceStartDate,
      final ZonedDateTime referenceEndDate, final double strike, final boolean isCap) {
    Validate.notNull(priceIndex, "Price index");
    return new CapFloorInflationZeroCouponMonthlyDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0,
        notional, priceIndex, lastKnownFixingDate, conventionalMonthLag, monthLag, maturity, referenceStartDate, referenceEndDate, strike, isCap);
  }

  /**
   * Builder from all the cap/floor details, using details inside the price index with standard reference end date.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate Coupon payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates.
   * @param monthLag  The lag in month between the index validity and the coupon dates.
   * @param maturity The cap/floor maturity in years.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor.
   */
  public static CapFloorInflationZeroCouponMonthlyDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int conventionalMonthLag, final int monthLag, final int maturity, final ZonedDateTime lastKnownFixingDate, final double strike,
      final boolean isCap) {
    Validate.notNull(priceIndex, "Price index");
    final ZonedDateTime referenceEndDate = paymentDate.minusMonths(conventionalMonthLag).with(TemporalAdjusters.lastDayOfMonth());
    final ZonedDateTime referenceStartDate = accrualStartDate.minusMonths(conventionalMonthLag).with(TemporalAdjusters.lastDayOfMonth());
    return new CapFloorInflationZeroCouponMonthlyDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0,
        notional, priceIndex, lastKnownFixingDate, conventionalMonthLag, monthLag, maturity, referenceStartDate, referenceEndDate, strike, isCap);
  }

  /**
   * Builder from a zero-coupon inflation interpolation coupon the cap/floor strike and isCap flag.
   * @param couponInflation The underlying inflation coupon.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param maturity The cap/floor maturity in years.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor
   */
  public static CapFloorInflationZeroCouponMonthlyDefinition from(final CouponInflationZeroCouponMonthlyDefinition couponInflation, final ZonedDateTime lastKnownFixingDate, final int maturity,
      final double strike, final boolean isCap) {
    Validate.notNull(couponInflation, "coupon Ibor");
    return new CapFloorInflationZeroCouponMonthlyDefinition(couponInflation.getCurrency(), couponInflation.getPaymentDate(), couponInflation.getAccrualStartDate(),
        couponInflation.getAccrualEndDate(), couponInflation.getPaymentYearFraction(), couponInflation.getNotional(), couponInflation.getPriceIndex(),
        lastKnownFixingDate, couponInflation.getMonthLag(), couponInflation.getMonthLag(), maturity, couponInflation.getReferenceStartDate(),
        couponInflation.getReferenceEndDate(), strike, isCap);
  }

  /**
   * Gets the fixing date (always the first of a month) of the last known fixing..
   * @return the last known fixing date.
   */
  public ZonedDateTime getlastKnownFixingDate() {
    return _lastKnownFixingDate;
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
   * Gets the lag in month between the index validity and the coupon dates for the standard product.
   * @return The lag.
   */
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
   * Gets the cap/floor maturity in years..
   * @return The maturity.
   */
  public int getMaturity() {
    return _maturity;
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
  public CouponInflationDefinition with(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double payOff(final double fixing) {
    final double omega = (_isCap) ? 1.0 : -1.0;
    return Math.max(omega * (fixing - Math.pow(1 + _strike, _maturity)), 0);
  }

  @Override
  public CapFloorInflationZeroCouponMonthly toDerivative(final ZonedDateTime date) {
    throw new OpenGammaRuntimeException("a time serie is needed");
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(priceIndexTimeSeries, "price index time series");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final Double fixedStartIndex = priceIndexTimeSeries.getValue(getReferenceStartDate());
    ArgumentChecker.notNull(fixedStartIndex, "first fixing not in the price index time series");
    final LocalDate dayConversion = date.toLocalDate();
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final LocalDate dayFixing = getReferenceEndDate().toLocalDate();
    if (dayConversion.isAfter(dayFixing)) {
      final Double fixedEndIndex = priceIndexTimeSeries.getValue(getReferenceEndDate());

      if (fixedEndIndex != null) {
        final Double fixedRate = (fixedEndIndex / fixedStartIndex - 1.0);
        return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), payOff(fixedRate));
      }
    }

    final double referenceEndTime = TimeCalculator.getTimeBetween(date, _referenceEndDate);
    final double lastKnownFixingTime = TimeCalculator.getTimeBetween(date, getlastKnownFixingDate());
    final ZonedDateTime naturalPaymentDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentEndTime = TimeCalculator.getTimeBetween(date, naturalPaymentDate);
    return new CapFloorInflationZeroCouponMonthly(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), lastKnownFixingTime, fixedStartIndex, referenceEndTime,
        naturalPaymentEndTime, _maturity, _strike, _isCap);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorInflationZeroCouponMonthlyDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorInflationZeroCouponMonthlyDefinition(this);
  }

  @Override
  public String toString() {
    return super.toString() + ", IsCap = " + _isCap + ", Strike = " + _strike;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _conventionalMonthLag;
    result = prime * result + (_isCap ? 1231 : 1237);
    result = prime * result + ((_lastKnownFixingDate == null) ? 0 : _lastKnownFixingDate.hashCode());
    result = prime * result + _maturity;
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
    final CapFloorInflationZeroCouponMonthlyDefinition other = (CapFloorInflationZeroCouponMonthlyDefinition) obj;
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
    if (_maturity != other._maturity) {
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
