/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSimpleSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponIborCompoundingSpread;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Ibor-like coupon with compounding and spread. There are three ISDA versions of compounding with spread.
 * The one referred in this class is the "Compounding treating spread as simple interest" (not "Compounding" and not "Flat Compounding").
 * The Ibor fixing are compounded over several sub-periods.
 * The amount paid is described in the reference below.
 * The fixing have their own start dates, end dates and accrual factors. In general they are close to the accrual
 * dates used to compute the coupon accrual factors.
 * <p> Reference: Mengle, D. (2009). Alternative compounding methods for over-the-counter derivative transactions. ISDA.
 */
public class CouponIborCompoundingSimpleSpreadDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * All the coupon sub-periods fix on the same index.
   */
  private final IborIndex _index;
  /**
   * The start dates of the accrual sub-periods.
   */
  private final ZonedDateTime[] _subperiodAccrualStartDates;
  /**
   * The end dates of the accrual sub-periods.
   */
  private final ZonedDateTime[] _subperiodAccrualEndDates;
  /**
   * The accrual factors (or year fraction) associated to the sub-periods.
   */
  private final double[] _subperiodAccrualFactors;
  /**
   * The coupon fixing dates.
   */
  private final ZonedDateTime[] _fixingDates;
  /**
   * The start dates of the fixing periods.
   */
  private final ZonedDateTime[] _fixingSubperiodStartDates;
  /**
   * The end dates of the fixing periods.
   */
  private final ZonedDateTime[] _fixingSubperiodEndDates;
  /**
   * The accrual factors (or year fraction) associated with the fixing periods in the Index day count convention.
   */
  private final double[] _fixingSubperiodAccrualFactors;
  /**
   * The spread paid above the Ibor rate.
   */
  private final double _spread;
  /**
   * The rate of the first compounded rate. This is an optional field.
   */
  private final double _initialRate;

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
   * @param spread The spread paid above the Ibor rate.
   * @param initialRate The rate of the first compound period.
   */
  protected CouponIborCompoundingSimpleSpreadDefinition(
      final Currency currency,
      final ZonedDateTime paymentDate,
      final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor,
      final double notional,
      final IborIndex index,
      final ZonedDateTime[] accrualStartDates,
      final ZonedDateTime[] accrualEndDates,
      final double[] paymentAccrualFactors,
      final ZonedDateTime[] fixingDates,
      final ZonedDateTime[] fixingPeriodStartDates,
      final ZonedDateTime[] fixingPeriodEndDates,
      final double[] fixingPeriodAccrualFactors,
      final double spread,
      final double initialRate) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.isTrue(accrualStartDates.length == accrualEndDates.length, "Accrual start and end dates should have same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingDates.length, "Same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingPeriodStartDates.length, "Same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingPeriodEndDates.length, "Same length");
    ArgumentChecker.isTrue(accrualStartDates.length == fixingPeriodAccrualFactors.length, "Same length");
    _index = index;
    _subperiodAccrualStartDates = accrualStartDates;
    _subperiodAccrualEndDates = accrualEndDates;
    _subperiodAccrualFactors = paymentAccrualFactors;
    _fixingDates = fixingDates;
    _fixingSubperiodStartDates = fixingPeriodStartDates;
    _fixingSubperiodEndDates = fixingPeriodEndDates;
    _fixingSubperiodAccrualFactors = fixingPeriodAccrualFactors;
    _spread = spread;
    _initialRate = initialRate;
  }

  /**
   * Builds a Ibor compounded coupon with a spread using the "Flat compounded" to calculate each compounded period rate.
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
   * @param spread The spread paid above the Ibor rate.
   * @param initialRate The rate of the first compounded period.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundingSimpleSpreadDefinition from(
      final Currency currency,
      final ZonedDateTime paymentDate,
      final ZonedDateTime accrualStartDate,
      final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor,
      final double notional,
      final IborIndex index,
      final ZonedDateTime[] accrualStartDates,
      final ZonedDateTime[] accrualEndDates,
      final double[] paymentAccrualFactors,
      final ZonedDateTime[] fixingDates,
      final ZonedDateTime[] fixingPeriodStartDates,
      final ZonedDateTime[] fixingPeriodEndDates,
      final double[] fixingPeriodAccrualFactors,
      final double spread,
      final double initialRate) {
    return new CouponIborCompoundingSimpleSpreadDefinition(
        currency,
        paymentDate,
        accrualStartDate,
        accrualEndDate,
        paymentAccrualFactor,
        notional,
        index,
        accrualStartDates,
        accrualEndDates,
        paymentAccrualFactors,
        fixingDates,
        fixingPeriodStartDates,
        fixingPeriodEndDates,
        fixingPeriodAccrualFactors,
        spread,
        initialRate);
  }

  /**
   * Builds an Ibor compounded coupon from the accrual and payment details. The fixing dates and fixing accrual periods are computed from those dates using the index conventions.
   * @param paymentDate The coupon payment date.
   * @param notional The coupon notional.
   * @param index The Ibor-like index on which the coupon fixes. The index currency should be the same as the coupon currency.
   * @param accrualStartDates The start dates of the accrual sub-periods.
   * @param accrualEndDates The end dates of the accrual sub-periods.
   * @param paymentAccrualFactors The accrual factors (or year fraction) associated to the sub-periods.
   * @param spread The spread paid above the Ibor rate.
   * @param calendar The holiday calendar for the ibor index.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundingSimpleSpreadDefinition from(final ZonedDateTime paymentDate, final double notional, final IborIndex index,
      final ZonedDateTime[] accrualStartDates, final ZonedDateTime[] accrualEndDates, final double[] paymentAccrualFactors, final double spread,
      final Calendar calendar) {
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime accrualStartDate = accrualStartDates[0];
    final ZonedDateTime accrualEndDate = accrualEndDates[nbSubPeriod - 1];
    double paymentAccrualFactor = 0.0;
    final ZonedDateTime[] fixingDates = new ZonedDateTime[nbSubPeriod];
    final ZonedDateTime[] fixingPeriodEndDates = new ZonedDateTime[nbSubPeriod];
    final double[] fixingPeriodAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactor += paymentAccrualFactors[loopsub];
      fixingDates[loopsub] = ScheduleCalculator.getAdjustedDate(accrualStartDates[loopsub], -index.getSpotLag(), calendar);
      fixingPeriodEndDates[loopsub] = ScheduleCalculator.getAdjustedDate(accrualStartDates[loopsub], index, calendar);
      fixingPeriodAccrualFactors[loopsub] = index.getDayCount().getDayCountFraction(accrualStartDates[loopsub], fixingPeriodEndDates[loopsub], calendar);
    }
    return new CouponIborCompoundingSimpleSpreadDefinition(
        index.getCurrency(),
        paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor,
        notional, index,
        accrualStartDates, accrualEndDates, paymentAccrualFactors,
        fixingDates, accrualStartDates, fixingPeriodEndDates, fixingPeriodAccrualFactors,
        spread, Double.NaN);
  }

  /**
   * Builds an Ibor compounded coupon from a total period and the Ibor index. The Ibor day count is used to compute the accrual factors.
   * If required the stub of the sub-periods will be short and last. The payment date is the adjusted end accrual date.
   * The payment accrual factors are in the day count of the index. 
   * @param notional The coupon notional.
   * @param accrualStartDate The first accrual date. 
   * @param accrualEndDate The end accrual date.
   * @param index The underlying Ibor index.
   * @param spread The spread paid above the Ibor rate.
   * @param stub The stub type used for the compounding sub-periods. Not null.
   * @param businessDayConvention The leg business day convention.
   * @param endOfMonth The leg end-of-month convention.
   * @param calendar The holiday calendar for the ibor leg.
   * @return The compounded coupon.
   */
  public static CouponIborCompoundingSimpleSpreadDefinition from(final double notional, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate, final IborIndex index,
      final double spread, final StubType stub, final BusinessDayConvention businessDayConvention, final boolean endOfMonth, final Calendar calendar) {
    ArgumentChecker.notNull(accrualStartDate, "Accrual start date");
    ArgumentChecker.notNull(accrualEndDate, "Accrual end date");
    ArgumentChecker.notNull(index, "Index");
    ArgumentChecker.notNull(stub, "Stub type");
    ArgumentChecker.notNull(calendar, "Calendar");
    final boolean isStubShort = stub.equals(StubType.SHORT_END) || stub.equals(StubType.SHORT_START);
    final boolean isStubStart = stub.equals(StubType.LONG_START) || stub.equals(StubType.SHORT_START); // Implementation note: dates computed from the end.
    final ZonedDateTime[] accrualEndDates = ScheduleCalculator.getAdjustedDateSchedule(accrualStartDate, accrualEndDate, index.getTenor(), isStubShort, isStubStart,
        businessDayConvention, calendar, endOfMonth);
    final int nbSubPeriod = accrualEndDates.length;
    final ZonedDateTime[] accrualStartDates = new ZonedDateTime[nbSubPeriod];
    accrualStartDates[0] = accrualStartDate;
    System.arraycopy(accrualEndDates, 0, accrualStartDates, 1, nbSubPeriod - 1);
    final double[] paymentAccrualFactors = new double[nbSubPeriod];
    for (int loopsub = 0; loopsub < nbSubPeriod; loopsub++) {
      paymentAccrualFactors[loopsub] = index.getDayCount().getDayCountFraction(accrualStartDates[loopsub], accrualEndDates[loopsub], calendar);
    }
    return from(accrualEndDates[nbSubPeriod - 1], notional, index, accrualStartDates, accrualEndDates, paymentAccrualFactors, spread, calendar);
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
  public ZonedDateTime[] getSubperiodsAccrualStartDates() {
    return _subperiodAccrualStartDates;
  }

  /**
   * Returns the accrual end dates of each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getSubperiodsAccrualEndDates() {
    return _subperiodAccrualEndDates;
  }

  /**
   * Returns the payment accrual factors for each sub-period.
   * @return The factors.
   */
  public double[] getSubperiodsAccrualFactors() {
    return _subperiodAccrualFactors;
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
  public ZonedDateTime[] getFixingSubperiodStartDates() {
    return _fixingSubperiodStartDates;
  }

  /**
   * Returns the fixing period end dates for each sub-period.
   * @return The dates.
   */
  public ZonedDateTime[] getFixingSubperiodEndDates() {
    return _fixingSubperiodEndDates;
  }

  /**
   * Returns the fixing period accrual factors for each sub-period.
   * @return The factors.
   */
  public double[] getFixingSubperiodAccrualFactors() {
    return _fixingSubperiodAccrualFactors;
  }

  /**
   * Returns the spread.
   * @return the spread
   */
  public double getSpread() {
    return _spread;
  }

  /**
   * Returns the rate of the first compounded period.
   * @return the rate of the first compounded period.
   */
  public double getInitialRate() {
    return _initialRate;
  }

  @Override
  public CouponIborCompoundingSimpleSpread toDerivative(final ZonedDateTime dateTime) {
    final LocalDate dateConversion = dateTime.toLocalDate();
    ArgumentChecker.isTrue(!dateConversion.isAfter(_fixingDates[0].toLocalDate()), "toDerivative without time series should have a date before the first fixing date.");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final double[] fixingTimes = TimeCalculator.getTimeBetween(dateTime, _fixingDates);
    final double[] fixingPeriodStartTimes = TimeCalculator.getTimeBetween(dateTime, _fixingSubperiodStartDates);
    final double[] fixingPeriodEndTimes = TimeCalculator.getTimeBetween(dateTime, _fixingSubperiodEndDates);
    return new CouponIborCompoundingSimpleSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getNotional(), _index, _subperiodAccrualFactors,
        fixingTimes, fixingPeriodStartTimes, fixingPeriodEndTimes, _fixingSubperiodAccrualFactors, _spread);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime dateTime, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    final LocalDate dateLocal = dateTime.toLocalDate();
    ArgumentChecker.notNull(indexFixingTimeSeries, "Index fixing time series");
    ArgumentChecker.isTrue(!dateLocal.isAfter(getPaymentDate().toLocalDate()), "date is after payment date");
    final double paymentTime = TimeCalculator.getTimeBetween(dateTime, getPaymentDate());
    final int nbSubPeriods = _fixingDates.length;
    int nbFixed = 0;
    if (!Double.isNaN(_initialRate)) { // Force the initial rate to be used
      nbFixed++;
    }
    while ((nbFixed < nbSubPeriods) && (dateLocal.isAfter(_fixingDates[nbFixed].toLocalDate()))) { // If fixing is strictly before today, period has fixed
      nbFixed++;
    }
    if ((nbFixed < nbSubPeriods) && (dateLocal.equals(_fixingDates[nbFixed].toLocalDate()))) { // Not all periods already fixed, checking if todays fixing is available
      final ZonedDateTime rezonedFixingDateNext = ZonedDateTime.of(LocalDateTime.of(_fixingDates[nbFixed].toLocalDate(), LocalTime.of(0, 0)), ZoneOffset.UTC);
      final Double fixedRate = indexFixingTimeSeries.getValue(rezonedFixingDateNext);
      if (fixedRate != null) {
        nbFixed++;
      }
    }
    double[] rateFixed = new double[nbFixed];
    double cpa = 1.0d;
    for (int loopsub = 0; loopsub < nbFixed; loopsub++) {
      if (!Double.isNaN(_initialRate) && loopsub == 0) {
        cpa *= 1.0d + _initialRate * _subperiodAccrualFactors[0];
      } else {
        final Double fixing;
        final ZonedDateTime rezonedFixingDate = ZonedDateTime.of(LocalDateTime.of(_fixingDates[loopsub].toLocalDate(), LocalTime.of(0, 0)), ZoneOffset.UTC);
        fixing = indexFixingTimeSeries.getValue(rezonedFixingDate);
        if (fixing == null) {
          throw new OpenGammaRuntimeException("Could not get fixing value for date " + rezonedFixingDate);
        }
        rateFixed[loopsub] = fixing;
        cpa *= 1.0d + fixing * _subperiodAccrualFactors[loopsub];
      }
    }
    if (nbFixed == nbSubPeriods) { // All dates already fixed: CouponFixed
      double rate = (cpa - 1.0d) / getPaymentYearFraction() + _spread;
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), rate, getAccrualStartDate(), getAccrualEndDate());
    }
    // Copying the remaining periods
    final int nbSubPeriodLeft = nbSubPeriods - nbFixed;
    final double[] paymentAccrualFactorsLeft = new double[nbSubPeriodLeft];
    System.arraycopy(_subperiodAccrualFactors, nbFixed, paymentAccrualFactorsLeft, 0, nbSubPeriodLeft);
    final double[] fixingTimesLeft = new double[nbSubPeriodLeft];
    System.arraycopy(TimeCalculator.getTimeBetween(dateTime, _fixingDates), nbFixed, fixingTimesLeft, 0, nbSubPeriodLeft);
    final double[] fixingPeriodStartTimesLeft = new double[nbSubPeriodLeft];
    System.arraycopy(TimeCalculator.getTimeBetween(dateTime, _fixingSubperiodStartDates), nbFixed, fixingPeriodStartTimesLeft, 0, nbSubPeriodLeft);
    final double[] fixingPeriodEndTimesLeft = new double[nbSubPeriodLeft];
    System.arraycopy(TimeCalculator.getTimeBetween(dateTime, _fixingSubperiodEndDates), nbFixed, fixingPeriodEndTimesLeft, 0, nbSubPeriodLeft);
    final double[] fixingPeriodAccrualFactorsLeft = new double[nbSubPeriodLeft];
    System.arraycopy(_fixingSubperiodAccrualFactors, nbFixed, fixingPeriodAccrualFactorsLeft, 0, nbSubPeriodLeft);
    return new CouponIborCompoundingSimpleSpread(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), getNotional() * cpa, _index, paymentAccrualFactorsLeft,
        fixingTimesLeft, fixingPeriodStartTimesLeft, fixingPeriodEndTimesLeft, fixingPeriodAccrualFactorsLeft, _spread);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborCompoundingSimpleSpreadDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponIborCompoundingSimpleSpreadDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingDates);
    result = prime * result + Arrays.hashCode(_fixingSubperiodAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingSubperiodEndDates);
    result = prime * result + Arrays.hashCode(_fixingSubperiodStartDates);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_spread);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + Arrays.hashCode(_subperiodAccrualEndDates);
    result = prime * result + Arrays.hashCode(_subperiodAccrualFactors);
    result = prime * result + Arrays.hashCode(_subperiodAccrualStartDates);
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
    CouponIborCompoundingSimpleSpreadDefinition other = (CouponIborCompoundingSimpleSpreadDefinition) obj;
    if (!Arrays.equals(_fixingDates, other._fixingDates)) {
      return false;
    }
    if (!Arrays.equals(_fixingSubperiodAccrualFactors, other._fixingSubperiodAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_fixingSubperiodEndDates, other._fixingSubperiodEndDates)) {
      return false;
    }
    if (!Arrays.equals(_fixingSubperiodStartDates, other._fixingSubperiodStartDates)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_spread) != Double.doubleToLongBits(other._spread)) {
      return false;
    }
    if (!Arrays.equals(_subperiodAccrualEndDates, other._subperiodAccrualEndDates)) {
      return false;
    }
    if (!Arrays.equals(_subperiodAccrualFactors, other._subperiodAccrualFactors)) {
      return false;
    }
    if (!Arrays.equals(_subperiodAccrualStartDates, other._subperiodAccrualStartDates)) {
      return false;
    }
    return true;
  }

}
