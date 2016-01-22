/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.option;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.commodity.definition.SettlementType;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * OG-Analytics derivative of the exchange traded Equity Index Option
 */
public class EquityIndexOption implements InstrumentDerivative, Serializable {
  /** The time to expiry in years*/
  private final double _timeToExpiry;
  /** The time to settlement in years */
  private final double _timeToSettlement;
  /** The option strike */
  private final double _strike;
  /** Is the option a put or call */
  private final boolean _isCall;
  /** The unit amount per tick */
  private final double _unitAmount;
  /** The currency */
  private final Currency _currency;
  /** The exercise type */
  private final ExerciseDecisionType _exerciseType;
  /** The settlement type */
  private final SettlementType _settlementType;

  /**
   * 
   * @param timeToExpiry time (in years as a double) until the date-time at which the reference index is fixed, not negative.
   * @param timeToSettlement time (in years as a double) until the date-time at which the contract is settled, not negative. Must be at or after the
   * expiry.
   * @param strike Strike price at trade time. Note that we may handle margin by resetting this at the end of each trading day, not negative or zero
   * @param isCall True if the option is a Call, false if it is a Put.
   * @param currency The reporting currency of the future, not null
   * @param unitAmount The unit value per tick, in given currency. A negative value may represent a short position. Not zero.
   * @param exerciseType The exercise type of this option, not null
   * @param settlementType The settlement type option this option, not null
   */
  public EquityIndexOption(final double timeToExpiry, final double timeToSettlement, final double strike, final boolean isCall, final Currency currency,
      final double unitAmount, final ExerciseDecisionType exerciseType, final SettlementType settlementType) {
    ArgumentChecker.isTrue(timeToExpiry >= 0, "Time to expiry must not be negative");
    ArgumentChecker.isTrue(timeToSettlement >= 0, "Time to settlement must not be negative");
    ArgumentChecker.isTrue(timeToSettlement >= timeToExpiry, "Settlement time must be after expiry");
    ArgumentChecker.notNegativeOrZero(strike, "strike");
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notZero(unitAmount, 1e-15, "unit amount");
    ArgumentChecker.notNull(exerciseType, "exercise type");
    ArgumentChecker.notNull(settlementType, "settlement type");
    _timeToExpiry = timeToExpiry;
    _timeToSettlement = timeToSettlement;
    _strike = strike;
    _isCall = isCall;
    _unitAmount = unitAmount;
    _currency = currency;
    _exerciseType = exerciseType;
    _settlementType = settlementType;
  }

  /** 
   * @return the expiry time (in years as a double)
   */
  public double getTimeToExpiry() {
    return _timeToExpiry;
  }

  /**
   * Gets the time when payments are made 
   * @return the delivery time (in years as a double)
   */
  public double getTimeToSettlement() {
    return _timeToSettlement;
  }

  /**
   * @return the strike of the option.
   */
  public double getStrike() {
    return _strike;
  }

  /**
   * @return True if the option is a Call, false if it is a Put.
   */
  public boolean isCall() {
    return _isCall;
  }

  /**
   * Gets the unit amount, this is the notional of a single security.
   * @return the point value
   */
  public double getUnitAmount() {
    return _unitAmount;
  }

  /**
   * Gets the currency.
   * @return The currency
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Gets the exercise type.
   * @return The exercise type
   */
  public ExerciseDecisionType getExerciseType() {
    return _exerciseType;
  }

  /**
   * Gets the settlement type.
   * @return The settlement type
   */
  public SettlementType getSettlementType() {
    return _settlementType;
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexOption(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEquityIndexOption(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _exerciseType.hashCode();
    result = prime * result + (_isCall ? 1231 : 1237);
    result = prime * result + _settlementType.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_strike);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_timeToExpiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_timeToSettlement);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EquityIndexOption)) {
      return false;
    }
    final EquityIndexOption other = (EquityIndexOption) obj;
    if (Double.compare(_strike, other._strike) != 0) {
      return false;
    }
    if (Double.compare(_timeToExpiry, other._timeToExpiry) != 0) {
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
    if (Double.compare(_timeToSettlement, other._timeToSettlement) != 0) {
      return false;
    }
    if (Double.compare(_unitAmount, other._unitAmount) != 0) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    return true;
  }

}
