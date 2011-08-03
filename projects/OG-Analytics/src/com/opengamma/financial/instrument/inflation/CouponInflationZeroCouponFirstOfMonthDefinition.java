/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.inflation;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.FixedIncomeInstrumentWithDataConverter;
import com.opengamma.financial.instrument.index.PriceIndex;
import com.opengamma.financial.instrument.payment.CouponDefinition;
import com.opengamma.financial.interestrate.inflation.derivatives.CouponInflationZeroCouponFirstOfMonth;
import com.opengamma.financial.interestrate.payments.Coupon;
import com.opengamma.financial.interestrate.payments.CouponFixed;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing an zero-coupon inflation coupon were the inflation figure are the one of the reference month and are not interpolated.
 * The start index value is known when the coupon is traded/issued.
 * The index for a given month is given in the yield curve and in the time series on the first of the month.
 */
public class CouponInflationZeroCouponFirstOfMonthDefinition extends CouponDefinition implements FixedIncomeInstrumentWithDataConverter<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The price index associated to the coupon.
   */
  private final PriceIndex _priceIndex;
  /**
   * The reference date for the index at the coupon start. May not be relevant as the index value is known.
   */
  private final ZonedDateTime _referenceStartDate;
  /**
   * The index value at the start of the coupon.
   */
  private final double _indexStartValue;
  /**
   * The reference date for the index at the coupon end. The first of the month. There is usually a difference of two or three month between the reference date and the payment date.
   */
  private final ZonedDateTime _referenceEndDate;
  /**
   * The date on which the end index is expected to be known. The index is usually known two week after the end of the reference month. 
   * The date is only an "expected date" as the index publication could be delayed for different reasons. The date should not be enforced to strictly in pricing and instrument creation.
   */
  private final ZonedDateTime _fixingEndDate;

  /**
   * Constructor for zero-coupon inflation coupon.
   * @param currency The coupon currency.
   * @param paymentDate The payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon. 
   * @param referenceStartDate The reference date for the index at the coupon start.
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param fixingEndDate The date on which the end index is expected to be known.
   */
  public CouponInflationZeroCouponFirstOfMonthDefinition(Currency currency, ZonedDateTime paymentDate, ZonedDateTime accrualStartDate, ZonedDateTime accrualEndDate, double paymentYearFraction,
      double notional, PriceIndex priceIndex, ZonedDateTime referenceStartDate, double indexStartValue, ZonedDateTime referenceEndDate, ZonedDateTime fixingEndDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    Validate.notNull(priceIndex, "Price index");
    Validate.notNull(referenceStartDate, "Reference start date");
    Validate.notNull(referenceEndDate, "Reference end date");
    Validate.notNull(fixingEndDate, "Fixing end date");
    this._priceIndex = priceIndex;
    this._referenceStartDate = referenceStartDate;
    this._indexStartValue = indexStartValue;
    this._referenceEndDate = referenceEndDate;
    this._fixingEndDate = fixingEndDate;
  }

  /**
   * Builder for inflation zero-coupon. 
   * The accrualStartDate is used for the referenceStartDate. The paymentDate is used for accrualEndDate. The paymentYearFraction is 1.0.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon. 
   * @param indexStartValue The index value at the start of the coupon.
   * @param referenceEndDate The reference date for the index at the coupon end.
   * @param fixingEndDate The date on which the end index is expected to be known.
   * @return The coupon.
   */
  public static CouponInflationZeroCouponFirstOfMonthDefinition from(ZonedDateTime accrualStartDate, ZonedDateTime paymentDate, double notional, PriceIndex priceIndex, double indexStartValue,
      ZonedDateTime referenceEndDate, ZonedDateTime fixingEndDate) {
    Validate.notNull(priceIndex, "Price index");
    return new CouponInflationZeroCouponFirstOfMonthDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, accrualStartDate, indexStartValue,
        referenceEndDate, fixingEndDate);
  }

  /**
   * Builder for inflation zero-coupon based on an inflation lag and a mid month publication. The fixing date is two weeks after the start of the last reference month.
   * @param accrualStartDate Start date of the accrual period.
   * @param paymentDate The payment date.
   * @param notional Coupon notional.
   * @param priceIndex The price index associated to the coupon. 
   * @param indexStartValue The index value at the start of the coupon.
   * @param monthLag The lag in month between the index validity and the coupon dates.
   * @return The inflation zero-coupon.
   */
  public static CouponInflationZeroCouponFirstOfMonthDefinition from(final ZonedDateTime accrualStartDate, final ZonedDateTime paymentDate, final double notional, final PriceIndex priceIndex,
      final double indexStartValue, final int monthLag) {
    int nbWeekPublication = 2; // Two weeks in the month after the reference month.
    ZonedDateTime referenceStartDate = accrualStartDate.minusMonths(monthLag);
    ZonedDateTime referenceEndDate = paymentDate.minusMonths(monthLag);
    referenceStartDate = referenceStartDate.withDayOfMonth(1);
    referenceEndDate = referenceEndDate.withDayOfMonth(1);
    ZonedDateTime fixingDate = referenceEndDate.minusDays(1).withDayOfMonth(1).plusMonths(2).plusWeeks(nbWeekPublication);
    return new CouponInflationZeroCouponFirstOfMonthDefinition(priceIndex.getCurrency(), paymentDate, accrualStartDate, paymentDate, 1.0, notional, priceIndex, referenceStartDate, indexStartValue,
        referenceEndDate, fixingDate);
  }

  /**
   * Gets the price index associated to the coupon. 
   * @return The price index.
   */
  public PriceIndex getPriceIndex() {
    return _priceIndex;
  }

  /**
   * Gets the reference date for the index at the coupon start.
   * @return The reference date for the index at the coupon start.
   */
  public ZonedDateTime getReferenceStartDate() {
    return _referenceStartDate;
  }

  /**
   * Gets the index value at the start of the coupon.
   * @return The index value at the start of the coupon.
   */
  public double getIndexStartValue() {
    return _indexStartValue;
  }

  /**
   * Gets the reference date for the index at the coupon end.
   * @return The reference date for the index at the coupon end.
   */
  public ZonedDateTime getReferenceEndDate() {
    return _referenceEndDate;
  }

  /**
   * Gets the date on which the end index is expected to be known.
   * @return The date on which the end index is expected to be known.
   */
  public ZonedDateTime getFixingEndDate() {
    return _fixingEndDate;
  }

  @Override
  public CouponInflationZeroCouponFirstOfMonth toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "Do not have any fixing data but are asking for a derivative after the payment date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 0, "at least one curve required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    double paymentTime = actAct.getDayCountFraction(date, getPaymentDate());
    double referenceEndTime = actAct.getDayCountFraction(date, getReferenceEndDate());
    double fixingTime = actAct.getDayCountFraction(date, getFixingEndDate());
    final String discountingCurveName = yieldCurveNames[0];
    return new CouponInflationZeroCouponFirstOfMonth(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), _priceIndex, _indexStartValue, referenceEndTime,
        fixingTime);
  }

  @Override
  public Coupon toDerivative(ZonedDateTime date, DoubleTimeSeries<ZonedDateTime> priceIndexTimeSeries, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    double paymentTime = actAct.getDayCountFraction(date, getPaymentDate());
    final String discountingCurveName = yieldCurveNames[0];
    boolean fixingKnown = false;
    double rate = 0.0;
    if (!_fixingEndDate.isAfter(date)) { // Fixing data to be checked
      ZonedDateTime requiredIndexDate = _referenceEndDate;
      Double knownIndex = priceIndexTimeSeries.getValue(requiredIndexDate);
      if (knownIndex != null) { // Fixing known
        fixingKnown = true;
        rate = knownIndex / _indexStartValue - 1.0;
      }
    }
    if (fixingKnown) {
      return new CouponFixed(getCurrency(), paymentTime, discountingCurveName, 1.0, getNotional(), rate);
    }
    double fixingTime = 0; // The reference index is expected to be known but is not known yet.
    if (_fixingEndDate.isAfter(date)) {
      fixingTime = actAct.getDayCountFraction(date, _fixingEndDate);
    }
    double referenceEndTime = 0.0;
    if (_referenceEndDate.isAfter(date)) {
      referenceEndTime = actAct.getDayCountFraction(date, _referenceEndDate);
    } else {
      referenceEndTime = -actAct.getDayCountFraction(_referenceEndDate, date);
    }
    return new CouponInflationZeroCouponFirstOfMonth(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), _priceIndex, _indexStartValue, referenceEndTime,
        fixingTime);
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitCouponInflationZeroCouponFirstOfMonth(this, data);
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCouponInflationZeroCouponFirstOfMonth(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _fixingEndDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_indexStartValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _priceIndex.hashCode();
    result = prime * result + _referenceEndDate.hashCode();
    result = prime * result + _referenceStartDate.hashCode();
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
    CouponInflationZeroCouponFirstOfMonthDefinition other = (CouponInflationZeroCouponFirstOfMonthDefinition) obj;
    if (!ObjectUtils.equals(_fixingEndDate, other._fixingEndDate)) {
      return false;
    }
    if (Double.doubleToLongBits(_indexStartValue) != Double.doubleToLongBits(other._indexStartValue)) {
      return false;
    }
    if (!ObjectUtils.equals(_priceIndex, other._priceIndex)) {
      return false;
    }
    if (!ObjectUtils.equals(_referenceEndDate, other._referenceEndDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_referenceStartDate, other._referenceStartDate)) {
      return false;
    }
    return true;
  }

}
