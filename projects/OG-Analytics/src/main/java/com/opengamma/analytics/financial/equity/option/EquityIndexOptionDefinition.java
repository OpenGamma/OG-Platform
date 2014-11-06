/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Calendar aware version of an EquityIndexOption
 * The definition is responsible for constructing the 'Derivative' for pricing visitors.
 */
public class EquityIndexOptionDefinition implements InstrumentDefinition<EquityIndexOption> {
  /**
   * Call if true, Put if false
   */
  private final boolean _isCall;
  /**
   * Strike, with same scaling as index has.
   * For example, DJX is 1/100 DOW JONES INDUSTRIAL AVERAGE
   */
  private final double _strike;
  /**
   * Currency
   */
  private final Currency _currency;
  /**
   * Exercise type, European or American
   */
  private final ExerciseDecisionType _exerciseType;
  /**
   * Expiry, date and time of last, or only, exercise decision
   */
  private final ZonedDateTime _expiryDT;
  /**
   * Cash settlement occurs on this LocalDate
   */
  private final LocalDate _settlementDate;
  /**
   * Point value, scaling of standard contract.
   * Unit notional. A unit move in price is multiplied by this to give P&L of a single contract
   */
  private final double _pointValue;
  /**
   * The settlement type of the option - cash or physical
   */
  private final SettlementType _settlementType;

  /**
   * @param isCall Call if true, Put if false
   * @param strike Strike, with same scaling as index has. Not negative or zero.
   * @param currency Currency of settlement, not null
   * @param exerciseType Exercise type, European or American, not null
   * @param expiryDT Expiry, date and time of last, or only, exercise decision, not null
   * @param settlementDate Cash settlement occurs on this LocalDate, not null
   * @param pointValue Unit notional. A unit move in price is multiplied by this to give P&L of a single contract. A negative amount
   * represents a short position. Not zero.
   * @param settlementType Whether the option is physically or cash-settled, not null
   */
  public EquityIndexOptionDefinition(final boolean isCall, final double strike, final Currency currency, final ExerciseDecisionType exerciseType,
      final ZonedDateTime expiryDT, final LocalDate settlementDate, final double pointValue, final SettlementType settlementType) {
    ArgumentChecker.notNegativeOrZero(strike, "strike");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(exerciseType, "exercise type");
    ArgumentChecker.notNull(expiryDT, "expiry");
    ArgumentChecker.notNull(settlementDate, "settlement");
    ArgumentChecker.notZero(pointValue, 1e-15, "point value");
    ArgumentChecker.notNull(settlementType, "settlement type");
    _isCall = isCall;
    _strike = strike;
    _currency = currency;
    _exerciseType = exerciseType;
    _expiryDT = expiryDT;
    _settlementDate = settlementDate;
    _pointValue = pointValue;
    _settlementType = settlementType;
  }

  /**
   * Is the option a call
   * @return true if the option is a call
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the strike.
   * @return the strike
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * Gets the currency.
   * @return the currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the exercise type.
   * @return the exercise type
   */
  public ExerciseDecisionType getExerciseType() {
    return _exerciseType;
  }

  /**
   * Gets the expiry date.
   * @return the expiry date
   */
  public ZonedDateTime getExpiryDate() {
    return _expiryDT;
  }

  /**
   * Gets the settlement date.
   * @return the settlement date
   */
  public LocalDate getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Gets the point value.
   * @return the point value
   */
  public double getPointValue() {
    return _pointValue;
  }

  /**
   * Gets the settlement type.
   * @return the settlement type
   */
  public SettlementType getSettlementType() {
    return _settlementType;
  }

  /**
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names.
   */
  @Deprecated
  @Override
  public EquityIndexOption toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());
  }

  @Override
  public EquityIndexOption toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    ArgumentChecker.inOrderOrEqual(date.toLocalDate(), getExpiryDate().toLocalDate(), "valuation date", "expiry");
    double timeToExpiry = TimeCalculator.getTimeBetween(date, getExpiryDate());
    if (timeToExpiry == 0) { // Day of expiration: Still time value if option has not expired.
      // REVIEW Stephen and Casey - This essentially assumes an Expiry with accuracy of 1 day.
      // The intended behaviour is that an option is still alive on the expiry date
      timeToExpiry = 0.0015; // Approximately half a day
    }
    double timeToSettlement = TimeCalculator.getTimeBetween(date, _settlementDate);
    if (timeToSettlement == 0) {
      timeToSettlement = 0.0015;
    }
    return new EquityIndexOption(timeToExpiry, timeToSettlement, _strike, _isCall, _currency, _pointValue, _exerciseType, _settlementType);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexOptionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexOptionDefinition(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _exerciseType.hashCode();
    result = prime * result + _expiryDT.hashCode();
    result = prime * result + (_isCall ? 1231 : 1237);
    long temp;
    temp = Double.doubleToLongBits(_pointValue);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _settlementDate.hashCode();
    result = prime * result + _settlementType.hashCode();
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EquityIndexOptionDefinition)) {
      return false;
    }
    final EquityIndexOptionDefinition other = (EquityIndexOptionDefinition) obj;
    if (Double.compare(_strike, other._strike) != 0) {
      return false;
    }
    if (_isCall != other._isCall) {
      return false;
    }
    if (_exerciseType != other._exerciseType) {
      return false;
    }
    if (_settlementType != other._settlementType) {
      return false;
    }
    if (Double.compare(_pointValue, other._pointValue) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_expiryDT, other._expiryDT)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    return true;
  }

}
