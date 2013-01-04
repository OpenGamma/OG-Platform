/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.Arrays;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompounded;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * Class describing a Ibor-like compounded coupon. The Ibor fixing are compounded over several sub-periods.
 * The amount paid is equal to
 * $$
 * \begin{equation*}
 * \left(\prod_{i=1}^n (1+\delta_i r_i) \right)-1
 * \end{equation*}
 * $$
 * where the $\delta_i$ are the accrual factors of the sub periods and the $r_i$ the fixing for the same periods.
 * The fixing have their own start dates, end dates and accrual factors. In general they are close to the accrual
 * dates used to compute the coupon accrual factors.
 */
public final class CouponIborCompoundedDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * All the coupon sub-periods fix on the same index.
   */
  private final IborIndex _index;
  /**
   * The start dates of the accrual sub-periods.
   */
  private final ZonedDateTime[] _accrualStartDates;
  /**
   * The end dates of the accrual sub-periods.
   */
  private final ZonedDateTime[] _accrualEndDates;
  /**
   * The accrual factors (or year fraction) associated to the sub-periods.
   */
  private final double[] _paymentAccrualFactors;
  /**
   * The coupon fixing dates.
   */
  private final ZonedDateTime[] _fixingDates;
  /**
   * The start dates of the fixing periods.
   */
  private final ZonedDateTime[] _fixingPeriodStartDates;
  /**
   * The end dates of the fixing periods.
   */
  private final ZonedDateTime[] _fixingPeriodEndDates;
  /**
   * The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactors;

  /**
   * Constructor.
   * @param currency The coupon currency.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @param fixingDates The coupon fixing dates.
   * @param fixingPeriodStartDates The start dates of the fixing periods.
   * @param fixingPeriodEndDates The end dates of the fixing periods.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   */
  private CouponIborCompoundedDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] accrualStartDates, final ZonedDateTime[] accrualEndDates,
      final double[] paymentAccrualFactors, final ZonedDateTime[] fixingDates, final ZonedDateTime[] fixingPeriodStartDates,
      final ZonedDateTime[] fixingPeriodEndDates, final double[] fixingPeriodAccrualFactors) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.isTrue(accrualStartDates.length == accrualEndDates.length, "Accrual start and end dates should have same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingDates.length, "Same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingPeriodStartDates.length, "Same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingPeriodEndDates.length, "Same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingPeriodAccrualFactors.length, "Same length");
    _index = index;
    _accrualStartDates = accrualStartDates;
    _accrualEndDates = accrualEndDates;
    _paymentAccrualFactors = paymentAccrualFactors;
    _fixingDates = fixingDates;
    _fixingPeriodStartDates = fixingPeriodStartDates;
    _fixingPeriodEndDates = fixingPeriodEndDates;
    _fixingPeriodAccrualFactors = fixingPeriodAccrualFactors;
  }

  /**
   * Builds an Ibor compounded coupon from all the details.
   * @param paymentDate The coupon payment date.
   * @param accrualStartDate The start date of the accrual period.
   * @param accrualEndDate The end date of the accrual period.
   * @param paymentAccrualFactor The accrual factor of the accrual period.
   * @param notional The coupon notional.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @param fixingDates The coupon fixing dates.
   * @param fixingPeriodStartDates The start dates of the fixing periods.
   * @param fixingPeriodEndDates The end dates of the fixing periods.
   * @param fixingPeriodAccrualFactors The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundedDefinition from(final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IborIndex index, final ZonedDateTime[] accrualStartDates, final ZonedDateTime[] accrualEndDates,
      final double[] paymentAccrualFactors, final ZonedDateTime[] fixingDates, final ZonedDateTime[] fixingPeriodStartDates,
      final ZonedDateTime[] fixingPeriodEndDates, final double[] fixingPeriodAccrualFactors) {
    return new CouponIborCompoundedDefinition(index.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, index, accrualStartDates, accrualEndDates,
        paymentAccrualFactors, fixingDates, fixingPeriodStartDates, fixingPeriodEndDates, fixingPeriodAccrualFactors);
  }

  /**
   * Builds an Ibor compounded coupon from the accrual and payment details. The fixing dates and fixing accrual periods are computed from those dates using the index conventions.
   * @param paymentDate The coupon payment date.
   * @param notional The coupon notional.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundedDefinition from(final ZonedDateTime paymentDate, final double notional, final IborIndex index, final ZonedDateTime[] accrualStartDates,
      final ZonedDateTime[] accrualEndDates, final double[] paymentAccrualFactors) {
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime accrualStartDate = accrualStartDates[0];
    final ZonedDateTime accrualEndDate = accrualEndDates[nbSubPeriod - 1];
    double paymentAccrualFactor = 0.0;
    final ZonedDateTime[] fixingDates = new ZonedDateTime[nbSubPeriod];
    final ZonedDateTime[] fixingPeriodEndDates = new ZonedDateTime[nbSubPeriod];
    final double[] fixingPeriodAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactor += paymentAccrualFactors[loopsub];
      fixingDates[loopsub] = ScheduleCalculator.getAdjustedDate(accrualStartDates[loopsub], -index.getSpotLag(), index.getCalendar());
      fixingPeriodEndDates[loopsub] = ScheduleCalculator.getAdjustedDate(accrualStartDates[loopsub], index);
      fixingPeriodAccrualFactors[loopsub] = index.getDayCount().getDayCountFraction(accrualStartDates[loopsub], fixingPeriodEndDates[loopsub]);
    }
    return new CouponIborCompoundedDefinition(index.getCurrency(), paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, index, accrualStartDates, accrualEndDates,
        paymentAccrualFactors, fixingDates, accrualStartDates, fixingPeriodEndDates, fixingPeriodAccrualFactors);
  }

  /**
   * Builds an Ibor compounded coupon from a total period and the Ibor index. The Ibor day count is used to compute the accrual factors.
   * If required the stub of the sub-periods will be short and last. The payment date is the start accrual date plus the tenor in the index conventions.
   * @param notional The coupon notional.
   * @param accrualStartDate The first accrual date. The other one are computed from that one and the index conventions.
   * @param tenor The total coupon tenor.
   * @param index The underlying Ibor index.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundedDefinition from(final double notional, final ZonedDateTime accrualStartDate, final Period tenor, final IborIndex index) {
    final ZonedDateTime[] accrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(accrualStartDate, tenor, true, false, index);
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime[] accrualStartDates = new ZonedDateTime[nbSubPeriod];
    accrualStartDates[0] = accrualStartDate;
    System.arraycopy(accrualEndDates, 0, accrualStartDates, 1, nbSubPeriod - 1);
    final double[] paymentAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactors[loopsub] = index.getDayCount().getDayCountFraction(accrualStartDates[loopsub], accrualEndDates[loopsub]);
    }
    return from(accrualEndDates[nbSubPeriod - 1], notional, index, accrualStartDates, accrualEndDates, paymentAccrualFactors);
  }

  /**
   * Returns the Ibor index underlying the coupon.
   * @return The index.
   */
  public IborIndex getIndex() {
    return _index;
  }

  /**
   * Returns the accrual start dates of each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getAccrualStartDates() {
    return _accrualStartDates;
  }

  /**
   * Returns the accrual end dates of each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getAccrualEndDates() {
    return _accrualEndDates;
  }

  /**
   * Returns the payment accrual factors for each sub-period.
   * @return The factors.
   */
  public double[] getPaymentAccrualFactors() {
    return _paymentAccrualFactors;
  }

  /**
   * Returns the fixing dates for each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getFixingDates() {
    return _fixingDates;
  }

  /**
   * Returns the fixing period start dates for each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getFixingPeriodStartDates() {
    return _fixingPeriodStartDates;
  }

  /**
   * Returns the fixing period end dates for each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getFixingPeriodEndDates() {
    return _fixingPeriodEndDates;
  }

  /**
   * Returns the fixing period accrual factors for each sub-period.
   * @return The factors.
   */
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodAccrualFactors;
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    final LocalDate dateConversion = dateTime.toLocalDate();
    ArgumentChecker.notNull(indexFixingTimeSeries, "Index fixing time series");
    ArgumentChecker.notNull(yieldCurveNames, "yield curve names");
    ArgumentChecker.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    ArgumentChecker.isTrue(!dateConversion.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final String discountingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final int nbSubPeriods = _fixingDates.length;
    int nbFixed = 0;
    double ratioAccrued = 1.0;
    while ((nbFixed < nbSubPeriods) && (dateConversion.isAfter(_fixingDates[nbFixed].toLocalDate()))) {
      final ZonedDateTime rezonedFixingDate = ZonedDateTime.of(_fixingDates[nbFixed].toLocalDate(), LocalTime.of(0, 0), TimeZone.UTC);
      final Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDate);
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + rezonedFixingDate);
      }
      ratioAccrued *= 1.0 + _paymentAccrualFactors[nbFixed] * fixedRate;
      nbFixed++;
    }
    if ((nbFixed < nbSubPeriods) && dateConversion.equals(_fixingDates[nbFixed].toLocalDate())) {
      final ZonedDateTime rezonedFixingDate = ZonedDateTime.of(_fixingDates[nbFixed].toLocalDate(), LocalTime.of(0, 0), TimeZone.UTC);
      final Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDate);
      if (fixedRate != null) {
        // Implementation note: on the fixing date and fixing already known.
        ratioAccrued *= 1.0 + _paymentAccrualFactors[nbFixed] * fixedRate;
        nbFixed++;
      }
    }
    if (nbFixed == nbSubPeriods) {
      // Implementation note: all dates already fixed: CouponFixed
      final double rate = (ratioAccrued - 1.0) / getPaymentYearFraction();
      return new CouponFixed(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), rate, getAccrualStartDate(), getAccrualEndDate());
    }
    final double notionalAccrued = getNotional() * ratioAccrued;
    final int nbSubPeriodLeft = nbSubPeriods - nbFixed;
    final double[] paymentAccrualFactorsLeft = new double[nbSubPeriodLeft];
    System.arraycopy(_paymentAccrualFactors, nbFixed, paymentAccrualFactorsLeft, 0, nbSubPeriodLeft);
    final double[] fixingTimesLeft = new double[nbSubPeriodLeft];
    System.arraycopy(TimeCalculator.getTimeBetween(dateTime, _fixingDates), nbFixed, fixingTimesLeft, 0, nbSubPeriodLeft);
    final double[] fixingPeriodStartTimesLeft = new double[nbSubPeriodLeft];
    System.arraycopy(TimeCalculator.getTimeBetween(dateTime, _fixingPeriodStartDates), nbFixed, fixingPeriodStartTimesLeft, 0, nbSubPeriodLeft);
    final double[] fixingPeriodEndTimesLeft = new double[nbSubPeriodLeft];
    System.arraycopy(TimeCalculator.getTimeBetween(dateTime, _fixingPeriodEndDates), nbFixed, fixingPeriodEndTimesLeft, 0, nbSubPeriodLeft);
    final double[] fixingPeriodAccrualFactorsLeft = new double[nbSubPeriodLeft];
    System.arraycopy(_fixingPeriodAccrualFactors, nbFixed, fixingPeriodAccrualFactorsLeft, 0, nbSubPeriodLeft);
    return new CouponIborCompounded(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), notionalAccrued, _index, paymentAccrualFactorsLeft, fixingTimesLeft,
        fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft, forwardCurveName);
  }

  @Override
  public CouponIborCompounded toDerivative(final ZonedDateTime dateTime, final String... yieldCurveNames) {
    final LocalDate dateConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dateConversion.isAfter(_fixingDates[0].toLocalDate()), "toDerivative without time series should have a date before the first fixing date.");
    final String discountingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final double[] fixingTimes = TimeCalculator.getTimeBetween(dateTime, _fixingDates);
    final double[] fixingPeriodStartTimes = TimeCalculator.getTimeBetween(dateTime, _fixingPeriodStartDates);
    final double[] fixingPeriodEndTimes = TimeCalculator.getTimeBetween(dateTime, _fixingPeriodEndDates);
    return new CouponIborCompounded(getCurrency(), paymentTime, discountingCurveName, getPaymentYearFraction(), getNotional(), getNotional(), _index, _paymentAccrualFactors, fixingTimes,
        fixingPeriodStartTimes, fixingPeriodEndTimes, _fixingPeriodAccrualFactors, forwardCurveName);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborCompoundedDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborCompoundedDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_accrualEndDates);
    result = prime * result + Arrays.hashCode(_accrualStartDates);
    result = prime * result + Arrays.hashCode(_fixingDates);
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingPeriodEndDates);
    result = prime * result + Arrays.hashCode(_fixingPeriodStartDates);
    result = prime * result + _index.hashCode();
    result = prime * result + Arrays.hashCode(_paymentAccrualFactors);
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
    final CouponIborCompoundedDefinition other = (CouponIborCompoundedDefinition) obj;
    if (!Arrays.equals(_accrualEndDates, other._accrualEndDates)) {
      return false;
    }
    if (!Arrays.equals(_accrualStartDates, other._accrualStartDates)) {
      return false;
    }
    if (!Arrays.equals(_fixingDates, other._fixingDates)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodAccrualFactors, other._fixingPeriodAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodEndDates, other._fixingPeriodEndDates)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodStartDates, other._fixingPeriodStartDates)) {
      return false;
    }
    if (_index == null) {
      if (other._index != null) {
        return false;
      }
    } else if (!_index.equals(other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    return true;
  }

}
