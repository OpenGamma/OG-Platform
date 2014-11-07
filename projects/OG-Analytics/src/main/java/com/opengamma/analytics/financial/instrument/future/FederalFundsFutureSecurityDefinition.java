/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.future;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an Federal Funds Futures security.
 */
public class FederalFundsFutureSecurityDefinition extends FuturesSecurityDefinition<FederalFundsFutureSecurity>
    implements InstrumentDefinitionWithData<FederalFundsFutureSecurity, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The OIS-like index on which the future fixes.
   */
  private final IndexON _index;
  /**
   * The dates of the fixing periods (start and end). There is one date more than period.
   */
  private final ZonedDateTime[] _fixingPeriodDates;
  /**
   * The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   */
  private final double[] _fixingPeriodAccrualFactor;
  /**
   * The total accrual factor for all fixing periods. Sum of the elements of _fixingPeriodAccrualFactor.
   */
  private double _fixingTotalAccrualFactor;
  /**
   * The future notional.
   */
  private final double _notional;
  /**
   * The future margining accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   */
  private final double _marginAccrualFactor;
  /**
   * The future name.
   */
  private final String _name;

  /**
   * The preceding business day convention.
   */
  private static final BusinessDayConvention BUSINESS_DAY_PRECEDING = BusinessDayConventions.PRECEDING;
  /**
   * The following business day convention.
   */
  private static final BusinessDayConvention BUSINESS_DAY_FOLLOWING = BusinessDayConventions.FOLLOWING;

  /** Constructor from all details.
   * @param lastTradingDate The future last trading date. Usually the last business day of the month.
   * @param index The OIS-like index on which the future fixes.
   * @param fixingPeriodDate The dates of the fixing periods (start and end). There is one date more than period.
   * @param fixingPeriodAccrualFactor The accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @param notional The future notional.
   * @param paymentAccrualFactor The future payment accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   * @param name The future name.
   */
  public FederalFundsFutureSecurityDefinition(final ZonedDateTime lastTradingDate, final IndexON index, final ZonedDateTime[] fixingPeriodDate,
      final double[] fixingPeriodAccrualFactor, final double notional, final double paymentAccrualFactor, final String name) {
    super(lastTradingDate);
    ArgumentChecker.notNull(lastTradingDate, "Last trading date");
    ArgumentChecker.notNull(index, "Index overnight");
    ArgumentChecker.notNull(fixingPeriodDate, "Fixing period dates");
    ArgumentChecker.notNull(fixingPeriodAccrualFactor, "Fixing period accrual factors");
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.isTrue(fixingPeriodDate.length == fixingPeriodAccrualFactor.length + 1, "Fixing dates length should be fixing accrual factors + 1.");
    _index = index;
    _fixingPeriodDates = fixingPeriodDate;
    _fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    _notional = notional;
    _marginAccrualFactor = paymentAccrualFactor;
    _name = name;
    _fixingTotalAccrualFactor = 0.0;
    for (final double element : _fixingPeriodAccrualFactor) {
      _fixingTotalAccrualFactor += element;
    }
  }

  /**
   * Builder for a given month. The future start on the first business day of the month and ends on the first business day of the next month.
   * The last trading date is the last good business day of the month.
   * @param monthDate Any date in the future month.
   * @param index The overnight index.
   * @param notional The future notional.
   * @param paymentAccrualFactor The future payment accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   * @param name The future name.
   * @param calendar The holiday calendar for the overnight rate.
   * @return The future.
   */
  public static FederalFundsFutureSecurityDefinition from(final ZonedDateTime monthDate, final IndexON index, final double notional, final double paymentAccrualFactor,
      final String name, final Calendar calendar) {
    ArgumentChecker.notNull(monthDate, "Reference date");
    ArgumentChecker.notNull(index, "Index overnight");
    final ZonedDateTime periodFirstDate = BUSINESS_DAY_FOLLOWING.adjustDate(calendar, monthDate.withDayOfMonth(1));
    final ZonedDateTime periodLastDate = BUSINESS_DAY_FOLLOWING.adjustDate(calendar, monthDate.withDayOfMonth(1).plusMonths(1));
    final ZonedDateTime last = BUSINESS_DAY_PRECEDING.adjustDate(calendar, periodLastDate.minusDays(1));
    final List<ZonedDateTime> fixingList = new ArrayList<>();
    ZonedDateTime date = periodFirstDate;
    while (!date.isAfter(periodLastDate)) {
      fixingList.add(date);
      date = BUSINESS_DAY_FOLLOWING.adjustDate(calendar, date.plusDays(1));
    }
    final ZonedDateTime[] fixingDate = fixingList.toArray(new ZonedDateTime[fixingList.size()]);
    final double[] fixingAccrualFactor = new double[fixingDate.length - 1];
    for (int loopfix = 0; loopfix < fixingDate.length - 1; loopfix++) {
      fixingAccrualFactor[loopfix] = index.getDayCount().getDayCountFraction(fixingDate[loopfix], fixingDate[loopfix + 1], calendar);
    }
    return new FederalFundsFutureSecurityDefinition(last, index, fixingDate, fixingAccrualFactor, notional, paymentAccrualFactor, name);
  }

  /**
   * Builder of the CBOT Federal Funds futures for a given month. The future start on the first business day of the month and ends on the first business day of the next month.
   * The last trading date is the last good business day of the month. The notional is 5m. The payment accrual fraction is 1/12. The name is "FF" + month in format "MMMYY".
   * @param monthDate Any date in the future month.
   * @param index The overnight index.
   * @param calendar The holiday calendar for the overnight rate.
   * @return The future.
   */
  public static FederalFundsFutureSecurityDefinition fromFedFund(final ZonedDateTime monthDate, final IndexON index, final Calendar calendar) {
    final double notionalFedFund = 5000000;
    final double accrualFedFund = 1.0 / 12.0;
    return from(monthDate, index, notionalFedFund, accrualFedFund, "FF" + monthDate.toString(DateTimeFormatter.ofPattern("MMMyy")), calendar);
  }

  /**
   * Gets the OIS-like index on which the future fixes.
   * @return The index.
   */
  public IndexON getIndex() {
    return _index;
  }

  /**
   * Gets the dates of the fixing periods (start and end). There is one date more than period.
   * @return The dates.
   */
  public ZonedDateTime[] getFixingPeriodDate() {
    return _fixingPeriodDates;
  }

  /**
   * Gets the accrual factors (or year fractions) associated to the fixing periods in the Index day count convention.
   * @return The accrual factors.
   */
  public double[] getFixingPeriodAccrualFactor() {
    return _fixingPeriodAccrualFactor;
  }

  /**
   * Gets the total accrual factor for all fixing periods.
   * @return The accrual factor.
   */
  public double getFixingTotalAccrualFactor() {
    return _fixingTotalAccrualFactor;
  }

  /**
   * Gets the future notional.
   * @return The notional.
   */
  public double getNotional() {
    return _notional;
  }

  /**
   * Gets the future payment accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   * @return The payment accrual factor.
   */
  public double getMarginAccrualFactor() {
    return _marginAccrualFactor;
  }

  /**
   * Gets the future name.
   * @return The future name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Gets the future currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _index.getCurrency();
  }

  @Override
  public String toString() {
    return _name + " - index: " + _index.toString() + " - start date: " + _fixingPeriodDates[0].toString(DateTimeFormatter.ofPattern("ddMMMyy"));
  }

  @Override
  public FederalFundsFutureSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "Date");
    ArgumentChecker.isTrue(!date.isAfter(_fixingPeriodDates[_index.getPublicationLag()]), "Date should not be after the fixing period start date");
    final double lastTradingTime = TimeCalculator.getTimeBetween(date, getLastTradingDate());
    final double[] fixingPeriodTime = TimeCalculator.getTimeBetween(date, _fixingPeriodDates);
    return new FederalFundsFutureSecurity(_index, 0.0, fixingPeriodTime, lastTradingTime, _fixingPeriodAccrualFactor, _fixingTotalAccrualFactor, _notional, _marginAccrualFactor, _name);
  }

  /**
   * {@inheritDoc}
   * @param indexFixingTimeSeries The time series of the ON index. It is used if the date is in the future month.
   * The date of the time series is the start fixing period date.
   */
  @Override
  public FederalFundsFutureSecurity toDerivative(final ZonedDateTime valZdt, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(valZdt, "valZdt - valuation date as ZonedDateTime");
    final LocalDate valDate = valZdt.toLocalDate();
    ArgumentChecker.isTrue(!valDate.isAfter(_fixingPeriodDates[_fixingPeriodDates.length - 1].toLocalDate()), "valuation date is after last trading date");
    final LocalDate firstPublicationDate = _fixingPeriodDates[_index.getPublicationLag()].toLocalDate(); // This is often one business day following the first fixing date
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

    final double lastTradingTime = TimeCalculator.getTimeBetween(valZdt, getLastTradingDate());
    int fixedPeriod = 0;
    double accruedInterest = 0.0;
    while ((fixedPeriod < _fixingPeriodDates.length - 1) && valDate.isAfter(_fixingPeriodDates[fixedPeriod + _index.getPublicationLag()].toLocalDate())) {
      final LocalDate currentDate = _fixingPeriodDates[fixedPeriod].toLocalDate();
      // Fixing should have taken place already
      final Double fixedRate = indexFixingDateSeries.getValue(currentDate);
      if (fixedRate == null) {
        final LocalDate latestDate = indexFixingDateSeries.getLatestTime();
        throw new OpenGammaRuntimeException("Could not get fixing value of index " + _index.getName() + " for date " + currentDate + ". The last data is available on " + latestDate);
      }
      accruedInterest += _fixingPeriodAccrualFactor[fixedPeriod] * fixedRate;
      fixedPeriod++;
    }
    if (fixedPeriod < _fixingPeriodDates.length - 1) { // Some FF period left
      final Double fixedRate = indexFixingTimeSeries.getValue(_fixingPeriodDates[fixedPeriod]);
      if (fixedRate != null) { // Fixed already
        accruedInterest += _fixingPeriodAccrualFactor[fixedPeriod] * fixedRate;
        fixedPeriod++;
      }
      if (fixedPeriod < _fixingPeriodDates.length - 1) { // Some FF period left
        final double[] fixingPeriodTime = new double[_fixingPeriodDates.length - fixedPeriod];
        final double[] fixingPeriodAccrualFactor = new double[_fixingPeriodDates.length - 1 - fixedPeriod];
        for (int loopfix = 0; loopfix < _fixingPeriodDates.length - fixedPeriod; loopfix++) {
          fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(valZdt, _fixingPeriodDates[loopfix + fixedPeriod]);
        }
        System.arraycopy(_fixingPeriodAccrualFactor, fixedPeriod, fixingPeriodAccrualFactor, 0, _fixingPeriodDates.length - 1 - fixedPeriod);
        return new FederalFundsFutureSecurity(_index, accruedInterest, fixingPeriodTime, lastTradingTime, fixingPeriodAccrualFactor, _fixingTotalAccrualFactor, _notional,
            _marginAccrualFactor, _name);
      }
      return new FederalFundsFutureSecurity(_index, accruedInterest, new double[] {TimeCalculator.getTimeBetween(valZdt, _fixingPeriodDates[_fixingPeriodDates.length - 1]) },
          lastTradingTime, new double[0], _fixingTotalAccrualFactor, _notional, _marginAccrualFactor, _name); // Only one period left
    }
    return new FederalFundsFutureSecurity(_index, accruedInterest, new double[] {TimeCalculator.getTimeBetween(valZdt, _fixingPeriodDates[_fixingPeriodDates.length - 1]) },
        lastTradingTime, new double[0], _fixingTotalAccrualFactor, _notional, _marginAccrualFactor, _name); // Only one period left
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFederalFundsFutureSecurityDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactor);
    result = prime * result + Arrays.hashCode(_fixingPeriodDates);
    long temp;
    temp = Double.doubleToLongBits(_fixingTotalAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _index.hashCode();
    temp = Double.doubleToLongBits(_marginAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _name.hashCode();
    temp = Double.doubleToLongBits(_notional);
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
    FederalFundsFutureSecurityDefinition other = (FederalFundsFutureSecurityDefinition) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactor, other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodDates, other._fixingPeriodDates)) {
      return false;
    }
    if (Double.doubleToLongBits(_fixingTotalAccrualFactor) != Double.doubleToLongBits(other._fixingTotalAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (Double.doubleToLongBits(_marginAccrualFactor) != Double.doubleToLongBits(other._marginAccrualFactor)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    return true;
  }

}
