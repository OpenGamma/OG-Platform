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
 * Calendar aware version of an Equity Option
 * The definition is responsible for constructing the 'Derivative' for pricing visitors.
 */
public class EquityOptionDefinition implements InstrumentDefinition<EquityOption> {
  /** 
   * Call if true, Put if false 
   */
  private final boolean _isCall;
  /** 
   * Strike
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
   * @param strike Strike, not negative or zero.
   * @param currency Settlement amount currency, not null
   * @param exerciseType Exercise type, not null
   * @param expiryDate Expiry of last, or only, exercise decision, not null
   * @param settlementDate Date for settlement, not null
   * @param pointValue Unit notional. A unit move in price is multiplied by this to give P&L of a single contract. A negative amount
   * represents a short position. Not zero.
   * @param settlementType Whether the option is physically or cash-settled, not null
   */
  public EquityOptionDefinition(final boolean isCall, final double strike, final Currency currency, final ExerciseDecisionType exerciseType,
      final ZonedDateTime expiryDate, final LocalDate settlementDate, final double pointValue, final SettlementType settlementType) {
    ArgumentChecker.notNegativeOrZero(strike, "strike");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(exerciseType, "exercise type");
    ArgumentChecker.notNull(expiryDate, "expiry");
    ArgumentChecker.notNull(settlementDate, "settlement");
    ArgumentChecker.notZero(pointValue, 1e-15, "point value");
    ArgumentChecker.notNull(settlementType, "settlement type");
    _isCall = isCall;
    _strike = strike;
    _currency = currency;
    _exerciseType = exerciseType;
    _expiryDT = expiryDate;
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

  @Override
  public EquityOption toDerivative(ZonedDateTime date, final String... yieldCurveNames) {
    return toDerivative(date);
  }

  @Override
  public EquityOption toDerivative(final ZonedDateTime date) {
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
    return new EquityOption(timeToExpiry, timeToSettlement, _strike, _isCall, _currency, _pointValue, _exerciseType, _settlementType);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityOptionDefinition(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityOptionDefinition(this);
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
    if (!(obj instanceof EquityOptionDefinition)) {
      return false;
    }
    final EquityOptionDefinition other = (EquityOptionDefinition) obj;
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
