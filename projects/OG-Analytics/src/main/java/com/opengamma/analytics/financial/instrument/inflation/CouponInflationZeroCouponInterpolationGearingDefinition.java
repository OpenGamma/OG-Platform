/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.interestrate.inflation.derivative.CouponInflationZeroCouponInterpolationGearing;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing an zero-coupon inflation coupon were the inflation figure are interpolated between monthly inflation figures and the inflation increment is multiplied by a gearing factor.
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 * The pay-off is factor*(Index_End / Index_Start - X) with X=0 for notional payment and X=1 for no notional payment.
 */
public class CouponInflationZeroCouponInterpolationGearingDefinition extends CouponInflationDefinition implements CouponInflationGearing {

  /**
   * The reference dates for the index at the coupon start. Two months are required for the interpolation.
   * There is usually a difference of two or three month between the reference date and the accrual start date.
   */
  private final ZonedDateTime[] _referenceStartDates;
  /**
   * The index value at the start of the coupon.
   */
  private final double _indexStartValue;
  /**
   * The reference dates for the index at the coupon end. Two months are required for the interpolation.
   * There is usually a difference of two or three month between the reference date and the payment date.
   */
  private final ZonedDateTime[] _referenceEndDates;
  /**
   * The weight on the first month index in the interpolation.
   */
  private final double _weight;
  /**
   * Flag indicating if the notional is paid (true) or not (false).
   */
  private final boolean _payNotional;
  /**
   * The gearing (multiplicative) factor applied to the inflation increment rate.
   */
  private final double _factor;
  /**
   * The lag in month between the index validity and the coupon dates for the standard product (the one in exchange market and used for the calibration, this lag is in most cases 3 month).
   */
  private final int _conventionalMonthLag;

  /**
   * The lag in month between the index validity and the coupon dates for the actual product. (In most of the cases,lags are standard so _conventionalMonthLag=_monthLag)
   */
  private final int _monthLag;

  /**
   * Constructor for zero-coupon inflation coupon.
   * @param currency The coupon currency.
   * @param paymentDate The payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param referenceStartDates The reference date for the index at the coupon start.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param weight The weight for the interpolation of the index.
   * @param factor The multiplicative factor.
   */
  public CouponInflationZeroCouponInterpolationGearingDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex, final int conventionalMonthLag,
      final int monthLag, final ZonedDateTime[] referenceStartDates, final double indexStartValue, final ZonedDateTime[] referenceEndDate, final boolean payNotional,
      final double weight, final double factor) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, priceIndex);
    ArgumentChecker.notNull(referenceStartDates, "Reference start date");
    ArgumentChecker.notNull(referenceEndDate, "Reference end date");
    _referenceStartDates = referenceStartDates;
    _indexStartValue = indexStartValue;
    _referenceEndDates = referenceEndDate;
    _weight = weight;
    _payNotional = payNotional;
    _factor = factor;
    _conventionalMonthLag = conventionalMonthLag;
    _monthLag = monthLag;
  }

  /**
   * Constructor for zero-coupon inflation coupon.
   * @param currency The coupon currency.
   * @param paymentDate The payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param referenceStartDate The reference date for the index at the coupon start.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param factor The multiplicative factor.
   * @return The inflation zero-coupon.
   */
  public static CouponInflationZeroCouponInterpolationGearingDefinition from(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex, final int conventionalMonthLag,
      final int monthLag, final ZonedDateTime[] referenceStartDate, final double indexStartValue, final ZonedDateTime[] referenceEndDate, final boolean payNotional, final double factor) {

    ArgumentChecker.notNull(referenceStartDate, "Reference start date");
    ArgumentChecker.notNull(referenceEndDate, "Reference end date");

    final double weight = 1.0 - (paymentDate.getDayOfMonth() - 1.0) / paymentDate.toLocalDate().lengthOfMonth();
    return new CouponInflationZeroCouponInterpolationGearingDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, 1.0, notional, priceIndex,
        conventionalMonthLag, monthLag, referenceStartDate, indexStartValue, referenceEndDate, payNotional, weight, factor);
  }

  /**
   * Builder for inflation zero-coupon.
   * The accrualStartDate is used for the referenceStartDate. The paymentDate is used for accrualEndDate. The paymentYearFraction is 1.0.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param factor The multiplicative factor.
   * @return The coupon.
   */
  public static CouponInflationZeroCouponInterpolationGearingDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate,
      final double notional, final IndexPrice priceIndex, final int conventionalMonthLag, final int monthLag, final double indexStartValue,
      final ZonedDateTime[] referenceEndDate, final boolean payNotional, final double factor) {
    ArgumentChecker.notNull(priceIndex, "Price index");
    final ZonedDateTime[] referenceStartDates = new ZonedDateTime[2];
    final ZonedDateTime refInterpolatedStartDate = accrualStartDate.minusMonths(monthLag);
    referenceStartDates[0] = refInterpolatedStartDate.with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceStartDates[1] = referenceStartDates[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    return from(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, conventionalMonthLag, monthLag, referenceStartDates, indexStartValue,
        referenceEndDate, payNotional, factor);
  }

  /**
   * Builder for inflation zero-coupon based on an inflation lag and the index publication lag. The fixing date is the publication lag after the last reference month.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param indexStartValue The index value at the start of the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param factor The multiplicative factor.
   * @return The inflation zero-coupon.
   */
  public static CouponInflationZeroCouponInterpolationGearingDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate,
      final double notional, final IndexPrice priceIndex, final double indexStartValue, final int conventionalMonthLag, final int monthLag, final boolean payNotional, final double factor) {
    final ZonedDateTime refInterpolatedStartDate = accrualStartDate.minusMonths(monthLag);
    final ZonedDateTime[] referenceStartDates = new ZonedDateTime[2];
    referenceStartDates[0] = refInterpolatedStartDate.with(TemporalAdjusters.lastDayOfMonth());
    referenceStartDates[1] = referenceStartDates[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

    final ZonedDateTime refInterpolatedEndDate = paymentDate.minusMonths(monthLag);
    final ZonedDateTime[] referenceEndDates = new ZonedDateTime[2];
    referenceEndDates[0] = refInterpolatedEndDate.with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceEndDates[1] = referenceEndDates[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    return from(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex,
        conventionalMonthLag, monthLag, referenceStartDates, indexStartValue, referenceEndDates, payNotional, factor);
  }

  /**
   * Builder for inflation zero-coupon based on an inflation lag and the index publication lag. The fixing date is the publication lag after the last reference month.
   * @param paymentDate The payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param indexStartValue The index value at the start of the coupon.
   * @param conventionalMonthLag The lag in month between the index validity and the coupon dates for the standard product.
   * @param monthLag The lag in month between the index validity and the coupon dates for the actual product.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param factor The multiplicative factor.
   * @return The inflation zero-coupon.
   */
  public static CouponInflationZeroCouponInterpolationGearingDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double notional, final IndexPrice priceIndex, final double indexStartValue, final int conventionalMonthLag,
      final int monthLag, final boolean payNotional, final double factor) {
    final ZonedDateTime refInterpolatedStartDate = accrualStartDate.minusMonths(monthLag);
    final ZonedDateTime[] referenceStartDates = new ZonedDateTime[2];
    referenceStartDates[0] = refInterpolatedStartDate.with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceStartDates[1] = referenceStartDates[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);

    final ZonedDateTime refInterpolatedEndDate = paymentDate.minusMonths(monthLag);
    final ZonedDateTime[] referenceEndDates = new ZonedDateTime[2];
    referenceEndDates[0] = refInterpolatedEndDate.with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    referenceEndDates[1] = referenceEndDates[0].plusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).withHour(0).withMinute(0);
    return from(priceIndex.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, 1.0, notional,
        priceIndex, conventionalMonthLag, monthLag, referenceStartDates, indexStartValue, referenceEndDates, payNotional, factor);
  }

  @Override
  public CouponInflationDefinition with(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional) {
    return from(paymentDate, accrualStartDate, accrualEndDate, notional, getPriceIndex(), _indexStartValue, _conventionalMonthLag, _monthLag, _payNotional, _factor);
  }

  /**
   * Gets the reference date for the index at the coupon start.
   * @return The reference date for the index at the coupon start.
   */
  public ZonedDateTime[] getReferenceStartDates() {
    return _referenceStartDates;
  }

  /**
   * Gets the index value at the start of the coupon.
   * @return The index value at the start of the coupon.
   */
  public double getIndexStartValue() {
    return _indexStartValue;
  }

  /**
   * Gets the reference dates for the index at the coupon end.
   * @return The reference date for the index at the coupon end.
   */
  public ZonedDateTime[] getReferenceEndDates() {
    return _referenceEndDates;
  }

  /**
   * Gets the weight on the first month index in the interpolation.
   * @return The weight.
   */
  public double getWeight() {
    return _weight;
  }

  /**
   * Gets the pay notional flag.
   * @return The flag.
   */
  public boolean payNotional() {
    return _payNotional;
  }

  /**
   * Gets the lag in month between the index validity and the coupon dates.
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

  @Override
  public double getFactor() {
    return _factor;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public CouponInflationZeroCouponInterpolationGearing toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, getReferenceEndDates()[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, getReferenceEndDates()[1]);
    final ZonedDateTime naturalPaymentDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentTime = TimeCalculator.getTimeBetween(date, naturalPaymentDate);
    return new CouponInflationZeroCouponInterpolationGearing(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), _indexStartValue, referenceEndTime,
        naturalPaymentTime, _weight, _payNotional, _factor);
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final LocalDate dayConversion = date.toLocalDate();
    final String discountingCurveName = yieldCurveNames[0];
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final LocalDate dayFixing = getReferenceEndDates()[1].toLocalDate();
    if (dayConversion.isAfter(dayFixing)) {
      final Double fixedEndIndex1 = priceIndexTimeSeries.getValue(getReferenceEndDates()[1]);

      if (fixedEndIndex1 != null) {
        final Double fixedEndIndex0 = priceIndexTimeSeries.getValue(getReferenceEndDates()[0]);
        final Double fixedEndIndex = getWeight() * fixedEndIndex0 + (1 - getWeight()) * fixedEndIndex1;
        final Double fixedRate = _factor * (fixedEndIndex / getIndexStartValue() - (payNotional() ? 0.0 : 1.0));
        return new CouponFixed(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), fixedRate);
      }
    }
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, _referenceEndDates[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, _referenceEndDates[1]);
    final ZonedDateTime naturalPaymentDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentTime = TimeCalculator.getTimeBetween(date, naturalPaymentDate);
    return new CouponInflationZeroCouponInterpolationGearing(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), _indexStartValue, referenceEndTime,
        naturalPaymentTime, _weight, _payNotional, _factor);
  }

  @Override
  public CouponInflationZeroCouponInterpolationGearing toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, getReferenceEndDates()[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, getReferenceEndDates()[1]);
    final ZonedDateTime naturalPaymentDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentTime = TimeCalculator.getTimeBetween(date, naturalPaymentDate);
    return new CouponInflationZeroCouponInterpolationGearing(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), _indexStartValue, referenceEndTime,
        naturalPaymentTime, _weight, _payNotional, _factor);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final LocalDate dayConversion = date.toLocalDate();
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final LocalDate dayFixing = getReferenceEndDates()[1].toLocalDate();
    if (dayConversion.isAfter(dayFixing)) {
      final Double fixedEndIndex1 = priceIndexTimeSeries.getValue(getReferenceEndDates()[1]);

      if (fixedEndIndex1 != null) {
        final Double fixedEndIndex0 = priceIndexTimeSeries.getValue(getReferenceEndDates()[0]);
        final Double fixedEndIndex = getWeight() * fixedEndIndex0 + (1 - getWeight()) * fixedEndIndex1;
        final Double fixedRate = _factor * (fixedEndIndex / getIndexStartValue() - (payNotional() ? 0.0 : 1.0));
        return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), fixedRate);
      }
    }
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, _referenceEndDates[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, _referenceEndDates[1]);
    final ZonedDateTime naturalPaymentDate = getPaymentDate().minusMonths(_monthLag - _conventionalMonthLag);
    final double naturalPaymentTime = TimeCalculator.getTimeBetween(date, naturalPaymentDate);
    return new CouponInflationZeroCouponInterpolationGearing(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), _indexStartValue, referenceEndTime,
        naturalPaymentTime, _weight, _payNotional, _factor);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponInterpolationGearing(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponInflationZeroCouponInterpolationGearing(this);
  }

  @Override
  public String toString() {
    return "CouponInflationZeroCouponInterpolationGearingDefinition [_referenceEndDate=" + Arrays.toString(_referenceEndDates) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _conventionalMonthLag;
    long temp;
    temp = Double.doubleToLongBits(_factor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _monthLag;
    result = prime * result + (_payNotional ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_referenceEndDates);
    result = prime * result + Arrays.hashCode(_referenceStartDates);
    temp = Double.doubleToLongBits(_weight);
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
    final CouponInflationZeroCouponInterpolationGearingDefinition other = (CouponInflationZeroCouponInterpolationGearingDefinition) obj;
    if (_conventionalMonthLag != other._conventionalMonthLag) {
      return false;
    }
    if (Double.doubleToLongBits(_factor) != Double.doubleToLongBits(other._factor)) {
      return false;
    }
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
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
    if (Double.doubleToLongBits(_weight) != Double.doubleToLongBits(other._weight)) {
      return false;
    }
    return true;
  }

}
