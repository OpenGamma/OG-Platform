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
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Description of an Federal Funds Futures security.
 */
public class FederalFundsFutureSecurityDefinition implements InstrumentDefinitionWithData<FederalFundsFutureSecurity, DoubleTimeSeries<ZonedDateTime>> {

  /**
   * The future last trading date. Usually the last business day of the month.
   */
  private final ZonedDateTime _lastTradingDate;
  /**
   * The OIS-like index on which the future fixes.
   */
  private final IndexON _index;
  /**
   * The dates of the fixing periods (start and end). There is one date more than period.
   */
  private final ZonedDateTime[] _fixingPeriodDate;
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
  private static final BusinessDayConvention BUSINESS_DAY_PRECEDING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding");
  /**
   * The following business day convention.
   */
  private static final BusinessDayConvention BUSINESS_DAY_FOLLOWING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");

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
    ArgumentChecker.notNull(lastTradingDate, "Last trading date");
    ArgumentChecker.notNull(index, "Index overnight");
    ArgumentChecker.notNull(fixingPeriodDate, "Fixing period dates");
    ArgumentChecker.notNull(fixingPeriodAccrualFactor, "Fixing period accrual factors");
    ArgumentChecker.notNull(name, "Name");
    ArgumentChecker.isTrue(fixingPeriodDate.length == fixingPeriodAccrualFactor.length + 1, "Fixing dates length should be fixing accrual factors + 1.");
    _lastTradingDate = lastTradingDate;
    _index = index;
    _fixingPeriodDate = fixingPeriodDate;
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
   * Gets the future last trading date. Usually the last business day of the month.
   * @return The date.
   */
  public ZonedDateTime getLastTradingDate() {
    return _lastTradingDate;
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
    return _fixingPeriodDate;
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
    return _name + " - index: " + _index.toString() + " - start date: " + _fixingPeriodDate[0].toString(DateTimeFormatter.ofPattern("ddMMMyy"));
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public FederalFundsFutureSecurity toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "Date");
    ArgumentChecker.isTrue(!date.isAfter(_fixingPeriodDate[_index.getPublicationLag()]), "Date should not be after the fixing period start date");
    final double[] fixingPeriodTime = new double[_fixingPeriodDate.length];
    for (int loopfix = 0; loopfix < _fixingPeriodDate.length; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[loopfix]);
    }
    return new FederalFundsFutureSecurity(_index, 0.0, fixingPeriodTime, _fixingPeriodAccrualFactor, _fixingTotalAccrualFactor, _notional, _marginAccrualFactor, _name,
        yieldCurveNames[0]);
  }

  /**
   * @param indexFixingTimeSeries The time series of the ON index. It is used if the date is in the future month.
   * The date of the time series is the publication date (for Fed Funds, it is the end date of the period).
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public FederalFundsFutureSecurity toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries, final String... yieldCurveNames) {
    ArgumentChecker.notNull(date, "Date");
    if (date.isBefore(_fixingPeriodDate[1])) { // Fixing period not started
      return toDerivative(date, yieldCurveNames);
    }
    int fixedPeriod = 0;
    double accruedInterest = 0.0;
    while (date.isAfter(_fixingPeriodDate[fixedPeriod + _index.getPublicationLag()]) && fixedPeriod < _fixingPeriodDate.length - 1) {
      // Fixing should have taken place already
      final Double fixedRate = indexFixingTimeSeries.getValue(_fixingPeriodDate[fixedPeriod + _index.getPublicationLag()]);
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + _fixingPeriodDate[fixedPeriod]);
      }
      accruedInterest += _fixingPeriodAccrualFactor[fixedPeriod] * fixedRate;
      fixedPeriod++;
    }
    if (fixedPeriod < _fixingPeriodDate.length - 1) { // Some FF period left
      final Double fixedRate = indexFixingTimeSeries.getValue(_fixingPeriodDate[fixedPeriod + _index.getPublicationLag()]);
      if (fixedRate != null) { // Fixed already
        accruedInterest += _fixingPeriodAccrualFactor[fixedPeriod] * fixedRate;
        fixedPeriod++;
      }
      if (fixedPeriod < _fixingPeriodDate.length - 1) { // Some FF period left
        final double[] fixingPeriodTime = new double[_fixingPeriodDate.length - fixedPeriod];
        final double[] fixingPeriodAccrualFactor = new double[_fixingPeriodDate.length - 1 - fixedPeriod];
        for (int loopfix = 0; loopfix < _fixingPeriodDate.length - fixedPeriod; loopfix++) {
          fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[loopfix + fixedPeriod]);
        }
        System.arraycopy(_fixingPeriodAccrualFactor, fixedPeriod, fixingPeriodAccrualFactor, 0, _fixingPeriodDate.length - 1 - fixedPeriod);
        return new FederalFundsFutureSecurity(_index, accruedInterest, fixingPeriodTime, fixingPeriodAccrualFactor, _fixingTotalAccrualFactor, _notional,
            _marginAccrualFactor, _name, yieldCurveNames[0]);
      }
      return new FederalFundsFutureSecurity(_index, accruedInterest, new double[] {TimeCalculator.getTimeBetween(date, _fixingPeriodDate[_fixingPeriodDate.length - 1])},
          new double[0], _fixingTotalAccrualFactor, _notional, _marginAccrualFactor, _name, yieldCurveNames[0]);
    }
    return new FederalFundsFutureSecurity(_index, accruedInterest, new double[] {TimeCalculator.getTimeBetween(date, _fixingPeriodDate[_fixingPeriodDate.length - 1])},
        new double[0], _fixingTotalAccrualFactor, _notional, _marginAccrualFactor, _name, yieldCurveNames[0]);
  }

  @Override
  public FederalFundsFutureSecurity toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "Date");
    ArgumentChecker.isTrue(!date.isAfter(_fixingPeriodDate[_index.getPublicationLag()]), "Date should not be after the fixing period start date");
    final double[] fixingPeriodTime = new double[_fixingPeriodDate.length];
    for (int loopfix = 0; loopfix < _fixingPeriodDate.length; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[loopfix]);
    }
    return new FederalFundsFutureSecurity(_index, 0.0, fixingPeriodTime, _fixingPeriodAccrualFactor, _fixingTotalAccrualFactor, _notional, _marginAccrualFactor, _name);
  }

  /**
   * {@inheritDoc}
   * @param indexFixingTimeSeries The time series of the ON index. It is used if the date is in the future month.
   * The date of the time series is the publication date (for Fed Funds, it is the end date of the period).
   */
  @Override
  public FederalFundsFutureSecurity toDerivative(final ZonedDateTime date, final DoubleTimeSeries<ZonedDateTime> indexFixingTimeSeries) {
    ArgumentChecker.notNull(date, "Date");
    if (date.isBefore(_fixingPeriodDate[1])) { // Fixing period not started
      return toDerivative(date);
    }
    int fixedPeriod = 0;
    double accruedInterest = 0.0;
    while (date.isAfter(_fixingPeriodDate[fixedPeriod + _index.getPublicationLag()]) && fixedPeriod < _fixingPeriodDate.length - 1) {
      // Fixing should have taken place already
      final Double fixedRate = indexFixingTimeSeries.getValue(_fixingPeriodDate[fixedPeriod + _index.getPublicationLag()]);
      if (fixedRate == null) {
        throw new OpenGammaRuntimeException("Could not get fixing value for date " + _fixingPeriodDate[fixedPeriod]);
      }
      accruedInterest += _fixingPeriodAccrualFactor[fixedPeriod] * fixedRate;
      fixedPeriod++;
    }
    if (fixedPeriod < _fixingPeriodDate.length - 1) { // Some FF period left
      final Double fixedRate = indexFixingTimeSeries.getValue(_fixingPeriodDate[fixedPeriod + _index.getPublicationLag()]);
      if (fixedRate != null) { // Fixed already
        accruedInterest += _fixingPeriodAccrualFactor[fixedPeriod] * fixedRate;
        fixedPeriod++;
      }
      if (fixedPeriod < _fixingPeriodDate.length - 1) { // Some FF period left
        final double[] fixingPeriodTime = new double[_fixingPeriodDate.length - fixedPeriod];
        final double[] fixingPeriodAccrualFactor = new double[_fixingPeriodDate.length - 1 - fixedPeriod];
        for (int loopfix = 0; loopfix < _fixingPeriodDate.length - fixedPeriod; loopfix++) {
          fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[loopfix + fixedPeriod]);
        }
        System.arraycopy(_fixingPeriodAccrualFactor, fixedPeriod, fixingPeriodAccrualFactor, 0, _fixingPeriodDate.length - 1 - fixedPeriod);
        return new FederalFundsFutureSecurity(_index, accruedInterest, fixingPeriodTime, fixingPeriodAccrualFactor, _fixingTotalAccrualFactor, _notional,
            _marginAccrualFactor, _name);
      }
      return new FederalFundsFutureSecurity(_index, accruedInterest, new double[] {TimeCalculator.getTimeBetween(date, _fixingPeriodDate[_fixingPeriodDate.length - 1])},
          new double[0], _fixingTotalAccrualFactor, _notional, _marginAccrualFactor, _name);
    }
    return new FederalFundsFutureSecurity(_index, accruedInterest, new double[] {TimeCalculator.getTimeBetween(date, _fixingPeriodDate[_fixingPeriodDate.length - 1])},
        new double[0], _fixingTotalAccrualFactor, _notional, _marginAccrualFactor, _name);
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
    int result = 1;
    result = prime * result + Arrays.hashCode(_fixingPeriodAccrualFactor);
    result = prime * result + Arrays.hashCode(_fixingPeriodDate);
    result = prime * result + _index.hashCode();
    result = prime * result + _lastTradingDate.hashCode();
    result = prime * result + _name.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_notional);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_marginAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final FederalFundsFutureSecurityDefinition other = (FederalFundsFutureSecurityDefinition) obj;
    if (!Arrays.equals(_fixingPeriodAccrualFactor, other._fixingPeriodAccrualFactor)) {
      return false;
    }
    if (!Arrays.equals(_fixingPeriodDate, other._fixingPeriodDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_index, other._index)) {
      return false;
    }
    if (!ObjectUtils.equals(_lastTradingDate, other._lastTradingDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_name, other._name)) {
      return false;
    }
    if (Double.doubleToLongBits(_notional) != Double.doubleToLongBits(other._notional)) {
      return false;
    }
    if (Double.doubleToLongBits(_marginAccrualFactor) != Double.doubleToLongBits(other._marginAccrualFactor)) {
      return false;
    }
    return true;
  }

}
