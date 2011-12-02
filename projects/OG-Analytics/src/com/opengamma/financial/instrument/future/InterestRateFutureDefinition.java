/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.future;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.interestrate.InstrumentDerivative;
import com.opengamma.financial.interestrate.future.definition.InterestRateFuture;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

/**
 * Description of an interest rate future security.
 */
public class InterestRateFutureDefinition implements InstrumentDefinitionWithData<InstrumentDerivative, Double> {

  /**
   * Future last trading date. Usually the date for which the third Wednesday of the month is the spot date.
   */
  private final ZonedDateTime _lastTradingDate;
  /**
   * Ibor index associated to the future.
   */
  private final IborIndex _iborIndex;
  /**
   * Fixing period of the reference Ibor starting date.
   */
  private final ZonedDateTime _fixingPeriodStartDate;
  /**
   * Fixing period of the reference Ibor end date.
   */
  private final ZonedDateTime _fixingPeriodEndDate;
  /**
   * Fixing period of the reference Ibor accrual factor.
   */
  private final double _fixingPeriodAccrualFactor;
  /**
   * Future notional.
   */
  private final double _notional;
  /**
   * Future payment accrual factor. Usually a standardized number of 0.25 for a 3M future.
   */
  private final double _paymentAccrualFactor;
  /**
   * Future name.
   */
  private final String _name;

  /**
   * Constructor of the interest rate future security.
   * @param lastTradingDate Future last trading date.
   * @param iborIndex Ibor index associated to the future.
   * @param referencePrice TODO
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor. 
   * @param name Future name.
   */
  public InterestRateFutureDefinition(final ZonedDateTime lastTradingDate, final IborIndex iborIndex, double referencePrice, final double notional, final double paymentAccrualFactor,
      final String name) {
    Validate.notNull(lastTradingDate, "Last trading date");
    Validate.notNull(iborIndex, "Ibor index");
    Validate.notNull(name, "Name");
    this._lastTradingDate = lastTradingDate;
    this._iborIndex = iborIndex;
    _fixingPeriodStartDate = ScheduleCalculator.getAdjustedDate(_lastTradingDate, _iborIndex.getBusinessDayConvention(), _iborIndex.getCalendar(), _iborIndex.getSettlementDays());
    _fixingPeriodEndDate = ScheduleCalculator
        .getAdjustedDate(_fixingPeriodStartDate, _iborIndex.getBusinessDayConvention(), _iborIndex.getCalendar(), _iborIndex.isEndOfMonth(), _iborIndex.getTenor());
    _fixingPeriodAccrualFactor = _iborIndex.getDayCount().getDayCountFraction(_fixingPeriodStartDate, _fixingPeriodEndDate);
    this._notional = notional;
    this._paymentAccrualFactor = paymentAccrualFactor;
    _name = name;
  }

  /**
   * Constructor of the interest rate future security.
   * @param lastTradingDate Future last trading date.
   * @param iborIndex Ibor index associated to the future.
   * @param referencePrice TODO
   * @param notional Future notional.
   * @param paymentAccrualFactor Future payment accrual factor.
   */
  public InterestRateFutureDefinition(final ZonedDateTime lastTradingDate, final IborIndex iborIndex, double referencePrice, final double notional, final double paymentAccrualFactor) {
    this(lastTradingDate, iborIndex, referencePrice, notional, paymentAccrualFactor, "RateFuture " + iborIndex.getName());
  }

  /**
   * Gets the future last trading date.
   * @return The last trading date.
   */
  public ZonedDateTime getLastTradingDate() {
    return _lastTradingDate;
  }

  /**
   * Gets the Ibor index associated to the future.
   * @return The Ibor index
   */
  public IborIndex getIborIndex() {
    return _iborIndex;
  }

  /**
   * Gets the fixing period of the reference Ibor starting date.
   * @return The fixing period starting date
   */
  public ZonedDateTime getFixingPeriodStartDate() {
    return _fixingPeriodStartDate;
  }

  /**
   * Gets the fixing period of the reference Ibor end date.
   * @return The fixing period end date.
   */
  public ZonedDateTime getFixingPeriodEndDate() {
    return _fixingPeriodEndDate;
  }

  /**
   * Gets the fixing period of the reference Ibor accrual factor.
   * @return The Fixing period accrual factor.
   */
  public double getFixingPeriodAccrualFactor() {
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
   * Gets the future payment accrual factor.
   * @return The future payment accrual factor.
   */
  public double getPaymentAccrualFactor() {
    return _paymentAccrualFactor;
  }

  /**
   * Gets the future name.
   * @return The name
   */
  public String getName() {
    return _name;
  }

  /**
   * The future currency.
   * @return The currency.
   */
  public Currency getCurrency() {
    return _iborIndex.getCurrency();
  }

  @Override
  public InterestRateFuture toDerivative(ZonedDateTime date, Double referencePrice, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    Validate.isTrue(yieldCurveNames.length > 1, "at least two curves required");
    Validate.isTrue(!date.isAfter(getFixingPeriodStartDate()), "Date is after last payment date");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final String discountingCurveName = yieldCurveNames[0];
    final String forwardCurveName = yieldCurveNames[1];
    final double lastTradingTime = actAct.getDayCountFraction(date, getLastTradingDate());
    final double fixingPeriodStartTime = actAct.getDayCountFraction(date, getFixingPeriodStartDate());
    final double fixingPeriodEndTime = actAct.getDayCountFraction(date, getFixingPeriodEndDate());
    InterestRateFuture future = new InterestRateFuture(lastTradingTime, _iborIndex, fixingPeriodStartTime, fixingPeriodEndTime, _fixingPeriodAccrualFactor, referencePrice,
        _notional, _paymentAccrualFactor, _name, discountingCurveName, forwardCurveName);
    return future;
  }

  @Override
  public InstrumentDerivative toDerivative(ZonedDateTime date, String... yieldCurveNames) {
    throw new UnsupportedOperationException("The method toDerivative of " + this.getClass().getSimpleName() +
        " does not support the two argument method (without margin price data).");
  }

  @Override
  public <U, V> V accept(InstrumentDefinitionVisitor<U, V> visitor, U data) {
    return visitor.visitInterestRateFutureSecurityDefinition(this, data);
  }

  @Override
  public <V> V accept(InstrumentDefinitionVisitor<?, V> visitor) {
    return visitor.visitInterestRateFutureSecurityDefinition(this);
  }

  @Override
  public String toString() {
    String result = "IRFuture Security: " + _name;
    result += " Last trading date: " + _lastTradingDate.toString();
    result += " Ibor Index: " + _iborIndex.getName();
    result += " Notional: " + _notional;
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_fixingPeriodAccrualFactor);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _fixingPeriodEndDate.hashCode();
    result = prime * result + _fixingPeriodStartDate.hashCode();
    result = prime * result + _iborIndex.hashCode();
    result = prime * result + _lastTradingDate.hashCode();
    result = prime * result + _name.hashCode();
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
    InterestRateFutureDefinition other = (InterestRateFutureDefinition) obj;
    if (!ObjectUtils.equals(_iborIndex, other._iborIndex)) {
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
