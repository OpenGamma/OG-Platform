/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.payments.PaymentFixed;
import com.opengamma.financial.interestrate.payments.derivative.CouponOIS;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a OIS-like floating coupon.
 */
public class CouponOISDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The OIS-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IndexON _index;
  /**
   * The dates of the fixing periods (start and end). There is one date more than period.
   */
  private final ZonedDateTime[] _fixingPeriodDate;
  /**
   * The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   */
  private final Double[] _fixingPeriodAccrualFactor;

  /**
   * Constructor from all the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentYearFraction Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param fixingPeriodEndDate The end date of the fixing period.
   */
  public CouponOISDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final double paymentYearFraction,
      final double notional, final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentYearFraction, notional);
    Validate.notNull(index, "Coupon OIS Simplified: index");
    Validate.notNull(fixingPeriodStartDate, "Coupon OIS Simplified: fixingPeriodStartDate");
    Validate.notNull(fixingPeriodEndDate, "Coupon OIS Simplified: fixingPeriodEndDate");
    Validate.isTrue(currency.equals(index.getCurrency()), "Currency and index not compatible");
    _index = index;
    ZonedDateTime date = fixingPeriodStartDate;
    ZonedDateTime previousDate;
    final List<ZonedDateTime> fixingDateList = new ArrayList<ZonedDateTime>();
    final List<Double> fixingAccrualFactorList = new ArrayList<Double>();
    fixingDateList.add(date);
    while (date.isBefore(fixingPeriodEndDate)) {
      previousDate = date;
      date = ScheduleCalculator.getAdjustedDate(date, 1, index.getCalendar());
      fixingDateList.add(date);
      fixingAccrualFactorList.add(index.getDayCount().getDayCountFraction(previousDate, date));
    }
    _fixingPeriodDate = fixingDateList.toArray(new ZonedDateTime[0]);
    _fixingPeriodAccrualFactor = fixingAccrualFactorList.toArray(new Double[0]);
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index. 
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param settlementDate The coupon settlement date.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param settlementDays The number of days between last fixing and the payment (also called spot lag). 
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @return The OIS coupon.
   */
  public static CouponOISDefinition from(final IndexON index, final ZonedDateTime settlementDate, final Period tenor, final double notional, final int settlementDays,
      final BusinessDayConvention businessDayConvention, final boolean isEOM) {
    final ZonedDateTime endFixingPeriodDate = ScheduleCalculator.getAdjustedDate(settlementDate, tenor, businessDayConvention, index.getCalendar(), isEOM);
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(endFixingPeriodDate, -1 + index.getPublicationLag() + settlementDays, index.getCalendar());
    final double paymentYearFraction = index.getDayCount().getDayCountFraction(settlementDate, endFixingPeriodDate);
    return new CouponOISDefinition(index.getCurrency(), paymentDate, settlementDate, endFixingPeriodDate, paymentYearFraction, notional, index, settlementDate, endFixingPeriodDate);
  }

  /**
   * Gets the OIS index of the instrument.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the dates of the fixing periods (start and end). There is one date more than period.
   * @return The dates of the fixing periods.
   */
  public ZonedDateTime[] getFixingPeriodDate() {
    return _fixingPeriodDate;
  }

  /**
   * Gets the accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @return The accrual factors.
   */
  public Double[] getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  @Override
  public CouponOIS toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(_fixingPeriodDate[0]), "toDerivative without time series only valid at dates where the fixing has not taken place yet.");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[0]);
    final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[_fixingPeriodDate.length - 1]);
    double fixingAccrualFactorTotal = 0.0;
    for (final Double element : _fixingPeriodAccrualFactor) {
      fixingAccrualFactorTotal += element;
    }
    final CouponOIS cpn = new CouponOIS(getCurrency(), paymentTime, yieldCurveNames[0], getPaymentYearFraction(), getNotional(), _index, fixingPeriodStartTime, fixingPeriodEndTime,
        fixingAccrualFactorTotal, getNotional(), yieldCurveNames[1]);
    return cpn;
  }

  @Override
  public Payment toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.isTrue(!date.isAfter(getPaymentDate()), "date is after payment date");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    if (date.isBefore(_fixingPeriodDate[0])) {
      return toDerivative(date, yieldCurveNames);
    }
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    int fixedPeriod = 0;
    double accruedNotional = getNotional();
    while (date.isAfter(_fixingPeriodDate[fixedPeriod]) && fixedPeriod < _fixingPeriodDate.length - 1) {
      final Double fixedRate = indexFixingTimeSeries.getValue(_fixingPeriodDate[fixedPeriod]);
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + _fixingPeriodDate[fixedPeriod]);
      }
      accruedNotional *= 1 + _fixingPeriodAccrualFactor[fixedPeriod] * fixedRate;
      fixedPeriod++;
    }
    if (fixedPeriod < _fixingPeriodDate.length - 1) { // Some OIS period left
      final Double fixedRate = indexFixingTimeSeries.getValue(_fixingPeriodDate[fixedPeriod]);
      if (fixedRate != null) { // Fixed already
        accruedNotional *= 1 + _fixingPeriodAccrualFactor[fixedPeriod] * fixedRate;
        fixedPeriod++;
      }
      if (fixedPeriod < _fixingPeriodDate.length - 1) { // Some OIS period left
        final double fixingPeriodStartTime = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[fixedPeriod]);
        final double fixingPeriodEndTime = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[_fixingPeriodDate.length - 1]);
        double fixingAccrualFactorLeft = 0.0;
        for (int loopperiod = fixedPeriod; loopperiod < _fixingPeriodAccrualFactor.length; loopperiod++) {
          fixingAccrualFactorLeft += _fixingPeriodAccrualFactor[loopperiod];
        }
        final CouponOIS cpn = new CouponOIS(getCurrency(), paymentTime, yieldCurveNames[0], getPaymentYearFraction(), getNotional(), _index, fixingPeriodStartTime, fixingPeriodEndTime,
            fixingAccrualFactorLeft, accruedNotional, yieldCurveNames[1]);
        return cpn;
      }
      return new PaymentFixed(getCurrency(), paymentTime, accruedNotional, yieldCurveNames[0]);
    }
    // All fixed already
    return new PaymentFixed(getCurrency(), paymentTime, accruedNotional, yieldCurveNames[0]);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitCouponOIS(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitCouponOIS(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactor);
    result = prime * result + Arrays.hashCode(_fixingPeriodDate);
    result = prime * result + _index.hashCode();
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
    final CouponOISDefinition other = (CouponOISDefinition) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactor, other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodDate, other._fixingPeriodDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

}
