/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.inflation;

import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.instrument.payment.CapFloor;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CapFloorInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Year on Year inflation caplet (or floorlet) were the inflation figure are interpolated.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is paymentYearFraction* /[(Index_End / Index_Start - 1)-strike/]^{+} t.
 */
public class CapFloorInflationYearOnYearInterpolationDefinition extends CouponInflationDefinition implements CapFloor {

  /**
   * The fixing date (always the first of a month) of the last known fixing.
   */
  private final ZonedDateTime _lastKnownFixingDate;
  /**
   * The reference date for the index at the coupon start. Two months are required for the interpolation.
   *  May not be relevant as the index value is known.
   */
  private final ZonedDateTime[] _referenceStartDate;
  /**
  * The reference dates for the index at the coupon end. Two months are required for the interpolation.
  * There is usually a difference of two or three month between the reference date and the payment date.
  */
  private final ZonedDateTime[] _referenceEndDate;
  /**
   * The weight on the first month index in the interpolation of the index at the coupon start.
   */
  private final double _weightStart;
  /**
   * The weight on the first month index in the interpolation of the index at the coupon end.
   */
  private final double _weightEnd;
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
   * @param weightStart he weight on the first month index in the interpolation of the index at the _referenceStartDate.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the _referenceEndDate.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   */
  public CapFloorInflationYearOnYearInterpolationDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex, final ZonedDateTime lastKnownFixingDate,
      final int conventionalMonthLag, final int monthLag, final ZonedDateTime[] referenceStartDate, final ZonedDateTime[] referenceEndDate, final double weightStart,
      final double weightEnd, final double strike, final boolean isCap) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, priceIndex);
    ArgumentChecker.notNull(lastKnownFixingDate, "Last known fixing date");
    ArgumentChecker.notNull(referenceStartDate, "Reference start date");
    ArgumentChecker.notNull(referenceEndDate, "Reference end date");
    ArgumentChecker.isTrue(conventionalMonthLag <= monthLag, "the month lag should be superior (or equal)to the conventional month lag");
    _lastKnownFixingDate = lastKnownFixingDate;
    _conventionalMonthLag = conventionalMonthLag;
    _monthLag = monthLag;
    _referenceStartDate = referenceStartDate;
    _referenceEndDate = referenceEndDate;
    _weightStart = weightStart;
    _weightEnd = weightEnd;
    _strike = strike;
    _isCap = isCap;
  }

  /**
   * Builder from all the cap/floor details except weights which are calculated using the payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate Coupon payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param referenceStartDate The reference dates for the index at the coupon start.
   * @param referenceEndDate The reference dates for the index at the coupon end.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor.
   */
  public static CapFloorInflationYearOnYearInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final ZonedDateTime lastKnownFixingDate, final int conventionalMonthLag, final int monthLag, final ZonedDateTime[] referenceStartDate,
      final ZonedDateTime[] referenceEndDate, final double strike, final boolean isCap) {
    Validate.notNull(priceIndex, "Price index");
    final double weightStart;
    final double weightEnd;
    weightStart = 1.0 - (paymentDate.getDayOfMonth() - 1.0) / paymentDate.toLocalDate().lengthOfMonth();
    weightEnd = weightStart;
    return new CapFloorInflationYearOnYearInterpolationDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0,
        notional, priceIndex, lastKnownFixingDate, conventionalMonthLag, monthLag, referenceStartDate, referenceEndDate, weightStart,
        weightEnd, strike, isCap);
  }

  /**
   * Builder from all the cap/floor details except weights which are calculated using the payment date, and the month lag is the conventional month lag.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate Coupon payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param referenceStartDate The reference dates for the index at the coupon start.
   * @param referenceEndDate The reference dates for the index at the coupon end.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor.
   */
  public static CapFloorInflationYearOnYearInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final ZonedDateTime lastKnownFixingDate, final int conventionalMonthLag, final ZonedDateTime[] referenceStartDate, final ZonedDateTime[] referenceEndDate,
      final double strike, final boolean isCap) {
    Validate.notNull(priceIndex, "Price index");
    final double weightStart;
    final double weightEnd;
    weightStart = 1.0 - (paymentDate.getDayOfMonth() - 1.0) / paymentDate.toLocalDate().lengthOfMonth();
    weightEnd = weightStart;
    return new CapFloorInflationYearOnYearInterpolationDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0,
        notional, priceIndex, lastKnownFixingDate, conventionalMonthLag, conventionalMonthLag, referenceStartDate, referenceEndDate, weightStart,
        weightEnd, strike, isCap);
  }

  /**
   * Builder from a zero-coupon inflation interpolation coupon the cap/floor strike and isCap flag.
   * @param coupon The underlying inflation coupon.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The cap/floor
   */
  public static CapFloorInflationYearOnYearInterpolationDefinition from(final CouponInflationYearOnYearInterpolationDefinition coupon, final ZonedDateTime lastKnownFixingDate,
      final double strike, final boolean isCap) {
    Validate.notNull(coupon, "coupon year on year interpolation Inflation");
    return new CapFloorInflationYearOnYearInterpolationDefinition(coupon.getCurrency(), coupon.getPaymentDate(), coupon.getAccrualStartDate(),
        coupon.getAccrualEndDate(), coupon.getPaymentYearFraction(), coupon.getNotional(), coupon.getPriceIndex(), lastKnownFixingDate,
        coupon.getConventionalMonthLag(), coupon.getMonthLag(), coupon.getReferenceStartDates(), coupon.getReferenceEndDate(), coupon.getWeightStart(),
        coupon.getWeightEnd(), strike, isCap);
  }

  /**
   * Builder for inflation Year on Yearn based on an inflation lag and index publication. The fixing date is the publication lag after the last reference month.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param weightStart The weight on the first month index in the interpolation of the index at the _referenceStartDate.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the _referenceEndDate.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The inflation zero-coupon.
   */
  public static CapFloorInflationYearOnYearInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final ZonedDateTime lastKnownFixingDate, final int conventionalMonthLag, final int monthLag, final double weightStart, final double weightEnd,
      final double strike, final boolean isCap) {
    final ZonedDateTime[] referenceStartDate = new ZonedDateTime[2];
    final ZonedDateTime[] referenceEndDate = new ZonedDateTime[2];
    referenceStartDate[0] = accrualStartDate.minusMonths(monthLag).with(TemporalAdjusters.lastDayOfMonth());
    referenceStartDate[1] = referenceStartDate[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
    referenceEndDate[0] = paymentDate.minusMonths(monthLag).with(TemporalAdjusters.lastDayOfMonth());
    referenceEndDate[1] = referenceEndDate[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

    return new CapFloorInflationYearOnYearInterpolationDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex,
        lastKnownFixingDate, conventionalMonthLag, monthLag, referenceStartDate, referenceEndDate, weightStart, weightEnd, strike, isCap);
  }

  /**
   * Builder for inflation year on year coupon based on an inflation lag and the index publication lag. The fixing date is the publication lag after the last reference month.
   * The index start value is calculated from a time series. The index value required for the coupon should be in the time series.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalmonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param lastKnownFixingDate The fixing date (always the first of a month) of the last known fixing.
   * @param strike The strike
   * @param isCap The cap/floor flag.
   * @return The inflation zero-coupon.
   */
  public static CapFloorInflationYearOnYearInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int conventionalmonthLag, final int monthLag, final ZonedDateTime lastKnownFixingDate, final double strike, final boolean isCap) {
    final ZonedDateTime refInterpolatedDateStart = accrualStartDate;
    final ZonedDateTime refInterpolatedDateEnd = paymentDate;

    final double weightStart = 1.0 - (refInterpolatedDateStart.getDayOfMonth() - 1.0) / refInterpolatedDateStart.toLocalDate().lengthOfMonth();
    final double weightEnd = 1.0 - (refInterpolatedDateEnd.getDayOfMonth() - 1.0) / refInterpolatedDateEnd.toLocalDate().lengthOfMonth();

    return from(accrualStartDate, paymentDate, notional, priceIndex, lastKnownFixingDate, conventionalmonthLag, monthLag, weightStart, weightEnd, strike, isCap);
  }

  /**
   * Gets the fixing date (always the first of a month) of the last known fixing..
   * @return the last known fixing date.
   */
  public ZonedDateTime getLastKnownFixingDate() {
    return _lastKnownFixingDate;
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
   * Gets the reference dates for the index at the coupon start.
   * @return The reference dates for the index at the coupon start.
   */
  public ZonedDateTime[] getReferenceStartDate() {
    return _referenceStartDate;
  }

  /**
   * Gets the reference dates for the index at the coupon end.
   * @return The reference dates for the index at the coupon end.
   */
  public ZonedDateTime[] getReferenceEndDate() {
    return _referenceEndDate;
  }

  /**
   * Gets the weight on the first month index in the interpolation for the index at the coupon start.
   * @return The weightStart.
   */
  public double getWeightStart() {
    return _weightStart;
  }

  /**
   * Gets the weight on the first month index in the interpolation for the index at the coupon end.
   * @return The weightEnd.
   */
  public double getWeightEnd() {
    return _weightEnd;
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

  @Override
  public CapFloorInflationYearOnYearInterpolation toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double lastKnownFixingTime = TimeCalculator.getTimeBetween(date, _lastKnownFixingDate);
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] referenceStartTime = new double[2];
    referenceStartTime[0] = TimeCalculator.getTimeBetween(date, _referenceStartDate[0]);
    referenceStartTime[1] = TimeCalculator.getTimeBetween(date, _referenceStartDate[1]);
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, _referenceEndDate[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, _referenceEndDate[1]);
    final ZonedDateTime naturalPaymentEndDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentEndTime = TimeCalculator.getTimeBetween(date, naturalPaymentEndDate);
    final ZonedDateTime naturalPaymentstartDate = naturalPaymentEndDate.minusMonths(12);
    final double naturalPaymentStartTime = TimeCalculator.getTimeBetween(date, naturalPaymentstartDate);
    return new CapFloorInflationYearOnYearInterpolation(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), lastKnownFixingTime, referenceStartTime,
        naturalPaymentStartTime, referenceEndTime, naturalPaymentEndTime, _weightStart, _weightEnd, _strike, _isCap);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final LocalDate dayConversion = date.toLocalDate();
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final LocalDate dayFixing = getReferenceEndDate()[1].toLocalDate();
    if (dayConversion.isAfter(dayFixing)) {
      final Double fixedEndIndex1 = priceIndexTimeSeries.getValue(_referenceEndDate[1]);

      if (fixedEndIndex1 != null) {
        final Double fixedEndIndex0 = priceIndexTimeSeries.getValue(_referenceEndDate[0]);
        final Double fixedEndIndex = getWeightEnd() * fixedEndIndex0 + (1 - getWeightEnd()) * fixedEndIndex1;
        final Double fixedStartIndex1 = priceIndexTimeSeries.getValue(_referenceStartDate[1]);
        final Double fixedStartIndex0 = priceIndexTimeSeries.getValue(_referenceStartDate[0]);
        final Double fixedStartIndex = getWeightStart() * fixedStartIndex0 + (1 - getWeightStart()) * fixedStartIndex1;
        final Double fixedRate = (fixedEndIndex / fixedStartIndex - 1.0);
        return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), payOff(fixedRate));
      }
    }
    final double lastKnownFixingTime = TimeCalculator.getTimeBetween(date, _lastKnownFixingDate);
    final double[] referenceStartTime = new double[2];
    referenceStartTime[0] = TimeCalculator.getTimeBetween(date, _referenceStartDate[0]);
    referenceStartTime[1] = TimeCalculator.getTimeBetween(date, _referenceStartDate[1]);
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, _referenceEndDate[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, _referenceEndDate[1]);
    final ZonedDateTime naturalPaymentEndDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentEndTime = TimeCalculator.getTimeBetween(date, naturalPaymentEndDate);
    final ZonedDateTime naturalPaymentstartDate = naturalPaymentEndDate.minusMonths(12);
    final double naturalPaymentStartTime = TimeCalculator.getTimeBetween(date, naturalPaymentstartDate);
    return new CapFloorInflationYearOnYearInterpolation(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), lastKnownFixingTime, referenceStartTime,
        naturalPaymentStartTime, referenceEndTime, naturalPaymentEndTime, _weightStart, _weightEnd, _strike, _isCap);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorInflationYearOnYearInterpolationDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCapFloorInflationYearOnYearInterpolationDefinition(this);
  }

  @Override
  public String toString() {
    return "CapFloorInflationYearOnYearInterpolationDefinition [_strike=" + _strike + ", _isCap=" + _isCap + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _conventionalMonthLag;
    result = prime * result + (_isCap ? 1231 : 1237);
    result = prime * result + ((_lastKnownFixingDate == null) ? 0 : _lastKnownFixingDate.hashCode());
    result = prime * result + _monthLag;
    result = prime * result + Arrays.hashCode(_referenceEndDate);
    result = prime * result + Arrays.hashCode(_referenceStartDate);
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_weightEnd);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_weightStart);
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
    final CapFloorInflationYearOnYearInterpolationDefinition other = (CapFloorInflationYearOnYearInterpolationDefinition) obj;
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
    if (!Arrays.equals(_referenceEndDate, other._referenceEndDate)) {
      return false;
    }
    if (!Arrays.equals(_referenceStartDate, other._referenceStartDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_strike) != Double.doubleToLongBits(other._strike)) {
      return false;
    }
    if (Double.doubleToLongBits(_weightEnd) != Double.doubleToLongBits(other._weightEnd)) {
      return false;
    }
    if (Double.doubleToLongBits(_weightStart) != Double.doubleToLongBits(other._weightStart)) {
      return false;
    }
    return true;
  }

}
