/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.instrument.inflation;

import java.util.Arrays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.index.IndexPrice;
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationYearOnYearInterpolation;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Year on Year inflation coupon were the inflation figure are interpolated.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is paymentYearFraction*(Index_End / Index_Start - X) with X=0 for notional payment and X=1 for no notional payment.
 */

public class CouponInflationYearOnYearInterpolationDefinition extends CouponInflationDefinition {

  /**
   * The reference date for the index at the coupon start. May not be relevant as the index value is known.
   */
  private final ZonedDateTime[] _referenceStartDates;

  /**
   * The reference date for the index at the coupon end. The first of the month. There is usually a difference of two or three month between the reference date and the payment date.
   */
  private final ZonedDateTime[] _referenceEndDates;
  /**
   * The weight on the first month index in the interpolation of the index at the coupon start.
   */
  private final double _weightStart;
  /**
   * The weight on the first month index in the interpolation of the index at the coupon end.
   */
  private final double _weightEnd;
  /**
   * Flag indicating if the notional is paid (true) or not (false).
   */
  private final boolean _payNotional;
  /**
   * The lag in month between the index validity and the coupon dates for the standard product (the one in exchange market and used for the calibration, this lag is in most cases 3 month).
   */
  private final int _conventionalMonthLag;

  /**
   * The lag in month between the index validity and the coupon dates for the actual product. (In most of the cases,lags are standard so _conventionalMonthLag=_monthLag)
   */
  private final int _monthLag;

  /**
   * Constructor for Year on Year inflation coupon.
   * @param currency The coupon currency.
   * @param paymentDate The payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param referenceStartDates The reference date for the index at the coupon start.
   * @param referenceEndDates The reference date for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param weightStart The weight on the first month index in the interpolation of the index at the coupon start.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the coupon end.
   */
  public CouponInflationYearOnYearInterpolationDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex, final int conventionalMonthLag,
      final int monthLag, final ZonedDateTime[] referenceStartDates, final ZonedDateTime[] referenceEndDates, final boolean payNotional, final double weightStart, final double weightEnd) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, priceIndex);
    ArgumentChecker.notNull(referenceStartDates, "Reference start date");
    ArgumentChecker.notNull(referenceEndDates, "Reference end date");
    _conventionalMonthLag = conventionalMonthLag;
    _monthLag = monthLag;
    _referenceStartDates = referenceStartDates;
    _referenceEndDates = referenceEndDates;
    _weightStart = weightStart;
    _weightEnd = weightEnd;
    _payNotional = payNotional;

  }

  /**
   * Builder for inflation Year on Yearn based on an inflation lag and index publication. The fixing date is the publication lag after the last reference month. The month lag is the conventional one.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param weightStart The weight on the first month index in the interpolation of the index at the _referenceStartDate.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the _referenceEndDate.
   * @return The inflation zero-coupon.
   */
  public static CouponInflationYearOnYearInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int conventionalMonthLag, final boolean payNotional, final double weightStart, final double weightEnd) {
    final ZonedDateTime[] referenceStartDate = new ZonedDateTime[2];
    final ZonedDateTime[] referenceEndDate = new ZonedDateTime[2];
    referenceStartDate[0] = accrualStartDate.minusMonths(conventionalMonthLag).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceStartDate[1] = referenceStartDate[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceEndDate[0] = paymentDate.minusMonths(conventionalMonthLag).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceEndDate[1] = referenceEndDate[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);

    return new CouponInflationYearOnYearInterpolationDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex,
        conventionalMonthLag, conventionalMonthLag, referenceStartDate, referenceEndDate, payNotional, weightStart, weightEnd);
  }

  /**
   * Builder for inflation Year on Yearn based on an inflation lag and index publication. The fixing date is the publication lag after the last reference month.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param weightStart The weight on the first month index in the interpolation of the index at the _referenceStartDate.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the _referenceEndDate.
   * @return The inflation zero-coupon.
   */
  public static CouponInflationYearOnYearInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int conventionalMonthLag, final int monthLag, final boolean payNotional, final double weightStart, final double weightEnd) {
    final ZonedDateTime[] referenceStartDate = new ZonedDateTime[2];
    final ZonedDateTime[] referenceEndDate = new ZonedDateTime[2];
    referenceStartDate[0] = accrualStartDate.minusMonths(monthLag).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceStartDate[1] = referenceStartDate[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceEndDate[0] = paymentDate.minusMonths(monthLag).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceEndDate[1] = referenceEndDate[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);

    return new CouponInflationYearOnYearInterpolationDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex,
        conventionalMonthLag, monthLag, referenceStartDate, referenceEndDate, payNotional, weightStart, weightEnd);
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
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @return The inflation zero-coupon.
   */
  public static CouponInflationYearOnYearInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int conventionalmonthLag, final int monthLag, final boolean payNotional) {
    final ZonedDateTime refInterpolatedDateStart = accrualStartDate;
    final ZonedDateTime refInterpolatedDateEnd = paymentDate;

    final double weightStart = 1.0 - (refInterpolatedDateStart.getDayOfMonth() - 1.0) / refInterpolatedDateStart.toLocalDate().lengthOfMonth();
    final double weightEnd = 1.0 - (refInterpolatedDateEnd.getDayOfMonth() - 1.0) / refInterpolatedDateEnd.toLocalDate().lengthOfMonth();

    return from(accrualStartDate, paymentDate, notional, priceIndex, conventionalmonthLag, monthLag, payNotional, weightStart, weightEnd);
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
   * Gets the reference date for the index at the coupon start.
   * @return The reference date for the index at the coupon start.
   */
  public ZonedDateTime[] getReferenceStartDates() {
    return _referenceStartDates;
  }

  /**
   * Gets the reference date for the index at the coupon end.
   * @return The reference date for the index at the coupon end.
   */
  public ZonedDateTime[] getReferenceEndDate() {
    return _referenceEndDates;
  }

  /**
   * Gets the weight on the first month index in the interpolation for the index at the coupon start.
   * @return The weight.
   */
  public double getWeightStart() {
    return _weightStart;
  }

  /**
   * Gets the weight on the first month index in the interpolation for the index at the coupon end.
   * @return The weight.
   */
  public double getWeightEnd() {
    return _weightEnd;
  }

  /**
   * Gets the pay notional flag.
   * @return The flag.
   */
  public boolean payNotional() {
    return _payNotional;
  }

  @Override
  public CouponInflationDefinition with(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional) {
    return from(accrualStartDate, paymentDate, notional, getPriceIndex(), _conventionalMonthLag, _monthLag, _payNotional);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final String... yieldCurveNames) {
    return toDerivative(date, priceIndexTimeSeries);
  }

  @Override
  public CouponInflationYearOnYearInterpolation toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] referenceStartTime = new double[2];
    referenceStartTime[0] = TimeCalculator.getTimeBetween(date, getReferenceStartDates()[0]);
    referenceStartTime[1] = TimeCalculator.getTimeBetween(date, getReferenceStartDates()[1]);
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, getReferenceEndDate()[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, getReferenceEndDate()[1]);
    final ZonedDateTime naturalPaymentEndDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentEndTime = TimeCalculator.getTimeBetween(date, naturalPaymentEndDate);
    final ZonedDateTime naturalPaymentstartDate = naturalPaymentEndDate.minusMonths(12);
    final double naturalPaymentStartTime = TimeCalculator.getTimeBetween(date, naturalPaymentstartDate);
    return new CouponInflationYearOnYearInterpolation(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), referenceStartTime, naturalPaymentStartTime,
        referenceEndTime,
        naturalPaymentEndTime, _payNotional, _weightStart, _weightEnd);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(priceIndexTimeSeries, "price index time series");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final LocalDate dayConversion = date.toLocalDate();
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final LocalDate dayFixing = getReferenceEndDate()[1].toLocalDate();
    if (dayConversion.isAfter(dayFixing)) {
      final Double fixedEndIndex0 = priceIndexTimeSeries.getValue(getReferenceEndDate()[0]);
      final Double fixedEndIndex1 = priceIndexTimeSeries.getValue(getReferenceEndDate()[1]);
      final Double fixedEndIndex = getWeightEnd() * fixedEndIndex0 + (1 - getWeightEnd()) * fixedEndIndex1;
      final Double fixedStartIndex0 = priceIndexTimeSeries.getValue(getReferenceStartDates()[0]);
      final Double fixedStartIndex1 = priceIndexTimeSeries.getValue(getReferenceStartDates()[1]);
      final Double fixedStartIndex = getWeightStart() * fixedStartIndex0 + (1 - getWeightStart()) * fixedStartIndex1;
      final Double fixedRate = (fixedEndIndex / fixedStartIndex - (payNotional() ? 0.0 : 1.0));
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixedRate);
    }
    final double[] referenceEndTime = new double[2];
    final double[] referenceStartTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, _referenceEndDates[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, _referenceEndDates[1]);
    referenceStartTime[0] = TimeCalculator.getTimeBetween(date, _referenceStartDates[0]);
    referenceStartTime[1] = TimeCalculator.getTimeBetween(date, _referenceStartDates[1]);
    final ZonedDateTime naturalPaymentEndDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentEndTime = TimeCalculator.getTimeBetween(date, naturalPaymentEndDate);
    final ZonedDateTime naturalPaymentstartDate = naturalPaymentEndDate.minusMonths(12);
    final double naturalPaymentStartTime = TimeCalculator.getTimeBetween(date, naturalPaymentstartDate);
    return new CouponInflationYearOnYearInterpolation(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), referenceStartTime, naturalPaymentStartTime,
        referenceEndTime, naturalPaymentEndTime, _payNotional, _weightStart, _weightEnd);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationYearOnYearInterpolationDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationYearOnYearInterpolationDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _conventionalMonthLag;
    result = prime * result + _monthLag;
    result = prime * result + (_payNotional ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_referenceEndDates);
    result = prime * result + Arrays.hashCode(_referenceStartDates);
    long temp;
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
    final CouponInflationYearOnYearInterpolationDefinition other = (CouponInflationYearOnYearInterpolationDefinition) obj;
    if (_conventionalMonthLag != other._conventionalMonthLag) {
      return false;
    }
    if (_monthLag != other._monthLag) {
      return false;
    }
    if (_payNotional != other._payNotional) {
      return false;
    }
    if (!Arrays.deepEquals(_referenceEndDates, other._referenceEndDates)) {
      return false;
    }
    if (!Arrays.deepEquals(_referenceStartDates, other._referenceStartDates)) {
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
