/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.time.calendar.ZonedDateTime;
import javax.time.calendar.format.DateTimeFormatters;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.financial.interestrate.future.derivative.FederalFundsFutureSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;

/**
 * Description of an Federal Funds Futures.
 */
public class FederalFundsFutureSecurityDefinition implements InstrumentDefinitionWithData<FederalFundsFutureSecurity, Double> {

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
   * The future notional.
   */
  private final double _notional;
  /**
   * The future payment accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   */
  private final double _paymentAccrualFactor;
  /**
   * The future name.
   */
  private final String _name;

  private static final BusinessDayConvention BUSINESS_DAY_PRECEDING = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Preceding");
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
  public FederalFundsFutureSecurityDefinition(final ZonedDateTime lastTradingDate, final IndexON index, final ZonedDateTime[] fixingPeriodDate, final double[] fixingPeriodAccrualFactor,
      final double notional, double paymentAccrualFactor, String name) {
    Validate.notNull(lastTradingDate, "Last trading date");
    Validate.notNull(index, "Index overnight");
    Validate.notNull(fixingPeriodDate, "Fixing period dates");
    Validate.notNull(fixingPeriodAccrualFactor, "Fixing period accrual factors");
    Validate.notNull(name, "Name");
    Validate.isTrue(fixingPeriodDate.length == fixingPeriodAccrualFactor.length + 1, "Fixing dates length should be fixing accrual factors + 1.");
    this._lastTradingDate = lastTradingDate;
    this._index = index;
    this._fixingPeriodDate = fixingPeriodDate;
    this._fixingPeriodAccrualFactor = fixingPeriodAccrualFactor;
    this._notional = notional;
    this._paymentAccrualFactor = paymentAccrualFactor;
    this._name = name;
  }

  /**
   * Builder for a given month. The future start on the first business day of the month and ends on the first business day of the next month. 
   * The last trading date is the last good business day of the month.
   * @param monthDate Any date in the future month.
   * @param index The overnight index.
   * @param notional The future notional.
   * @param paymentAccrualFactor The future payment accrual factor. Usually a standardized number of 1/12 for a 30-day future.
   * @param name The future name.
   * @return The future.
   */
  public static FederalFundsFutureSecurityDefinition from(final ZonedDateTime monthDate, final IndexON index, final double notional, double paymentAccrualFactor, String name) {
    Validate.notNull(monthDate, "Reference date");
    Validate.notNull(index, "Index overnight");
    ZonedDateTime periodFirstDate = BUSINESS_DAY_FOLLOWING.adjustDate(index.getCalendar(), monthDate.withDayOfMonth(1));
    ZonedDateTime periodLastDate = BUSINESS_DAY_FOLLOWING.adjustDate(index.getCalendar(), monthDate.withDayOfMonth(1).plusMonths(1));
    ZonedDateTime last = BUSINESS_DAY_PRECEDING.adjustDate(index.getCalendar(), periodLastDate.minusDays(1));
    List<ZonedDateTime> fixingList = new ArrayList<ZonedDateTime>();
    ZonedDateTime date = periodFirstDate;
    while (!date.isAfter(periodLastDate)) {
      fixingList.add(date);
      date = BUSINESS_DAY_FOLLOWING.adjustDate(index.getCalendar(), date.plusDays(1));
    }
    ZonedDateTime[] fixingDate = fixingList.toArray(new ZonedDateTime[0]);
    double[] fixingAccrualFactor = new double[fixingDate.length - 1];
    for (int loopfix = 0; loopfix < fixingDate.length - 1; loopfix++) {
      fixingAccrualFactor[loopfix] = index.getDayCount().getDayCountFraction(fixingDate[loopfix], fixingDate[loopfix + 1]);
    }
    return new FederalFundsFutureSecurityDefinition(last, index, fixingDate, fixingAccrualFactor, notional, paymentAccrualFactor, name);
  }

  /**
   * Builder of the CBOT Federal Funds futures for a given month. The future start on the first business day of the month and ends on the first business day of the next month. 
   * The last trading date is the last good business day of the month. The notional is 5m. The payment accrual fraction is 1/12. The name is "FF" + month in format "MMMYY".
   * @param monthDate Any date in the future month.
   * @param index The overnight index.
   * @return The future.
   */
  public static FederalFundsFutureSecurityDefinition fromFedFund(final ZonedDateTime monthDate, final IndexON index) {
    final double notionalFedFund = 5000000;
    final double accrualFedFund = 1.0 / 12.0;
    return from(monthDate, index, notionalFedFund, accrualFedFund, "FF" + monthDate.toString(DateTimeFormatters.pattern("MMMyy")));
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
  public double getPaymentAccrualFactor() {
    return _paymentAccrualFactor;
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
    return _name + " - index: " + _index.toString() + " - start date: " + _fixingPeriodDate[0].toString(DateTimeFormatters.pattern("ddMMMyy"));
  }

  @Override
  public FederalFundsFutureSecurity toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    Validate.notNull(date, "Date");
    Validate.isTrue(!date.isAfter(_fixingPeriodDate[0]), "Date should not be after the fixing period start date");
    double[] fixingPeriodTime = new double[_fixingPeriodDate.length];
    for (int loopfix = 0; loopfix < _fixingPeriodDate.length; loopfix++) {
      fixingPeriodTime[loopfix] = TimeCalculator.getTimeBetween(date, _fixingPeriodDate[loopfix]);
    }
    return new FederalFundsFutureSecurity(_index, 0.0, fixingPeriodTime, _fixingPeriodAccrualFactor, _notional, _paymentAccrualFactor, _name, yieldCurveNames[0]);
  }

  @Override
  public FederalFundsFutureSecurity toDerivative(ZonedDateTime date, Double data, String... yieldCurveNames) {
    return null;
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitFederalFundsFutureSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
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
    temp = Double.doubleToLongBits(_paymentAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    FederalFundsFutureSecurityDefinition other = (FederalFundsFutureSecurityDefinition) obj;
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
    if (Double.doubleToLongBits(_paymentAccrualFactor) != Double.doubleToLongBits(other._paymentAccrualFactor)) {
      return false;
    }
    return true;
  }

}
