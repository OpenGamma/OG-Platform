/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.instrument.inflation;

import java.util.Arrays;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

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
  private final ZonedDateTime[] _referenceStartDate;

  /**
   * The reference date for the index at the coupon end. The first of the month. There is usually a difference of two or three month between the reference date and the payment date.
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
   * Flag indicating if the notional is paid (true) or not (false).
   */
  private final boolean _payNotional;
  /**
   * The lag in month between the index validity and the coupon dates.
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
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param referenceStartDate The reference date for the index at the coupon start.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param weightStart The weight on the first month index in the interpolation of the index at the coupon start.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the coupon end.
   */
  public CouponInflationYearOnYearInterpolationDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate, final double paymentYearFraction, final double notional, final IndexPrice priceIndex, final int monthLag,
      final ZonedDateTime[] referenceStartDate, final ZonedDateTime[] referenceEndDate, final boolean payNotional, final double weightStart, final double weightEnd) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional, priceIndex);
    ArgumentChecker.notNull(referenceStartDate, "Reference start date");
    ArgumentChecker.notNull(referenceEndDate, "Reference end date");
    this._referenceStartDate = referenceStartDate;
    this._referenceEndDate = referenceEndDate;
    _weightStart = weightStart;
    _weightEnd = weightEnd;
    _payNotional = payNotional;
    _monthLag = monthLag;
  }

  /**
   * Builder for inflation Year on Yearn based on an inflation lag and index publication. The fixing date is the publication lag after the last reference month.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @param weightStart The weight on the first month index in the interpolation of the index at the _referenceStartDate.
   * @param weightEnd The weight on the first month index in the interpolation of the index at the _referenceEndDate.
   * @return The inflation zero-coupon.
   */
  public static CouponInflationYearOnYearInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int monthLag, final boolean payNotional, final double weightStart, final double weightEnd) {
    final ZonedDateTime[] referenceStartDate = new ZonedDateTime[2];
    final ZonedDateTime[] referenceEndDate = new ZonedDateTime[2];
    referenceStartDate[0] = accrualStartDate.minusMonths(monthLag).withDayOfMonth(1);
    referenceStartDate[1] = referenceStartDate[0].plusMonths(1);
    referenceEndDate[0] = paymentDate.minusMonths(monthLag).withDayOfMonth(1);
    referenceEndDate[1] = referenceEndDate[0].plusMonths(1);

    return new CouponInflationYearOnYearInterpolationDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex,
        monthLag, referenceStartDate, referenceEndDate, payNotional, weightStart, weightEnd);
  }

  /**
   * Builder for inflation year on year coupon based on an inflation lag and the index publication lag. The fixing date is the publication lag after the last reference month.
   * The index start value is calculated from a time series. The index value required for the coupon should be in the time series.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @param payNotional Flag indicating if the notional is paid (true) or not (false).
   * @return The inflation zero-coupon.
   */
  public static CouponInflationYearOnYearInterpolationDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional,
      final IndexPrice priceIndex, final int monthLag, final boolean payNotional) {
    final ZonedDateTime refInterpolatedDateStart = accrualStartDate;
    final ZonedDateTime refInterpolatedDateEnd = paymentDate;

    final double weightStart = 1.0 - (refInterpolatedDateStart.getDayOfMonth() - 1.0) / refInterpolatedDateStart.toLocalDate().lengthOfMonth();
    final double weightEnd = 1.0 - (refInterpolatedDateEnd.getDayOfMonth() - 1.0) / refInterpolatedDateEnd.toLocalDate().lengthOfMonth();

    return from(accrualStartDate, paymentDate, notional, priceIndex, monthLag, payNotional, weightStart, weightEnd);
  }

  /**
   * Gets the reference date for the index at the coupon start.
   * @return The reference date for the index at the coupon start.
   */
  public ZonedDateTime[] getReferenceStartDate() {
    return _referenceStartDate;
  }

  /**
   * Gets the reference date for the index at the coupon end.
   * @return The reference date for the index at the coupon end.
   */
  public ZonedDateTime[] getReferenceEndDate() {
    return _referenceEndDate;
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

  /**
   * Gets the lag in month between the index validity and the coupon dates.
   * @return The lag.
   */
  public int getMonthLag() {
    return _monthLag;
  }

  @Override
  public CouponInflationDefinition with(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double notional) {
    final ZonedDateTime refInterpolatedDate = accrualEndDate.minusMonths(_monthLag);
    final ZonedDateTime[] referenceEndDate = new ZonedDateTime[2];
    referenceEndDate[0] = refInterpolatedDate.withDayOfMonth(1);
    referenceEndDate[1] = referenceEndDate[0].plusMonths(1);
    return new CouponInflationYearOnYearInterpolationDefinition(getCurrency(), paymentDate, accrualStartDate, accrualEndDate, getPaymentYearFraction(), getNotional(),
        getPriceIndex(), _monthLag, getReferenceStartDate(), referenceEndDate, payNotional(), getWeightStart(), getWeightEnd());
  }

  @Override
  public CouponInflationYearOnYearInterpolation toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] referenceStartTime = new double[2];
    referenceStartTime[0] = TimeCalculator.getTimeBetween(date, getReferenceStartDate()[0]);
    referenceStartTime[1] = TimeCalculator.getTimeBetween(date, getReferenceStartDate()[1]);
    final double[] referenceEndTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, getReferenceEndDate()[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, getReferenceEndDate()[1]);
    return new CouponInflationYearOnYearInterpolation(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), referenceStartTime, referenceEndTime, _payNotional,
        _weightStart, _weightStart);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    ArgumentChecker.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final LocalDate dayConversion = date.toLocalDate();
    final String discountingCurveName = yieldCurveNames[0];
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final LocalDate dayFixing = getReferenceEndDate()[1].toLocalDate();
    if (dayConversion.isAfter(dayFixing)) {
      final Double fixedEndIndex0 = priceIndexTimeSeries.getValue(getReferenceEndDate()[0]);
      final Double fixedEndIndex1 = priceIndexTimeSeries.getValue(getReferenceEndDate()[1]);
      final Double fixedEndIndex = getWeightEnd() * fixedEndIndex0 + (1 - getWeightEnd()) * fixedEndIndex1;
      final Double fixedStartIndex0 = priceIndexTimeSeries.getValue(getReferenceStartDate()[0]);
      final Double fixedStartIndex1 = priceIndexTimeSeries.getValue(getReferenceStartDate()[1]);
      final Double fixedStartIndex = getWeightStart() * fixedStartIndex0 + (1 - getWeightStart()) * fixedStartIndex1;
      final Double fixedRate = (fixedEndIndex / fixedStartIndex - (payNotional() ? 0.0 : 1.0));
      return new CouponFixed(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), fixedRate);
    }
    final double[] referenceEndTime = new double[2];
    final double[] referenceStartTime = new double[2];
    referenceEndTime[0] = TimeCalculator.getTimeBetween(date, _referenceEndDate[0]);
    referenceEndTime[1] = TimeCalculator.getTimeBetween(date, _referenceEndDate[1]);
    referenceStartTime[0] = TimeCalculator.getTimeBetween(date, _referenceStartDate[0]);
    referenceStartTime[1] = TimeCalculator.getTimeBetween(date, _referenceStartDate[1]);
    return new CouponInflationYearOnYearInterpolation(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getPriceIndex(), referenceStartTime, referenceEndTime, _payNotional,
        _weightStart, _weightStart);
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
    result = prime * result + _monthLag;
    result = prime * result + (_payNotional ? 1231 : 1237);
    result = prime * result + Arrays.hashCode(_referenceEndDate);
    result = prime * result + Arrays.hashCode(_referenceStartDate);
    long temp;
    temp = Double.doubleToLongBits(_weightEnd);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_weightStart);
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
    if (getClass() != obj.getClass()) {
      return false;
    }
    CouponInflationYearOnYearInterpolationDefinition other = (CouponInflationYearOnYearInterpolationDefinition) obj;
    if (_monthLag != other._monthLag) {
      return false;
    }
    if (_payNotional != other._payNotional) {
      return false;
    }
    if (!Arrays.equals(_referenceEndDate, other._referenceEndDate)) {
      return false;
    }
    if (!Arrays.equals(_referenceStartDate, other._referenceStartDate)) {
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
