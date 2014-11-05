/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.payment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Coupon;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponFixed;
import com.opengamma.analytics.financial.interestrate.payments.derivative.CouponONArithmeticAverage;
import com.opengamma.analytics.financial.interestrate.payments.derivative.Payment;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Class describing a Fed Fund swap-like floating coupon (arithmetic average on overnight rates).
 */
public final class CouponONArithmeticAverageDefinition extends CouponDefinition implements InstrumentDefinitionWithData<Payment, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The overnight index on which the coupon fixes. The index currency should be the same as the coupon currency.
   */
  private final IndexON _index;
  /**
   * The start dates of the fixing periods. The length is the same as the number of periods.
   */
  private final ZonedDateTime[] _fixingPeriodStartDates;

  /**
   * The end dates of the fixing periods. The length is the same as the number of periods.
   */
  private final ZonedDateTime[] _fixingPeriodEndDates;

  /**
   * The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactors;

  /**
   * Constructor from all details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentAccrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param fixingPeriodStartDates The start dates of the fixing period.
   * @param fixingPeriodEndDates The end dates of the fixing period.
   * @param paymentAccrualFactors Accrual factors of the fixing periods.
   * @param calendar The holiday calendar for the overnight leg.
   */
  public CouponONArithmeticAverageDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IndexON index, final ZonedDateTime[] fixingPeriodStartDates, final ZonedDateTime[] fixingPeriodEndDates,
      final double[] paymentAccrualFactors, final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(index, "CouponArithmeticAverageONDefinition: index");
    ArgumentChecker.notNull(fixingPeriodStartDates, "CouponArithmeticAverageONDefinition: fixingPeriodStartDates");
    ArgumentChecker.notNull(fixingPeriodEndDates, "CouponArithmeticAverageONDefinition: fixingPeriodEndDates");
    ArgumentChecker.notNull(paymentAccrualFactors, "CouponArithmeticAverageONDefinition: paymentAccrualFactors");
    ArgumentChecker.isTrue(fixingPeriodStartDates.length == fixingPeriodEndDates.length, "fixingPeriodStartDates and fixingPeriodEndDates should have the same length");
    ArgumentChecker.isTrue(paymentAccrualFactors.length == fixingPeriodEndDates.length, "paymentAccrualFactors and fixingPeriodEndDates should have the same length");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Coupon and index currencies are not compatible. Expected to be the same");
    _index = index;
    _fixingPeriodStartDates = fixingPeriodStartDates;
    _fixingPeriodEndDates = fixingPeriodEndDates;
    _fixingPeriodAccrualFactors = paymentAccrualFactors;
  }

  /**
   * Constructor from all the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentAccrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param fixingPeriodEndDate The end date of the fixing period.
   * @param calendar The holiday calendar for the overnight leg.
   */
  public CouponONArithmeticAverageDefinition(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate,
      final Calendar calendar) {
    super(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional);
    ArgumentChecker.notNull(index, "CouponArithmeticAverageONDefinition: index");
    ArgumentChecker.notNull(fixingPeriodStartDate, "CouponArithmeticAverageONDefinition: fixingPeriodStartDate");
    ArgumentChecker.notNull(fixingPeriodEndDate, "CouponArithmeticAverageONDefinition: fixingPeriodEndDate");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Coupon and index currencies are not compatible. Expected to be the same");
    _index = index;
    final List<ZonedDateTime> fixingStartDateList = new ArrayList<>();
    final List<ZonedDateTime> fixingEndDateList = new ArrayList<>();
    final List<Double> fixingAccrualFactorList = new ArrayList<>();
    ZonedDateTime currentDate = fixingPeriodStartDate;
    fixingStartDateList.add(currentDate);
    ZonedDateTime nextDate;
    while (currentDate.isBefore(fixingPeriodEndDate)) {
      nextDate = ScheduleCalculator.getAdjustedDate(currentDate, 1, calendar);
      fixingStartDateList.add(nextDate);
      fixingEndDateList.add(nextDate);
      fixingAccrualFactorList.add(index.getDayCount().getDayCountFraction(currentDate, nextDate, calendar));
      currentDate = nextDate;
    }
    fixingStartDateList.remove(fixingPeriodEndDate);
    _fixingPeriodStartDates = fixingStartDateList.toArray(new ZonedDateTime[fixingStartDateList.size()]);
    _fixingPeriodEndDates = fixingEndDateList.toArray(new ZonedDateTime[fixingEndDateList.size()]);
    _fixingPeriodAccrualFactors = ArrayUtils.toPrimitive(fixingAccrualFactorList.toArray(new Double[fixingAccrualFactorList.size()]));
  }

  /**
   * Constructor with the rate cut off (the last n=rateCutoff fixings are the same) from all the coupon details.
   * @param currency The payment currency.
   * @param paymentDate Coupon payment date.
   * @param accrualStartDate Start date of the accrual period.
   * @param accrualEndDate End date of the accrual period.
   * @param paymentAccrualFactor Accrual factor of the accrual period.
   * @param notional Coupon notional.
   * @param index The OIS-like index on which the coupon fixes.
   * @param fixingPeriodStartDate The start date of the fixing period.
   * @param fixingPeriodEndDate The end date of the fixing period.
   * @param calendar The holiday calendar for the overnight leg.
   * @param  rateCutoff The rate cut off should be bigger than 1,and smaller than the number of period (which is the number of open days between the two fixing periods). 1 is for the normal case.
   * @return The OIS coupon.
   */
  public static CouponONArithmeticAverageDefinition withRateCutOff(final Currency currency, final ZonedDateTime paymentDate, final ZonedDateTime accrualStartDate, final ZonedDateTime accrualEndDate,
      final double paymentAccrualFactor, final double notional, final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate,
      final Calendar calendar, final int rateCutoff) {
    ArgumentChecker.notNull(index, "CouponArithmeticAverageONDefinition: index");
    ArgumentChecker.notNull(fixingPeriodStartDate, "CouponArithmeticAverageONDefinition: fixingPeriodStartDate");
    ArgumentChecker.notNull(fixingPeriodEndDate, "CouponArithmeticAverageONDefinition: fixingPeriodEndDate");
    ArgumentChecker.isTrue(currency.equals(index.getCurrency()), "Coupon and index currencies are not compatible. Expected to be the same");
    ArgumentChecker.isTrue(rateCutoff >= 1, "");
    if (rateCutoff == 1) {
      return new CouponONArithmeticAverageDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, index, fixingPeriodStartDate, fixingPeriodEndDate,
          calendar);
    }
    final List<ZonedDateTime> fixingStartDateList = new ArrayList<>();
    final List<ZonedDateTime> fixingEndDateList = new ArrayList<>();
    final List<Double> fixingAccrualFactorList = new ArrayList<>();
    ZonedDateTime currentDate = fixingPeriodStartDate;
    fixingStartDateList.add(currentDate);
    ZonedDateTime nextDate;
    final ZonedDateTime fixingPeriodEndDateMinusRateCutOff = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, -rateCutoff + 1, calendar);
    ArgumentChecker.isTrue(fixingPeriodEndDateMinusRateCutOff.isAfter(fixingPeriodStartDate), "");
    while (currentDate.isBefore(fixingPeriodEndDateMinusRateCutOff)) {
      nextDate = ScheduleCalculator.getAdjustedDate(currentDate, 1, calendar);
      fixingStartDateList.add(nextDate);
      fixingEndDateList.add(nextDate);
      fixingAccrualFactorList.add(index.getDayCount().getDayCountFraction(currentDate, nextDate, calendar));
      currentDate = nextDate;
    }
    fixingStartDateList.remove(fixingPeriodEndDateMinusRateCutOff);
    for (int i = 0; i < rateCutoff - 1; i++) {
      fixingStartDateList.add(ScheduleCalculator.getAdjustedDate(currentDate, -1, calendar));
      fixingEndDateList.add(currentDate);
    }
    final ZonedDateTime[] fixingPeriodStartDates = fixingStartDateList.toArray(new ZonedDateTime[fixingStartDateList.size()]);
    final ZonedDateTime[] fixingPeriodEndDates = fixingEndDateList.toArray(new ZonedDateTime[fixingEndDateList.size()]);
    final double[] fixingPeriodAccrualFactors = ArrayUtils.toPrimitive(fixingAccrualFactorList.toArray(new Double[fixingAccrualFactorList.size()]));
    return new CouponONArithmeticAverageDefinition(currency, paymentDate, accrualStartDate, accrualEndDate, paymentAccrualFactor, notional, index, fixingPeriodStartDates, fixingPeriodEndDates,
        fixingPeriodAccrualFactors, calendar);
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing and the payment (also called payment delay).
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONArithmeticAverageDefinition from(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final Period tenor, final double notional, final int paymentLag,
      final BusinessDayConvention businessDayConvention, final boolean isEOM, final Calendar calendar) {
    ArgumentChecker.notNull(index, "Index");
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, tenor, businessDayConvention, calendar, isEOM);
    return from(index, fixingPeriodStartDate, fixingPeriodEndDate, notional, paymentLag, calendar);
  }

  /**
   * Builder from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one of the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param fixingPeriodEndDate The last date of the fixing period. Interest accrues up to this date. If publicationLag==0, 1 day following publication. If lag==1, the publication date.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing and the payment (also called payment delay).
   * @param calendar The holiday calendar for the overnight index.
   * @return The OIS coupon.
   */
  public static CouponONArithmeticAverageDefinition from(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final double notional,
      final int paymentLag, final Calendar calendar) {
    ArgumentChecker.notNull(fixingPeriodEndDate, "Fixing Period End Date");
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, -1 + index.getPublicationLag() + paymentLag, calendar);
    final double paymentAccrualFactor = index.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate, calendar);
    return new CouponONArithmeticAverageDefinition(index.getCurrency(), paymentDate, fixingPeriodStartDate, fixingPeriodEndDate, paymentAccrualFactor,
        notional, index, fixingPeriodStartDate, fixingPeriodEndDate, calendar);
  }

  /**
   * Builder with the rate cut off (the last n=rateCutoff fixings are the same) from financial details. The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one for the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param tenor The coupon tenor.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing and the payment (also called payment delay).
   * @param businessDayConvention The business day convention to compute the end date of the coupon.
   * @param isEOM The end-of-month convention to compute the end date of the coupon.
   * @param calendar The holiday calendar for the overnight index.
   * @param  rateCutOff The rate cut off should be bigger than 1,and smaller than the number of period (which is the number of open days between the two fixing periods)
   * @return The OIS coupon.
   */
  public static CouponONArithmeticAverageDefinition withRateCutOff(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final Period tenor, final double notional, final int paymentLag,
      final BusinessDayConvention businessDayConvention, final boolean isEOM, final Calendar calendar, final int rateCutOff) {
    ArgumentChecker.notNull(index, "Index");
    final ZonedDateTime fixingPeriodEndDate = ScheduleCalculator.getAdjustedDate(fixingPeriodStartDate, tenor, businessDayConvention, calendar, isEOM);
    return withRateCutOff(index, fixingPeriodStartDate, fixingPeriodEndDate, notional, paymentLag, calendar, rateCutOff);
  }

  /**
   * Builder with the rate cut off (the last n=rateCutoff fixings are the same) from financial details. 
   * The accrual and fixing start and end dates are the same. The day count for the payment is the same as the one of the index.
   * The payment date is adjusted by the publication lag and the settlement days.
   * @param index The OIS index.
   * @param fixingPeriodStartDate The coupon settlement date and start of the fixing period.
   * @param fixingPeriodEndDate The last date of the fixing period. Interest accrues up to this date. If publicationLag==0, 1 day following publication. If lag==1, the publication date.
   * @param notional The notional.
   * @param paymentLag The number of days between last fixing and the payment (also called payment delay).
   * @param calendar The holiday calendar for the overnight index.
   * @param rateCutOff The rate cut off should be bigger than 1 ,and smaller than the number of period (which is the number of open days between the two fixing periods)
   * @return The OIS coupon.
   */
  public static CouponONArithmeticAverageDefinition withRateCutOff(final IndexON index, final ZonedDateTime fixingPeriodStartDate, final ZonedDateTime fixingPeriodEndDate, final double notional,
      final int paymentLag, final Calendar calendar, final int rateCutOff) {
    ArgumentChecker.notNull(fixingPeriodEndDate, "Fixing Period End Date");
    final ZonedDateTime paymentDate = ScheduleCalculator.getAdjustedDate(fixingPeriodEndDate, -1 + index.getPublicationLag() + paymentLag, calendar);
    final double paymentAccrualFactor = index.getDayCount().getDayCountFraction(fixingPeriodStartDate, fixingPeriodEndDate, calendar);
    return withRateCutOff(index.getCurrency(), paymentDate, fixingPeriodStartDate, fixingPeriodEndDate, paymentAccrualFactor,
        notional, index, fixingPeriodStartDate, fixingPeriodEndDate, calendar, rateCutOff);
  }

  /**
   * Gets the OIS index of the instrument.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the start dates of the fixing periods. 
   * @return The start dates of the fixing periods.
   */
  public ZonedDateTime[] getFixingPeriodStartDates() {
    return _fixingPeriodStartDates;
  }

  /**
   * Gets the end dates of the fixing periods. 
   * @return The end dates of the fixing periods.
   */
  public ZonedDateTime[] getFixingPeriodEndDates() {
    return _fixingPeriodEndDates;
  }

  // TODO : this should be remove when all overnight coupon will be coherent (ie with two different vectors for fixing start dates and fixing end dates)
  /**
   * Gets the dates of the fixing periods (start and end). There is one date more than period.
   * @return The dates of the fixing periods.
   */
  public ZonedDateTime[] getFixingPeriodDates() {
    final ZonedDateTime[] dates = new ZonedDateTime[_fixingPeriodEndDates.length + 1];
    dates[0] = _fixingPeriodStartDates[0];
    for (int i = 0; i < _fixingPeriodEndDates.length; i++) {
      dates[i + 1] = _fixingPeriodEndDates[i];
    }
    return dates;
  }

  /**
   * Gets the accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @return The accrual factors.
   */
  public double[] getFixingPeriodAccrualFactors() {
    return _fixingPeriodAccrualFactors;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public Coupon toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    return toDerivative(valZdt, indexFixingTimeSeries);
  }

  @Override
  public CouponONArithmeticAverage toDerivative(final ZonedDateTime date) {
    ArgumentChecker.isTrue(!_fixingPeriodStartDates[0].plusDays(_index.getPublicationLag()).isBefore(date), "First fixing publication strictly before reference date");
    final double paymentTime = TimeCalculator.getTimeBetween(date, getPaymentDate());
    final double[] fixingPeriodStartTimes = TimeCalculator.getTimeBetween(date, _fixingPeriodStartDates);
    final double[] fixingPeriodEndTimes = TimeCalculator.getTimeBetween(date, _fixingPeriodEndDates);
    return CouponONArithmeticAverage.from(paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodStartTimes, fixingPeriodEndTimes, _fixingPeriodAccrualFactors, 0);
  }

  @Override
  public Coupon toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(valZdt, "valZdt - valuation date as ZonedDateTime");
    final LocalDate valDate = valZdt.toLocalDate();
    ArgumentChecker.isTrue(!valDate.isAfter(getPaymentDate().toLocalDate()), "valuation date is after payment date");
    final LocalDate firstPublicationDate = _fixingPeriodStartDates[_index.getPublicationLag()].toLocalDate(); // This is often one business day following the first fixing date
    if (valDate.isBefore(firstPublicationDate)) {
      return toDerivative(valZdt);
    }

    // FIXME Historical time series do not have time information to begin with.
    final ZonedDateTime[] instants = indexFixingTimeSeries.timesArray();
    final LocalDate[] dates = new LocalDate[indexFixingTimeSeries.size()];
    for (int i = 0; i < instants.length; i++) {
      dates[i] = instants[i].toLocalDate();
    }
    final LocalDateDoubleTimeSeries indexFixingDateSeries = ImmutableLocalDateDoubleTimeSeries.of(dates, indexFixingTimeSeries.valuesArray());

    // Accrued rate for fixings before today; up to and including yesterday
    int fixedPeriod = 0;
    double accruedRate = 0.0;
    while ((fixedPeriod < _fixingPeriodStartDates.length) && valDate.isAfter(_fixingPeriodEndDates[fixedPeriod + _index.getPublicationLag() - 1].toLocalDate())) {

      final LocalDate currentDate = _fixingPeriodStartDates[fixedPeriod].toLocalDate();
      final Double fixedRate = indexFixingDateSeries.getValue(currentDate);

      if (fixedRate == null) {
        final LocalDate latestDate = indexFixingDateSeries.getLatestTime();
        throw new OpenGammaRuntimeException("Could not get fixing value of index " + _index.getName() + " for date " + currentDate + ". The last data is available on " + latestDate);
      }
      accruedRate += _fixingPeriodAccrualFactors[fixedPeriod] * fixedRate;
      fixedPeriod++;
    }

    final double paymentTime = TimeCalculator.getTimeBetween(valZdt, getPaymentDate());
    if (fixedPeriod < _fixingPeriodStartDates.length) { // Some OIS period left
      // Check to see if a fixing is available on current date
      final Double fixedRate = indexFixingDateSeries.getValue(_fixingPeriodStartDates[fixedPeriod].toLocalDate());
      if (fixedRate != null) { // There is!
        accruedRate += _fixingPeriodAccrualFactors[fixedPeriod] * fixedRate;
        fixedPeriod++;
      }
      if (fixedPeriod < _fixingPeriodStartDates.length) { // More OIS period left
        final double[] fixingAccrualFactorsLeft = new double[_fixingPeriodAccrualFactors.length - fixedPeriod];
        final double[] fixingPeriodStartTimes = new double[_fixingPeriodStartDates.length - fixedPeriod];
        final double[] fixingPeriodEndTimes = new double[_fixingPeriodEndDates.length - fixedPeriod];

        for (int i = 0; i < _fixingPeriodStartDates.length - fixedPeriod; i++) {
          fixingPeriodStartTimes[i] = TimeCalculator.getTimeBetween(valZdt, _fixingPeriodStartDates[i + fixedPeriod]);
          fixingPeriodEndTimes[i] = TimeCalculator.getTimeBetween(valZdt, _fixingPeriodEndDates[i + fixedPeriod]);

        }

        for (int loopperiod = 0; loopperiod < _fixingPeriodAccrualFactors.length - fixedPeriod; loopperiod++) {
          fixingAccrualFactorsLeft[loopperiod] = _fixingPeriodAccrualFactors[loopperiod + fixedPeriod];
        }
        final CouponONArithmeticAverage cpn = CouponONArithmeticAverage.from(paymentTime, getPaymentYearFraction(), getNotional(), _index, fixingPeriodStartTimes,
            fixingPeriodEndTimes, fixingAccrualFactorsLeft, accruedRate);
        return cpn;
      }
      return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), accruedRate / getPaymentYearFraction());

    }

    // All fixed already
    return new CouponFixed(getCurrency(), paymentTime, getPaymentYearFraction(), getNotional(), accruedRate / getPaymentYearFraction());
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageONDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitCouponArithmeticAverageONDefinition(this);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactors);
    result = prime * result + Arrays.hashCode(_fixingPeriodEndDates);
    result = prime * result + Arrays.hashCode(_fixingPeriodStartDates);
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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
    final CouponONArithmeticAverageDefinition other = (CouponONArithmeticAverageDefinition) obj;
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
    return true;
  }

}
